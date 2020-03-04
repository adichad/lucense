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
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.adichad.lucense.exception.TooManyRequestsException;
import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.DeleteDocumentsResult;
import com.adichad.lucense.result.ExceptionResult;
import com.adichad.lucense.result.Result;

/**
 * @author adichad
 * 
 */
public class DeleteDocumentsRequest extends Request implements Runnable {
  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  private boolean closeSock;

  private SearchResourceManager searchResourceManager;

  private String indexName;

  private boolean commit;

  private String queryString;

  private Version luceneVersion;

  private long timeTaken;

  private boolean requestServed;

  public DeleteDocumentsRequest(Socket sock, int version, int id) {
    super(sock, version, id);
  }

  @Override
  protected void readFrom(InputStream in) throws IOException {
    DataInputStream dis = new DataInputStream(in);
    this.indexName = readString(dis);
    this.queryString = readString(dis);
    this.luceneVersion = Version.valueOf(readString(dis));

    this.commit = dis.readByte() == 1 ? true : false;
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
        List<String> indexNames = new ArrayList<String>(1);
        indexNames.add(indexName);
        Analyzer an = searchResourceManager.getAnalyzers(indexNames).iterator().next();
        QueryParser p = new QueryParser(this.luceneVersion, "", an);
        Query query = p.parse(queryString);
        this.searchResourceManager.deleteDocumentsInIndex(indexName, query, commit);
        Result result = new DeleteDocumentsResult(this.id);
        result.writeTo(this.sock.getOutputStream());
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
    buff.append("[deldocs] [").append(indexName).append("] [").append(queryString).append("]");
    return buff.toString();
  }

}
