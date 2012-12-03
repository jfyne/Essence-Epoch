/**
 * A OMV user
 *
 * @author Josh Fyne
 */
package models

import play.api.db._
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.concurrent._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import anorm._
import anorm.SqlParser._

import java.security.MessageDigest

sealed class EpochUser
case class User(
    id:Pk[Long],
    email:String,
    name:Option[String],
    given:Option[String],
    family:Option[String],
    picture:Option[String],
    token:Option[String],
    role:String
) extends EpochUser

object User {

    // -- Parsers

    val simple = {
        get[Pk[Long]]("epoch_users.user_id") ~
        get[String]("epoch_users.user_email") ~
        get[Option[String]]("epoch_users.user_name") ~
        get[Option[String]]("epoch_users.user_given") ~
        get[Option[String]]("epoch_users.user_family") ~
        get[Option[String]]("epoch_users.user_picture") ~
        get[Option[String]]("epoch_users.user_token") ~
        get[String]("epoch_users.user_role") map {
            case id~email~name~given~family~picture~token~role => User(
                id, email, name, given, family, picture, token, role
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
     * Create a new user from google
     *
     */
    def createUserFromGoogle(token:String):User = {
        val data: Promise[Response] = WS.url("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token).get()

        val json = data.value.get.json
        var user = fetchByEmail((json \ "email").as[String])

        if (user.isEmpty) {
            DB.withConnection { implicit connection =>
                SQL("""
                    insert into epoch_users (
                        user_google_id,
                        user_email,
                        user_name,
                        user_given_name,
                        user_family_name,
                        user_link,
                        user_picture,
                        user_gender,
                        user_locale
                    ) values (
                        {googleId},
                        {email},
                        {name},
                        {givenName},
                        {familyName},
                        {link},
                        {picture},
                        {gender},
                        {locale}
                    )
                """).on(
                    'googleId -> (json \ "id").as[String],
                    'email -> (json \ "email").as[String],
                    'name -> (json \ "name").as[String],
                    'givenName -> (json \ "given_name").as[String],
                    'familyName -> (json \ "family_name").as[String],
                    'link -> (json \ "link").as[String],
                    'picture -> (json \ "picture").as[String],
                    'gender -> (json \ "gender").as[String],
                    'locale -> (json \ "locale").as[String]
                ).executeUpdate
            }
        } else {
            DB.withConnection { implicit connection =>
                SQL("""
                    update epoch_users
                    set
                    user_google_id={googleId},
                    user_name={name},
                    user_given_name={givenName},
                    user_family_name={familyName},
                    user_link={link},
                    user_picture={picture},
                    user_gender={gender},
                    user_locale={locale}
                    where
                    user_email={email}
                """).on(
                    'googleId -> (json \ "id").as[String],
                    'email -> (json \ "email").as[String],
                    'name -> (json \ "name").as[String],
                    'givenName -> (json \ "given_name").as[String],
                    'familyName -> (json \ "family_name").as[String],
                    'link -> (json \ "link").as[String],
                    'picture -> (json \ "picture").as[String],
                    'gender -> (json \ "gender").as[String],
                    'locale -> (json \ "locale").as[String]
                ).executeUpdate
            }
        }
        user = fetchByEmail((json \ "email").as[String])

        user.get
    }
}
