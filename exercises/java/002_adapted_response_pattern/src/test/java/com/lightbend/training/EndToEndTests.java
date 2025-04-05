package com.lightbend.training;

import akka.actor.testkit.typed.javadsl.LoggingTestKit;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import com.akka.training.Barista;
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
  // (When the coffee machine child actor send back a CoffeeIsReady message, the Barista log its
  // Pickup coffee action)
  @Test
  public void brewCoffee() {
    ActorRef<Barista.BaristaCommand> barista = testKit.spawn(Barista.create(), "barista1");
    var coffee = new Coffee.Akkaccino();
    LoggingTestKit.info("Barista: Picking up " + coffee)
        .expect(
            testKit.system(),
            () -> {
              barista.tell(new Barista.OrderCoffee("Lisa", coffee));
              return null;
            });
  }
}
