/**
 * Google calendar
 *
 * @author Josh Fyne
 */
package models

import java.util.{Date}
import java.text.{DateFormat,SimpleDateFormat}

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
     * @param String token
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

    /**
     * Fetch a users primary events
     *
     * @param String token
     * @return
     */
    def fetchPrimary(token:String):JsValue = {
        val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
        val request = WS.url("https://www.googleapis.com/calendar/v3/calendars/primary/events").withHeaders(
            ("Authorization", ("OAuth " + token))
        ).withQueryString(
            ("timeMin", (dateFormat.format(new Date) + "T00:00:00Z"))
        )
        println(request.toString)

        val response = request.get()
        val json = response.value.get.json

        println(json)

        (json \ "error").asOpt[String].map { error =>
            throw new Exception(error)
        }

        (json \ "items")
    }
}
