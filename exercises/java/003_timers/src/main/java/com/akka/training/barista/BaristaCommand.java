package com.akka.training.barista;

import com.akka.training.Coffee;
import com.akka.training.coffeeMachine.CoffeeMachineCommand;
import static com.akka.training.barista.BaristaCommand.*;

public sealed interface BaristaCommand
    permits OrderCoffee, WrappedCoffeeMachineCoffeeReady {

  record OrderCoffee(String whom, Coffee coffee) implements BaristaCommand {}

  record WrappedCoffeeMachineCoffeeReady(CoffeeMachineCommand.CoffeeReady coffeeReady)
      implements BaristaCommand {}
}
