package runner

import agent.MoveRightAgent
import client.LingsClient
import engine.PerceptEngine

object ClientRunner extends App {
  val agent = MoveRightAgent()
  val engine = PerceptEngine(agent)
  LingsClient(engine)
}
