package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.ParseException;

import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.ExceptionResult;
import com.adichad.lucense.result.IndexLoadResult;
import com.adichad.lucense.result.Result;

/**
 * 
 * @author aditya
 * 
 */

public class IndexLoadRequest extends Request implements Runnable {

  HashMap<String, String> indexLoadRequests;

  private SearchResourceManager searchResourceManager;

  private boolean closeSock;

  private boolean ram;

  public static boolean inProgress = false;

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  public IndexLoadRequest(Socket sock, int version, int id) {
    super(sock, version, id);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void readFrom(InputStream in) throws IOException, ParseException {
    DataInputStream dis = new DataInputStream(in);
    int len = dis.readInt();
    this.indexLoadRequests = new HashMap<String, String>();
    for (int i = 0; i < len; i++) {
      String indexName = readString(dis);
      this.indexLoadRequests.put(indexName, readString(dis));
    }
    this.ram = dis.readByte() == 1 ? true : false;

  }

  @Override
  protected void sendTo(OutputStream out) throws IOException {

  }

  @Override
  public void process(SearchResourceManager context, ExecutorService executor, boolean closeSock) {
    this.searchResourceManager = context;
    this.closeSock = closeSock;
    executor.submit(this);
  }

  @Override
  public void run() {
    try {
      readFrom(this.sock.getInputStream());
      Result iResult = new IndexLoadResult(this.id, this.searchResourceManager.loadIndexes(this.indexLoadRequests,
          this.ram));
      iResult.writeTo(this.sock.getOutputStream());
      statusLogger.log(Level.INFO, "Successfully Loaded Index(es): " + this.toString());
    } catch (IOException e) {
      errorLogger.log(Level.ERROR, e + " [" + this.sock.getInetAddress().toString() + "]");
      statusLogger.log(Level.INFO, "Failed Loading Index(es): " + this.toString());
      if (!this.sock.isClosed()) {
        try {
          ExceptionResult result = new ExceptionResult(new UnknownException(e), this.id);
          result.writeTo(this.sock.getOutputStream());
        } catch (IOException e2) {
          errorLogger.log(Level.ERROR, e2 + " [" + this.sock.getInetAddress().toString() + "]");
        }
      }
      e.printStackTrace();
    } catch (Throwable e) {
      errorLogger.log(Level.ERROR, e + " [" + this.sock.getInetAddress().toString() + "]");
      statusLogger.log(Level.INFO, "Failed Loading Index(es): " + this.toString());
      if (!this.sock.isClosed()) {
        try {
          ExceptionResult result = new ExceptionResult(new UnknownException(e), this.id);
          result.writeTo(this.sock.getOutputStream());
        } catch (IOException e2) {
          errorLogger.log(Level.ERROR, e2 + " [" + this.sock.getInetAddress().toString() + "]");
        }
      }
      e.printStackTrace();
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

  @Override
  public String toString() {
    StringBuffer buff = new StringBuffer("");
    for (String key : this.indexLoadRequests.keySet())
      buff.append("[").append(key).append(": ").append(this.indexLoadRequests.get(key)).append("]");

    return buff.toString();
  }
}
