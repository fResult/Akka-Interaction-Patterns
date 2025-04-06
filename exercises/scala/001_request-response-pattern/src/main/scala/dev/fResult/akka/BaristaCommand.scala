package dev.fResult.akka

import dev.fResult.akka.Coffee

enum BaristaCommand {
  case OrderCoffee(whom: String, coffee: Coffee)
}
