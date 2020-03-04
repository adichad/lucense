/*
 * @(#)com.adichad.lucense.request.TermStatsRequest.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;

import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.ExceptionResult;
import com.adichad.lucense.result.TermStatsResult;

public class TermStatsRequest extends Request implements Runnable {
  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger queryLogger = Logger.getLogger("QueryLogger");

  private SearchResourceManager searchResourceManager;

  private boolean closeSock;

  private int offset, limit;

  private ArrayList<String> indexes;

  private Date startTime;

  private long timeTaken;

  private Date endTime;

  public TermStatsRequest(Socket sock, int version, int id) {
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

    int len = dis.readInt();
    this.indexes = new ArrayList<String>(len);
    for (int i = 0; i < len; i++) {
      this.indexes.add(readString(dis));
    }

    this.offset = dis.readInt();
    this.limit = dis.readInt();

  }

  @Override
  protected void sendTo(OutputStream out) throws IOException {

  }

  @Override
  public void run() {
    try {
      readFrom(this.sock.getInputStream());
      this.startTime = new Date();

      IndexReader reader = this.searchResourceManager.getReader(indexes);
      TermEnum te = reader.terms();

      int i;
      for (i = 0; i < offset && te.next(); ++i)
        ;

      TermStatsResult iResult = new TermStatsResult(this.id, limit);
      if (i == offset) {
        for (i = 0; i < limit && te.next(); ++i) {
          iResult.addTerm(te.term(), te.docFreq());
        }
      }
      iResult.writeTo(this.sock.getOutputStream());

      te.close();
      reader.close();

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
    buff.append("[termstats] [").append(this.indexes).append("] [").append(this.offset).append(",").append(this.limit)
        .append("]");
    return buff.toString();
  }

}
