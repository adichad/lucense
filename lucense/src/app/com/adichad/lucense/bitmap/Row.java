package com.adichad.lucense.bitmap;

import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;

public abstract class Row {

  protected byte[] mainData = null;

  protected HoleHandler hh = null;

  protected Database mainDB = null;

  protected Database auxDB = null;

  protected byte[] row = null;

  protected DatabaseEntry dbRow = new DatabaseEntry();

  protected DatabaseEntry dbData = new DatabaseEntry();

  protected CellDictionaryHandler cellDict = null;

  public void setRowId(byte[] row) {
    this.row = row;
    dbRow.setData(this.row);
    dbRow.setPartial(false);
    dbData.setPartial(false);
  }

  public void setHoleHandler(HoleHandler hh) {
    this.hh = hh;
  }

  public void setMainDatabase(Database mainDb) {
    this.mainDB = mainDb;
  }

  public void setAuxData(Database auxDb) {
    this.auxDB = auxDb;
  }

  public void setMainData(byte[] mainData) {
    this.mainData = mainData;
  }

  public void setCellDictionary(CellDictionaryHandler cellDict) {
    this.cellDict = cellDict;
  }

  protected BitMapOperationStatus searchFromAux(int cellId) throws DatabaseException {
    dbData.setPartial(false);
    dbData.setData(BitMapUtil.intToByteArray(cellId));
    if (OperationStatus.SUCCESS == auxDB.getSearchBoth(null, dbRow, dbData, LockMode.DEFAULT)) {
      return BitMapOperationStatus.SUCCESS;
    }
    return BitMapOperationStatus.NOTFOUND;
  }

  protected BitMapOperationStatus searchFromMainData(int cellId) {
    if (mainData == null)
      return BitMapOperationStatus.NOTFOUND;
    int byteId = BitMapUtil.getPhysicalByteId(cellId);
    if (byteId >= mainData.length)
      return BitMapOperationStatus.NOTFOUND;
    ;
    if ((mainData[byteId] & (1 << BitMapUtil.getRelativeCellIdInByte(cellId))) == 0)
      return BitMapOperationStatus.NOTFOUND;
    return BitMapOperationStatus.SUCCESS;
  }

  private static class MainDataSearchableRow extends Row {
    public MainDataSearchableRow(byte[] row, byte[] mainData, CellDictionaryHandler cellDict) {

      this.setRowId(row);
      this.mainData = mainData;
      this.cellDict = cellDict;
    }

    /*
     * (non-Javadoc)
     * @see com.adichad.lucense.bitmap.Row#search(int) Searches only Main Data
     * without using Hole Info This class shuld be created When there are no
     * holes Will not even look at AuxData
     */
    public BitMapOperationStatus resolvedSearch(int cellId) {
      return searchFromMainData(cellId);
    }
  }

  private static class MainDataWithHolesSearchableRow extends Row {
    public MainDataWithHolesSearchableRow(byte[] row, byte[] mainData, HoleHandler hh, CellDictionaryHandler cellDict) {
      this.setRowId(row);
      this.mainData = mainData;
      this.hh = hh;
      this.cellDict = cellDict;
    }

    /*
     * (non-Javadoc)
     * @see com.adichad.lucense.bitmap.Row#search(int) Searches only Main Data by
     * using Hole Info This class shuld be created When there are holes but no
     * AuxData Will not even look at AuxData
     */
    public BitMapOperationStatus resolvedSearch(int cellId) {
      if (mainData == null)
        return BitMapOperationStatus.NOTFOUND;
      int resolvedCellId = cellId;
      if ((resolvedCellId = hh.resolveCell(cellId)) == HoleHandler.CELL_IN_HOLE) {
        return BitMapOperationStatus.NOTFOUND;
      }
      return searchFromMainData(resolvedCellId);
    }
  }

  private static class AuxDataSearchableRow extends Row {
    public AuxDataSearchableRow(byte[] row, Database auxDB, CellDictionaryHandler cellDict) {
      this.setRowId(row);
      this.auxDB = auxDB;
      this.cellDict = cellDict;
    }

    /*
     * (non-Javadoc)
     * @see com.adichad.lucense.bitmap.Row#search(int) Will Search only AuxData
     */
    public BitMapOperationStatus resolvedSearch(int cellId) throws DatabaseException {
      return searchFromAux(cellId);
    }
  }

  private static class GeneralRow extends Row {
    public GeneralRow(byte[] row, byte[] mainData, HoleHandler hh, Database auxDB, CellDictionaryHandler cellDict) {
      this.setRowId(row);
      this.mainData = mainData;
      this.hh = hh;
      this.auxDB = auxDB;
      this.cellDict = cellDict;
      // TODO Auto-generated constructor stub
    }

    public BitMapOperationStatus resolvedSearch(int cellId) throws DatabaseException {
      int resolvedCellId = cellId;
      if ((resolvedCellId = hh.resolveCell(cellId)) == HoleHandler.CELL_IN_HOLE) {
        return searchFromAux(cellId);
      }
      // / This case will be handled by AuxDataSearchableRow
      /*
       * else if (dB.exists(null, dbRow) != OperationStatus.SUCCESS) { //ZZZZ
       * System.out.println("Row not Found in Main: Search in Aux.");
       * dbData.setData(BitMapUtil.intToByteArray(cellId)); return
       * searchFromHelperDatabase(dbRow, dbData, auxDb); }
       */
      return searchFromMainData(resolvedCellId);

    }
  }

  public BitMapOperationStatus search(String cell) throws DatabaseException {
    int cellId = cellDict.resolveCell(cell);
    return resolvedSearch(cellId);
  }

  public BitMapOperationStatus search(int cell) throws DatabaseException {
    int cellId = cellDict.resolveCell(cell);
    return resolvedSearch(cellId);
  }

  public BitMapOperationStatus[] search(CellResolver cells) throws DatabaseException {
    BitMapOperationStatus[] ret = new BitMapOperationStatus[cells.getSize()];
    for (int i = 0; i < cells.getSize(); i++) {
      ret[i] = resolvedSearch(cells.get(i));
    }
    return ret;
  }

  public BitMapOperationStatus search(CellResolver cells, int index) throws DatabaseException {
    return resolvedSearch(cells.get(index));
  }

  public abstract BitMapOperationStatus resolvedSearch(int cellId) throws DatabaseException;

  public static Row getInstance(byte[] row, byte[] mainData, HoleHandler hh, Database auxDB,
      CellDictionaryHandler cellDict) {
    Row rowObj = null;

    // Main Data Row Does not Exists
    if (mainData == null) {
      if (auxDB != null) {
        // // ZZZZ System.out.println("herer1");
        rowObj = new AuxDataSearchableRow(row, auxDB, cellDict);
      }
      // / ZZZZ System.out.println("herer2");
    } else if (hh == null) {// /ZZZ System.out.println("herer3");
      rowObj = new MainDataSearchableRow(row, mainData, cellDict);
    } else {
      if (auxDB != null) { // / ZZZ System.out.println("herer4");
        rowObj = new GeneralRow(row, mainData, hh, auxDB, cellDict);
      } else { // /ZZZ System.out.println("herer5");
        rowObj = new MainDataWithHolesSearchableRow(row, mainData, hh, cellDict);
      }
    }
    return rowObj;
  }

}
