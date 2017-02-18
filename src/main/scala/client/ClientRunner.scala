package client

import client.Engine.PerceptEngine

object ClientRunner extends App {
  val engine = PerceptEngine()
  LingsClient(engine)
}
