package client

import play.api.libs.json.{JsValue, Json, Reads, Writes}

object LingsProtocol {
  sealed trait LingsMessage
  sealed trait OutMessage extends LingsMessage
  sealed trait InMessage extends LingsMessage

  case class MapMessage(columns: Int, rows: Int, map: String) extends InMessage

  case class AgentMessage(id: Int, x: Int, y: Int) extends InMessage

  case class FoodMessage(x: Int, y: Int) extends InMessage

  case class IdMessage(id: Int) extends InMessage

  case class AgentMoveMessage(id: Int, x: Int, y: Int) extends InMessage with OutMessage

  case class AgentEatMessage(id: Int) extends InMessage with OutMessage

  object OutMessage {
    implicit val writes = new Writes[OutMessage] {
      override def writes(outMessage: OutMessage): JsValue = outMessage match {
        case msg: AgentMoveMessage => Json.toJson(msg)(AgentMoveMessage.writes)
        case msg: AgentEatMessage  => Json.toJson(msg)(AgentEatMessage.writes)
      }
    }
  }

  object MapMessage {
    implicit val reads: Reads[MapMessage] = Json.reads[MapMessage]
  }

  object AgentMessage {
    implicit val reads: Reads[AgentMessage] = Json.reads[AgentMessage]
  }

  object FoodMessage {
    implicit val reads: Reads[FoodMessage] = Json.reads[FoodMessage]
  }

  object IdMessage {
    implicit val reads: Reads[IdMessage] = Json.reads[IdMessage]
  }

  object AgentMoveMessage {
    implicit val reads: Reads[AgentMoveMessage] = Json.reads[AgentMoveMessage]
    implicit val writes = new Writes[AgentMoveMessage] {
      override def writes(e: AgentMoveMessage): JsValue = Json.obj(
        "type" -> "move",
        "id"   -> e.id,
        "x"    -> e.x,
        "y"    -> e.y
      )
    }
  }

  object AgentEatMessage {
    implicit val reads: Reads[AgentEatMessage] = Json.reads[AgentEatMessage]
    implicit val writes = new Writes[AgentEatMessage] {
      override def writes(e: AgentEatMessage): JsValue = Json.obj(
        "type" -> "eat",
        "id"   -> e.id
      )
    }
  }
}
