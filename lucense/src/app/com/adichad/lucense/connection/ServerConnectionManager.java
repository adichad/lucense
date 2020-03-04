package com.adichad.lucense.connection;

import java.io.IOException;
import java.net.Socket;

public interface ServerConnectionManager {
  public Socket getNextProcessable() throws IOException;
}
