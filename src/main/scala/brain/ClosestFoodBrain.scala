package brain

import agent.ReactiveAgent.{Agent, Food, State}
import client.LingsProtocol.{AgentEatMessage, AgentMoveMessage, OutMessage}

case class ClosestFoodBrain() extends ReactiveBrain {
  private def manhattanDistance(agent: Agent)(food: Food) = {
    val diffX = Math.abs(agent.x - food.x)
    val diffY = Math.abs(agent.y - food.y)
    diffX + diffY
  }

  private def moveOrEat(agent: Agent, food: Food): OutMessage = (agent, food) match {
    case (Agent(id, ax, ay), Food(fx,  _)) if fx > ax => AgentMoveMessage(id, ax + 1, ay)
    case (Agent(id, ax, ay), Food(fx,  _)) if fx < ax => AgentMoveMessage(id, ax - 1, ay)
    case (Agent(id, ax, ay), Food( _, fy)) if fy > ay => AgentMoveMessage(id, ax, ay + 1)
    case (Agent(id, ax, ay), Food( _, fy)) if fy < ay => AgentMoveMessage(id, ax, ay - 1)
    case (Agent(id,  _,  _), Food( _,  _))            => AgentEatMessage(id)
  }

  override def nextAction: State => Option[OutMessage] = {
    case State(_,    Nil,     _,       _) => None
    case State(_,      _,   Nil,       _) => None
    case State(_,      _,     _,     Nil) => None
    case State(_, agents, foods, id :: _) => for {
      agent      <- agents.find(_.id == id)
      closestFood = foods.minBy(manhattanDistance(agent))
      message     = moveOrEat(agent, closestFood)
    } yield message
  }
}
