package client

import play.api.libs.json.{JsValue, Json, Reads, Writes}

object Lings {
  sealed trait LingsMessage
  sealed trait OutMessage extends LingsMessage
  sealed trait InMessage extends LingsMessage

  case class MapMessage(columns: Int, rows: Int, map: String) extends InMessage

  case class EntityMessage(id: Int, x: Int, y: Int) extends InMessage

  case class FoodMessage(x: Int, y: Int) extends InMessage

  case class IdMessage(id: Int) extends InMessage

  case class EntityMoveMessage(id: Int, x: Int, y: Int) extends InMessage with OutMessage

  case class EntityEatMessage(id: Int) extends InMessage with OutMessage

  object OutMessage {
    implicit val writes = new Writes[OutMessage] {
      override def writes(outMessage: OutMessage): JsValue = outMessage match {
        case msg: EntityMoveMessage => Json.toJson(msg)(EntityMoveMessage.writes)
        case msg: EntityEatMessage  => Json.toJson(msg)(EntityEatMessage.writes)
      }
    }
  }

  object MapMessage {
    implicit val reads: Reads[MapMessage] = Json.reads[MapMessage]
  }

  object EntityMessage {
    implicit val reads: Reads[EntityMessage] = Json.reads[EntityMessage]
  }

  object FoodMessage {
    implicit val reads: Reads[FoodMessage] = Json.reads[FoodMessage]
  }

  object IdMessage {
    implicit val reads: Reads[IdMessage] = Json.reads[IdMessage]
  }

  object EntityMoveMessage {
    implicit val reads: Reads[EntityMoveMessage] = Json.reads[EntityMoveMessage]
    implicit val writes = new Writes[EntityMoveMessage] {
      override def writes(e: EntityMoveMessage): JsValue = Json.obj(
        "type" -> "move",
        "id"   -> e.id,
        "x"    -> e.x,
        "y"    -> e.y
      )
    }
  }

  object EntityEatMessage {
    implicit val reads: Reads[EntityEatMessage] = Json.reads[EntityEatMessage]
    implicit val writes = new Writes[EntityEatMessage] {
      override def writes(e: EntityEatMessage): JsValue = Json.obj(
        "type" -> "eat",
        "id"   -> e.id
      )
    }
  }
}
