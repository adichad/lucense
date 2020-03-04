package com.adichad.lucense.indexer.source;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.adichad.lucense.connection.db.MySQLDatabaseConnectionManager;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.resource.Substitution;

public class DataSource {
  private String dsname;

  private String dstype;

  private String dshost;

  private int dsport;

  private String dsuser;

  private String dbpass;

  private String dsdb;

  private int dsbatchstep;

  private String dspath;

  private String dsfiletype;

  private boolean subdirs;

  private String dsidfield;

  private HashMap<String, String> dsvarqueries;

  private HashMap<String, HashSet<Substitution>> dsvarsubqueries;

  private HashMap<String, DataSourceQuery> finalvarqueries;

  public DataSource() {
    this.dsvarqueries = new HashMap<String, String>();
    this.dsvarsubqueries = new HashMap<String, HashSet<Substitution>>();
    this.dsbatchstep = 1000;
    this.dstype = "mysql";
    this.dsidfield = null;
    this.finalvarqueries = new HashMap<String, DataSourceQuery>();

  }

  public DocumentSource getDocumentSource() throws SQLException, IOException {
    if (this.dstype.equals("mysql")) {
      MySQLDatabaseConnectionManager db = new MySQLDatabaseConnectionManager(this.dshost + ":" + this.dsport,
          this.dsdb, this.dsuser, this.dbpass);
      ;
      DataSourceQuery prequery = getVarQuery(db, "prequery");
      DataSourceQuery query = getVarQuery(db, "query");
      DataSourceQuery postquery = getVarQuery(db, "postquery");
      DataSourceQuery batchminquery = null;
      DataSourceQuery batchmaxquery = null;
      for (Substitution sub : this.dsvarsubqueries.get("query"))
        if (sub.getVarName().equals("batchmin"))
          batchminquery = this.finalvarqueries.get(sub.getVarQueryName());
        else if (sub.getVarName().equals("batchmax"))
          batchmaxquery = this.finalvarqueries.get(sub.getVarQueryName());

      return new MySQLDocumentSource(this.dsname, prequery, query, batchminquery, batchmaxquery, postquery,
          this.dsbatchstep, this.dsidfield);
    } else if (this.dstype.equals("file")) {
      return new FileDocumentSource(this.dspath, this.subdirs, this.dsfiletype);
    } else if (this.dstype.equals("file-sentence")) {
      return new FileMaxEntSentenceDocumentSource(this.dspath, this.subdirs, this.dsfiletype);
    } else
      return null;
  }

  private DataSourceQuery getVarQuery(MySQLDatabaseConnectionManager db, String queryName) throws SQLException {
    if (!this.finalvarqueries.containsKey(queryName) && (this.dsvarqueries.get(queryName) != null)
        && !this.dsvarqueries.get(queryName).equals("")) {
      Map<DataSourceQuery, Map<String, Integer>> substitutionTargets = null;
      Map<String, FieldType> substitutionTypes = null;
      if (this.dsvarsubqueries.containsKey(queryName) && (this.dsvarsubqueries.get(queryName) != null)
          && !this.dsvarsubqueries.get(queryName).isEmpty()) {
        substitutionTargets = new HashMap<DataSourceQuery, Map<String, Integer>>();
        substitutionTypes = new HashMap<String, FieldType>();
        for (Substitution sub : this.dsvarsubqueries.get(queryName)) {
          DataSourceQuery subq = getVarQuery(db, sub.getVarQueryName());
          if (!substitutionTargets.containsKey(subq))
            substitutionTargets.put(subq, new HashMap<String, Integer>());
          substitutionTargets.get(subq).put(sub.getVarName(), sub.getPos());
          substitutionTypes.put(sub.getVarName(), sub.getVarType());
        }
      }
      this.finalvarqueries.put(queryName, new DataSourceQuery(this.dsvarqueries.get(queryName), db,
          substitutionTargets, substitutionTypes));
    }
    return this.finalvarqueries.get(queryName);
  }

  public void setQuery(String value, HashSet<Substitution> varsubstitutions) {
    this.dsvarqueries.put("query", value);
    this.dsvarsubqueries.put("query", varsubstitutions);
  }

  public void setName(String value) {
    this.dsname = value;

  }

  public String getName() {
    return this.dsname;
  }

  public void setType(String value) {
    this.dstype = value;

  }

  public void setPreQuery(String value, HashSet<Substitution> varsubstitutions) {
    this.dsvarqueries.put("prequery", value);
    this.dsvarsubqueries.put("prequery", varsubstitutions);

  }

  public void setPostQuery(String value, HashSet<Substitution> varsubstitutions) {
    this.dsvarqueries.put("postquery", value);
    this.dsvarsubqueries.put("postquery", varsubstitutions);
  }

  public void setBatchStep(int value) {
    this.dsbatchstep = value;
  }

  public void setHost(String value) {
    this.dshost = value;
  }

  public void setPort(int value) {
    this.dsport = value;

  }

  public void setUser(String value) {
    this.dsuser = value;

  }

  public void setPassword(String value) {
    this.dbpass = value;

  }

  public void setDatabase(String value) {
    this.dsdb = value;
  }

  public void setPath(String value) {
    this.dspath = value;

  }

  public void setFileType(String value) {
    this.dsfiletype = value;
  }

  public void setSubDirs(boolean b) {
    this.subdirs = b;
  }

  public void setIdField(String value) {
    this.dsidfield = value;
  }

  public void addVarQuery(String name, HashSet<Substitution> varsubstitutions, String query) {
    this.dsvarqueries.put(name, query);
    this.dsvarsubqueries.put(name, varsubstitutions);
  }

  public String getIdField() {
    return this.dsidfield;
  }

}
