package com.lightbend.training;

import static com.lightbend.training.TestsUtils.*;
import static org.junit.Assert.assertEquals;

import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import com.akka.training.Coffee;
import com.akka.training.CoffeeMachine;
import org.junit.Test;

public class CoffeeMachineTests {

  // CoffeeMachine should transition from IDLE to Brewing on receiving BrewCoffee message
  @Test
  public void brewing() {
    final var testKit = BehaviorTestKit.create(CoffeeMachine.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachine.CoffeeIsReady>create();

    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    testKit.clearLog();
    testKit.run(
        new CoffeeMachine.BrewCoffee(new Coffee.Akkaccino(), baristaMessageAdapter.getRef()));
    assertEquals(
        expectedInfoLog("CoffeeMachine: Brewing 1 Akkaccino"), offsetCapturedLogEvent(testKit, 1));
  }

  // CoffeeMachine should transition between its states along the full cycle
  @Test
  public void fullCycle() {
    final var testKit = BehaviorTestKit.create(CoffeeMachine.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachine.CoffeeIsReady>create();

    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    testKit.clearLog();
    testKit.run(
        new CoffeeMachine.BrewCoffee(new Coffee.Akkaccino(), baristaMessageAdapter.getRef()));
    assertEquals(
        expectedInfoLog("CoffeeMachine: Brewing 1 Akkaccino"), offsetCapturedLogEvent(testKit, 1));
    assertEquals(expectedInfoLog("CoffeeMachine: Coffee is ready"), lastCapturedLogEvent(testKit));

    testKit.run(new CoffeeMachine.PickupCoffee());

    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
  }

  // CoffeeMachine should be able to start a new cycle after completing one
  @Test
  public void restartCycle() {
    final var testKit = BehaviorTestKit.create(CoffeeMachine.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachine.CoffeeIsReady>create();

    testKit.run(
        new CoffeeMachine.BrewCoffee(new Coffee.Akkaccino(), baristaMessageAdapter.getRef()));
    testKit.run(new CoffeeMachine.PickupCoffee());
    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    testKit.run(
        new CoffeeMachine.BrewCoffee(new Coffee.MochaPlay(), baristaMessageAdapter.getRef()));
    assertEquals(
        expectedInfoLog("CoffeeMachine: Brewing 1 MochaPlay"), offsetCapturedLogEvent(testKit, 1));
    assertEquals(expectedInfoLog("CoffeeMachine: Coffee is ready"), lastCapturedLogEvent(testKit));
  }

  @Test
  public void shouldSendReadySignalToBarista() {
    final var coffee = new Coffee.CaffeJava();

    final var testKit = BehaviorTestKit.create(CoffeeMachine.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachine.CoffeeIsReady>create();

    testKit.run(new CoffeeMachine.BrewCoffee(coffee, baristaMessageAdapter.getRef()));

    final var coffeesReady = baristaMessageAdapter.getAllReceived();
    assertEquals(1, coffeesReady.size());

    CoffeeMachine.CoffeeIsReady readyCoffee = coffeesReady.get(0);
    assertEquals(coffee, readyCoffee.coffee);
  }

  @Test
  public void shouldNotProcessCoffeesUntilReset() {
    final var coffee1 = new Coffee.MochaPlay();
    final var coffee2 = new Coffee.Akkaccino();

    final var testKit = BehaviorTestKit.create(CoffeeMachine.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachine.CoffeeIsReady>create();

    testKit.run(new CoffeeMachine.BrewCoffee(coffee1, baristaMessageAdapter.getRef()));
    testKit.run(new CoffeeMachine.BrewCoffee(coffee2, baristaMessageAdapter.getRef()));

    final var coffeesReady = baristaMessageAdapter.getAllReceived();
    assertEquals(1, coffeesReady.size());

    final var readyCoffee = coffeesReady.getFirst();
    assertEquals(coffee1, readyCoffee.coffee);
  }
}
