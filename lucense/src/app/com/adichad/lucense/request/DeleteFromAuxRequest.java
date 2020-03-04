/**
 * 
 */
package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Formatter;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.adichad.lucense.bitmap.AuxIndexManager;
import com.adichad.lucense.bitmap.BitMapOperationStatus;
import com.adichad.lucense.bitmap.CellResolver;
import com.adichad.lucense.exception.TooManyRequestsException;
import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.DeleteAuxResult;
import com.adichad.lucense.result.ExceptionResult;

/**
 * 
 * 
 */
public class DeleteFromAuxRequest extends Request implements Runnable {
  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  boolean closeSock = true;

  private SearchResourceManager searchResourceManager;

  private String indexName;

  private CellResolver cs = null;

  private AuxIndexManager indexer = null;

  private byte[] rowId = null;

  private byte commandType;

  private byte boolByPassDictionary = 0;

  private long timeTaken;

  private boolean requestServed;

  public DeleteFromAuxRequest(Socket sock, int version, int id, byte type) {
    super(sock, version, id);
    this.commandType = type;
  }

  @Override
  protected void readFrom(InputStream in) throws IOException {
    DataInputStream dis = new DataInputStream(in);
    this.boolByPassDictionary = dis.readByte();
    this.indexName = readString(dis);
    indexer = searchResourceManager.getAuxIndexer(this.indexName);
    if (indexer == null)
      throw new IOException("aux-index: " + indexName + " not found");
    rowId = readStringInBytes(dis);
    cs = indexer.getCellDictionary().readArrayFrom(dis);

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
      this.searchResourceManager = context;
      executor.submit(this);
    } catch (Exception e) {
      errorLogger.log(Level.ERROR, e + " [" + this.sock.getInetAddress().toString() + "]");
    }
  }

  @Override
  public void run() {
    try {
      this.requestServed = false;
      if (this.searchResourceManager.checkIncrementConcurrancy()) {
        this.requestServed = true;
        long start = System.currentTimeMillis();
        readFrom(this.sock.getInputStream());
        BitMapOperationStatus os = indexer.getRowHandler().delete(rowId, cs, null);
        DeleteAuxResult res = new DeleteAuxResult(this.id, os, commandType);
        res.writeTo(this.sock.getOutputStream());
        this.timeTaken = System.currentTimeMillis() - start;
        statusLogger.log(Level.INFO, this.toString());
      } else
        throw new TooManyRequestsException();
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
      if(requestServed)
        this.searchResourceManager.removeRequest(null);
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
    StringBuilder buff = new StringBuilder();
    Formatter formatter = new Formatter(buff);
    long secs = timeTaken / (1000L);
    long msecs = timeTaken % (1000L);

    buff.append(secs);
    buff.append(".");
    formatter.format("%03d", msecs);
    // formatter.format("%3.3f", time);
    buff.append(" sec ");
    buff.append("[rowwise-auxdelete] [").append(indexName).append("] [").append(new String(rowId)).append("] [")
        .append(cs.getSize()).append("]");
    return buff.toString();
  }
}
