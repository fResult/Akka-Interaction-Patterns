package dev.fResult.akka

enum BaristaCommand {
  case OrderCoffee(whom: String, coffee: Coffee)
}
