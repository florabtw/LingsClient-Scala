package client

import akka.http.scaladsl.model.ws.TextMessage
import client.LingsProtocol._
import play.api.libs.json.{JsValue, Json}

object Parser {
  def toLingsMessage(msgString: String): InMessage = {
    val msgJson = Json.parse(msgString)
    val msgType = (msgJson \ "type").as[String]
    msgType match {
      case "map"    => msgJson.as[MapMessage]
      case "entity" => parseEntity(msgJson)
      case "id"     => msgJson.as[IdMessage]
      case "move"   => msgJson.as[AgentMoveMessage]
      case "eat"    => msgJson.as[AgentEatMessage]
    }
  }

  private def parseEntity(msgJson: JsValue): InMessage = {
    val entityType = (msgJson \ "entityType").as[String]
    entityType match {
      case "food"  => msgJson.as[FoodMessage]
      case "agent" => msgJson.as[AgentMessage]
    }
  }

  def toTextMessage(outMessage: OutMessage): TextMessage.Strict = {
    val json = Json.toJson(outMessage)
    TextMessage.Strict(json.toString)
  }
}
