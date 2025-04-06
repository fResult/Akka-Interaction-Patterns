package dev.fResult.akka

import akka.actor.typed.ActorRef
import dev.fResult.akka.barista.BaristaCommand

enum CoffeeMachineCommand {
  case BrewCoffee(coffee: Coffee, replyTo: ActorRef[BaristaCommand])
  case PickupCoffee
}
