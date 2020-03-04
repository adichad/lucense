/**
 * 
 */
package com.adichad.lucense.indexer.source;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.indexer.target.IndexingTarget;

/**
 * @author adichad
 */
public class MySQLDocumentSource implements DocumentSource {

  private String dsname;

  private DataSourceQuery query;

  // private DataSourceQuery prequery;

  private DataSourceQuery postquery;

  private ResultSet result;

  private ResultSetMetaData metaResult;

  private int max;

  private int min;

  private int batchstep;

  // private String idfield;

  private Map<String, Object> batchParams;

  private long totalTimeDoc = 0;

  private boolean enableBatching;

  private long totalTimeSource = 0;

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  // private static Logger statusLogger = Logger.getLogger("StatusLogger");

  public MySQLDocumentSource(String dsname, DataSourceQuery prequery, DataSourceQuery query,
      DataSourceQuery batchminquery, DataSourceQuery batchmaxquery, DataSourceQuery postquery, int batchstep,
      String idfield) {
    this.dsname = dsname;
    // this.prequery = prequery;
    this.query = query;
    this.batchstep = batchstep;
    this.postquery = postquery;
    // this.idfield = idfield;
    this.enableBatching = true;
    try {
      if (prequery != null)
        prequery.executeUpdate(null);

      if (batchminquery != null) {
        this.result = batchminquery.getHarvest(null);
        this.result.next();

        this.min = this.result.getInt("batchmin");
      } else {
        // this.min = 0;
        this.enableBatching = false;
      }

      if (batchmaxquery != null) {
        this.result = batchmaxquery.getHarvest(null);
        this.result.next();
        this.max = this.result.getInt("batchmax");
        this.result.close();
      } else {
        // this.max = Integer.MAX_VALUE;
        this.enableBatching = false;
      }
      if (this.batchstep < 1)
        this.enableBatching = false;

      if (this.enableBatching) {
        this.batchParams = new HashMap<String, Object>();
        this.batchParams.put("batchmin", this.min);
        this.batchParams.put("batchmax", Math.min(this.max, this.min + batchstep - 1));
      }
      long start = System.currentTimeMillis();
      this.result = query.getHarvest(this.batchParams);
      this.metaResult = this.result.getMetaData();
      this.totalTimeSource += System.currentTimeMillis() - start;
    } catch (SQLException e) {
      errorLogger.log(Level.ERROR, e);
      e.printStackTrace();
    }

  }

  public void initFieldFactory(IndexingTarget target) throws SQLException {
    // FieldFactory ff = target.getFieldFactory();
    // Document theDoc = target.newDocument();
    for (int i = 1; i <= this.metaResult.getColumnCount(); i++) {
      target.initField(this.metaResult.getColumnLabel(i));

    }
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.indexer.DocumentSource#getDocument()
   */
  @Override
  public Document getDocument(IndexingTarget target, Context cx) throws SQLException {
    long start = System.currentTimeMillis();

    // FieldFactory ff = target.getFieldFactory();
    for (int i = 1; i <= this.metaResult.getColumnCount(); i++) {
      String val = this.result.getString(i);
      if (val != null) {
        target.setField(i - 1, val);
      } else {
        target.setField(i - 1, "");
      }
    }
    this.totalTimeDoc += System.currentTimeMillis() - start;
    return target.getDocument();
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.indexer.DocumentSource#next()
   */
  @Override
  public boolean next() throws SQLException {
    long start = System.currentTimeMillis();
    if (this.result.next()) {
      this.totalTimeSource += System.currentTimeMillis() - start;
      return true;
    } else {
      if (!this.enableBatching) {
        this.totalTimeDoc += System.currentTimeMillis() - start;
        return false;
      }
      if (this.min + this.batchstep <= this.max) {
        do {
          this.result.close();
          this.min += this.batchstep;
          this.batchParams.put("batchmin", this.min);
          this.batchParams.put("batchmax", Math.min(this.max, this.min + this.batchstep - 1));
          this.result = this.query.getHarvest(this.batchParams);
        } while ((this.min <= this.max) && !this.result.next());
        if (this.min <= this.max) {
          this.totalTimeDoc += System.currentTimeMillis() - start;
          return true;
        }
      }
      this.totalTimeSource += System.currentTimeMillis() - start;
      return false;
    }

  }

  @Override
  public void executePostQuery() throws SQLException {
    if (this.postquery != null)
      this.postquery.executeUpdate(null);
  }

  @Override
  public void close() throws Exception {
    if (!this.result.isClosed()) {
      this.result.close();
    }
  }

  @Override
  public String toString() {
    return this.dsname;
  }

  @Override
  public long getTotalTimeDoc() {
    return this.totalTimeDoc;
  }

  @Override
  public long getTotalTimeSource() {
    return this.totalTimeSource;
  }

  @Override
  public void finalize() {
    try {
      close();
    } catch (Exception e) {
      errorLogger.log(Level.ERROR, e);
    }
  }
}
