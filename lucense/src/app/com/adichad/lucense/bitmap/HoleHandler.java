package com.adichad.lucense.bitmap;

import java.io.Serializable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

public class HoleHandler {

  public static int CELL_IN_HOLE = -1;

  private TreeSet<Hole> holeTree = new TreeSet<Hole>();

  // public static int MAX_HOLE_END = 0xFFFFFFFF ;
  private int totHoleSize = 0;

  // private Iterator iterator = null ;

  public TreeSet<Hole> getHoles() {
    return holeTree;
  }

  public boolean add(Hole hole) {
    Hole lower = holeTree.lower(hole);
    return holeTree.add(hole);
  }

  public boolean add(int start, int end) {
    Hole hole = new Hole(start, end);
    boolean added = holeTree.add(hole);

    if (added) {
      // fill TotalSizeTillThisHole on the basis of last hole
      totHoleSize += hole.getSize();
      Hole lower = holeTree.lower(hole);
      if (lower != null) {
        hole.setTotalSizeTillThisHole(lower.getSize() + lower.getTotalSizeTillThisHole());
      }

      // if not added in last (i.e. in somewhere b/w existing holes)
      // setTotalSizeTillThisHole for every hole after this hole
      Hole higher = hole;
      while ((higher = holeTree.higher(higher)) != null) {
        higher.setTotalSizeTillThisHole(hole.getSize() + higher.getTotalSizeTillThisHole());
      }
    }
    return added;
  }

  public boolean isInHole(int cellid) {
    Hole hole = new Hole(cellid, cellid);
    return holeTree.contains(hole);
  }

  public int resolveCell(int cellid) {
    Hole hole = new Hole(cellid, cellid);
    if (holeTree.contains(hole)) {
      return HoleHandler.CELL_IN_HOLE;
    }

    Hole lower = holeTree.lower(hole);
    // ZZZZ System.out.println("hh.reolveCell:lowerHole:"+lower);
    if (lower != null) {
      return (cellid - lower.getTotalSizeIncludingThisHole());
    }
    return cellid;
  }

  Hole getEnclosingHole(int cellId) {
    Hole h = null;
    Hole hole = new Hole(cellId, cellId);
    h = holeTree.ceiling(hole);
    return h;
    /*
     * if(h.getEnd() >= cellId && h.getStart() <= cellId ) { return h ; } return
     * null;
     */
  }

  public boolean removeHole(Hole h) {
    // / ZZZ System.out.println("remove Hole");
    boolean flag = holeTree.remove(h);
    // / ZZZ System.out.println("remove Hole2");
    if (flag) {
      Hole higher = h;
      while ((higher = holeTree.higher(higher)) != null) {
        // // ZZZZ System.out.println("remove Hole3");
        higher.setTotalSizeTillThisHole((higher.getTotalSizeTillThisHole() - h.getSize()));
      }
    }
    return flag;
  }

  public int resolvePhysicalCell(int physicalCellId, Hole prevH, Hole nextH) {

    // Hole prevH=null;
    // Hole nextH=null;
    int i = 0;
    for (Iterator<Hole> iterator = holeTree.iterator(); iterator.hasNext();) {

      Hole cur = iterator.next();

      if (cur.getEnd() - cur.getTotalSizeIncludingThisHole() > physicalCellId) {
        // System.out.println("nextH before assign:"+nextH);

        nextH.copyFrom(cur);
        // nextH =cur ;
        // System.out.println("nextH"+nextH);
        return i + physicalCellId;

        // return h;
      } else {
        i = cur.getTotalSizeIncludingThisHole();
        prevH.copyFrom(cur);
        // System.out.println("prevH"+prevH);
      }
    }
    return physicalCellId + i;

  }

  public boolean expand(int start, int end, boolean canBuildNew) {
    // ZZZZ System.out.println("Expand hole call + start:"+start+", end:"+end);
    boolean ableToAddInHole = false;
    Hole next = holeTree.higher(new Hole(start, start));
    Hole prev = holeTree.lower(new Hole(end, end));
    if (next != null && prev != null && start - prev.getEnd() == 1 && next.getStart() - end == 1) {
      // ZZZZ System.out.println("Expand hole call1");
      this.removeHole(prev);
      this.removeHole(next);
      this.add(prev.getStart(), next.getEnd());
      ableToAddInHole = true;
    } else if (prev != null && start - prev.getEnd() == 1) {
      // ZZZZ System.out.println("Expand hole call2");
      this.removeHole(prev);
      this.add(prev.getStart(), end);
      ableToAddInHole = true; // merge in prev
    } else if (next != null && next.getStart() - end == 1) {
      // ZZZZ System.out.println("Expand hole call31");
      this.removeHole(next);
      // ZZZZ System.out.println("Expand hole call32");
      this.add(start, next.getEnd());
      // ZZZZ System.out.println("Expand hole call33");
      ableToAddInHole = true; // merge in next
    } else if (canBuildNew) { // add as new
      // ZZZZ System.out.println("Expand hole call4");
      ableToAddInHole = true;
      this.add(start, end);
    }

    return ableToAddInHole;
  }

  public boolean isEmpty() {
    return holeTree.isEmpty();
  }

  public String toString() {
    String ret = "";
    for (Iterator iterator = holeTree.iterator(); iterator.hasNext();) {
      Hole type = (Hole) iterator.next();
      ret += type.toString() + "\n";
      // System.out.println(type);

    }
    return ret;

  }

  public static void main(String args[]) {
    HoleHandler hh = new HoleHandler();

    hh.add(39, 39);
    hh.add(40, 63);
    hh.add(128, 151);
    hh.add(64, 115);

    // ZZZZ System.out.println(hh);
    // Hole h = hh.resolvePhysicalCell(48) ;
    Hole h1, h2;
    h1 = new Hole(3, 1000);
    h2 = new Hole(100, 444);
    int i = hh.resolvePhysicalCell(47, h1, h2);

    // ZZZZ System.out.println(i);
    // ZZZZ System.out.println(h1);
    // ZZZZ System.out.println(h2);
    // System.out.println(h);
    System.exit(1);

    // 1 and 5 in hole
    /*
     * Hole h = new Hole(1, 5) ; Hole h2 = new Hole(11, 15) ; Hole h3 = new
     * Hole(21, 35) ; Hole h4 = new Hole(36, 45) ;
     */
    // hh.add(0,2000) ;
    hh.add(1, 5);

    hh.add(21, 35);
    hh.add(36, 45);
    hh.add(11, 15);
    hh.add(13, 14);

    // hh.getEnclosingHole(33)
    // ZZZZ System.out.println("Enclosing 21  :" +hh.getEnclosingHole(21));

    // ZZZZ System.out.println("Enclosing  35 :" +hh.getEnclosingHole(35));
    // ZZZZ System.out.println("Enclosing 23  :" +hh.getEnclosingHole(33));
    // ZZZZ System.out.println("Enclosing 14  :" +hh.getEnclosingHole(14));

    // ZZZZ System.out.println("Enclosing 100000  :"
    // +hh.getEnclosingHole(1000000));

    /*
     * hh.add(h2) ; hh.add(h4) ; hh.add(h3) ; hh.add(h) ;
     */
    System.out.println("HETETETETTETE");
    System.out.println(hh.isInHole(1000000));
    System.out.println(hh.isInHole(6));
    System.out.println(hh.isInHole(4));
    System.out.println(hh);
    System.out.println("reolvecell:" + hh.resolveCell(0));
    System.out.println("reolvecell:" + hh.resolveCell(3));
    System.out.println("reolvecell:" + hh.resolveCell(20));
    System.out.println("reolvecell:" + hh.resolveCell(35));
    System.out.println("reolvecell:" + hh.resolveCell(43));
    System.out.println("reolvecell:" + hh.resolveCell(1000));
    /*
     * 1 -- 5[holeSize:5] [totHoleSizeTillThisHole:0] 11 -- 15[holeSize:5]
     * [totHoleSizeTillThisHole:5] 21 -- 35[holeSize:15]
     * [totHoleSizeTillThisHole:10] 36 -- 45[holeSize:10]
     * [totHoleSizeTillThisHole:25] reolvecell:0 reolvecell:3 reolvecell:10 0 6
     * 7 8 9 10 16 17 18 19 20 0 1 2 3 4 5 6 7 8 9 10 reolvecell:25
     * reolvecell:18 reolvecell:965
     */
    int resolved = HoleHandler.CELL_IN_HOLE;
    int cellid = 43;
    if ((resolved = hh.resolveCell(cellid)) == HoleHandler.CELL_IN_HOLE) {
      // put in auxfile

    } else {
      // put in main file
    }
    /*
     * 1 -- 5[holeSize:5] [totHoleSizeTillThisHole:0] 11 -- 15[holeSize:5]
     * [totHoleSizeTillThisHole:5] 21 -- 35[holeSize:15]
     * [totHoleSizeTillThisHole:10] 36 -- 45[holeSize:10]
     * [totHoleSizeTillThisHole:25] reolvecell:0 reolvecell:-1 reolvecell:10
     * reolvecell:-1 reolvecell:-1 reolvecell:965
     */

  }
}
