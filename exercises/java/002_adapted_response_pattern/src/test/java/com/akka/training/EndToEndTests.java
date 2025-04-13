package com.akka.training;

import akka.actor.testkit.typed.javadsl.LoggingTestKit;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import com.akka.training.BaristaActor;
import com.akka.training.Coffee;
import com.typesafe.config.ConfigFactory;
import org.junit.ClassRule;
import org.junit.Test;

public class EndToEndTests {

  @ClassRule
  public static final TestKitJunitResource testKit =
      new TestKitJunitResource(
          // Increase the default timeout of expect logging
          ConfigFactory.parseString("akka.actor.testkit.typed.filter-leeway = 15s")
              .withFallback(ConfigFactory.load()));

  // Test if the system [Barista + Coffee machine] is able to a full cycle
  // (When the coffee machine child actor send back a CoffeeReady message, the Barista log its
  // Pickup coffee action)
  @Test
  public void brewCoffee() {
    final var barista = testKit.spawn(BaristaActor.create(), "barista1");
    final var coffee = new Coffee.Akkaccino();

    LoggingTestKit.info("Barista: Picking up " + coffee)
        .expect(
            testKit.system(),
            () -> {
              barista.tell(new BaristaActor.OrderCoffee("Lisa", coffee));
              return null;
            });
  }
}
