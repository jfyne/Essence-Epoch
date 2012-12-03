/**
 * A user
 *
 * @author Josh Fyne
 */
package models

import play.api._
import play.api.db._
import play.api.Play.{configuration,current}
import play.api.libs.ws._
import play.api.libs.concurrent._
import play.api.libs.json._

import com.codahale.jerkson.Json._

import anorm._
import anorm.SqlParser._

import java.security.MessageDigest

sealed class EpochUser
case class User(
    id:Pk[String],
    email:Pk[String],
    name:Option[String],
    given:Option[String],
    family:Option[String],
    picture:Option[String],
    token:Option[String],
    refresh:Option[String],
    role:String
) extends EpochUser

object User {

    // -- Parsers

    val simple = {
        get[Pk[String]]("epoch_users.user_id") ~
        get[Pk[String]]("epoch_users.user_email") ~
        get[Option[String]]("epoch_users.user_name") ~
        get[Option[String]]("epoch_users.user_given") ~
        get[Option[String]]("epoch_users.user_family") ~
        get[Option[String]]("epoch_users.user_picture") ~
        get[Option[String]]("epoch_users.user_token") ~
        get[Option[String]]("epoch_users.user_refresh") ~
        get[String]("epoch_users.user_role") map {
            case id~email~name~given~family~picture~token~refresh~role => User(
                id, email, name, given, family, picture, token, refresh, role
            )
        }
    }

    // -- Queries

    /**
     * Fetch by id
     *
     */
    def find(id:Int):Option[User] = DB.withConnection { implicit connection =>
        SQL("select * from epoch_users where user_id = {id}").on('id -> id).as(simple.singleOpt)
    }

    /**
     * Get a user by email
     *
     */
    def fetchByEmail(email:String):Option[User] = DB.withConnection { implicit connection =>
        SQL("select * from epoch_users where user_email = {email}").on('email -> email).as(simple.singleOpt)
    }

    /**
     * Fetch all users
     *
     */
    def fetchAll:Seq[User] = DB.withConnection { implicit connection =>
        SQL("select * from epoch_users").as(simple *)
    }

    /**
     * Refresh the access token
     *
     * @param User user
     * @return String token
     */
    def refreshToken(user:User) = {
        val response = WS.url("https://accounts.google.com/o/oauth2/token").post(Map(
            "client_id" -> Seq(Play.configuration.getString("google.clientId").get),
            "client_secret" -> Seq(Play.configuration.getString("google.clientSecret").get),
            "refresh_token" -> Seq(user.refresh.get),
            "grant_type" -> Seq("refresh_token")
        ))
        val json = response.value.get.json

        (json \ "access_token").asOpt[String].map { token =>
            DB.withConnection { implicit connection =>
                SQL("""
                    update epoch_users
                    set
                    user_token={token}
                    where
                    user_email={email}
                    """
                ).on(
                    'token -> token,
                    'email -> user.email.get
                ).executeUpdate
            }
        }.getOrElse {
            throw new Exception("Token refresh failed")
        }
    }

    /**
     * Create a new user from google
     *
     */
    def createUserFromGoogle(token:String, refreshToken:Option[String]):User = {
        val data = WS.url("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token).get()

        val json = data.value.get.json
        val email = (json \ "email").as[String]

        var user = fetchByEmail(email)

        if (user.isEmpty && !refreshToken.isEmpty) {
            // Create the applications calendar
            val calendarId = Calendar.createEpoch(token)

            DB.withConnection { implicit connection =>
                SQL("""
                    insert into epoch_users (
                        user_id,
                        user_email,
                        user_name,
                        user_given,
                        user_family,
                        user_picture,
                        user_token,
                        user_refresh,
                        user_calendar
                    ) values (
                        {id},
                        {email},
                        {name},
                        {given},
                        {family},
                        {picture},
                        {token},
                        {refresh},
                        {calendarId}
                    )
                """).on(
                    'id -> (json \ "id").as[String],
                    'email -> (json \ "email").as[String],
                    'name -> (json \ "name").as[String],
                    'given -> (json \ "given_name").as[String],
                    'family -> (json \ "family_name").as[String],
                    'picture -> (json \ "picture").as[String],
                    'token -> token,
                    'refresh -> refreshToken.get,
                    'calendarId -> calendarId
                ).executeUpdate
            }

        } else if (!user.isEmpty && !refreshToken.isEmpty) {
            DB.withConnection { implicit connection =>
                SQL("""
                    update epoch_users
                    set
                    user_id={id},
                    user_name={name},
                    user_given={given},
                    user_family={family},
                    user_picture={picture},
                    user_token={token},
                    user_refresh={refresh}
                    where
                    user_email={email}
                """).on(
                    'id -> (json \ "id").as[String],
                    'email -> (json \ "email").as[String],
                    'name -> (json \ "name").as[String],
                    'given -> (json \ "given_name").as[String],
                    'family -> (json \ "family_name").as[String],
                    'picture -> (json \ "picture").as[String],
                    'token -> token,
                    'refresh -> refreshToken
                ).executeUpdate
            }
        }
        user = fetchByEmail((json \ "email").as[String])

        user.get
    }
}
