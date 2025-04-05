package com.akka.training;

import akka.actor.typed.ActorSystem;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
  private static final Logger log = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    final var baristaActor = ActorSystem.create(BaristaActor.create(), "barista");

    baristaActor.tell(new BaristaActor.OrderCoffee("Bart", new Coffee.Akkaccino()));
    baristaActor.tell(new BaristaActor.OrderCoffee("Lisa", new Coffee.MochaPlay()));

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ex) {
      log.error("Error: {}", ex.getMessage());
    } finally {
      baristaActor.terminate();
    }
  }
}
