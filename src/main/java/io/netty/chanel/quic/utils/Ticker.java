package io.netty.chanel.quic.utils;

public interface Ticker {

  static Ticker systemTicker() {
    return System::nanoTime;
  }

  long nanoTime();
}
