/**
 * 
 */
package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.CloseWriterResult;
import com.adichad.lucense.result.ExceptionResult;
import com.adichad.lucense.result.Result;

/**
 * @author adichad
 * 
 */
public class CloseWriterRequest extends Request implements Runnable {
  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  private boolean closeSock;

  private SearchResourceManager searchResourceManager;

  private String indexName;

  private boolean optimize;

  public CloseWriterRequest(Socket sock, int version, int id) {
    super(sock, version, id);
  }

  @Override
  protected void readFrom(InputStream in) throws IOException {
    DataInputStream dis = new DataInputStream(in);
    this.indexName = readString(dis);
    this.optimize = dis.readByte() == 1 ? true : false;

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
      this.searchResourceManager = context;
      this.closeSock = closeSock;
      executor.submit(this);
    } catch (Exception e) {
      errorLogger.log(Level.ERROR, e + " [" + this.sock.getInetAddress().toString() + "]");
    }
  }

  @Override
  public void run() {
    try {
      readFrom(this.sock.getInputStream());
      this.searchResourceManager.closeWriter(indexName, optimize);
      Result result = new CloseWriterResult(this.id);
      result.writeTo(this.sock.getOutputStream());
      
      statusLogger.log(Level.INFO, "index " + indexName + " closed successfully");
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
