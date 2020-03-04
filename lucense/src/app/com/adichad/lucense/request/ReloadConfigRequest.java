/**
 * 
 */
package com.adichad.lucense.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;

import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.resource.ResourceManagerFactory;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.ExceptionResult;
import com.adichad.lucense.result.ReloadConfigResult;
import com.adichad.lucense.result.Result;
import com.adichad.lucense.searchd.SearchServer;

/**
 * @author adichad
 * 
 */
public class ReloadConfigRequest extends Request implements Runnable {
  private static Logger errorLogger = Logger.getLogger("ErrorLogger");
  private static Logger statusLogger = Logger.getLogger("StatusLogger");
  private boolean closeSock;

  private SearchResourceManager searchResourceManager;

  public ReloadConfigRequest(Socket sock, int version, int id) {
    super(sock, version, id);
  }

  @Override
  protected void readFrom(InputStream in) throws IOException {

  }

  @Override
  protected void sendTo(OutputStream out) throws IOException {

  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Request#process(com.adichad.lucense.request
   * .ServerContext, java.util.concurrent.ExecutorService)
   */
  @Override
  public void process(SearchResourceManager context, ExecutorService executor, boolean closeSock) {
    try {
      this.closeSock = closeSock;
      executor.submit(this);
      this.searchResourceManager = context;
    } catch (Exception e) {
      errorLogger.log(Level.ERROR, e + " [" + this.sock.getInetAddress().toString() + "]");
    }
  }

  @Override
  public void run() {
    try {
      Context cx = Context.enter();
      readFrom(this.sock.getInputStream());
      SearchServer server = this.searchResourceManager.getSearchServer();
      SearchResourceManager newRes = ResourceManagerFactory.createMergedSearchResourceManager(this.searchResourceManager, cx);
      server.setSearchResourceManager(newRes);
      Result result = new ReloadConfigResult(id);
      result.writeTo(this.sock.getOutputStream());
      statusLogger.log(Level.INFO, "Reloaded configuration at ["+newRes.getConfigPath()+"] successfully");
    } catch (Throwable e) {
      errorLogger.log(Level.ERROR, e);
      e.printStackTrace();
      if (!this.sock.isClosed()) {
        try {
          ExceptionResult result = new ExceptionResult(new UnknownException(e), this.id);
          result.writeTo(this.sock.getOutputStream());
        } catch (IOException e2) {
          errorLogger.log(Level.ERROR, e2 + " [" + this.sock.getInetAddress().toString() + "]");
        }
      }
    } finally {
      if (!this.sock.isClosed()) {
        try {
          if (this.closeSock) {
            this.sock.shutdownOutput();
            this.sock.close();
          }
        } catch (IOException e) {
          errorLogger.log(Level.ERROR, e + " [" + this.sock.getInetAddress().toString() + "]");
        }
      }
    }
  }

}
