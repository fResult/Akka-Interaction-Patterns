package com.akka.training;

import akka.actor.typed.ActorSystem;
import java.io.IOException;

public class Main {

  public static void main(String[] args) {
    final ActorSystem<BaristaActor.BaristaCommand> actorSystem =
        ActorSystem.create(BaristaActor.create(), "barista");

    actorSystem.tell(new BaristaActor.OrderCoffee("Bart", new Coffee.Akkaccino()));
    actorSystem.tell(new BaristaActor.OrderCoffee("Lisa", new Coffee.MochaPlay()));

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      actorSystem.terminate();
    }
  }
}
