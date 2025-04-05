package com.akka.training;

public sealed interface Coffee permits Coffee.Akkaccino, Coffee.CaffeJava, Coffee.MochaPlay {
  record Akkaccino() implements Coffee {}

  record CaffeJava() implements Coffee {}

  record MochaPlay() implements Coffee {}
}

// public interface Coffee extends Serializable {
//  final class Akkaccino implements Coffee {
//    @Override
//    public String toString() {
//      return this.getClass().getSimpleName();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//      return o == this || o instanceof Akkaccino;
//    }
//  }
//
//  final class CaffeJava implements Coffee {
//    @Override
//    public String toString() {
//      return this.getClass().getSimpleName();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//      return o == this || o instanceof CaffeJava;
//    }
//  }
//
//  final class MochaPlay implements Coffee {
//    @Override
//    public String toString() {
//      return this.getClass().getSimpleName();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//      return o == this || o instanceof MochaPlay;
//    }
//  }
// }
