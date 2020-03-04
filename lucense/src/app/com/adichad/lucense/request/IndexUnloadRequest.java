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

import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.IndexUnloadResult;
import com.adichad.lucense.result.Result;

/**
 * 
 * @author aditya
 * 
 */

public class IndexUnloadRequest extends Request implements Runnable {

  HashMap<String, String> indexUnloadRequests;

  private SearchResourceManager searchResourceManager;

  private boolean closeSock;

  public static boolean inProgress = false;

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  // private static Logger statusLogger = Logger.getLogger("StatusLogger");

  public IndexUnloadRequest(Socket sock, int version, int id) {
    super(sock, version, id);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void readFrom(InputStream in) throws IOException, ParseException {
    DataInputStream dis = new DataInputStream(in);
    int len = dis.readInt();
    this.indexUnloadRequests = new HashMap<String, String>();
    for (int i = 0; i < len; i++) {
      String indexName = readString(dis);
      this.indexUnloadRequests.put(indexName, readString(dis));
    }
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
      System.out.println("About to Read LoadIndex Request");
      readFrom(this.sock.getInputStream());
      if (this.searchResourceManager == null)
        System.out.println("SearchResourceManager: " + this.searchResourceManager);
      Result iResult = new IndexUnloadResult(this.id,
          this.searchResourceManager.unloadIndexes(this.indexUnloadRequests));
      System.out.println("Read LoadIndex Request");
      iResult.writeTo(this.sock.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
      errorLogger.log(Level.ERROR, e);
    } catch (ParseException e) {
      e.printStackTrace();
      errorLogger.log(Level.ERROR, e);
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
          e.printStackTrace();
          errorLogger.log(Level.ERROR, e);
        }
      }
    }
  }

}
