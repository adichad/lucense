/**
 * 
 */
package com.adichad.lucense.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author adichad
 */

public class DefaultServerConnectionManager implements ServerConnectionManager {
  private ServerSocket serverSock;

  private Set<String> allowedHosts;

  private int socketTimeout;

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  public DefaultServerConnectionManager(int portNumber, int socketTimeout, Set<String> allowedHosts) throws IOException {
    this.serverSock = new ServerSocket(portNumber);
    this.allowedHosts = allowedHosts;
    this.socketTimeout = socketTimeout;
  }
  
  public DefaultServerConnectionManager(DefaultServerConnectionManager old, int socketTimeout, Set<String> allowedHosts) throws IOException {
    this.serverSock = old.serverSock;
    this.allowedHosts = allowedHosts;
    this.socketTimeout = socketTimeout;
  }

  private boolean isAcceptable(Socket sock) {
    String actualHost = sock.getInetAddress().toString();
    if (this.allowedHosts.contains(actualHost))
      return true;
    try {
      errorLogger.log(Level.WARN, "Hack attempt from: " + actualHost);
      sock.close();
    } catch (IOException e) {
      errorLogger.log(Level.ERROR, "Socket not closed: " + actualHost);
    }
    return false;
  }

  @Override
  public Socket getNextProcessable() throws IOException {
    Socket sock;
    do {
      sock = this.serverSock.accept();
    } while (!isAcceptable(sock));
    sock.setSoTimeout(this.socketTimeout);
    return sock;
  }
}
