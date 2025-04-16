package dev.fResult.akka

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}

@main
def main(): Unit = {
  val baristaActor = ActorSystem[BaristaCommand](BaristaActor(), "barista")
}


object BaristaActor {
  def apply(): Behavior[BaristaCommand] = Behaviors.setup(context => {
    context.log.info("Barista actor is applied")
    
    Behaviors.same
  })
}

// <- Barista protocol
sealed trait BaristaCommand

case class OrderCoffee(whom: String, coffee: Coffee) extends BaristaCommand
// Barista protocol ->

// <- Coffee types
sealed trait Coffee

case object Akkacino extends Coffee
// Coffee types ->

