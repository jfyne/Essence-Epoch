/**
 * Worker to make models do things
 *
 * @author Josh Fyne
 */
package epoch

import java.util.{Date}
import java.text.{DateFormat,SimpleDateFormat}

import akka.actor._
import akka.util.duration._
import akka.routing.{RoundRobinRouter}

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import com.codahale.jerkson.Json._

import models._
import epoch._

class Worker extends Actor {

    val dateFormat = new SimpleDateFormat("y-M-d");

    /**
     * Handles messages from the api
     *
     */
    def receive = {
        case Dispatch(id, group, request, params) => {
            try {
                var (result, send) = request.asInstanceOf[String] match {
                    case "users"        => (User.fetchAll, true)
                    case _              => (Map("error" -> "Unknown request"), true)
                }

                if (true == send) sendResults(id, request, params, result)
            } catch {
                case e => sender ! sendResults(id, request, params, Map("error" -> e.getMessage))
            }
        }
    }

    /**
     * Send results back
     *
     */
    def sendResults(id:Long, request:String, params:Map[String, String], result:Any) = {
        sender ! Result(id, Json.toJson(Map(
            "request"   -> Json.toJson(request),
            "params"    -> Json.toJson(params),
            "result"    -> Json.parse(generate(result))
        )))
    }
    /**
     * Extract params
     *
     */
    private def extract(params:Map[String, Any], item:String, default:String = "0"):Any = {
        try {
            params(item)
        } catch {
            case _ => default
        }
    }
}
