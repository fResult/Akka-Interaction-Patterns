package com.akka.training;

public sealed interface Coffee permits Coffee.Akkaccino, Coffee.CaffeJava, Coffee.MochaPlay {
  record Akkaccino() implements Coffee {}

  record CaffeJava() implements Coffee {}

  record MochaPlay() implements Coffee {}
}
