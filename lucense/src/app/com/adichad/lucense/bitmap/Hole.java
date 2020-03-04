package com.adichad.lucense.bitmap;

import java.util.Comparator;

public class Hole implements Comparable<Hole> {

  private int holeStart = -1;// inclusive

  private int holeEnd = -1; // inclusive

  private int holeSize = 0;

  private int totHoleSizeTillThisHole = 0;

  private int totHoleSizeincludingThisHole = 0;

  public Hole(int holeStart, int holeEnd) {
    this.holeStart = holeStart;
    this.holeEnd = holeEnd;
    this.holeSize = this.holeEnd - this.holeStart + 1;
    this.totHoleSizeincludingThisHole = this.holeSize;
  }

  public void copyFrom(Hole h) {
    this.holeStart = h.holeStart;
    this.holeEnd = h.holeEnd;
    this.holeSize = h.holeSize;
    this.totHoleSizeincludingThisHole = h.totHoleSizeincludingThisHole;
    this.totHoleSizeTillThisHole = h.totHoleSizeTillThisHole;
  }

  public void setTotalSizeTillThisHole(int size) {
    this.totHoleSizeTillThisHole = size;
    this.totHoleSizeincludingThisHole = this.totHoleSizeTillThisHole + this.holeSize;
  }

  public int getTotalSizeTillThisHole() {
    return this.totHoleSizeTillThisHole;
  }

  public int getTotalSizeIncludingThisHole() {
    return this.totHoleSizeincludingThisHole;
  }

  public void reset() {
    this.holeStart = -1;
    this.holeEnd = -1;
    this.holeSize = 0;
    this.totHoleSizeTillThisHole = 0;
    this.totHoleSizeincludingThisHole = 0;
  }

  public int getSize() {
    return this.holeSize;
  }

  public int getStart() {
    return holeStart;
  }

  public int getEnd() {
    return holeEnd;
  }

  @Override
  public int compareTo(Hole o) {

    // System.out.println("ComapreTo called");

    // / this is completely in o
    if (this.holeStart >= o.holeStart && this.holeEnd <= o.holeEnd) {
      // System.out.println("this.holeStart : "+ this.holeStart + "o.holeStart:"
      // + o.holeStart);
      // System.out.println("this.holeEnd : "+ this.holeEnd + "o.holeEnd:" +
      // o.holeEnd);
      return 0;
    }

    /*
     * /// this.Start is completely in o if(this.holeStart <= o.holeEnd &&
     * this.holeStart >= o.holeStart) { o.holeEnd = this.holeEnd ; return 0 ; }
     * /// this.End is completely in o if(this.holeEnd >= o.holeStart &&
     * this.holeEnd <= o.holeEnd) { o.holeStart = this.holeStart ; return 0 ; }
     */

    return (this.holeStart - o.holeEnd);
  }

  public String toString() {
    return holeStart + " -- " + holeEnd + "[holeSize:" + holeSize + "]" + " [totHoleSizeTillThisHole:"
        + totHoleSizeTillThisHole + "][totalsizeIncludingThisHole:" + totHoleSizeincludingThisHole + "]";
  }

}
