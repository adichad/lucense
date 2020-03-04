/*******************************************************************************
 * SearchServer.java Threadpooled, socketized, distributed search daemon for
 * search request processing. *
 ***************************************************************************/

package com.adichad.lucense.searchd;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.adichad.lucense.connection.ServerConnectionManager;
import com.adichad.lucense.request.Request;
import com.adichad.lucense.request.RequestFactory;
import com.adichad.lucense.resource.SearchResourceManager;

public class SearchServer implements Runnable {

  protected boolean runOnConsole;

  protected ServerConnectionManager connectionManager;

  protected SearchResourceManager searchResourceManager;

  protected ExecutorService executor;

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger queryLogger = Logger.getLogger("QueryLogger");

  public SearchServer(SearchResourceManager searchResourceManager) {
    this.searchResourceManager = searchResourceManager;
    this.searchResourceManager.setSearchServer(this);
  }

  public void start(boolean runOnConsole) {
    this.runOnConsole = runOnConsole;
    this.connectionManager = this.searchResourceManager.getConnectionManager();
    this.executor = Executors.newCachedThreadPool();

    Thread fireStarter = new Thread(this);
    fireStarter.start(); // !
    statusLogger.log(Level.INFO,
        "SearchServer started on port: " + Integer.toString(this.searchResourceManager.getServerPort()));

    if (!runOnConsole) {
      statusLogger.removeAppender(statusLogger.getAppender("stdout"));
      statusLogger.removeAppender(statusLogger.getAppender("stderr"));

      errorLogger.removeAppender(errorLogger.getAppender("stdout"));
      errorLogger.removeAppender(errorLogger.getAppender("stderr"));

      queryLogger.removeAppender(queryLogger.getAppender("stdout"));
      queryLogger.removeAppender(queryLogger.getAppender("stderr"));
    }

  }

  public synchronized void setSearchResourceManager(SearchResourceManager newRes) {
    this.searchResourceManager = newRes;
    this.connectionManager = newRes.getConnectionManager();
    newRes.setSearchServer(this);
    newRes.closeAll();
  }
  
  @Override
  public void run() {
    Socket sock = null;
    while (true) {
      try {
        sock = this.connectionManager.getNextProcessable();
        Request request;
        if ((request = RequestFactory.receiveRequest(sock, this.searchResourceManager)) != null) {
          request.process(this.searchResourceManager, this.executor, true);
        } else
          sock.close();
      } catch (IOException e) {
        errorLogger.log(Level.ERROR, e + " [" + sock.getInetAddress().toString() + "]");
        try {
          sock.close();
        } catch (IOException ex) {
          errorLogger.log(Level.ERROR, ex + " [" + sock.getInetAddress().toString() + "]");
        }
        // e.printStackTrace();
      } catch (Exception e) {
        errorLogger.log(Level.ERROR, e + " [" + sock.getInetAddress().toString() + "]");
        try {
          sock.close();
        } catch (IOException ex) {
          errorLogger.log(Level.ERROR, ex + " [" + sock.getInetAddress().toString() + "]");
        }
        // e.printStackTrace();
      }
    }
  }
}
/*******************************************************************************
 * SearchServerStart.java ends *
 ***************************************************************************/

