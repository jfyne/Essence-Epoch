/**
 * Display the calendars
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

object Timeline extends Controller with Secured {

    def index = withUser { user => _ =>
        Ok(views.html.index(user))
    }
}
