/**
 * 
 */
package com.adichad.lucense.connection;

import java.io.IOException;
import java.net.Socket;

/**
 * @author adichad
 * 
 */
public class DefaultClientConnectionManager implements ClientConnectionManager {

  private String host;

  private int port;

  public DefaultClientConnectionManager(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public Socket getConnection() throws IOException {
    return new Socket(this.host, this.port);
  }
}
