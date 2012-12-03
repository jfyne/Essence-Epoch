/**
 * Google calendar
 *
 * @author Josh Fyne
 */
package models

import play.api._
import play.api.libs.ws._
import play.api.libs.concurrent._
import play.api.libs.json._

import com.codahale.jerkson.Json._

object Calendar {

    // -- Queries

    /**
     * Create the epoch calendar
     *
     * @param User user
     * @return String calendarId
     */
    def createEpoch(token:String):String = {
        val request = WS.url("https://www.googleapis.com/calendar/v3/calendars").withHeaders(
            ("Content-type", "application/json"),
            ("Authorization", ("OAuth " + token))
        )
        // FUCK YOU GOOGLE
        val body = Json.parse(generate(Map(
            "summary" -> "Epoch"
        )))
        val response = request.post(body)
        val json = response.value.get.json
        (json \ "id").asOpt[String].map { calendarId =>
            calendarId
        }.getOrElse(throw new Exception("Calendar creation failed"))
    }

}
