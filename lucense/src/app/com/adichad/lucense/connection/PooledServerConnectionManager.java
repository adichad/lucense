/**
 * 
 */
package com.adichad.lucense.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.adichad.lucense.annotation.Broken;

/**
 * @author adichad
 */

@Broken
public class PooledServerConnectionManager implements ServerConnectionManager {
  private ServerSocket serverSock;

  private ConcurrentHashMap<SocketAddress, SocketReader> sockMap;

  private class SocketReader implements Runnable {
    // private Socket sock;

    public SocketReader(Socket sock) {
      // this.sock = sock;
      Thread t = new Thread(this);
      t.start();
    }

    @Override
    public void run() {
      // TODO: split this into two threads doing publish-subscribe on a
      // queue?

    }
  }

  private boolean isAcceptable(Socket sock) {
    // TODO: implement socket-level security rules
    return true;
  }

  public void run() {
    while (true) {
      try {
        Socket sock = this.serverSock.accept();
        if (isAcceptable(sock)) {
          this.sockMap.put(sock.getRemoteSocketAddress(), new SocketReader(sock));
        } else {
          sock.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public PooledServerConnectionManager(int portNumber, int socketTimeout, Set<String> allowedHosts) throws IOException {
    this.serverSock = new ServerSocket(portNumber);
    this.sockMap = new ConcurrentHashMap<SocketAddress, SocketReader>();
  }

  @Override
  public Socket getNextProcessable() throws IOException {
    // TODO Auto-generated method stub
    return null;
  }
}
