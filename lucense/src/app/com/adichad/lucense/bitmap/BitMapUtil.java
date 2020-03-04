package com.adichad.lucense.bitmap;

public class BitMapUtil {

  public static final int ByteArrayToInt(byte[] b) {
    int j = 0;
    for (int i = 0; i < b.length; i++) {
      int shift = (4 - 1 - i) * 8;
      j += (b[i] & 0x000000FF) << shift;
      // j |= j << value[i] ;
    }
    return j;
  }

  public static final int getPhysicalCellId(int cellId) {
    return cellId;
  }

  public static final int getPhysicalByteId(int physicalCellId) {
    return physicalCellId / 8;
  }

  public static final int getRelativeCellIdInByte(int physicalCellId) {
    return physicalCellId % 8;
  }

  public static final byte[] intToByteArray(int value) {
    return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
  }

  public static final byte[] growData(byte arr[], byte arrToWrite[], int offset, int size, boolean partial) {

    // System.out.println("offset:" +offset
    // +" size:"+size+" ,arrToWrite.length:"+arrToWrite.length );
    /*
     * for (int i = 0; i < arrToWrite.length; i++) {
     * System.out.println(arrToWrite[i]); }
     */

    /*
     * if(arr==null || offset >= arr.length) { byte tmp[] = new
     * byte[offset+arrToWrite.length] ; for (int i = 0; i < arr.length; i++) {
     * tmp[i] = arr[i] ; } arr = tmp ; }
     */

    int growSize = arrToWrite.length;
    int i = 0;
    /*
     * if(arr ==null) { arr = new byte[offset+1] ; }
     */

    for (; i < size && i + offset < arr.length; i++) {
      arr[i + offset] = arrToWrite[i];
    }

    growSize = arrToWrite.length - i;
    // / ZZZ System.out.println("growSize:" + growSize);

    if (growSize >= 0) {
      int prevArrsize = 0;
      if (arr != null)
        prevArrsize = arr.length;
      byte tmpArr[] = new byte[prevArrsize + growSize];
      int alreadyWritten = i;

      i = 0;
      for (; i < offset + alreadyWritten; i++) {
        tmpArr[i] = arr[i];
      }
      int newData = growSize + alreadyWritten + offset;
      int j = alreadyWritten;
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
}
