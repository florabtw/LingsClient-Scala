package runner

import agent.ReactiveAgent
import brain.ClosestFoodBrain
import client.LingsClient
import engine.PerceptEngine

object ClientRunner extends App {
  val brain = ClosestFoodBrain()
  val agent = ReactiveAgent(brain)
  val engine = PerceptEngine(agent)
  LingsClient(engine)
}
