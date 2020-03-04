package com.adichad.lucense.indexer.source;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.adichad.lucense.connection.db.MySQLDatabaseConnectionManager;
import com.adichad.lucense.connection.db.MySQLDatabaseConnectionManager.MySQLPreparedStatement;
import com.adichad.lucense.request.Request.FieldType;

public class DataSourceQuery {
  private List<MySQLPreparedStatement> preparedStatements;

  private Map<DataSourceQuery, Map<String, Integer>> substitutionTargets;

  private Map<String, FieldType> substitutionTypes;

  private ResultSet harvest;

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  public DataSourceQuery(String queryString, MySQLDatabaseConnectionManager db,
      Map<DataSourceQuery, Map<String, Integer>> substitutionTargets, Map<String, FieldType> substitutionTypes)
      throws SQLException {
    String[] queryStrings = queryString.split(";", 0);
    this.preparedStatements = new ArrayList<MySQLPreparedStatement>();
    for (String qstr : queryStrings)
      this.preparedStatements.add(db.prepareStatement(qstr));

    this.substitutionTargets = substitutionTargets;
    this.substitutionTypes = substitutionTypes;
    this.harvest = null;
  }

  public int executeUpdate(Map<String, Object> groundedSubstitutions) throws SQLException {
    makeSubstitutions(groundedSubstitutions);
    int i = 0;
    for (MySQLPreparedStatement preparedStatement : this.preparedStatements) {
      i = preparedStatement.executeUpdate();
    }
    return i;
  }

  public ResultSet getHarvest(Map<String, Object> groundedSubstitutions) throws SQLException {
    if ((this.harvest == null) || this.harvest.isClosed()
        || ((groundedSubstitutions != null) && (this.substitutionTargets != null))) {

      makeSubstitutions(groundedSubstitutions);
      for (MySQLPreparedStatement preparedStatement : this.preparedStatements) {
        statusLogger.log(Level.INFO, "Executing query on source: " + preparedStatement);
        this.harvest = preparedStatement.executeQuery();
      }
    }
    this.harvest.beforeFirst();
    return this.harvest;
  }

  private void makeSubstitutions(Map<String, Object> groundedSubstitutions) throws SQLException {
    if (this.substitutionTargets != null) {
      for (DataSourceQuery subQuery : this.substitutionTargets.keySet()) {
        Map<String, Integer> vars = this.substitutionTargets.get(subQuery);
        ResultSet subQueryHarvest = subQuery.getHarvest(groundedSubstitutions);

        for (String name : vars.keySet()) {
          Integer pos = vars.get(name);
          FieldType type = this.substitutionTypes.get(name);
          if ((groundedSubstitutions != null) && groundedSubstitutions.containsKey(name))
            prepareStatement(pos, type, groundedSubstitutions.get(name));
          else
            prepareStatement(pos, type, getData(subQueryHarvest, name, type));
        }
      }
    }

  }

  private Object getData(ResultSet subQueryHarvest, String name, FieldType type) throws SQLException {
    if (subQueryHarvest.first()) {
      switch (type) {
      case TYPE_INT:
        return subQueryHarvest.getInt(name);
      case TYPE_STRING:
        return subQueryHarvest.getString(name);
      default:
        return subQueryHarvest.getString(name);
      }
    }
    return null;
  }

  private void prepareStatement(Integer pos, FieldType type, Object object) throws SQLException {
    switch (type) {
    case TYPE_INT:
      for (MySQLPreparedStatement preparedStatement : this.preparedStatements)
        preparedStatement.setInt(pos, (Integer) object);
      break;
    case TYPE_STRING:
      for (MySQLPreparedStatement preparedStatement : this.preparedStatements)
        preparedStatement.setString(pos, (String) object);
      break;
    default:
      for (MySQLPreparedStatement preparedStatement : this.preparedStatements)
        preparedStatement.setString(pos, (String) object);
      break;
    }
  }

}
