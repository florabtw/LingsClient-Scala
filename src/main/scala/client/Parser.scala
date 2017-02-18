package client

import akka.http.scaladsl.model.ws.TextMessage
import client.Lings._
import play.api.libs.json.Json

object Parser {
  def toLingsMessage(msgString: String): InMessage = {
    val msgJson = Json.parse(msgString)
    val msgType = (msgJson \ "type").as[String]
    msgType match {
      case "map"   => msgJson.as[MapMessage]
      case "agent" => msgJson.as[EntityMessage]
      case "food"  => msgJson.as[FoodMessage]
      case "id"    => msgJson.as[IdMessage]
      case "move"  => msgJson.as[EntityMoveMessage]
      case "eat"   => msgJson.as[EntityEatMessage]
    }
  }

  def toTextMessage(outMessage: OutMessage): TextMessage.Strict = {
    val json = Json.toJson(outMessage)
    TextMessage.Strict(json.toString)
  }
}
