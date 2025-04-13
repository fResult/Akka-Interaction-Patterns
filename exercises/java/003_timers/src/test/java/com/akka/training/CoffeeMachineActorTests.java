package com.akka.training;

import akka.actor.testkit.typed.javadsl.LoggingTestKit;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.akka.training.coffeeMachine.CoffeeMachineActor;
import java.time.Duration;
import org.junit.ClassRule;
import org.junit.Test;

public class CoffeeMachineActorTests {

  @ClassRule public static final TestKitJunitResource testKit = new TestKitJunitResource();
  private final int BREWING_DURATION_Millis = 10000;

  // CoffeeMachine should transition from IDLE to Brewing on receiving BrewCoffee message
  @Test
  public void brewing() {
    ActorRef<CoffeeMachineActor.CoffeeMachineCommand> coffeeMachine =
        testKit.spawn(CoffeeMachineActor.create(), "coffee-machine-brewing");
    TestProbe<CoffeeMachineActor.CoffeeIsReady> probe = testKit.createTestProbe();
    var coffee = new Coffee.Akkaccino();
    LoggingTestKit.info("CoffeeMachine: Brewing 1 " + coffee)
        .withOccurrences(1)
        .expect(
            testKit.system(),
            () -> {
              coffeeMachine.tell(new CoffeeMachineActor.BrewCoffee(coffee, probe.getRef()));
              return null;
            });

    testKit.stop(coffeeMachine);
  }

  @Test
  public void shouldSendReadySignalToBarista() {
    ActorRef<CoffeeMachineActor.CoffeeMachineCommand> coffeeMachine =
        testKit.spawn(CoffeeMachineActor.create(), "coffee-machine-shouldSendReadySignalToBarista");
    TestProbe<CoffeeMachineActor.CoffeeIsReady> probe = testKit.createTestProbe();

    coffeeMachine.tell(
        new CoffeeMachineActor.BrewCoffee(new Coffee.Akkaccino(), probe.getRef()));

    probe.expectNoMessage(Duration.ofMillis(BREWING_DURATION_Millis - 100));
    probe.expectMessageClass(
        CoffeeMachineActor.CoffeeIsReady.class, Duration.ofMillis(BREWING_DURATION_Millis + 100));

    testKit.stop(coffeeMachine);
  }

  // Once a full cycle is completed => Barista pick-up the coffee, CoffeeMachine is back to idle
  // state.
  // The CoffeeMachine should be able a new brewing cycle.
  @Test
  public void restartCycle() {
    ActorRef<CoffeeMachineActor.CoffeeMachineCommand> coffeeMachine =
        testKit.spawn(CoffeeMachineActor.create(), "coffee-machine-restartCycle");
    TestProbe<CoffeeMachineActor.CoffeeIsReady> probe = testKit.createTestProbe();

    coffeeMachine.tell(
        new CoffeeMachineActor.BrewCoffee(new Coffee.Akkaccino(), probe.getRef()));
    probe.expectNoMessage(Duration.ofMillis(BREWING_DURATION_Millis - 100));
    probe.expectMessageClass(
        CoffeeMachineActor.CoffeeIsReady.class, Duration.ofMillis(BREWING_DURATION_Millis + 100));
    coffeeMachine.tell(new CoffeeMachineActor.PickupCoffee());

    coffeeMachine.tell(
        new CoffeeMachineActor.BrewCoffee(new Coffee.MochaPlay(), probe.getRef()));
    probe.expectNoMessage(Duration.ofMillis(BREWING_DURATION_Millis - 100));
    probe.expectMessageClass(
        CoffeeMachineActor.CoffeeIsReady.class, Duration.ofMillis(BREWING_DURATION_Millis + 100));

    testKit.stop(coffeeMachine);
  }

  // Once it started a brewing cycle, any other "BrewCoffee" command will be ignored.
  // (First, Barista need to pickup the ready coffee to reset the machine to idle state)
  @Test
  public void shouldNotProcessCoffeesUntilReset() {
    var coffee1 = new Coffee.MochaPlay();
    var coffee2 = new Coffee.Akkaccino();

    ActorRef<CoffeeMachineActor.CoffeeMachineCommand> coffeeMachine =
        testKit.spawn(
            CoffeeMachineActor.create(), "coffee-machine-shouldNotProcessCoffeesUntilReset");
    TestProbe<CoffeeMachineActor.CoffeeIsReady> probe = testKit.createTestProbe();

    coffeeMachine.tell(new CoffeeMachineActor.BrewCoffee(coffee1, probe.getRef()));
    coffeeMachine.tell(new CoffeeMachineActor.BrewCoffee(coffee2, probe.getRef()));

    // We receive a CoffeeIsReady signal, expected
    probe.expectMessageClass(
        CoffeeMachineActor.CoffeeIsReady.class, Duration.ofMillis(BREWING_DURATION_Millis + 1000));
    // Then the CoffeeMachine stay in its state 'coffee ready' (the second brew coffee command was
    // ignored),
    // We are checking that by waiting and see that no new message is sent by the CoffeeMachine.
    probe.expectNoMessage(Duration.ofMillis(BREWING_DURATION_Millis + 1000));

    testKit.stop(coffeeMachine);
  }
}
