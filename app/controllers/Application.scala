/**
 * Application auth and shiz
 *
 * @author Josh Fyne
 */
package controllers

import play.api._
import play.api.mvc._
import play.api.Play.{configuration,current}
import play.api.libs.ws._
import play.api.libs.json._

import models._

object Application extends Controller {

    /**
     * Login
     *
     */
    def login = Action { implicit request =>
        val config = Play.configuration

        val redirectUrl = java.net.URLEncoder.encode("http://" + request.host + routes.Application.authenticate)

        var loginUrl = "https://accounts.google.com/o/oauth2/auth"
        loginUrl += "?response_type=code"
        loginUrl += "&client_id=" + config.getString("google.clientId").get
        loginUrl += "&redirect_uri=" + redirectUrl
        loginUrl += "&scope=" + java.net.URLEncoder.encode("https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo#email https://www.googleapis.com/auth/calendar")
        loginUrl += "&state="
        loginUrl += "&access_type=offline"
        loginUrl += "&approval_prompt=auto"

        Ok(views.html.login(loginUrl, flash.get("error").getOrElse("Welcome!")))
    }

    /**
     * Google callback url
     *
     */
    def authenticate = Action { implicit request =>
        val error = getParam("error", request)
        val state = getParam("state", request)
        val code = getParam("code", request)

        if (code.isEmpty) {
            Redirect(routes.Application.login).withNewSession.flashing(
                "error" -> "Something went wrong"
            )
        }

        if (!error.isEmpty) {
            Redirect(routes.Application.login).withNewSession.flashing(
                "error" -> error.get
            )
        }

        Async {
            WS.url("https://accounts.google.com/o/oauth2/token").post(Map(
                "code" -> Seq(code.get),
                "client_id" -> Seq(Play.configuration.getString("google.clientId").get),
                "client_secret" -> Seq(Play.configuration.getString("google.clientSecret").get),
                "redirect_uri" -> Seq("http://" + request.host + routes.Application.authenticate),
                "grant_type" -> Seq("authorization_code")
            )).map { response =>
                println(response.json)
                (response.json \ "refresh_token").asOpt[String].map { refreshToken =>
                    val token = (response.json \ "access_token").asOpt[String].get
                    val user = User.createUserFromGoogle(token, refreshToken)
                    Redirect(routes.Timeline.index).withSession(
                        "email" -> user.email
                    )
                }.getOrElse {
                    Redirect(routes.Application.login).withNewSession.flashing(
                        "error" -> (response.json \ "error").as[String]
                    )
                }
            }
        }
    }

    /**
     * Logout
     *
     */
    def logout = Action {
        Redirect(routes.Application.login).withNewSession.flashing(
            "success" -> "You have been logged out"
        )
    }

    /**
     * Query string helper function
     *
     */
    def getParam(query:String, request:Request[AnyContent]):Option[String] = {
        try {
            Some(request.queryString(query).mkString)
        } catch {
            case _ => None
        }
    }

}

/**
 * Provide auth
 *
 */
trait Secured {

    /**
     * Retrieve user email
     *
     */
    private def username(request:RequestHeader) = request.session.get("email")

    /**
     * Redirect if no auth
     *
     */
    private def onUnauthorized(request:RequestHeader) = Results.Redirect(routes.Application.login)

    /**
     * Redirect if no admin
     *
     */
    private def onNoAdmin(request:RequestHeader) = Results.Redirect(routes.Timeline.index)

    /**
     * Authed
     *
     */
    def withAuth(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
        Action(request => f(user)(request))
    }

    /**
     * Authed with user
     *
     */
    def withUser(f: => User => Request[AnyContent] => Result) = withAuth { username => implicit request =>
        User.fetchByEmail(username).map { user =>
            f(user)(request)
        }.getOrElse(onUnauthorized(request))
    }

    /**
     * Authed with user and an admin
     *
     */
    def withAdmin(f: => User => Request[AnyContent] => Result) = withUser { user => implicit request =>
        if (user.role == "admin") {
            f(user)(request)
        } else {
            onNoAdmin(request)
        }
    }
}
