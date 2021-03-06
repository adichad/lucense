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

import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.StatusResult;

/**
 * @author adichad
 * 
 */
public class StatusRequest extends Request implements Runnable {

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private boolean closeSock;

  private SearchResourceManager context;

  public StatusRequest(Socket sock, int version, int id) {
    super(sock, version, id);
  }

  @Override
  protected void readFrom(InputStream in) throws IOException {}

  @Override
  protected void sendTo(OutputStream out) {}

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Request#process(com.adichad.lucense.request.
   * ServerContext, java.util.concurrent.ExecutorService)
   */
  @Override
  public void process(SearchResourceManager context, ExecutorService executor, boolean closeSock) {
    try {
      this.context = context;
      this.closeSock = closeSock;
      executor.submit(this);
    } catch (Exception e) {
      errorLogger.log(Level.ERROR, e);
    }
  }

  @Override
  public void run() {
    try {
      readFrom(this.sock.getInputStream());
      StatusResult result = new StatusResult(this.id);
      result.setStatus(this.context.getStatus());
      result.writeTo(this.sock.getOutputStream());
      
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (!this.sock.isClosed()) {
        try {
          if (this.closeSock) {
            this.sock.shutdownOutput();
            this.sock.close();
          }
        } catch (IOException e) {
          errorLogger.log(Level.ERROR, e + " [" + this.toString() + "]");
        }
      }

    }

  }
}
