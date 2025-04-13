package com.akka.training;

import akka.actor.typed.ActorSystem;
import java.io.IOException;

public class Main {

  public static void main(String[] args) {
    final ActorSystem<BaristaActor.BaristaCommand> barista =
        ActorSystem.create(BaristaActor.create(), "barista");

    barista.tell(new BaristaActor.OrderCoffee("Bart", new CoffeeCommand.Akkaccino()));
    barista.tell(new BaristaActor.OrderCoffee("Lisa", new CoffeeCommand.MochaPlay()));

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      barista.terminate();
    }
  }
}
