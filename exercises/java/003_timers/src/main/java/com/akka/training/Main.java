package com.akka.training;

import static com.akka.training.Coffee.*;
import static com.akka.training.barista.BaristaCommand.*;

import akka.actor.typed.ActorSystem;
import com.akka.training.barista.BaristaActor;
import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    final var baristaActor = ActorSystem.create(BaristaActor.create(), "barista");

    baristaActor.tell(new OrderCoffee("Bart", new Akkaccino()));
    baristaActor.tell(new OrderCoffee("Lisa", new MochaPlay()));

    try {
      System.out.println(">>> Press ENTER to exit <<<");
      System.in.read();
    } catch (IOException ignored) {
    } finally {
      baristaActor.terminate();
    }
  }
}
