package com.adichad.lucense.bitmap;

import java.io.*;
import java.util.HashMap;
import java.util.Random;

import org.hamcrest.core.IsInstanceOf;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import com.sleepycat.*;
import com.sleepycat.bind.tuple.TupleBinding;
//import com.sleepycat.db.CacheMode;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.LockDetectMode;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
//import com.sleepycat.db.PreloadConfig;
import com.sleepycat.db.StatsConfig;

public class AuxIndexManager {

  public static int Optimize = 0;

  public static int Put = 1;

  public static int Search = 2;

  private int opCount = 0;

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  private RowHandler rowhandler;

  // private CellHandler cellhandler;

  // private DataStore rowStore = null;
  // private DataStore cellDict = null;
  // private RowDictionaryFileHandler rowDict ;
  private CellDictionaryHandler cellDict;

  private HoleManager holeManager = null;

  private AuxDataHandler auxHandler = null;

  private int maxDocs = 300000;

  Environment dbEnv = null;

  private String lucenseFieldName;

  private String stringRep;

  public int construct(String envHome) throws FileNotFoundException, DatabaseException {
    EnvironmentConfig dBEnvConfig = new EnvironmentConfig();
    dBEnvConfig.setAllowCreate(true);
    dBEnvConfig.setErrorStream(null);
    dBEnvConfig.setInitializeCache(true);
    dBEnvConfig.setTransactional(true);
    dBEnvConfig.setInitializeLocking(true); // n
    // dBEnvConfig.setCDBLockAllDatabases(true) ;
    // dBEnvConfig.setInitializeLogging(true);
    // dBEnvConfig.setPrivate(true) ;
    // dBEnvConfig.setRunRecovery(true);
    dBEnvConfig.setInitializeRegions(true);
    // Indicate that we want db to internally perform deadlock
    // detection. Also indicate that the transaction that has
    // performed the least amount of write activity to
    // receive the deadlock notification, if any.
    // dBEnvConfig.setLockDetectMode(LockDetectMode.MINWRITE);

    // dBEnvConfig.setInitializeLogging(true);//n

    dbEnv = new Environment(new File(envHome), dBEnvConfig);
    cellDict = CellDictionaryFactory.getInstance("int-dict");

    holeManager = new HoleManager(dbEnv, null);
    holeManager.setMinimunHoleSize(500);
    // ZZZZ System.out.println("HoleManger : " +holeManager );

    try {
      // auxHandler = new AuxDataHandler() ;
      rowhandler = new RowHandler(cellDict, holeManager, auxHandler, dbEnv, null, null); // RowHandlerFactory.getInstance(cellDict,holeManager,auxHandler)
                                                                                         // ;
      if (rowhandler == null) {
        System.out.println("Row Handler could not created");
      }

    } catch (FileNotFoundException e) {
      errorLogger.log(Level.ERROR, e + " [" + envHome + " not found]");
      // TODO Auto-generated catch block
      // e.printStackTrace();
    } catch (DatabaseException e) {
      errorLogger.log(Level.ERROR, e);
      // TODO Auto-generated catch block
      // e.printStackTrace();
    }

    statusLogger.log(Level.INFO, "AuxIndex loaded successfully");
    return 0;
  }

  public AuxIndexManager(HashMap<String, String> auxEnvConfig, int minHoleSize, String cellDictName,
      HashMap<String, String> holeDBConfig, HashMap<String, String> auxDBConfig, HashMap<String, String> mainDBConfig,
      String lucenseFieldName) throws FileNotFoundException, DatabaseException {

    EnvironmentConfig dBEnvConfig = new EnvironmentConfig();
    String envPath = "";
    this.lucenseFieldName = lucenseFieldName;
    if (auxEnvConfig != null) {
      envPath = auxEnvConfig.remove("path");
      if (auxEnvConfig.containsKey("cache-size"))
        dBEnvConfig.setCacheSize(Long.parseLong(auxEnvConfig.get("cache-size")));
      if (auxEnvConfig.containsKey("cache-count"))
        dBEnvConfig.setCacheCount(Integer.parseInt(auxEnvConfig.get("cache-count")));
      if (auxEnvConfig.containsKey("cache-max"))
        dBEnvConfig.setCacheMax(Long.parseLong(auxEnvConfig.get("cache-max")));
      if (auxEnvConfig.containsKey("cache-page-size"))
        dBEnvConfig.setCachePageSize(Integer.parseInt(auxEnvConfig.get("cache-page-size")));
      if (auxEnvConfig.containsKey("cache-table-size"))
        dBEnvConfig.setCacheTableSize(Integer.parseInt(auxEnvConfig.get("cache-table-size")));
      if (auxEnvConfig.containsKey("data-dir"))
        dBEnvConfig.addDataDir(new File(auxEnvConfig.get("data-dir")));
    }
    this.stringRep = "("+lucenseFieldName+"): cache-size="+auxEnvConfig.get("cache-size") + " bytes ["+envPath+"]";
    dBEnvConfig.setAllowCreate(true);
    dBEnvConfig.setErrorStream(null);
    dBEnvConfig.setInitializeCache(true);
    dBEnvConfig.setTransactional(true);
    dBEnvConfig.setInitializeLocking(true); // n

    // dBEnvConfig.setCDBLockAllDatabases(true) ;
    // //dBEnvConfig.setInitializeLogging(true);//dBEnvConfig.setPrivate(true) ;
    // //dBEnvConfig.setRunRecovery(true);
    // dBEnvConfig.setInitializeRegions(true) ;
    // Indicate that we want db to internally perform deadlock
    // detection. Also indicate that the transaction that has
    // performed the least amount of write activity to
    // receive the deadlock notification, if any.
    // dBEnvConfig.setLockDetectMode(LockDetectMode.MINWRITE);
    // dBEnvConfig.setInitializeLogging(true);//n
    try {
      dbEnv = new Environment(new File(envPath), dBEnvConfig);
      holeManager = new HoleManager(dbEnv, holeDBConfig);
      holeManager.setMinimunHoleSize(minHoleSize);
      cellDict = CellDictionaryFactory.getInstance(cellDictName);
      rowhandler = new RowHandler(cellDict, holeManager, auxHandler, dbEnv, mainDBConfig, auxDBConfig);
    } catch (DatabaseException e) {
      if (dbEnv != null)
        dbEnv.close();
      if (holeManager != null)
        holeManager.close();
      if (rowhandler != null)
        rowhandler.close();
      throw e;

    } catch (FileNotFoundException e) {
      if (dbEnv != null)
        dbEnv.close();
      if (holeManager != null)
        holeManager.close();
      if (rowhandler != null)
        rowhandler.close();
      throw e;
    }

  }

  @Override
  public String toString() {
    return this.stringRep;
  }
  
  public AuxIndexManager(String envHome) throws FileNotFoundException, DatabaseException {
    construct(envHome);
  }

  public String getLucenseFieldName() {
    return this.lucenseFieldName;
  }

  public RowHandler getRowHandler() {
    return this.rowhandler;
  }

  public BitMapOperationStatus search(String row, int cell) {

    try {
      return rowhandler.search(row, cell);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return BitMapOperationStatus.NOTFOUND;
  }

  public BitMapOperationStatus search(String row, String cell) {

    try {
      return rowhandler.search(row, cell);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return BitMapOperationStatus.NOTFOUND;
  }

  public int optimize(String rowId) {

    rowhandler.optimizeOld(rowId);
    // for every key
    return 0;
  }

  public int optimize2(String rowId) {

    rowhandler.optimize(rowId.getBytes());
    // for every key
    return 0;
  }

  public BitMapOperationStatus add(String[] rows, String cells[][]) {

    if (rows.length != cells.length) {
      return BitMapOperationStatus.FAIL;
    }
    System.out.println(rowhandler);
    for (int i = 0; i < rows.length; i++) {
      try {

        StringCellResolverArray resolver = new StringCellResolverArray(cells[i],
            (StringInputCellDictionaryHandler) cellDict);
        rowhandler.put(rows[i], resolver);

        // rowhandler.put(rows[i], cells[i]) ;
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return BitMapOperationStatus.SUCCESS;
  }

  public BitMapOperationStatus add(String[] rows, int cells[][]) {

    if (rows.length != cells.length) {
      return BitMapOperationStatus.FAIL;
    }
    System.out.println(rowhandler);
    for (int i = 0; i < rows.length; i++) {
      try {
        IntCellResolverArray resolver = new IntCellResolverArray(cells[i], (IntInputCellDictionaryHandler) cellDict);
        rowhandler.put(rows[i], resolver);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return BitMapOperationStatus.SUCCESS;
  }

  public BitMapOperationStatus add(String row, int cells[]) {

    try {
      IntCellResolverArray resolver = new IntCellResolverArray(cells, (IntInputCellDictionaryHandler) cellDict);
      rowhandler.put(row, resolver);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return BitMapOperationStatus.SUCCESS;
  }

  public BitMapOperationStatus delete(String row, String[] cells) {

    try {
      StringCellResolverArray resolver = new StringCellResolverArray(cells, (StringInputCellDictionaryHandler) cellDict);
      return rowhandler.delete(row.getBytes(), resolver,null);
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return BitMapOperationStatus.FAIL;
  }

  public BitMapOperationStatus fullOptimize(String row) {
    try {
      rowhandler.fullOptimize(row.getBytes());
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();

    }
    return BitMapOperationStatus.FAIL;
  }

  public void searchAndPrint(String row) {
    rowhandler.searchAndPrint(row);
  }

  /**
   * @param args
   */
  void test() {

    String keys[] = { "1", "3" };
    String values[][] = { { "1", "24", "48", "56", "64", "72", "80", "104" },
        { "2", "7", "49", "50", "65", "70", "80", "104" } };
    // String values[][] ={{"1","24","48"}} ;
    // String values[][] ={{"1","24"}} ;
    // String values[][] ={{"1"}} ;
    add(keys, values);
    // /searchAndPrint("1") ;
    optimize2("1");
    // searchAndPrint("1") ;

  }

  public static void main(String[] args) {
    // TODO Auto-generated method stub

    AuxIndexManager im = null;
    try {
      im = new AuxIndexManager("/tmp/bdb/data");

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // im.test() ;

    // System.exit(0);

    /*
     * String keyz[] = {"1", "2"}; int valuez[][]
     * ={{1,3,8,16,25,164,4,172,180,189, 2}, {201,208,216,225,364,372,380,389}}
     * ; im.testBenchMark(keyz, valuez); System.exit(0);
     */
    String keyz[] = { "1", "2" };
    System.out.println("Benchmarking for docs = " + im.maxDocs);

    // im.benchMarkRead(keyz); ; im.benchMarkRead(keyz); ;
    // im.benchMarkRead(keyz); ;System.exit(0) ;
    // im.benchMarkRead(keyz); im.benchMarkReadOld(keyz) ; System.exit(0) ;

    im.benchMark(false, true, keyz);
    im.benchMarkOptimize(false, keyz);

    im.benchMarkRead(keyz);
    // im.benchMarkReadOld(keyz) ;

    System.exit(0);

    im.benchMarkOptimize(false, keyz);
    im.benchMarkRead(keyz);
    System.exit(0);

    im.benchMark(false, true, keyz);
    im.benchMarkOptimize(true, keyz);

    System.exit(0);

    // ///////////
    String keys[] = { "1", "3" };
    String values[][] = { { "1", "24", "48", "56", "64", "72", "80", "104" },
        { "2", "7", "49", "50", "65", "70", "80", "104" } };

    im.add(keys, values);
    // im.optimize("1") ;
    // im.delete("1", new String[]{"48","56","64"}) ;
    // im.searchAndPrint("1");
    // im.fullOptimize("1");
    im.fullOptimize("3");
    // /im.searchAndPrint("3");
    for (int i = 0; i < 106; i++) {
      if (im.search("3", i) == BitMapOperationStatus.SUCCESS) {
        System.out.println("Found     " + i);
      } else {
        System.out.println("Not Found " + i);
      }
    }
    // String keyz[] = {"1", "2"};
    im.benchMark(true, true, keyz);
    im.benchMarkOptimize(false, keyz);
    im.benchMark(true, true, keyz);
    im.benchMarkOptimize(true, keyz);

    System.exit(0);
    // im.
    // //////////
    /*
     * String keys[] = {"1","23453","3456","567"} ; String values[][] = {
     * //{"1","23453","3456","567"} {"1","18","35","64"} ,{"1","2","3","10000"}
     * ,{"100000","22","32","10000"} ,{"100000","22","32","10000"} } ;
     */

    im.add(keys, values);
    if (im.search("1", "64") != BitMapOperationStatus.SUCCESS) {
      System.out.println("Not Found");
    }
    // "23453","3456","567"}
    im.optimize("1");
    im.optimize("567");
    im.optimize("3456");
    im.optimize("23453");

    // im.searchAndPrint("1") ;
    if (im.search("1", "64") != BitMapOperationStatus.SUCCESS) {
      System.out.println("Not Found");
    }

    if (im.search("1", "1") != BitMapOperationStatus.SUCCESS) {
      System.out.println("Not Found");
    }

    im.add(new String[] { "1" }, new String[][] { { "1024" } });
    im.optimize("1");
    im.delete("1", new String[] { "18", "35", "64" });

    im.fullOptimize("1");
    // im.searchAndPrint("1");

  }

  public void testBenchMark(String[] keys, int[][] values) {
    for (int i = 0; i < 2; i++) {
      System.out.println("adding key " + keys[i]);
      printData(values[i]);
      add(keys[i], values[i]);
      System.out.println("\noptimizing key " + keys[i]);
      // optimizedOptimize(keys[i]);
      optimize2(keys[i]);
      System.out.println("fulloptimizing key " + keys[i]);
      fullOptimize(keys[i]);
    }
  }

  private void printData(int[] values) {
    for (int i : values) {
      System.out.print(i + " ");
    }
  }

  public void benchMarkOptimize(boolean fullOptimize, String[] keyz) {
    int opNum = opCount++;
    System.out.println("Optimization Start:" + opNum);
    long startTime, endTime;
    for (String key : keyz) {
      System.out.println("Optimization Start: key:" + key);
      startTime = System.currentTimeMillis();
      if (fullOptimize) {
        fullOptimize(key);
      } else {
        optimize2(key);
      }
      // searchAndPrint(key);
      endTime = System.currentTimeMillis();
      startTime = endTime - startTime;
      System.out.println("opNum:" + opNum + " Key " + key + " Optimize time " + startTime);
      startTime = endTime;
    }
  }

  class MyThread implements Runnable {
    private int whatToPerformInThread = 1;

    AuxIndexManager im = null;

    String row;

    int cellId;

    MyThread(int whatToPerformInThread, AuxIndexManager im) {
      this.whatToPerformInThread = whatToPerformInThread;
      this.im = im;
      this.row = row;
      this.cellId = cellId;
    }

    @Override
    public void run() {

      String keys[] = { "1", "2" };
      if (whatToPerformInThread == AuxIndexManager.Optimize) {
        im.benchMarkOptimize(false, keys);// optimize2(row) ;
      }

      else if (whatToPerformInThread == AuxIndexManager.Put) {
        im.benchMark(false, true, keys);
      } else {
        im.benchMarkRead(keys);
        ;// (false, true, keys) ;
      }
    }

  }

  public void benchMark(boolean optimize, boolean add, String[] keys) {
    // TODO Auto-generated method stub
    int opNum = opCount++;
    System.out.println("put Starat:" + opNum);
    int keyLen = keys.length;
    int key, numV;
    Random randomGenerator = new Random();
    // im.add(keys, values) ;
    long startTime = System.currentTimeMillis();
    long endTime = 0, timeTaken = 0, finalTime = 0;
    finalTime = startTime;
    int totalBitsSet = 0;
    for (int i = 0; i < 200/* 00000 */; i++) {
      key = randomGenerator.nextInt(keyLen);
      numV = randomGenerator.nextInt(1000) + 10;
      int[] values;
      values = new int[numV];
      // System.out.println("time: " + timeTaken + "\t going to add values " +
      // numV);
      numV--;
      // values[0] = new String[numV];

      // values[0][] = new String[numV] ;
      for (; numV >= 0; numV--) {
        // rowhandler.put(keys[key], randomGenerator.nextInt(30000000));
        values[numV] = randomGenerator.nextInt(maxDocs) + 5000; // 000);
        totalBitsSet++;
      }
      if (add) {
        System.out.println("put key:" + keys[key]);
        add(keys[key], values);
        System.out.println("putEnd key :" + keys[key]);
        if (optimize) {
          optimize(keys[key]);
        }
      }
      endTime = System.currentTimeMillis();
      timeTaken = endTime - startTime;
      startTime = endTime;
      // rowhandler.put(keys[key], values);
      // System.out.println("hi" + key);
    }
    timeTaken = endTime - finalTime;
    System.out.println("OpNum :" + opNum + " Keys " + keyLen + " Bits Set " + totalBitsSet + "\t total time: "
        + timeTaken);
  }

  public void benchMarkRead(String[] keys) {

    int opNum = opCount++;
    // TODO Auto-generated method stub
    System.out.println("benchMarkRead Start:" + opNum);
    int keyLen = keys.length;
    int key, numV;
    Random randomGenerator = new Random();
    // im.add(keys, values) ;
    long startTime = System.currentTimeMillis();
    long endTime = 0, timeTaken = 0, finalTime = 0, loadTime = 0;
    finalTime = startTime;
    loadTime = startTime;
    HashMap<Integer, Row> rowResSet = new HashMap<Integer, Row>();
    for (int i = 0; i < keys.length; i++) {
      try {

        System.out.println("opNum:" + opNum + " Load Row:" + keys[i]);
        Row row = rowhandler.loadRow(keys[i].getBytes());
        endTime = System.currentTimeMillis();
        System.out.println("opNum:" + opNum + "Load Row Complete:" + keys[i] + " Time:" + (endTime - loadTime));
        loadTime = endTime;

        rowResSet.put(i, row);

      } catch (DatabaseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    for (int i = 0; i < maxDocs; i++) {
      key = randomGenerator.nextInt(keyLen);
      try {
        if (rowResSet.get(key) != null)
          rowResSet.get(key).resolvedSearch(i);
      } catch (DatabaseException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      // search(keys[key], i);
    }

    endTime = System.currentTimeMillis();
    timeTaken = endTime - finalTime;
    System.out.println("opNum:" + opNum + " Keys " + keyLen + " read total time: " + timeTaken);
    // System.out.println("benchMarkRead Ends");
  }

  public void benchMarkReadOld(String[] keys) {

    // TODO Auto-generated method stub
    int keyLen = keys.length;
    int key, numV;
    Random randomGenerator = new Random();
    // im.add(keys, values) ;
    long startTime = System.currentTimeMillis();
    long endTime = 0, timeTaken = 0, finalTime = 0;
    finalTime = startTime;

    for (int i = 0; i < maxDocs; i++) {
      key = randomGenerator.nextInt(keyLen);
      search(keys[key], i);
    }

    endTime = System.currentTimeMillis();
    timeTaken = endTime - finalTime;
    System.out.println("Keys " + keyLen + " OLD read total time: " + timeTaken);
  }

  private int loadIndex() {

    return 0;
  }

  public CellDictionaryHandler getCellDictionary() {
    // TODO Auto-generated method stub
    return cellDict;
  }

  @Override
  protected void finalize() throws Throwable {
    this.close () ;     
    super.finalize();
  }
  
  public void close () throws DatabaseException
  {
    if(rowhandler !=null)
      rowhandler.close();
    if(holeManager !=null)
      holeManager.close();
    if(dbEnv != null)
      dbEnv.close();
  }
  
}
