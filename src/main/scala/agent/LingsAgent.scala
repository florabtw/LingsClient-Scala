package agent

import agent.MoveRightAgent._
import agent.LingsAgent.{AgentState, EmptyState}
import client.Lings._

object LingsAgent {
  sealed trait AgentState
  case object EmptyState extends AgentState
}

sealed trait LingsAgent {
  def perceive: InMessage => AgentState => AgentState

  def nextAction: AgentState => Option[OutMessage]
}

object MoveRightAgent {
  case class Food(x: Int, y: Int)

  case class Agent(id: Int, x: Int, y: Int)

  sealed trait LingsMap
  case object EmptyMap extends LingsMap
  case class WorldMap(rows: Int, columns: Int, repr: String) extends LingsMap

  case class State(map: LingsMap, agents: List[Agent], foods: List[Food], ids: List[Int]) extends AgentState
}

case class MoveRightAgent() extends LingsAgent {
  override def perceive: InMessage => AgentState => AgentState = {
    case map:   MapMessage   => perceiveMap(map)
    case agent: AgentMessage => perceiveAgent(agent)
    case id:    IdMessage    => perceiveId(id)
    case m                   => perceiveMessage(m)
  }

  private def perceiveMessage(m: InMessage): AgentState => AgentState = {
    case EmptyState => EmptyState
    case state      => state
  }

  private def perceiveAgent(agent: AgentMessage): AgentState => AgentState = {
    case EmptyState   => State(EmptyMap, List(Agent(agent.id, agent.x, agent.y)), Nil, Nil)
    case state: State => state.copy(agents = state.agents :+ Agent(agent.id, agent.x, agent.y))
  }

  private def perceiveMap(map: MapMessage): AgentState => AgentState = {
    case EmptyState   => State(WorldMap(map.rows, map.columns, map.repr), Nil, Nil, Nil)
    case state: State => state.copy(map = WorldMap(map.rows, map.columns, map.repr))
  }

  private def perceiveId(id: IdMessage): AgentState => AgentState = {
    case EmptyState   => State(EmptyMap, Nil, Nil, List(id.id))
    case state: State => state.copy(ids = state.ids :+ id.id)
  }

  override def nextAction: AgentState => Option[OutMessage] = {
    case EmptyState                   => None
    case State(_, _, _, Nil)          => None
    case State(_, agents, _, id :: _) =>
      val ownedAgent = agents.find(_.id == id)
      ownedAgent.map { a => AgentMoveMessage(a.id, a.x + 1, a.y) }
  }
}
