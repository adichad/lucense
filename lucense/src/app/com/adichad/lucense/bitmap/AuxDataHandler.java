package com.adichad.lucense.bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;

import com.sleepycat.db.Cursor;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;

public class AuxDataHandler {

  HashMap<String, HashSet<String>> AuxData = new HashMap<String, HashSet<String>>();

  private Database auxDb = null;

  private Environment dbEnv = null;

  EnvironmentConfig dBEnvConfig = null;

  DatabaseConfig dbConfig = null;

  public AuxDataHandler() throws DatabaseException, FileNotFoundException {
    String envHome = "/tmp/bdb/data";

    dBEnvConfig = new EnvironmentConfig();
    dBEnvConfig.setAllowCreate(true);
    dBEnvConfig.setInitializeCache(true);

    dbConfig = new DatabaseConfig();
    dbConfig.setErrorStream(System.err);
    dbConfig.setType(DatabaseType.BTREE);
    dbConfig.setAllowCreate(true);
    dbConfig.setSortedDuplicates(true);

    dbEnv = new Environment(new File(envHome), dBEnvConfig);
    auxDb = dbEnv.openDatabase(null, "Aux", "Aux", dbConfig);

  }

  public BitMapOperationStatus search(String row, String cell) throws DatabaseException {
    if (AuxData.containsKey(row)) {
      HashSet<String> cells = AuxData.get(row);
      if (cells.contains(cell))
        return BitMapOperationStatus.SUCCESS;
    }

    String key = row + "|X|" + cell;

    if (OperationStatus.SUCCESS == auxDb.exists(null, new DatabaseEntry(key.getBytes()))) {
      return BitMapOperationStatus.SUCCESS;
    }
    return BitMapOperationStatus.NOTFOUND;
  }

  public BitMapOperationStatus put(String row, String cell) throws DatabaseException {

    if (!AuxData.containsKey(row)) {
      AuxData.put(row, new HashSet<String>());
    }
    HashSet<String> data = AuxData.get(row);
    data.add(cell);
    DatabaseEntry dbKey = new DatabaseEntry(row.getBytes());
    DatabaseEntry dbData = new DatabaseEntry(cell.getBytes());
    if (OperationStatus.SUCCESS != auxDb.put(null, dbKey, dbData)) {
      return BitMapOperationStatus.NOTFOUND;
    }
    return BitMapOperationStatus.SUCCESS;
    /*
     * if(OperationStatus.SUCCESS != auxDb.exists(null, dbKey) ) { DatabaseEntry
     * nullData = new DatabaseEntry() ; nullData.setPartial(0, 0, true) ;
     * auxDb.put(null, dbKey, new DatabaseEntry(a)) ; return
     * BitMapOperationStatus.SUCCESS ; } return BitMapOperationStatus.SUCCESS ;
     */

  }

}
