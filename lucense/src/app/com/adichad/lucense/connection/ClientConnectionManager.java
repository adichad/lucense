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
public interface ClientConnectionManager {
  public Socket getConnection() throws IOException;
}
