package engine

import akka.actor.ActorSystem
import engine.TurnClock.{IntToTicks, TurnListener}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object TurnClock {
  trait TurnListener[T] {
    def onTurn(state: T): Option[Ticks]
  }

  case class Ticks(value: Int)

  implicit class IntToTicks(num: Int) {
    def tick: Ticks = Ticks(num)

    def ticks: Ticks = Ticks(num)
  }
}

case class TurnClock[T](listener: TurnListener[T], state: T) {
  private          val ticksPerSecond  = 30
  private          val millisPerSecond = 1000
  private          val tickDuration    = (millisPerSecond / ticksPerSecond).millis
  private implicit val system          = ActorSystem()

  def start(): Unit = scheduleTurn(tickDuration)

  private def scheduleTurn(duration: FiniteDuration): Unit = {
    system.scheduler.scheduleOnce(duration) {
      val turnTickCost = listener.onTurn(state).getOrElse(1.tick)
      scheduleTurn(tickDuration * turnTickCost.value)
    }
  }
}
