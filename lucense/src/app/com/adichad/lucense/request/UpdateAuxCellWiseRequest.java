/**
 * 
 */
package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.adichad.lucense.bitmap.AuxIndexManager;
import com.adichad.lucense.bitmap.BitMapOperationStatus;
import com.adichad.lucense.bitmap.CellResolver;
import com.adichad.lucense.exception.TooManyRequestsException;
import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.AddToAuxResult;
import com.adichad.lucense.result.ExceptionResult;
import com.adichad.lucense.result.UpdateAuxResult;
import com.sleepycat.db.DatabaseException;

/**
 * 
 * 
 */
public class UpdateAuxCellWiseRequest extends Request implements Runnable {
  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  boolean closeSock = true;

  private SearchResourceManager searchResourceManager;

  private String indexName;

  // private CellResolver cs = null;

  byte csWiseRowsToAddIn[][][] = null;

  byte csWiseRowsToDeleteFrom[][][] = null;

  CellResolver cellResolvers[] = null;

  private AuxIndexManager indexer = null;

  // private byte[] rowId = null;

  private byte commandType;

  private long timeTaken;

  private StringBuilder succeeds;

  private StringBuilder fails;

  private HashSet<CellResolver> cellsFailed;

  private boolean requestServed;

  public UpdateAuxCellWiseRequest(Socket sock, int version, int id, byte type) {
    super(sock, version, id);
    this.commandType = type;
  }

  @Override
  protected void readFrom(InputStream in) throws IOException {
    DataInputStream dis = new DataInputStream(in);
    this.indexName = readString(dis);
    indexer = searchResourceManager.getAuxIndexer(this.indexName);
    if (indexer == null)
      throw new IOException("aux-index: " + indexName + " not found");
    int count = dis.readInt();

    csWiseRowsToAddIn = new byte[count][][];
    csWiseRowsToDeleteFrom = new byte[count][][];
    cellResolvers = new CellResolver[count];

    for (int i = 0; i < count; i++) {
      cellResolvers[i] = indexer.getCellDictionary().readValueFrom(dis);

      int countToAdd = dis.readInt();
      byte rowsToaddIn[][] = new byte[countToAdd][];
      for (int j = 0; j < countToAdd; j++) {
        rowsToaddIn[j] = readStringInBytes(dis);
      }
      csWiseRowsToAddIn[i] = rowsToaddIn;

      int countTodelete = dis.readInt();
      byte rowsToDeleteFrom[][] = new byte[countTodelete][];
      for (int j = 0; j < countTodelete; j++) {
        rowsToDeleteFrom[j] = readStringInBytes(dis);
      }
      csWiseRowsToDeleteFrom[i] = rowsToDeleteFrom;

    }
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

        cellsFailed = new HashSet<CellResolver>();
        succeeds = new StringBuilder();
        fails = new StringBuilder();
        StringBuilder addrows = new StringBuilder();
        StringBuilder delrows = new StringBuilder();
        for (int i = 0; i < cellResolvers.length; i++) {
          addrows.setLength(0);
          for (byte[] addrow : csWiseRowsToAddIn[i])
            addrows.append(new String(addrow)).append(" ");

          delrows.setLength(0);
          for (byte[] delrow : csWiseRowsToDeleteFrom[i])
            delrows.append(new String(delrow)).append(" ");
          try {

            BitMapOperationStatus os = indexer.getRowHandler().update(cellResolvers[i], csWiseRowsToAddIn[i],
                csWiseRowsToDeleteFrom[i]);

            if (os != BitMapOperationStatus.SUCCESS) {
              fails.append("[").append(cellResolvers[i].getOriginal(0));
              fails.append("+[").append(addrows.toString().trim()).append("] ");
              fails.append("-[").append(delrows.toString().trim()).append("]");
              fails.append("]");
              cellsFailed.add(cellResolvers[i]);
            } else {
              succeeds.append("[").append(cellResolvers[i].getOriginal(0)).append(" ");
              succeeds.append("+[").append(addrows.toString().trim()).append("] ");
              succeeds.append("-[").append(delrows.toString().trim()).append("]");
              succeeds.append("]");
            }
          } catch (DatabaseException e) {
            fails.append("[").append(cellResolvers[i].getOriginal(0)).append(" ");
            fails.append("+[").append(addrows.toString().trim()).append("] ");
            fails.append("-[").append(delrows.toString().trim()).append("]");
            fails.append("]");
            cellsFailed.add(cellResolvers[i]);
          }
        }

        UpdateAuxResult res = new UpdateAuxResult(this.id, cellsFailed, commandType);
        res.writeTo(this.sock.getOutputStream());
        this.timeTaken = System.currentTimeMillis() - start;
        statusLogger.log(Level.INFO, this.toString());// "[UpdateAux]" +
                                                      // status);
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
      if (requestServed)
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
    buff.append("[cellwise-auxupdate] [").append(this.indexName).append("] [")
        .append(this.cellResolvers.length - this.cellsFailed.size()).append("/").append(this.cellResolvers.length)
        .append("] ");
    buff.append("failed [").append(this.fails.toString().trim()).append("], succeeded [")
        .append(succeeds.toString().trim()).append("]");

    return buff.toString();
  }
}
