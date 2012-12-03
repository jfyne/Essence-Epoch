/**
 * Api model
 *
 * @author Josh Fyne
 */
package epoch

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.actor._
import akka.util.duration._
import akka.util.{Timeout}
import akka.pattern.{ask}
import akka.routing.{RoundRobinRouter}

import play.api.Play.current

import epoch._
import models.{User}

/**
 * Socket messages
 *
 */
case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg:String)

object Api {

    implicit val timeout = Timeout(1 second)

    lazy val default = {
        val apiActor = Akka.system.actorOf(Props[Api])
        apiActor
    }

    def connect(id:Long, user:Option[User]):Promise[(Iteratee[JsValue,_],Enumerator[JsValue])] = {
        (default ? Connect(id, user)).asPromise.map {

            case Connected(enumerator) =>
                val iteratee = Iteratee.foreach[JsValue] { event =>
                    default ! Dispatch(id, user, (event \ "request").as[String], (event \ "params").as[Map[String, String]])
                }.mapDone { _ =>
                    default ! Disconnect(id)
                }

                (iteratee, enumerator)

            case CannotConnect(error) =>

                val iteratee = Done[JsValue,Unit]((),Input.EOF)
                val enumerator = Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))

                (iteratee, enumerator)
        }
    }

}

/**
 * Api messages
 *
 */
case class Connect(id:Long, user:Option[User])
case class Dispatch(id:Long, user:Option[User], request:String, params:Map[String, String])
case class Disconnect(id:Long)
case class Result(id:Long, resultSet:JsValue)

class Api extends Actor {

    var connections = Map.empty[Long, PushEnumerator[JsValue]]

    val workerRouter = Akka.system.actorOf(Props[Worker].withRouter(RoundRobinRouter(8)), name = "workerRouter")

    def receive = {

        case Connect(id, user) => {
            if (user.isEmpty) sender ! CannotConnect("Unauthorised")

            val channel = Enumerator.imperative[JsValue]()
            if (connections.contains(id)) {
                sender ! CannotConnect("This id is already in use")
            } else {
                connections = connections + (id -> channel)
                sender ! Connected(channel)
            }
        }

        case Result(id, resultSet) => {
            connections(id).push(resultSet)
        }

        case Disconnect(id) => {
            connections = connections - id
        }

        case Dispatch(id, user, request, params) => {
            workerRouter ! Dispatch(id, user, request, params)
        }
    }

}
