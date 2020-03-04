package com.adichad.lucense.connection.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.connection.db.MySQLDatabaseConnectionManager.MySQLPreparedStatement;

public class MySQLPreparedStatementTest {

  private MySQLDatabaseConnectionManager db;

  private static final String queryString = "select field1, field2 from samples1 where docid between ? and ?";

  @Before
  public void setUp() throws Exception {
    this.db = new MySQLDatabaseConnectionManager("127.0.0.1", "lucensetest", "adichad", "qwerty12");
    // pstmt =
    // db.prepareStatement("select field1, field2 from samples1 where docid between ? and ?");
  }

  @After
  public void tearDown() throws Exception {
    this.db.close();
  }

  private MySQLPreparedStatement getPreparedStatement(MySQLDatabaseConnectionManager db) throws SQLException {
    return db.prepareStatement(queryString);
  }

  private MySQLPreparedStatement getInitializedPreparedStatement(MySQLDatabaseConnectionManager db) throws SQLException {
    MySQLPreparedStatement pstmt = getPreparedStatement(db);
    pstmt.setInt(1, 1);
    pstmt.setInt(2, 5);
    return pstmt;
  }

  @Test
  public void testPrepareStatementAfterReconnect() throws SQLException {
    this.db.reconnect();
    MySQLPreparedStatement pstmt = getPreparedStatement(this.db);
    assertNotNull(pstmt);
  }

  @Test
  public void testSetIntAfterReconnect() throws SQLException {
    MySQLPreparedStatement pstmt = getPreparedStatement(this.db);
    try {
      this.db.reconnect();
      pstmt.setInt(1, 1);
      pstmt.setInt(2, 5);
    } catch (SQLException ex) {
      fail(ex.getMessage());
    }
  }

  @Test
  public void testExecuteQueryAfterReconnect() throws SQLException {
    MySQLPreparedStatement pstmt = getInitializedPreparedStatement(this.db);
    this.db.reconnect();
    ResultSet rs = pstmt.executeQuery();
    assertNotNull(rs);
  }

  @Test
  public void testResultSetTraversalAfterReconnect() throws SQLException {
    MySQLPreparedStatement pstmt = getInitializedPreparedStatement(this.db);
    ResultSet rs = pstmt.executeQuery();
    this.db.reconnect();
    try {
      while (rs.next()) {
        System.out.println("'" + rs.getString("field1") + "','" + rs.getString("field2") + "'");
      }
    } catch (SQLException e) {
      e.printStackTrace();
      assertEquals("S1000", e.getSQLState());
      return;
    }
    fail();
  }
}
