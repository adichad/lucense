package com.adichad.lucense.bitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;

public class HoleManager {

  public static final int DEF_HOLE_SIZE = 1;

  // private Environment holeEnv = null;
  private Database holedB = null;

  private EntryBinding<HoleHandler> binding = new HoleHandlerTupleBinding();

  // EnvironmentConfig holeEnvConfig = null ;
  // DatabaseConfig holeDbConfig = null ;
  StoredClassCatalog classCatlog = null;

  private int minHoleSize = DEF_HOLE_SIZE;

  public HoleManager(Environment dbEnv, HashMap<String, String> holeDbConfig) throws FileNotFoundException,
      DatabaseException {
    if (holeDbConfig != null) {
      // / set user defined Config
    }
    DatabaseConfig dbConfig = new DatabaseConfig();
    dbConfig.setErrorStream(System.err);
    dbConfig.setType(DatabaseType.HASH);
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(true);

    // holeEnv = new Environment(new File(envHome), holeEnvConfig);
    holedB = dbEnv.openDatabase(null, "hole", "hole", dbConfig);
  }

  public int add(DatabaseEntry dBRow, HoleHandler hh, Transaction txn) throws DatabaseException {
    DatabaseEntry dbData = new DatabaseEntry();
    binding.objectToEntry(hh, dbData);
    holedB.put(txn, dBRow, dbData);
    return 0;
  }

  public HoleHandler get(DatabaseEntry dBRow) throws DatabaseException {
    DatabaseEntry dbData = new DatabaseEntry();
    dbData.setPartial(false);

    if (holedB.get(null, dBRow, dbData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
      return null;
    }

    if (dbData.getSize() > 0) {
      return binding.entryToObject(dbData);
    }
    return null;

  }

  public int getMinimunHoleSize() {
    return minHoleSize;
  }

  public void setMinimunHoleSize(int minHoleSize) {
    this.minHoleSize = minHoleSize;
  }

  public void close() throws DatabaseException {
    if (holedB != null) {
      holedB.close();
      holedB = null;
    }
    // TODO Auto-generated method stub

  }
}
