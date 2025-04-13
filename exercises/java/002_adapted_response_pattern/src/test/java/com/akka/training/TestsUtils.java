package com.akka.training;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import java.util.HashMap;
import java.util.Optional;
import org.slf4j.event.Level;

class TestsUtils {

  public static CapturedLogEvent expectedInfoLog(String message) {
    return new CapturedLogEvent(
        Level.INFO, message, Optional.empty(), Optional.empty(), new HashMap<>());
  }

  public static <T> CapturedLogEvent lastCapturedLogEvent(BehaviorTestKit<T> testKit) {
    final var logs = testKit.getAllLogEntries();
    return logs.get(logs.size() - 1);
  }

  public static <T> CapturedLogEvent offsetCapturedLogEvent(
      BehaviorTestKit<T> testKit, int offsetFromEnd) {

    final var logs = testKit.getAllLogEntries();
    if (offsetFromEnd > 0 && offsetFromEnd <= logs.size()) {
      return logs.get(logs.size() - offsetFromEnd - 1);
    } else {
      throw new RuntimeException(
          String.format(
              "Invalid offsetFromEnd parameter, %s is not within [0, logsSize]", offsetFromEnd));
    }
  }
}
