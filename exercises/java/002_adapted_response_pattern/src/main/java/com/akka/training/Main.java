package com.akka.training;

import akka.actor.typed.ActorSystem;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        final ActorSystem<Barista.BaristaCommand> barista =
                ActorSystem.create(Barista.create(), "barista");

        barista.tell(new Barista.OrderCoffee("Bart", new Coffee.Akkaccino()));
        barista.tell(new Barista.OrderCoffee("Lisa", new Coffee.MochaPlay()));

        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (IOException ignored) {
        } finally {
            barista.terminate();
        }
    }
}
