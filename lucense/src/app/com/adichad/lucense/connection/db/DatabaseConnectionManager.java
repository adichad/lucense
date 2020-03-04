package com.adichad.lucense.connection.db;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DatabaseConnectionManager {
  public abstract ResultSet executeQuery(String sql) throws SQLException;

  public abstract int executeUpdate(String sql) throws SQLException;

  public abstract void close();
}
