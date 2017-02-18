package client

object Messages {
  sealed trait LingsMessage { def text: String }

  case class StringMessage(override val text: String) extends LingsMessage
}
