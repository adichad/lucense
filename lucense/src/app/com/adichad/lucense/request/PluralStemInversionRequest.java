package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.AnalysingOnlyQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.Version;

import com.adichad.lucense.analysis.stem.StemInversionAttribute;
import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.ExceptionResult;
import com.adichad.lucense.result.PluralStemInversionResult;
import com.adichad.lucense.result.Result;

public class PluralStemInversionRequest extends Request implements Runnable {

  private SearchResourceManager searchResourceManager;

  private boolean closeSock;

  private String analyzerName;

  private String query;

  private Date startTime;

  private Date endTime;

  private long timeTaken;

  private Version luceneVersion;

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger queryLogger = Logger.getLogger("QueryLogger");

  public PluralStemInversionRequest(Socket sock, int version, int id) {
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
    this.analyzerName = readString(dis);
    this.query = readString(dis);
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
      Vector<String> indexes = new Vector<String>();
      indexes.add(this.analyzerName);
      Analyzer analyzer = this.searchResourceManager.getAnalyzers(indexes).iterator().next();
      AnalysingOnlyQueryParser qp = new AnalysingOnlyQueryParser(this.luceneVersion, "", analyzer);
      qp.addAttributeClass(StemInversionAttribute.class);
      qp.parse(this.query);
      Map<String, List<Attribute>> attMap = qp.getAttributes();
      Map<String, Map<String, Set<String>>> inversions = new HashMap<String, Map<String, Set<String>>>();
      for (String field : attMap.keySet()) {
        List<Attribute> atts = attMap.get(field);
        for (Attribute att : atts) {
          if (att instanceof StemInversionAttribute) {
            if (!inversions.containsKey(field))
              inversions.put(field, new HashMap<String, Set<String>>());
            Map<String, Set<String>> currInvs = ((StemInversionAttribute) att).getStemInversions();
            if (!currInvs.isEmpty()) {
              Map<String, Set<String>> ci = inversions.get(field);
              for (String orig : currInvs.keySet()) {
                if (!ci.containsKey(orig))
                  ci.put(orig, new HashSet<String>());
                ci.get(orig).addAll(currInvs.get(orig));
              }
            }
          }
        }
      }
      Result iResult = new PluralStemInversionResult(this.id, inversions);
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
    buff.append("[plustem-invert] [").append(this.analyzerName).append("] [").append(this.query).append("]");
    return buff.toString();
  }

}
