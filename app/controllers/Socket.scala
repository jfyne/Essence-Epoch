/**
 * OMV api
 *
 * @author Josh Fyne
 */
package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._

import epoch._
import models._

object Socket extends Controller with Secured {

    /**
     * Handles the web socket connection
     *
     */
    def index = WebSocket.async[JsValue] { request =>
        val email = request.session.get("email")
        var user:Option[User] = None
        if (!email.isEmpty) {
            user = User.fetchByEmail(email.get)
        }

        val id = System.currentTimeMillis();
        Api.connect(id, user)
    }
}
