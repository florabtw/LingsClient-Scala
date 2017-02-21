package runner

import agent.ReactiveAgent
import brain.ClosestFoodBrain
import client.LingsClient
import engine.LingsEngine

object ClientRunner extends App {
  val brain = ClosestFoodBrain()
  val agent = ReactiveAgent(brain)
  val engine = LingsEngine(agent)
  LingsClient(engine)
}
