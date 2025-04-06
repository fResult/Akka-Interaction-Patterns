package dev.fResult.akka.barista

import dev.fResult.akka.Coffee

enum BaristaCommand {
  case OrderCoffee(whom: String, coffee: Coffee)
}
