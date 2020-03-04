package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.AnalysingOnlyQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;

import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.AnalyzeQueryResult;
import com.adichad.lucense.result.ExceptionResult;
import com.adichad.lucense.result.Result;

public class AnalyzeQueryRequest extends Request implements Runnable {

  private SearchResourceManager searchResourceManager;

  private boolean closeSock;

  private String analyzerName;

  private String query;

  private Date startTime;

  private Date endTime;

  private long timeTaken;

  private List<String> indexes;

  private Version luceneVersion;

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger queryLogger = Logger.getLogger("QueryLogger");

  public AnalyzeQueryRequest(Socket sock, int version, int id) {
    super(sock, version, id);
  }

  @Override
  public void process(SearchResourceManager context, ExecutorService executor, boolean closeSock) {
    this.searchResourceManager = context;
    this.closeSock = closeSock;
    executor.submit(this);
  }

  @Override
  protected void readFrom(InputStream in) throws IOException, ParseException {
    DataInputStream dis = new DataInputStream(in);
    this.query = readString(dis);
    int len = dis.readInt();
    this.indexes = new ArrayList<String>(len);
    for (int i = 0; i < len; i++) {
      this.indexes.add(readString(dis));
    }
    this.analyzerName = readString(dis);
    this.luceneVersion = Version.valueOf(readString(dis));

  }

  @Override
  protected void sendTo(OutputStream out) throws IOException {

  }

  @Override
  public void run() {
    try {
      readFrom(this.sock.getInputStream());
      this.startTime = new Date();

      Analyzer analyzer = this.searchResourceManager.getAnalyzers(indexes, analyzerName).iterator().next();
      AnalysingOnlyQueryParser qp = new AnalysingOnlyQueryParser(this.luceneVersion, "", analyzer);
      qp.parse(this.query);
      Result iResult = new AnalyzeQueryResult(this.id, qp.analyzedTerms());
      iResult.writeTo(this.sock.getOutputStream());
      this.endTime = new Date();
      this.timeTaken = this.endTime.getTime() - this.startTime.getTime();
      queryLogger.log(Level.INFO, this.toString());
    } catch (IOException e) {
      errorLogger.log(Level.ERROR, e + " [" + this.sock.getInetAddress().toString() + "]");
      if (!this.sock.isClosed()) {
        try {
          ExceptionResult result = new ExceptionResult(new UnknownException(e), this.id);
          result.writeTo(this.sock.getOutputStream());
        } catch (IOException e2) {
          errorLogger.log(Level.ERROR, e2 + " [" + this.sock.getInetAddress().toString() + "]");
        }
      }
      e.printStackTrace();
    } catch (ParseException e) {
      errorLogger.log(Level.ERROR, e + " [" + this.sock.getInetAddress().toString() + "]");
      if (!this.sock.isClosed()) {
        try {
          ExceptionResult result = new ExceptionResult(new UnknownException(e), this.id);
          result.writeTo(this.sock.getOutputStream());
        } catch (IOException e2) {
          errorLogger.log(Level.ERROR, e2 + " [" + this.sock.getInetAddress().toString() + "]");
        }
      }
      e.printStackTrace();
    } catch (Exception e) {
      errorLogger.log(Level.ERROR, e + " [" + this.sock.getInetAddress().toString() + "]");
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
    StringBuilder buff = new StringBuilder();
    Formatter formatter = new Formatter(buff);

    long secs = this.timeTaken / (1000L);
    long msecs = this.timeTaken % (1000L);

    buff.append(secs);
    buff.append(".");
    formatter.format("%03d", msecs);
    buff.append(" sec ");
    buff.append("[analyzequery] [").append(this.analyzerName).append("] [").append(this.query).append("]");
    return buff.toString();
  }

}
