package com.adichad.lucense.bitmap;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

import com.sleepycat.db.Cursor;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;

public class RowHandler {

  public static int CELL_EXISTS = 1;

  public static int CELL_NOT_EXISTS = 0;

  CellDictionaryHandler cellDict = null;

  HoleManager holeManager = null;

  // AuxDataHandler auxHandler = null ;

  private Environment dbEnv = null;

  private Database dB = null;

  EnvironmentConfig dBEnvConfig = null;

  DatabaseConfig dbConfig = null;

  // / Aux Data Handling

  HashMap<String, HashSet<Integer>> AuxData = new HashMap<String, HashSet<Integer>>();

  private Database auxDb = null;

  private Environment auxEnv = null;

  EnvironmentConfig auxEnvConfig = null;

  DatabaseConfig auxDbConfig = null;

  Map<String, ReentrantLock> optimizeLockMap = Collections.synchronizedMap(new HashMap<String, ReentrantLock>());

  Map<String, ReentrantLock> putLockMap = Collections.synchronizedMap(new HashMap<String, ReentrantLock>());

  // pass Config object
  public RowHandler(CellDictionaryHandler cellDict, HoleManager holeManager, AuxDataHandler auxHandler,
      Environment dBEnv, HashMap<String, String> mainDBConfig, HashMap<String, String> auxDBConfig)
      throws FileNotFoundException, DatabaseException {
    this.cellDict = cellDict;
    this.holeManager = holeManager;
    dbEnv = dBEnv;
    this.load(mainDBConfig);
    this.initAux(auxDBConfig);
  }

  public Row loadRow(byte row[]) throws DatabaseException {
    DatabaseEntry dBrow = new DatabaseEntry(row);
    byte[] data = null;
    DatabaseEntry DBdata = new DatabaseEntry();
    if (dB.get(null, dBrow, DBdata, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
      data = DBdata.getData();
    }

    HoleHandler hh = holeManager.get(dBrow);
    if (auxDb.exists(null, dBrow) == OperationStatus.SUCCESS) {
      return Row.getInstance(row, data, hh, auxDb, cellDict);
    }
    return Row.getInstance(row, data, hh, null, cellDict);
  }

  public int load(HashMap<String, String> mainDBConfig) throws FileNotFoundException, DatabaseException {

    // String envHome = "/tmp/bdb/data";
    // String dbName = "bitmap";

    /*
     * dBEnvConfig = new EnvironmentConfig(); dBEnvConfig.setAllowCreate(true);
     * dBEnvConfig.setErrorStream(null) ; dBEnvConfig.setInitializeCache(true);
     * dBEnvConfig.setTransactional(true);
     * dBEnvConfig.setInitializeLocking(true); // n //
     * dBEnvConfig.setInitializeLogging(true);//n
     */
    // IMPPPPPPPP NNOOTTEESS
    // Configure db to perform deadlock detection internally, and to
    // choose the transaction that has performed the least amount
    // of writing to break the deadlock in the event that one
    // is detected.
    // dBEnvConfig.setLockDetectMode(LockDetectMode.MINWRITE);

    // Never close a database or store that has active transactions.
    // Make sure all
    // transactions are resolved (either committed or aborted) before
    // closing the database.

    // Note
    // Never have more than one active transaction in your thread at a
    // time. This is
    // especially a problem if you mix an explicit transaction with
    // another operation that
    // uses auto commit. Doing so can result in undetectable deadlocks.

    dbConfig = new DatabaseConfig();
    dbConfig.setErrorStream(System.err);
    // dbConfig.setType(DatabaseType.BTREE);
    dbConfig.setType(DatabaseType.HASH);
    dbConfig.setAllowCreate(true);
    dbConfig.setTransactional(true);
    String dbName = "bitmap";

    // dbEnv = new Environment(new File(envHome), dBEnvConfig);
    // Transaction txn = dbEnv.beginTransaction(null, null) ;//n
    dB = dbEnv.openDatabase(null, dbName, dbName, dbConfig);
    // txn.commit() ; //n
    // ZZZZ System.out.println(dB);

    // auxDb = dbEnv.openDatabase(null, "Aux","Aux", dbConfig);

    // StatsConfig statCfg = StatsConfig.DEFAULT;
    // statCfg.setFast(true);
    // int cacheSize=dbEnv.getCacheStats(statCfg).getBytes();
    // System.out.println("cacheSize" + cacheSize);

    return 0;

  }

  private void initAux(HashMap<String, String> auxDBConfig) throws FileNotFoundException, DatabaseException {
    // String envHome = "/tmp/bdb/data";

    /*
     * auxEnvConfig = new EnvironmentConfig();
     * auxEnvConfig.setAllowCreate(true); auxEnvConfig.setInitializeCache(true);
     * auxEnvConfig.setTransactional(true); // n
     * auxEnvConfig.setInitializeLocking(true);// n //
     * auxEnvConfig.setInitializeLogging(true); //n
     */
    auxDbConfig = new DatabaseConfig();
    auxDbConfig.setErrorStream(System.err);
    // auxDbConfig.setType(DatabaseType.BTREE);
    auxDbConfig.setType(DatabaseType.HASH);
    auxDbConfig.setAllowCreate(true);
    auxDbConfig.setSortedDuplicates(true);
    auxDbConfig.setTransactional(true);// n
    auxDbConfig.setErrorStream(null);

    // auxEnv = new Environment(new File(envHome), auxEnvConfig);
    // Transaction txn = auxEnv.beginTransaction(null, null) ;//n
    auxDb = dbEnv.openDatabase(null, "Aux", "Aux", auxDbConfig);
    // txn.commit() ; //n

  }

  /*
   * private void initDeleteList() { String envHome = "/tmp/bdb/data" ;
   * deleteEnvConfig = new EnvironmentConfig();
   * deleteEnvConfig.setAllowCreate(true);
   * deleteEnvConfig.setInitializeCache(true);
   * deleteEnvConfig.setTransactional(true) ; //n
   * deleteEnvConfig.setInitializeLocking(true) ;//n
   * //auxEnvConfig.setInitializeLogging(true); //n deleteDbConfig = new
   * DatabaseConfig(); deleteDbConfig.setErrorStream(System.err);
   * deleteDbConfig.setType(DatabaseType.BTREE);
   * deleteDbConfig.setAllowCreate(true);
   * deleteDbConfig.setSortedDuplicates(true) ;
   * deleteDbConfig.setTransactional(true);//n try { deleteEnv = new
   * Environment(new File(envHome), deleteEnvConfig); //Transaction txn =
   * dbEnv.beginTransaction(null, null) ;//n deleteDb = dbEnv.openDatabase(null,
   * "delete","delete", deleteDbConfig); //putToHelperDataBase("1", 2, deleteDb,
   * txn) ; //deleteFromHelperDataBase("1",2,deleteDb,txn) ; //txn.commit() ;
   * //System.out.println("deleteDB:"+deleteDb); //txn.commit() ; //n } catch
   * (FileNotFoundException e) { // TODO Auto-generated catch block
   * e.printStackTrace(); } catch (DatabaseException e) { // TODO Auto-generated
   * catch block e.printStackTrace(); } }
   */

  private int sync() {
    try {
      dB.sync();
      auxDb.sync();
      // deleteDb.sync() ;

    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return 0;
  }

  private int cleanup() {
    try {
      if (dB != null) {
        dB.close();
        dB = null;
      }
      if (dbEnv != null) {
        dbEnv.close();
        dbEnv = null;
      }

      if (auxDb != null) {
        auxDb.close();
        auxDb = null;
      }
      if (auxEnv != null) {
        auxEnv.close();
        auxEnv = null;
      }

      // if (deleteDb != null) {deleteDb.close(); deleteDb = null;}
      // if (deleteEnv != null) {deleteEnv.close(); deleteEnv = null;}

    } catch (DatabaseException de) {
      System.out.println("BerkeleyDB.cleanup: " + de);
    }
    return 0;
  }

  public BitMapOperationStatus search(String row, String cell) throws DatabaseException {
    int cellId = cellDict.resolveCell(cell);
    return this.search(row, cellId);
  }

  public BitMapOperationStatus search(String row, int cellId) throws DatabaseException {
    return search(row.getBytes(), cellId);
  }

  public BitMapOperationStatus search(byte[] row, int cellId) throws DatabaseException {
    DatabaseEntry dBRow = new DatabaseEntry();
    dBRow.setData(row);
    DatabaseEntry dbData = new DatabaseEntry();
    dbData.setPartial(false);

    /*
     * // if row does not exists in Main DB it still might be in Aux before
     * first optimize call if(dB.exists(null, dbRow) != OperationStatus.SUCCESS)
     * { System.out.println("Row not Found in Main: Search in Aux.");
     * dbData.setData(intToByteArray(cellId)) ; return
     * searchFromHelperDatabase(dbRow,dbData,auxDb) ; }
     */

    HoleHandler hh = this.holeManager.get(dBRow);
    // ZZZZ System.out.println("row found " + hh);

    if (hh != null) {
      int resolvedCellId = cellId;
      // if(hh.isInHole(cellId))
      if ((resolvedCellId = hh.resolveCell(cellId)) == HoleHandler.CELL_IN_HOLE) {
        dbData.setData(BitMapUtil.intToByteArray(cellId));
        return searchFromHelperDatabase(dBRow, dbData, auxDb);
      }
      cellId = resolvedCellId;
      // cellId = hh.resolveCell(cellId) ;
      // ZZZZ System.out.println("resolved cellid" + cellId);
    } else if (dB.exists(null, dBRow) != OperationStatus.SUCCESS) {
      // ZZZZ System.out.println("Row not Found in Main: Search in Aux.");
      dbData.setData(BitMapUtil.intToByteArray(cellId));
      return searchFromHelperDatabase(dBRow, dbData, auxDb);
    }

    int byteId = BitMapUtil.getPhysicalByteId(cellId);
    // ZZZZ System.out.println("byteId:" + byteId);
    dbData.setPartial(byteId, 1, true);
    OperationStatus os = dB.get(null, dBRow, dbData, LockMode.DEFAULT);
    if (os != OperationStatus.SUCCESS || dbData.getSize() == 0)
      return BitMapOperationStatus.NOTFOUND;
    byte[] data = dbData.getData();
    // ZZZZ System.out.println("byte get:size" + data.length);

    cellId = BitMapUtil.getRelativeCellIdInByte(cellId);
    // ZZZZ System.out.println("relative cell ID : " + cellId);
    // ZZZZ System.out.println("byte value: " + data[0]);
    if ((data[0] & (1 << cellId)) == 0)
      return BitMapOperationStatus.NOTFOUND;
    return BitMapOperationStatus.SUCCESS;
  }

  public void addRow(String row) {
    DatabaseEntry dbRow = new DatabaseEntry();
    dbRow.setData(row.getBytes());
    try {
      if (dB.exists(null, dbRow) != OperationStatus.SUCCESS) {

      }
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public BitMapOperationStatus delete(byte[] row, CellResolver cells,Transaction parentTxn) throws DatabaseException {
    String strRow = new String(row);
    synchronized (putLockMap) {
      if (!putLockMap.containsKey(strRow)) {
        putLockMap.put(strRow, new ReentrantLock());
      }
    }
    boolean putLockAquired = false;
    synchronized (putLockMap.get(strRow)) {
      putLockAquired = putLockMap.get(strRow).tryLock();
      if (optimizeLockMap.containsKey(strRow) && optimizeLockMap.get(strRow).isLocked()) {

        if (putLockAquired)
          putLockMap.get(strRow).unlock();
        return BitMapOperationStatus.FAIL;
      }
    }
    DatabaseEntry dBKey = new DatabaseEntry(row);
    DatabaseEntry dbData = new DatabaseEntry();
    dbData.setPartial(false);
    HoleHandler hh = holeManager.get(dBKey);
    try {
      Transaction txn = dbEnv.beginTransaction(parentTxn, null);
      int cellId = -1;
      try {
        if (dB.exists(txn, dBKey) != OperationStatus.SUCCESS) {
          // ZZZ
          // System.out.println("Row Does not Exists in Main: Adding To Aux");
          ;
          for (int i = 0; i < cells.getSize(); i++) {
            // cellId = cellDict.resolveCell(cells.get(i));
            cellId = cells.get(i);
            dbData.setData(BitMapUtil.intToByteArray(cellId));
            deleteFromHelperDataBase(dBKey, dbData, auxDb, txn);
            // cellsToAddInAux.add(cellId) ;
          }
          // System.out.println("InMem Aux Map" + cellsToAddInAux);
        } else {
          for (int i = 0; i < cells.getSize(); i++) {

            // cellId = cellDict.resolveCell(cells[i]);
            cellId = cells.get(i);

            // if (hh.isInHole(cellId)) {
            int resolvedCellId = cellId;
            if (hh != null && (resolvedCellId = hh.resolveCell(cellId)) == HoleHandler.CELL_IN_HOLE) {
              dbData.setData(BitMapUtil.intToByteArray(cellId));
              dbData.setPartial(false);
              deleteFromHelperDataBase(dBKey, dbData, auxDb, txn);
              // deleteFromAux(row,cellId,txn) ;
            } else {
              /*
               * If We maintain Delete List putToHelperDataBase(row, cellId,
               * deleteDb, txn) ;
               */
              // putToDeleteList(row,cellId,txn) ;
              // cellId = hh.resolveCell(cellId);
              cellId = resolvedCellId;
              int byteId = BitMapUtil.getPhysicalByteId(cellId);
              cellId = BitMapUtil.getRelativeCellIdInByte(cellId);
              dbData.setPartial(byteId, 1, true);

              if (dB.get(txn, dBKey, dbData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {

              }
              byte arrByte[] = dbData.getData();
              if (arrByte.length == 1) {
                arrByte[0] &= (byte) (~(1 << cellId));
                dbData.setData(arrByte);
                dB.put(txn, dBKey, dbData);
              }
            }
          }
        }
      } catch (DatabaseException e) {

        if (txn != null) {
          txn.abort();
          txn = null;
        }
        throw e;
      }
      if (txn != null) {
        txn.commit();
        txn = null;
      }
    } catch (DatabaseException e) {
      throw e;
    }finally {
      if (putLockAquired) {
        putLockMap.get(strRow).unlock();
      }
    }

    return BitMapOperationStatus.SUCCESS;
  }

  public BitMapOperationStatus put(String row, CellResolver cells) throws DatabaseException {
    return put(row.getBytes(), cells,null);
  }

  public BitMapOperationStatus put(byte[] row, CellResolver cells, Transaction parentTxn) throws DatabaseException {
    String strRow = new String(row);
    synchronized (putLockMap) {
      if (!putLockMap.containsKey(strRow)) {
        putLockMap.put(strRow, new ReentrantLock());
      }
    }
    boolean putLockAquired = false;
    synchronized (putLockMap.get(strRow)) {
      putLockAquired = putLockMap.get(strRow).tryLock();
      if (optimizeLockMap.containsKey(strRow) && optimizeLockMap.get(strRow).isLocked()) {

        if (putLockAquired)
          putLockMap.get(strRow).unlock();
        return BitMapOperationStatus.FAIL;
      }
    }
    
    TreeSet<Integer> cellsToAddInMain = new TreeSet<Integer>();
    // TreeSet<Integer> cellsToAddInAux = new TreeSet <Integer>();

    try {

      Transaction txn = dbEnv.beginTransaction(parentTxn, null);// n
      DatabaseEntry dBRow = new DatabaseEntry();
      dBRow.setData(row);
      DatabaseEntry dbData = new DatabaseEntry();
      dbData.setPartial(false);
      int cellId = -1;
      try {// n
        // ZZZZ System.out.print(">>>>>>>>>>>>>>>>>>>>>>>>To Add Row:" + row +
        // " Cells : ");
        // ZZZZ System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        /*
         * if hole is null case 1 : row Exists in DB but there is not hole in
         * data actually case 2 : row does not exits in case 2 : Add to AuxData
         * only (will become the part of main data after first optimize call) if
         * row does not exists in main Data : Add to Aux Till first optimize
         * call
         */
        if (dB.exists(null, dBRow) != OperationStatus.SUCCESS) {

          for (int i = 0; i < cells.getSize(); i++) {
            cellId = cells.get(i); // cellDict.resolveCell(cells[i])
            dbData.setData(BitMapUtil.intToByteArray(cellId));
            putToHelperDataBase(dBRow, dbData, auxDb, txn);
            // cellsToAddInAux.add(cellId) ;
          }
          // System.out.println("InMem Aux Map" + cellsToAddInAux);
        } else {
          HoleHandler hh = this.holeManager.get(dBRow);
          for (int i = 0; i < cells.getSize(); i++) {
            cellId = cells.get(i); // cellDict.resolveCell(cells[i])
            // ;
            /*
             * If we maintain delete list
             * deleteFromHelperDataBase(row,cellId,deleteDb,txn) ;
             */
            // / ZZZ System.out.println("Dict Cell resolution:"+
            // cells.getOriginal(i) + "->" + cellId);
            int resolvedCellId = cellId;
            if (hh != null && (resolvedCellId = hh.resolveCell(cellId)) == HoleHandler.CELL_IN_HOLE) {
              // ZZZ System.out.println("Cell:" + cellId+
              // " is in hole. Adding to Aux");
              dbData.setData(BitMapUtil.intToByteArray(cellId));
              putToHelperDataBase(dBRow, dbData, auxDb, txn);
              // cellsToAddInAux.add(cellId) ;
            } else {
              // / ZZZ System.out.print("Hole Cell resolution:" + cellId);

              // cellId = hh.resolveCell(cellId) ;

              // / ZZZ System.out.println("->" + cellId);
              // / ZZZ System.out.println("Adding To Main");
              cellsToAddInMain.add(resolvedCellId);
            }
          }
        }

        if (!cellsToAddInMain.isEmpty()) {
          // / ZZZ System.out.println("Writing To Main DB");
          int firstByte = BitMapUtil.getPhysicalByteId(cellsToAddInMain.first());
          int lastByte = BitMapUtil.getPhysicalByteId(cellsToAddInMain.last());
          int size = lastByte - firstByte + 1;
          // / ZZZ System.out.println("firstCell:" + cellsToAddInMain.first()+
          // ",lastCell:" + cellsToAddInMain.last());
          // / ZZZ System.out.println("firstByte:" + firstByte + ",lastByte:"+
          // lastByte + "size:" + size);

          // / ZZZ System.out.println("SetPartial:" + firstByte + "," + size+
          // ", " + true);

          dbData.setData(null);
          dbData.setPartial(firstByte, size, true);
          // ZZZZ System.out.println("dB.get");
          OperationStatus os = dB.get(txn, dBRow, dbData, LockMode.DEFAULT);
          byte[] dataToPut = null;

          // if key is not found
          if (os != OperationStatus.SUCCESS) {
            /*
             * zzz
             * System.out.println("Key not Found:building new byte array size:"
             * + size);
             */
            dataToPut = new byte[size];
          } else if (dbData.getSize() < size) {
            /*
             * ZZZ System.out .println("Key Found:Size is small.foundsize:" +
             * dbData.getSize() + ". Coping old data to new byte array of size:"
             * + size);
             */
            dataToPut = new byte[size];
            byte data[] = dbData.getData();
            for (int i = 0; i < data.length; i++) {
              dataToPut[i] = data[i];
            }
          } else {
            // / ZZZ System.out.println("Key Found:Size is same.");
            dataToPut = dbData.getData();
          }
          // / ZZZ System.out.println("cellsToAddInMain : Iteatrion start");
          for (Iterator<Integer> iterator = cellsToAddInMain.iterator(); iterator.hasNext();) {

            Integer integer = (Integer) iterator.next();
            cellId = integer.intValue();
            int byteId = BitMapUtil.getPhysicalByteId(cellId);
            /*
             * ZZZ System.out .print("cellsToAddInMain : iterator.next():cellId"
             * + cellId + " byteId:" + byteId);
             */
            cellId = BitMapUtil.getRelativeCellIdInByte(cellId);
            /*
             * ZZZ System.out.println(" relativeCellId:" + cellId +
             * " ORing to dataToPut[byteId -firstByte]:[" + (byteId - firstByte)
             * + "]");
             */

            dataToPut[byteId - firstByte] |= 1 << cellId;
          }
          // init need to check
          dbData.setData(null);
          dbData.setSize(0);
          // / ZZZ System.out.println("dbData.setPartial: for put" + firstByte+
          // "," + size + "," + true);
          dbData.setPartial(firstByte, size, true);
          dbData.setData(dataToPut);
          os = dB.put(txn, dBRow, dbData);// n
        }

        /*
         * for (Iterator iterator = cellsToAddInAux.iterator();
         * iterator.hasNext();) { //auxHandler.put(row, (String)iterator.next())
         * ; System.out.println("putToAux"); putToHelperDataBase(row,
         * (Integer)iterator.next(), auxDb, txn) ; //putToAux(row,
         * (Integer)iterator.next(),txn) ; //if(!AuxData.containsKey(row)) //{
         * // HashSet<String> cellSet = new HashSet<String>() ; //
         * AuxData.put(row, cellSet) ; //} //HashSet<String> cellSet =
         * AuxData.get(row) ; //cellSet.add((String)iterator.next()) ; }
         */
        txn.commit();
        txn = null;
        // System.out.println("PUT : PUT PASSDED");
      } catch (DatabaseException e) {
        
        if (txn != null) {
          txn.abort();
          txn = null;
        }
        throw e ; 
      }
    } catch (DatabaseException de) {
      throw de;
    } finally {
      if (putLockAquired) {
        putLockMap.get(strRow).unlock();
      }
    }

    return BitMapOperationStatus.SUCCESS;
  }

  public BitMapOperationStatus searchFromHelperDatabase(DatabaseEntry row, DatabaseEntry cell, Database db)
      throws DatabaseException {
    if (OperationStatus.SUCCESS == db.getSearchBoth(null, row, cell, LockMode.DEFAULT)) {
      return BitMapOperationStatus.SUCCESS;
    }
    return BitMapOperationStatus.NOTFOUND;
  }

  public BitMapOperationStatus searchFromAux(String row, int cell) throws DatabaseException {
    if (AuxData.containsKey(row)) {
      HashSet<Integer> cells = AuxData.get(row);

      if (cells.contains(cell)) {
        return BitMapOperationStatus.SUCCESS;
      }
    }
    if (OperationStatus.SUCCESS == auxDb.getSearchBoth(null, new DatabaseEntry(row.getBytes()), new DatabaseEntry(
        BitMapUtil.intToByteArray(cell)), LockMode.DEFAULT)) {
      return BitMapOperationStatus.SUCCESS;
    }
    return BitMapOperationStatus.NOTFOUND;
  }

  /*
   * public BitMapOperationStatus searchFromDeleteList(String row, int cell)
   * throws DatabaseException { if(OperationStatus.SUCCESS ==
   * deleteDb.getSearchBoth(null, new DatabaseEntry(row.getBytes()), new
   * DatabaseEntry(intToByteArray(cell)), LockMode.DEFAULT)) { return
   * BitMapOperationStatus.SUCCESS ; } return BitMapOperationStatus.NOTFOUND ; }
   */

  /*
   * private BitMapOperationStatus putToDeleteList(String row, int
   * cell,Transaction txn) { DatabaseEntry dbKey = new
   * DatabaseEntry(row.getBytes()) ; DatabaseEntry dbData = new
   * DatabaseEntry(intToByteArray(cell)) ; dbData.setPartial(false) ; try {
   * if(OperationStatus.SUCCESS != deleteDb.put(txn, dbKey,dbData)) { return
   * BitMapOperationStatus.NOTFOUND ; } } catch (DatabaseException e) { // TODO
   * Auto-generated catch block e.printStackTrace(); } return
   * BitMapOperationStatus.SUCCESS ; }
   */
  private BitMapOperationStatus putToHelperDataBase(DatabaseEntry row, DatabaseEntry cell, Database db, Transaction txn) {
    // DatabaseEntry dbKey = new DatabaseEntry(row.getBytes());
    // DatabaseEntry dbData = new DatabaseEntry(intToByteArray(cell));

    // dbData.setPartial(false);
    try {
      if (OperationStatus.SUCCESS != db.put(txn, row, cell)) {
        return BitMapOperationStatus.NOTFOUND;
      }
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return BitMapOperationStatus.SUCCESS;
  }

  private BitMapOperationStatus putToAux(String row, int cell, Transaction txn) {
    if (!AuxData.containsKey(row)) {

      AuxData.put(row, new HashSet<Integer>());
    }
    HashSet<Integer> data = AuxData.get(row);

    data.add(cell);

    DatabaseEntry dbKey = new DatabaseEntry(row.getBytes());
    DatabaseEntry dbData = new DatabaseEntry(BitMapUtil.intToByteArray(cell));

    dbData.setPartial(false);
    try {
      if (OperationStatus.SUCCESS != auxDb.put(txn, dbKey, dbData)) {
        return BitMapOperationStatus.NOTFOUND;
      }
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return BitMapOperationStatus.SUCCESS;
  }

  private BitMapOperationStatus deleteFromHelperDataBase(DatabaseEntry row, DatabaseEntry cellId,
      Database dbToDeleteFrom, Transaction txn) throws DatabaseException {
    // DatabaseEntry dbKey = new DatabaseEntry(row.getBytes());

    // DatabaseEntry dbData = new DatabaseEntry(intToByteArray(cellId));
    // dbData.setPartial(false);
    Cursor cusr = null;
    try {
      // can not delete directly from AuxDB, there is no call for removing
      // joint key-data pair
      cusr = dbToDeleteFrom.openCursor(txn, null);
      if (cusr.getSearchBoth(row, cellId, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
        if (cusr.delete() != OperationStatus.SUCCESS) {
          return BitMapOperationStatus.FAIL;
        }
      } else {
        return BitMapOperationStatus.NOTFOUND;
      }
      cusr.close();
      cusr = null;

    } catch (DatabaseException e) {
      // e.printStackTrace();
      throw e;
    } finally {
      if (cusr != null) {
        try {
          cusr.close();
          cusr = null;
        } catch (DatabaseException e) {

          // e.printStackTrace();
          throw e;

        }
      }
      /*
       * if(txn !=null) { try { txn.abort() ; txn = null; } catch
       * (DatabaseException e) { //e.printStackTrace(); throw e; } }
       */
    }
    return BitMapOperationStatus.SUCCESS;
  }

  private BitMapOperationStatus deleteFromAux(String row, int cellId, Transaction txn) throws DatabaseException {
    if (AuxData.containsKey(row)) {
      HashSet<Integer> data = AuxData.get(row);
      data.remove(cellId);
    }

    DatabaseEntry dbKey = new DatabaseEntry(row.getBytes());

    DatabaseEntry dbData = new DatabaseEntry(BitMapUtil.intToByteArray(cellId));
    dbData.setPartial(false);
    Cursor cusr = null;
    try {
      // can not delete directly from AuxDB, there is no call for removing
      // joint key-data pair
      cusr = auxDb.openCursor(txn, null);
      if (cusr.getSearchBoth(dbKey, dbData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
        if (cusr.delete() != OperationStatus.SUCCESS) {
          return BitMapOperationStatus.FAIL;
        }
      } else {
        return BitMapOperationStatus.NOTFOUND;
      }
      cusr.close();
      cusr = null;

    } catch (DatabaseException e) {
      // e.printStackTrace();
      throw e;
    } finally {
      if (cusr != null) {
        try {
          cusr.close();
          cusr = null;
        } catch (DatabaseException e) {

          // e.printStackTrace();
          throw e;

        }
      }
      if (txn != null) {
        try {
          txn.abort();
          txn = null;
        } catch (DatabaseException e) {
          // e.printStackTrace();
          throw e;
        }
      }
    }

    return BitMapOperationStatus.SUCCESS;
  }

  public byte[] setData(byte arr[], byte arrToWrite[], int offset, int size, boolean partial) {
    // [][][][]

    for (int i = 0; i < size; i++) {
      arr[i + offset] = arrToWrite[i];
    }
    int growSize = arrToWrite.length - size;
    if (growSize >= 0) {
      byte tmpArr[] = new byte[arr.length + growSize];
      int alreadyWritten = offset + size;
      int i = 0;
      for (; i < offset + size; i++) {
        tmpArr[i] = arr[i];
      }
      int newData = growSize + alreadyWritten;
      int j = size;
      for (; i < newData; i++) {
        tmpArr[i] = arrToWrite[j];
        j++;
      }

      for (; i < tmpArr.length; i++) {
        tmpArr[i] = arr[i - growSize];
      }
      arr = tmpArr;
    }
    return arr;
  }

  public BitMapOperationStatus optimize(byte[] row) {

    String strRow = new String(row);
    synchronized (optimizeLockMap) {
      if (!optimizeLockMap.containsKey(strRow)) {
        optimizeLockMap.put(strRow, new ReentrantLock());
      }
    }
    synchronized (putLockMap) {
      if (!putLockMap.containsKey(strRow)) {
        putLockMap.put(strRow, new ReentrantLock());
      }
    }

    synchronized (putLockMap.get(strRow)) {
      // will be a blocking call
      optimizeLockMap.get(strRow).lock();
      if (putLockMap.containsKey(strRow) && putLockMap.get(strRow).isLocked()) {
        optimizeLockMap.get(strRow).unlock();
        return BitMapOperationStatus.FAIL;
      }
    }

    // System.out.println("OPTIMIZE : LOCKING PASSED");
    DatabaseEntry dBRow = new DatabaseEntry(row);
    BitMapOperationStatus os = optimizeImpl(dBRow);
    // System.out.println("OPTIMIZE : OPTIMIZED PASSED");
    if (optimizeLockMap.get(strRow).isLocked())
      optimizeLockMap.get(strRow).unlock();
    // System.out.println("OPTIMIZE : UNLOCKING PASSED");
    return os;

  }

  public BitMapOperationStatus optimizeImpl(DatabaseEntry dBRow) {
    Cursor cusr = null;
    Transaction txn = null;
    try {

      //txn = dbEnv.beginTransaction(null, null);// n //
      //cusr = auxDb.openCursor(txn, null); // n //
      cusr = auxDb.openCursor(null, null); // n
      DatabaseEntry auxData = new DatabaseEntry();
      DatabaseEntry dBDataToWrite = new DatabaseEntry();

      OperationStatus retVal = cusr.getSearchKey(dBRow, auxData, LockMode.DEFAULT);
      HoleHandler hh = holeManager.get(dBRow);
      // niether MainDB contains this key nor hole
      //if (hh == null && dB.exists(txn, dBRow) != OperationStatus.SUCCESS) { //
      if (hh == null && dB.exists(null, dBRow) != OperationStatus.SUCCESS) {
        hh = new HoleHandler();
        hh.add(0, (Integer.MAX_VALUE / 8) * 8);
      }

      byte arrFinalData[] = null;
      // DatabaseEntry dBDataActual = new DatabaseEntry();
      //dB.get(txn, dBRow, dBDataToWrite, LockMode.DEFAULT); //
      dB.get(null, dBRow, dBDataToWrite, LockMode.DEFAULT);
      arrFinalData = dBDataToWrite.getData();

      while (retVal == OperationStatus.SUCCESS) {
        int cellId = BitMapUtil.ByteArrayToInt(auxData.getData());
        // System.out.println("opt: cell:"+cellId);
        byte[] arrByteToWrite = null;
        int resolvedCellId = cellId;
        if ((resolvedCellId = hh.resolveCell(cellId)) == HoleHandler.CELL_IN_HOLE) {
          int byteId = BitMapUtil.getPhysicalByteId(cellId);
          Hole h = hh.getEnclosingHole(cellId);
          int offset = BitMapUtil.getPhysicalByteId(h.getStart() - h.getTotalSizeTillThisHole());
          int holeStartByte = BitMapUtil.getPhysicalByteId(h.getStart());
          int holeEndByte = BitMapUtil.getPhysicalByteId(h.getEnd());

          int nextHoleStartByte = byteId + 1;
          int nextNewHoleSizeInBytes = (holeEndByte - nextHoleStartByte) + 1;
          hh.removeHole(h);
          if (byteId > 0) {
            int prevHoleEndByte = byteId - 1;
            int prevNewHoleSizeInByte = (prevHoleEndByte - holeStartByte) + 1;
            if ((prevNewHoleSizeInByte) >= holeManager.getMinimunHoleSize()) {

              // start will always be multiple of 8
              // end will always be multiple of 8 -1
              // +1 in End to reach endth bit of that byte
              // e.g. 7th byte is 56 57 58 59 60 61 62 63
              // but 7*8 = 56 but we have to include hole till 63

              hh.add(holeStartByte * 8, ((prevHoleEndByte + 1) * 8) - 1);
            } else {
              arrByteToWrite = new byte[prevNewHoleSizeInByte];
              arrFinalData = BitMapUtil.growData(arrFinalData, arrByteToWrite, offset, 0, true);
              offset += prevNewHoleSizeInByte;
            }
          }
          int relCell = BitMapUtil.getRelativeCellIdInByte(cellId);
          arrByteToWrite = new byte[1];
          arrByteToWrite[0] |= 1 << relCell;
          arrFinalData = BitMapUtil.growData(arrFinalData, arrByteToWrite, offset, 0, true);
          if (nextNewHoleSizeInBytes >= holeManager.getMinimunHoleSize()) {
            hh.add((nextHoleStartByte * 8), ((holeEndByte + 1) * 8) - 1);
          } else {
            arrByteToWrite = new byte[nextNewHoleSizeInBytes];
            arrFinalData = BitMapUtil.growData(arrFinalData, arrByteToWrite, offset, 0, true);
          }
        } else {
          int byteId = BitMapUtil.getPhysicalByteId(resolvedCellId);
          resolvedCellId = BitMapUtil.getRelativeCellIdInByte(resolvedCellId);
          arrFinalData[byteId] |= 1 << resolvedCellId;
        }

        retVal = cusr.getNextDup(dBRow, auxData, LockMode.DEFAULT);
      }
      cusr.close();
      cusr = null;
      // AuxData.remove(dBRow);
      txn = dbEnv.beginTransaction(null, null);// n //
      holeManager.add(dBRow, hh, txn);
      auxDb.delete(txn, dBRow);
      dBDataToWrite.setData(arrFinalData);
      dBDataToWrite.setPartial(false);
      dB.put(txn, dBRow, dBDataToWrite);
      txn.commit();
      txn = null;
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (cusr != null) {
        try {
          cusr.close();
        } catch (DatabaseException e) {
          e.printStackTrace();
        }
      }
      if (txn != null) {
        try {
          txn.abort();

        } catch (DatabaseException e) {
          e.printStackTrace();
        }
      }

    }
    return BitMapOperationStatus.SUCCESS;
  }

  public BitMapOperationStatus optimizeOld(String row) {
    Cursor cusr = null;
    Transaction txn = null;
    // ZZZZ System.out.println(">>>>>>>>>>>>>>>>>>Optimize calld : Row : " + row
    // + "<<<<<<<<<<<<<<<<<<<");
    try {

      txn = dbEnv.beginTransaction(null, null);// n
      cusr = auxDb.openCursor(txn, null); // n
      DatabaseEntry dBRow = new DatabaseEntry(row.getBytes());
      DatabaseEntry auxData = new DatabaseEntry();
      DatabaseEntry dBDataToWrite = new DatabaseEntry();

      OperationStatus retVal = cusr.getSearchKey(dBRow, auxData, LockMode.DEFAULT);
      HoleHandler hh = holeManager.get(dBRow);
      // niether MainDB contains this key nor hole
      if (hh == null && dB.exists(txn, dBRow) != OperationStatus.SUCCESS) {
        /*
         * ZZZ System.out
         * .println("Hole handler is null & row dos not exists in MainDB.");
         * System.out.println("Creating def hole");
         */
        hh = new HoleHandler();
        hh.add(0, (Integer.MAX_VALUE / 8) * 8);
      }

      while (retVal == OperationStatus.SUCCESS) {
        int cellId = BitMapUtil.ByteArrayToInt(auxData.getData());
        // ZZZZ System.out.println("Read from Aux:cell>>>>>>" + cellId);
        // int cellId = cellDict.resolveCell(cell) ;

        byte[] arrByteToWrite = null;
        if (hh.isInHole(cellId)) {
          // / ZZZ System.out.println("in Hole.");
          int byteId = BitMapUtil.getPhysicalByteId(cellId);
          // ZZZZ System.out.println("Inhole ByteId:" + byteId);
          Hole h = hh.getEnclosingHole(cellId);
          // / ZZZ System.out.println("Enclosing Hole:" + h);
          int offset = BitMapUtil.getPhysicalByteId(h.getStart() - h.getTotalSizeTillThisHole());
          // / ZZZ System.out.println("offset" + offset);

          int holeStartByte = BitMapUtil.getPhysicalByteId(h.getStart());
          int holeEndByte = BitMapUtil.getPhysicalByteId(h.getEnd());

          int nextHoleStartByte = byteId + 1;
          int nextNewHoleSizeInBytes = (holeEndByte - nextHoleStartByte) + 1;
          hh.removeHole(h);
          // / ZZZ System.out.println("Old Hole removed :" + hh);
          // if((prevEnd - holeStart) >=
          // holeManager.getMinimunHoleSize())
          if (byteId > 0) {
            // / ZZZ System.out.println("hererere");
            int prevHoleEndByte = byteId - 1;
            int prevNewHoleSizeInByte = (prevHoleEndByte - holeStartByte) + 1;
            /*
             * ZZZ System.out.println("To add prev hole holeStart(byte):" +
             * holeStartByte + ",prevEnd(byte):" + prevHoleEndByte);
             */
            /*
             * ZZZ System.out.println("To add prev hole holeStart(cell):" +
             * (holeStartByte * 8) + ",prevEnd(cell):" + (((prevHoleEndByte + 1)
             * * 8) - 1));
             */
            if ((prevNewHoleSizeInByte) >= holeManager.getMinimunHoleSize()) {

              // start will always be multiple of 8
              // end will always be multiple of 8 -1
              // +1 in End to reach endth bit of that byte
              // e.g. 7th byte is 56 57 58 59 60 61 62 63
              // but 7*8 = 56 but we have to include hole till 63

              hh.add(holeStartByte * 8, ((prevHoleEndByte + 1) * 8) - 1);
              // / ZZZ System.out.println("Add new prev Hole:" + hh);
            } else {
              /*
               * ZZZ System.out .println(
               * "New perv hole is small in size.Add this in Main Data itself at offset:"
               * + offset);
               */
              arrByteToWrite = new byte[prevNewHoleSizeInByte];
              dBDataToWrite.setPartial(offset, 0, true);
              dBDataToWrite.setData(arrByteToWrite);

              if (dB.put(txn, dBRow, dBDataToWrite) != OperationStatus.SUCCESS) {
                // // need to decide
              }
              offset += prevNewHoleSizeInByte;

              /*
               * for(int i = 0 ; i< prevNewHoleSize ; i++) { bytes.put(new
               * Integer(holeStart+i), new Byte((byte)0)) ; }
               */

            }
          }
          // / ZZZ System.out.println("Add Cell to main DB at offset:" +
          // offset);

          int relCell = BitMapUtil.getRelativeCellIdInByte(cellId);
          // / ZZZ System.out.println("relCell:" + relCell);
          arrByteToWrite = new byte[1];
          arrByteToWrite[0] |= 1 << relCell;
          dBDataToWrite.setPartial(offset, 0, true);
          dBDataToWrite.setData(arrByteToWrite);
          // / ZZZ System.out.println("byte:" + arrByteToWrite[0]);
          if (dB.put(txn, dBRow, dBDataToWrite) != OperationStatus.SUCCESS) {
            // // need to decide
          }

          /*
           * Byte b = bytes.get(new Integer(byteId)) ; byte bTowrite = 0 ; if(b
           * != null) { bTowrite = b.byteValue() ; } bytes.put(new
           * Integer(byteId), new Byte( (byte) (bTowrite | (byte)(1<<relCell))
           * )) ;
           */

          if (nextNewHoleSizeInBytes >= holeManager.getMinimunHoleSize()) {
            /*
             * ZZZ System.out .println("To add new next hole holeStart(byte):" +
             * nextHoleStartByte + ",nextEnd(byte):" + holeEndByte); System.out
             * .println("To add new next hole holeStart(cell):" +
             * (nextHoleStartByte * 8) + ",nextEnd(cell):" + (((holeEndByte + 1)
             * * 8) - 1));
             */
            hh.add((nextHoleStartByte * 8), ((holeEndByte + 1) * 8) - 1);
          } else {
            /*
             * ZZZ System.out .println(
             * "New next hole is small in size.Add this in Main Data itself.");
             * System.out.println("new Byte allocated:" + nextNewHoleSizeInBytes
             * + " offset:" + offset);
             */
            arrByteToWrite = new byte[nextNewHoleSizeInBytes];
            dBDataToWrite.setPartial(offset, 0, true);
            dBDataToWrite.setData(arrByteToWrite);
            if (dB.put(txn, dBRow, dBDataToWrite) != OperationStatus.SUCCESS) {
              // // need to decide
            }
            /*
             * for(int i = 0 ; i< nextNewHoleSize ; i++) { bytes.put(new
             * Integer(nextStart+i), new Byte((byte)0)) ; }
             */
          }
          // System.out.println("AFTER ALL hole status" + hh);

        } else {
          cellId = hh.resolveCell(cellId);
          int byteId = BitMapUtil.getPhysicalByteId(cellId);
          dBDataToWrite.setPartial(byteId, 1, true);
          arrByteToWrite = new byte[1];
          cellId = BitMapUtil.getRelativeCellIdInByte(cellId);
          arrByteToWrite[0] |= 1 << cellId;
          dBDataToWrite.setData(arrByteToWrite);
          if (dB.put(txn, dBRow, dBDataToWrite) != OperationStatus.SUCCESS) {
            // // decide what to do on error ;
          }

        }

        retVal = cusr.getNextDup(dBRow, auxData, LockMode.DEFAULT);
      }
      cusr.close();
      cusr = null;
      AuxData.remove(row);
      holeManager.add(dBRow, hh, txn);
      auxDb.delete(txn, dBRow);
      txn.commit();
      txn = null;
      // / ZZZ searchAndPrint(row);
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (cusr != null) {
        try {
          cusr.close();
        } catch (DatabaseException e) {
          e.printStackTrace();
        }
      }
      if (txn != null) {
        try {
          txn.abort();

        } catch (DatabaseException e) {
          e.printStackTrace();
        }
      }
    }
    // ZZZZ System.out.println("Optimize complete");
    return BitMapOperationStatus.SUCCESS;
  }

  public BitMapOperationStatus fullOptimize(byte[] row) throws DatabaseException {

    String strRow = new String(row);
    synchronized (optimizeLockMap) {
      if (!optimizeLockMap.containsKey(strRow)) {
        optimizeLockMap.put(strRow, new ReentrantLock());
      }
    }
    synchronized (putLockMap) {
      if (!putLockMap.containsKey(strRow)) {
        putLockMap.put(strRow, new ReentrantLock());
      }
    }

    synchronized (putLockMap.get(strRow)) {
      // will be a blocking call
      optimizeLockMap.get(strRow).lock();
      if (putLockMap.containsKey(strRow) && putLockMap.get(strRow).isLocked()) {
        optimizeLockMap.get(strRow).unlock();
        return BitMapOperationStatus.FAIL;
      }
    }
    // ZZZZ System.out.println("fullOptimize called");
    DatabaseEntry dbKey = new DatabaseEntry(row);
    if (optimizeImpl(dbKey) != BitMapOperationStatus.SUCCESS) {
      optimizeLockMap.get(strRow).unlock();
      return BitMapOperationStatus.FAIL;
    }
    // ZZZZ System.out.println("fullOptimize called Part");

    DatabaseEntry dbData = new DatabaseEntry();
    int offset = 0;
    int size = 1024;
    dbData.setPartial(offset, size, true);
    try {
      Transaction txn = dbEnv.beginTransaction(null, null);
      try {
        HoleHandler hh = holeManager.get(dbKey);
        // ZZZZ System.out.println(hh);
        // perv state will be used in next iteration
        while (dB.get(txn, dbKey, dbData, LockMode.DEFAULT) == OperationStatus.SUCCESS && dbData.getSize() > 0) {
          int holeStart = -1, holeEnd = Integer.MAX_VALUE, holeStartByte = -1, holeEndByte = Integer.MAX_VALUE, blankLastByte = Integer.MAX_VALUE, blankStartByte = -1;
          Hole prevHole = new Hole(-1, -1), nextHole = new Hole(-1, -1);
          int remainingBlankBytes = 0;
          // ZZZZ System.out.println("gete datata");
          int totBytesShrinked = 0;
          byte arrBytes[] = dbData.getData();
          int curReadTotBytesShrinked = 0;
          for (int i = 0; i < arrBytes.length; i++) {
            // int count = offset + arrBytes.length ;
            // for (int i = offset; i < count ; i++) {
            // ZZZZ System.out.println("byte:" + arrBytes[i]);
            if (arrBytes[i] == (byte) 0) {
              if (blankStartByte == -1)
                blankStartByte = i + offset;
              blankLastByte = i + offset;
              // ZZZZ System.out.println("In Blank Byte:blankStartByte:" +
              // blankStartByte + ", blankLastByte:" + blankLastByte);
            } else {
              // ZZZZ System.out.println("In non-Blank Byte:blankStartByte:" +
              // blankStartByte);
              if (blankStartByte != -1) {
                if (hh == null) {
                  hh = new HoleHandler();
                  hh.add(blankStartByte * 8, ((blankLastByte + 1) * 8 - 1));
                  int sizeToWrite = holeEndByte - holeStartByte + 1;
                  dbData.setSize(0);
                  dbData.setPartial(holeStartByte - curReadTotBytesShrinked, sizeToWrite, true);
                  dB.put(txn, dbKey, dbData);
                  curReadTotBytesShrinked += sizeToWrite;
                } else {
                  // ZZZZ System.out.println("Shrinking bytes : " +
                  // blankStartByte + "-" + blankLastByte);
                  for (int j = blankStartByte; j <= blankLastByte; j++) {
                    prevHole.reset();
                    nextHole.reset();
                    int tempHoleStart = hh.resolvePhysicalCell(j * 8, prevHole, nextHole), tempByteStart = j;
                    // ZZZZ System.out.println("tempHoleStart:" + tempHoleStart
                    // + ", prevHoleEnd:" + holeEnd);
                    if (tempHoleStart - holeEnd > 1) {
                      // ZZZZ System.out.println("hh.expand Block");
                      // ZZZZ System.out.println("holeStart:" + holeStart +
                      // ", holeEnd:" + holeEnd + " ,holeEndByte:" + holeEndByte
                      // + ", holeStartByte:" + holeStartByte);
                      if (hh.expand(holeStart, holeEnd,
                          (holeEndByte - holeStartByte + 1) >= holeManager.getMinimunHoleSize())) {
                        int sizeToWrite = holeEndByte - holeStartByte + 1;
                        // ZZZZ
                        // System.out.println("Writing to DB:holeStartByte:" +
                        // holeStartByte + ",size:" + sizeToWrite);
                        dbData.setSize(0);
                        dbData.setPartial(holeStartByte - curReadTotBytesShrinked, sizeToWrite, true);
                        dB.put(txn, dbKey, dbData);
                        curReadTotBytesShrinked += sizeToWrite;
                      }
                      holeStart = -1;
                      holeStartByte = -1;
                    }
                    if (holeStart == -1) {
                      holeStart = tempHoleStart;
                      holeStartByte = j;
                    }
                    // ZZZZ System.out.println("HoleEnd" + holeEnd);
                    holeEnd = tempHoleStart + 7;
                    holeEndByte = j;
                  }
                  // ZZZZ System.out.println("out of loop");
                  // ZZZZ System.out.println("holeStart:" + holeStart +
                  // ", holeEnd:" + holeEnd + " ,holeEndByte:" + holeEndByte +
                  // ", holeStartByte:" + holeStartByte);
                  if (holeStart > 0
                      && holeEnd < Integer.MAX_VALUE
                      && hh.expand(holeStart, holeEnd,
                          (holeEndByte - holeStartByte + 1) >= holeManager.getMinimunHoleSize())) {

                    int sizeToWrite = holeEndByte - holeStartByte + 1;
                    // ZZZZ System.out.println("Writing to DB:holeStartByte:" +
                    // holeStartByte + ",size:" + sizeToWrite);
                    dbData.setSize(0);
                    dbData.setPartial(holeStartByte - curReadTotBytesShrinked, sizeToWrite, true);
                    dB.put(txn, dbKey, dbData);
                    curReadTotBytesShrinked += sizeToWrite;
                  } else if (holeEnd == arrBytes.length + offset - 1) // handle
                                                                      // last
                                                                      // balank
                  // bytes, which
                  // could have been
                  // the part of hole
                  // but due to size
                  // could not
                  {
                    remainingBlankBytes += holeEnd - holeStart + 1;
                  }
                }
              }
              blankStartByte = -1;
            }
          }
          if (blankStartByte != -1) {
            // ZZZZ System.out.println("loop on bytes Ends");
            if (hh == null) {
              hh = new HoleHandler();
              hh.add(blankStartByte * 8, ((blankLastByte + 1) * 8 - 1));
              int sizeToWrite = holeEndByte - holeStartByte + 1;
              dbData.setSize(0);
              dbData.setPartial(holeStartByte - curReadTotBytesShrinked, sizeToWrite, true);
              dB.put(txn, dbKey, dbData);
              curReadTotBytesShrinked += sizeToWrite;
            } else {
              for (int j = blankStartByte; j <= blankLastByte; j++) {
                prevHole.reset();
                nextHole.reset();
                int tempHoleStart = hh.resolvePhysicalCell(j * 8, prevHole, nextHole);
                if (tempHoleStart - holeEnd > 1) {
                  // ZZZZ System.out.println("hetererererer");
                  if (hh.expand(holeStart, holeEnd,
                      (holeEndByte - holeStartByte + 1) >= holeManager.getMinimunHoleSize())) {
                    int sizeToWrite = holeEndByte - holeStartByte + 1;
                    dbData.setSize(0);
                    dbData.setPartial(holeStartByte - curReadTotBytesShrinked, sizeToWrite, true);
                    dB.put(txn, dbKey, dbData);
                    curReadTotBytesShrinked += sizeToWrite;
                  }

                  holeStart = -1;
                  holeStartByte = -1;
                  // holeStart = tempHoleStart ;holeStartByte
                  // = j;
                }
                if (holeStart == -1) {
                  holeStart = tempHoleStart;
                  holeStartByte = j;
                }
                holeEnd = tempHoleStart + 7;
                holeEndByte = j;
              }
              // ZZZZ System.out.println("out of loop");
              // ZZZZ System.out.println("holeStart:" + holeStart + ", holeEnd:"
              // + holeEnd + " ,holeEndByte:" + holeEndByte + ", holeStartByte:"
              // + holeStartByte);
              if (holeStart > 0
                  && holeEnd < Integer.MAX_VALUE
                  && hh.expand(holeStart, holeEnd,
                      (holeEndByte - holeStartByte + 1) >= holeManager.getMinimunHoleSize())) {

                int sizeToWrite = holeEndByte - holeStartByte + 1;
                // ZZZZ System.out.println("Writing to DB:holeStartByte:" +
                // holeStartByte + ",size:" + sizeToWrite);
                dbData.setSize(0);
                dbData.setPartial(holeStartByte - curReadTotBytesShrinked, sizeToWrite, true);
                dB.put(txn, dbKey, dbData);
                curReadTotBytesShrinked += sizeToWrite;
              } else if (holeEnd == arrBytes.length + offset - 1) // handle
              // last
              // balank
              // bytes,
              // which
              // could
              // have
              // been
              // the
              // part
              // of
              // hole
              // but
              // due
              // to
              // size
              // could
              // not
              {
                remainingBlankBytes += holeEnd - holeStart + 1;
              }
            }
          }

          totBytesShrinked += curReadTotBytesShrinked;
          offset += size - curReadTotBytesShrinked - remainingBlankBytes;
          // ZZZZ
          // System.out.println("dbData.setPartial(offset,size, true)::: curReadTotBytesShrinked:"
          // + curReadTotBytesShrinked + ",offset:" + offset);
          dbData.setPartial(offset, size, true);
          dbData.setData(null);
        }
        if (hh != null)
          holeManager.add(dbKey, hh, txn);
        txn.commit();
        txn = null;
      } catch (DatabaseException e) {
        throw e;
        // TODO: handle exception
      } finally {
        if (txn != null) {
          txn.abort();
        }
        if (optimizeLockMap.get(strRow).isLocked())
          optimizeLockMap.get(strRow).unlock();
      }
    } catch (DatabaseException e) {
      throw e;
    } finally {
      if (optimizeLockMap.get(strRow).isLocked())
        optimizeLockMap.get(strRow).unlock();
    }
    return BitMapOperationStatus.SUCCESS;
  }

  public void searchAndPrint(String row) {
    DatabaseEntry dBRow = new DatabaseEntry();
    dBRow.setData(row.getBytes());
    DatabaseEntry dbData = new DatabaseEntry();
    dbData.setPartial(false);

    // dbData.setPartial(byteId,1, true) ;
    try {
      OperationStatus os = dB.get(null, dBRow, dbData, LockMode.DEFAULT);
      byte data[] = dbData.getData();
      if (data != null) {
        for (int i = 0; i < data.length; i++) {
          System.out.println(data[i] & 0xFF);
        }
      } else {
        System.out.println("Data is null");
      }
      System.out.println(holeManager.get(dBRow));

    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void close() throws DatabaseException {

    if (dB != null) {
      dB.close();
      dB = null;
    }
    if (auxDb != null) {
      auxDb.close();
      auxDb = null;
    }
  }

  public BitMapOperationStatus update(CellResolver cellResolver, byte[][] rowsToAddIn, byte[][] rowsToDeleteFrom) throws DatabaseException {
    
    Transaction txn = null; 
    try {
      txn = dbEnv.beginTransaction(null, null);
      if(rowsToAddIn !=null)
      {  
        for(int i= 0 ; i< rowsToAddIn.length ; i++ )
        {
          if(this.put(rowsToAddIn[i], cellResolver,txn) != BitMapOperationStatus.SUCCESS)
          {
              txn.abort() ; 
              txn=null;
              return BitMapOperationStatus.FAIL ; 
          }
        }
      }
      
      if(rowsToDeleteFrom != null)
      {
        for(int i= 0 ; i< rowsToDeleteFrom.length ; i++ )
        {
          if(this.delete(rowsToDeleteFrom[i], cellResolver,txn) != BitMapOperationStatus.SUCCESS)
          {
              txn.abort() ; 
              txn=null;
              return BitMapOperationStatus.FAIL ; 
          }
        }
        
      }
      txn.commit() ; 
      txn=null;  
    }
    catch(DatabaseException e)
    {
        if(txn !=null)
        {
            txn.abort() ; 
            txn = null ;   
        }
        throw e  ; 
    }
    
    return BitMapOperationStatus.SUCCESS;
  }

}
