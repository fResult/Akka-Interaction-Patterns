package com.akka.training;

import akka.actor.testkit.typed.javadsl.LoggingTestKit;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import java.time.Duration;
import org.junit.ClassRule;
import org.junit.Test;

public class CoffeeMachineTests {

  @ClassRule public static final TestKitJunitResource testKit = new TestKitJunitResource();
  private final int BREWING_DURATION_Millis = 10000;

  // CoffeeMachine should transition from IDLE to Brewing on receiving BrewCoffee message
  @Test
  public void brewing() {
    ActorRef<CoffeeMachine.CoffeeMachineCommand> coffeeMachine =
        testKit.spawn(CoffeeMachine.create(), "coffee-machine-brewing");
    TestProbe<CoffeeMachine.CoffeeIsReady> probe = testKit.createTestProbe();
    var coffee = new Coffee.Akkaccino();
    LoggingTestKit.info("CoffeeMachine: Brewing 1 " + coffee)
        .withOccurrences(1)
        .expect(
            testKit.system(),
            () -> {
              coffeeMachine.tell(new CoffeeMachine.BrewCoffee(coffee, probe.getRef()));
              return null;
            });

    testKit.stop(coffeeMachine);
  }

  @Test
  public void shouldSendReadySignalToBarista() {
    ActorRef<CoffeeMachine.CoffeeMachineCommand> coffeeMachine =
        testKit.spawn(CoffeeMachine.create(), "coffee-machine-shouldSendReadySignalToBarista");
    TestProbe<CoffeeMachine.CoffeeIsReady> probe = testKit.createTestProbe();

    coffeeMachine.tell(new CoffeeMachine.BrewCoffee(new Coffee.Akkaccino(), probe.getRef()));

    probe.expectNoMessage(Duration.ofMillis(BREWING_DURATION_Millis - 100));
    probe.expectMessageClass(
        CoffeeMachine.CoffeeIsReady.class, Duration.ofMillis(BREWING_DURATION_Millis + 100));

    testKit.stop(coffeeMachine);
  }

  // Once a full cycle is completed => Barista pick-up the coffee, CoffeeMachine is back to idle
  // state.
  // The CoffeeMachine should be able a new brewing cycle.
  @Test
  public void restartCycle() {
    ActorRef<CoffeeMachine.CoffeeMachineCommand> coffeeMachine =
        testKit.spawn(CoffeeMachine.create(), "coffee-machine-restartCycle");
    TestProbe<CoffeeMachine.CoffeeIsReady> probe = testKit.createTestProbe();

    coffeeMachine.tell(new CoffeeMachine.BrewCoffee(new Coffee.Akkaccino(), probe.getRef()));
    probe.expectNoMessage(Duration.ofMillis(BREWING_DURATION_Millis - 100));
    probe.expectMessageClass(
        CoffeeMachine.CoffeeIsReady.class, Duration.ofMillis(BREWING_DURATION_Millis + 100));
    coffeeMachine.tell(new CoffeeMachine.PickupCoffee());

    coffeeMachine.tell(new CoffeeMachine.BrewCoffee(new Coffee.MochaPlay(), probe.getRef()));
    probe.expectNoMessage(Duration.ofMillis(BREWING_DURATION_Millis - 100));
    probe.expectMessageClass(
        CoffeeMachine.CoffeeIsReady.class, Duration.ofMillis(BREWING_DURATION_Millis + 100));

    testKit.stop(coffeeMachine);
  }

  // Once it started a brewing cycle, any other "BrewCoffee" command will be ignored.
  // (First, Barista need to pickup the ready coffee to reset the machine to idle state)
  @Test
  public void shouldNotProcessCoffeesUntilReset() {
    var coffee1 = new Coffee.MochaPlay();
    var coffee2 = new Coffee.Akkaccino();

    ActorRef<CoffeeMachine.CoffeeMachineCommand> coffeeMachine =
        testKit.spawn(CoffeeMachine.create(), "coffee-machine-shouldNotProcessCoffeesUntilReset");
    TestProbe<CoffeeMachine.CoffeeIsReady> probe = testKit.createTestProbe();

    coffeeMachine.tell(new CoffeeMachine.BrewCoffee(coffee1, probe.getRef()));
    coffeeMachine.tell(new CoffeeMachine.BrewCoffee(coffee2, probe.getRef()));

    // We receive a CoffeeIsReady signal, expected
    probe.expectMessageClass(
        CoffeeMachine.CoffeeIsReady.class, Duration.ofMillis(BREWING_DURATION_Millis + 1000));
    // Then the CoffeeMachine stay in its state 'coffee ready' (the second brew coffee command was
    // ignored),
    // We are checking that by waiting and see that no new message is sent by the CoffeeMachine.
    probe.expectNoMessage(Duration.ofMillis(BREWING_DURATION_Millis + 1000));

    testKit.stop(coffeeMachine);
  }
}
