/*
 * 
 */
package com.adichad.lucense.connection.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class MySQLDatabaseConnectionManager implements DatabaseConnectionManager {
  Connection con;

  Statement stmt;

  PreparedStatement pstmt;

  String host;

  String db;

  String user;

  String passwd;

  private int errorid;

  private int insertid;

  private int numDBTries = 0;

  private static int MAX_FAILS = 4;

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  public MySQLDatabaseConnectionManager(String host, String db, String user, String pwd) throws SQLException {
    this.host = host;
    this.db = db;
    this.user = user;
    this.passwd = pwd;
    this.errorid = 0;
    connect();
  }

  public void reconnect() throws SQLException {
    close();
    connect();
  }

  private void connect() throws SQLException {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      this.con = DriverManager.getConnection("jdbc:mysql://" + this.host + "/" + this.db + "?user=" + this.user
          + "&password=" + this.passwd + "&autoReconnect=true");
      this.stmt = this.con.createStatement();
      this.numDBTries = 0;
    } catch (SQLException sqlx) {
      if ((sqlx.getSQLState().equals("S1000") || sqlx.getSQLState().equals("08S01")) && (sqlx.getErrorCode() == 0)) {
        errorLogger.log(Level.WARN, sqlx);
        this.numDBTries++;
        if (this.numDBTries < MAX_FAILS) {
          try {
            Thread.sleep(5000);
          } catch (Exception exception) {
            errorLogger.log(Level.ERROR, exception);
          }
          connect();
        } else
          throw sqlx;
      } else {
        errorLogger.log(Level.ERROR, sqlx);
        sqlx.printStackTrace();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * com.adichad.lucense.connection.db.DatabaseConnectionManager#runQuery(java
   * .lang.String)
   */
  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    if (this.con.isClosed())
      connect();
    ResultSet rs = null;
    try {
      rs = this.stmt.executeQuery(sql);
    } catch (SQLException sqex) {
      if ((sqex.getSQLState().equals("S1000") || sqex.getSQLState().equals("08S01")) && (sqex.getErrorCode() == 0)) {
        reconnect();
        try {
          rs = this.stmt.executeQuery(sql);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      } else
        sqex.printStackTrace();
    } catch (Exception ee) {
      ee.printStackTrace();
    }
    return rs;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.adichad.lucense.connection.db.DatabaseConnectionManager#executeUpdate
   * (java.lang.String)
   */
  @Override
  public int executeUpdate(String sql) throws SQLException {
    if (this.con.isClosed())
      connect();
    int retval = 1;
    try {
      this.stmt.executeUpdate(sql);
      ResultSet res = this.stmt.getGeneratedKeys();
      while (res.next()) {
        this.insertid = res.getInt(1);
      }
      retval = 0;
    } catch (SQLException sqex) {
      if ((sqex.getSQLState().equals("S1000") || sqex.getSQLState().equals("08S01")) // 08001
          && (sqex.getErrorCode() == 0)) {
        errorLogger.log(Level.WARN, sqex);
        reconnect();
        try {
          this.stmt.executeUpdate(sql);
          ResultSet res = this.stmt.getGeneratedKeys();
          while (res.next()) {
            this.insertid = res.getInt(1);
          }
          retval = 0;
        } catch (Exception ex) {
          errorLogger.log(Level.ERROR, sqex);
          ex.printStackTrace();
        }
      } else
        errorLogger.log(Level.ERROR, sqex);
    } catch (Exception ex) {
      errorLogger.log(Level.ERROR, ex);
      ex.printStackTrace();
    }
    return retval;
  }

  @Override
  protected void finalize() {
    close();
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.connection.db.DatabaseConnectionManager#close()
   */
  @Override
  public void close() {
    try {
      this.stmt.close();
      this.con.close();
    } catch (Exception ex) {
      errorLogger.log(Level.ERROR, ex);
      ex.printStackTrace();
    }
  }

  public MySQLPreparedStatement prepareStatement(String queryString) throws SQLException {
    return new MySQLPreparedStatement(queryString, this);
  }

  public class MySQLPreparedStatement {
    private PreparedStatement pstmt;

    private String qstr;

    MySQLDatabaseConnectionManager dbman;

    Map<Integer, Integer> intParams;

    Map<Integer, String> strParams;

    private MySQLPreparedStatement(String qstr, MySQLDatabaseConnectionManager dbman) throws SQLException {
      this.dbman = dbman;
      this.qstr = qstr;
      this.intParams = new HashMap<Integer, Integer>();
      this.strParams = new HashMap<Integer, String>();
      prepare();
    }

    private MySQLPreparedStatement() {

    }

    private void prepare() throws SQLException {
      try {
        this.pstmt = this.dbman.con.prepareStatement(this.qstr);
      } catch (SQLException e) {
        if (e.getSQLState().equals("S1000") || e.getSQLState().equals("08S01")) {
          this.dbman.reconnect();
          this.pstmt = MySQLDatabaseConnectionManager.this.con.prepareStatement(this.qstr);
          for (Map.Entry<Integer, Integer> entry : this.intParams.entrySet()) {
            this.pstmt.setInt(entry.getKey(), entry.getValue());
          }
          for (Map.Entry<Integer, String> entry : this.strParams.entrySet()) {
            this.pstmt.setString(entry.getKey(), entry.getValue());
          }
        } else
          throw e;
      }
    }

    public void setInt(int pos, Integer val) throws SQLException {
      try {
        this.pstmt.setInt(pos, val);
        this.intParams.put(pos, val);
      } catch (SQLException e) {
        if (e.getSQLState().equals("08003") || e.getSQLState().equals("S1000") || e.getSQLState().equals("08S01")) {
          prepare();
          this.pstmt.setInt(pos, val);
          this.intParams.put(pos, val);
        } else
          throw e;
      }
    }

    public void setString(int pos, String val) throws SQLException {
      try {
        this.pstmt.setString(pos, val);
        this.strParams.put(pos, val);
      } catch (SQLException e) {
        if (e.getSQLState().equals("08003") || e.getSQLState().equals("S1000") || e.getSQLState().equals("08S01")) {
          prepare();
          this.pstmt.setString(pos, val);
          this.strParams.put(pos, val);
        } else
          throw e;
      }

    }

    public ResultSet executeQuery() throws SQLException {
      try {
        return this.pstmt.executeQuery();
      } catch (SQLException e) {
        if (e.getSQLState().equals("08003") || e.getSQLState().equals("S1000") || e.getSQLState().equals("08S01")) { // No
                                                                                                                     // operations
                                                                                                                     // allowed
                                                                                                                     // after
          // statement closed
          prepare();
          for (int pos : this.strParams.keySet())
            this.pstmt.setString(pos, this.strParams.get(pos));
          for (int pos : this.intParams.keySet())
            this.pstmt.setInt(pos, this.intParams.get(pos));
          return this.pstmt.executeQuery();
        } else {
          System.out.println("SQL State: " + e.getSQLState());
          throw e;
        }
      }
    }

    public int executeUpdate() throws SQLException {
      try {
        return this.pstmt.executeUpdate();
      } catch (SQLException e) {
        if (e.getSQLState().equals("08003") || e.getSQLState().equals("S1000") || e.getSQLState().equals("08S01")) { // No
                                                                                                                     // operations
                                                                                                                     // allowed
                                                                                                                     // after
          // statement closed
          prepare();
          for (int pos : this.strParams.keySet())
            this.pstmt.setString(pos, this.strParams.get(pos));
          for (int pos : this.intParams.keySet())
            this.pstmt.setInt(pos, this.intParams.get(pos));
          return this.pstmt.executeUpdate();
        } else
          throw e;
      }
    }

    public void clearParameters() throws SQLException {
      this.pstmt.clearParameters();
      this.intParams.clear();
      this.strParams.clear();
    }

    @Override
    public String toString() {
      return "[" + this.intParams + "], [" + this.strParams + "]";
    }

  }
}
