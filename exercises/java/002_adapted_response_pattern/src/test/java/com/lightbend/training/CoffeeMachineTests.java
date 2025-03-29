package com.akka.training;

import static com.akka.training.TestsUtils.*;
import static org.junit.Assert.assertEquals;

import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import java.util.List;
import org.junit.Test;

public class CoffeeMachineTests {

    // CoffeeMachine should transition from IDLE to Brewing on receiving BrewCoffee message
    @Test
    public void brewing() {
        BehaviorTestKit<CoffeeMachine.CoffeeMachineCommand> testKit = BehaviorTestKit.create(CoffeeMachine.create());
        TestInbox<CoffeeMachine.CoffeeIsReady> baristaMessageAdapter = TestInbox.create();

        assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
        testKit.clearLog();
        testKit.run(new CoffeeMachine.BrewCoffee(new Coffee.Akkaccino(), baristaMessageAdapter.getRef()));
        assertEquals(expectedInfoLog("CoffeeMachine: Brewing 1 Akkaccino"), offsetCapturedLogEvent(testKit, 1));
    }

    // CoffeeMachine should transition between its states along the full cycle
    @Test
    public void fullCycle() {
        BehaviorTestKit<CoffeeMachine.CoffeeMachineCommand> testKit = BehaviorTestKit.create(CoffeeMachine.create());
        TestInbox<CoffeeMachine.CoffeeIsReady> baristaMessageAdapter = TestInbox.create();

        assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
        testKit.clearLog();
        testKit.run(new CoffeeMachine.BrewCoffee(new Coffee.Akkaccino(), baristaMessageAdapter.getRef()));
        assertEquals(expectedInfoLog("CoffeeMachine: Brewing 1 Akkaccino"), offsetCapturedLogEvent(testKit, 1));
        assertEquals(expectedInfoLog("CoffeeMachine: Coffee is ready"), lastCapturedLogEvent(testKit));

        testKit.run(new CoffeeMachine.PickupCoffee());

        assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    }

    // CoffeeMachine should be able to start a new cycle after completing one
    @Test
    public void restartCycle() {
        BehaviorTestKit<CoffeeMachine.CoffeeMachineCommand> testKit = BehaviorTestKit.create(CoffeeMachine.create());
        TestInbox<CoffeeMachine.CoffeeIsReady> baristaMessageAdapter = TestInbox.create();

        testKit.run(new CoffeeMachine.BrewCoffee(new Coffee.Akkaccino(), baristaMessageAdapter.getRef()));
        testKit.run(new CoffeeMachine.PickupCoffee());
        assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
        testKit.run(new CoffeeMachine.BrewCoffee(new Coffee.MochaPlay(), baristaMessageAdapter.getRef()));
        assertEquals(expectedInfoLog("CoffeeMachine: Brewing 1 MochaPlay"), offsetCapturedLogEvent(testKit, 1));
        assertEquals(expectedInfoLog("CoffeeMachine: Coffee is ready"), lastCapturedLogEvent(testKit));
    }

    @Test
    public void shouldSendReadySignalToBarista() {
        var coffee = new Coffee.CaffeJava();

        BehaviorTestKit<CoffeeMachine.CoffeeMachineCommand> testKit = BehaviorTestKit.create(CoffeeMachine.create());
        TestInbox<CoffeeMachine.CoffeeIsReady> baristaMessageAdapter = TestInbox.create();

        testKit.run(new CoffeeMachine.BrewCoffee(coffee, baristaMessageAdapter.getRef()));

        List<CoffeeMachine.CoffeeIsReady> coffeesReady = baristaMessageAdapter.getAllReceived();
        assertEquals(1, coffeesReady.size());

        CoffeeMachine.CoffeeIsReady readyCoffee = coffeesReady.get(0);
        assertEquals(coffee, readyCoffee.coffee);
    }

    @Test
    public void shouldNotProcessCoffeesUntilReset() {
        var coffee1 = new Coffee.MochaPlay();
        var coffee2 = new Coffee.Akkaccino();

        BehaviorTestKit<CoffeeMachine.CoffeeMachineCommand> testKit = BehaviorTestKit.create(CoffeeMachine.create());
        TestInbox<CoffeeMachine.CoffeeIsReady> baristaMessageAdapter = TestInbox.create();

        testKit.run(new CoffeeMachine.BrewCoffee(coffee1, baristaMessageAdapter.getRef()));
        testKit.run(new CoffeeMachine.BrewCoffee(coffee2, baristaMessageAdapter.getRef()));

        List<CoffeeMachine.CoffeeIsReady> coffeesReady = baristaMessageAdapter.getAllReceived();
        assertEquals(1, coffeesReady.size());

        CoffeeMachine.CoffeeIsReady readyCoffee = coffeesReady.get(0);
        assertEquals(coffee1, readyCoffee.coffee);
    }
}
