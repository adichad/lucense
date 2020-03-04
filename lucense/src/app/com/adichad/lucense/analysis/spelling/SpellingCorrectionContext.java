package com.adichad.lucense.analysis.spelling;

import java.util.Map.Entry;

import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

public class SpellingCorrectionContext {

  public static class CorrectionContextEntry {
    String term;

    int pos;

    public CorrectionContextEntry(String term, int pos) {
      this.term = term;
      this.pos = pos;
    }

    /*
     * public void setTerm(String term) { this.term = term; } public void
     * setPos(int pos) { this.pos = pos; }
     */
    public String getTerm() {
      return this.term;
    }

    public int getPos() {
      return this.pos;
    }

    @Override
    public String toString() {
      return this.term + "[" + this.pos + "]";
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof CorrectionContextEntry)
        if ((((CorrectionContextEntry) o).pos == this.pos) && ((CorrectionContextEntry) o).term.equals(this.term))
          return true;
      return false;
    }

    @Override
    public int hashCode() {
      return this.term.hashCode() ^ this.pos;
    }

  }

  // private final TermSequenceGraph g;
  private final TermAttribute tAttr;

  private final PositionIncrementAttribute posIncrAttr;

  private int lastPos;

  private CorrectionContextEntry curr, next, prev, prevPrev;

  private Entry<String, Double> correctedPrev;

  public SpellingCorrectionContext(TermSequenceGraph g, TermAttribute tAttr, PositionIncrementAttribute posIncrAttr) {
    // this.g = g;
    this.tAttr = tAttr;
    this.posIncrAttr = posIncrAttr;
    this.curr = this.next = this.prev = this.prevPrev = null;
    this.correctedPrev = null;
    this.lastPos = 0;
  }

  public void incrementToken() {
    int posIncr = this.posIncrAttr.getPositionIncrement();
    if (this.curr == null) {
      this.curr = new CorrectionContextEntry(this.tAttr.term(), this.lastPos);
    } else if (this.next == null) {
      this.next = new CorrectionContextEntry(this.tAttr.term(), this.lastPos);
    } else {
      this.prevPrev = this.prev;
      this.prev = this.curr;
      this.curr = this.next;
      this.next = new CorrectionContextEntry(this.tAttr.term(), this.lastPos);
    }
    this.lastPos += posIncr;
  }

  public CorrectionContextEntry getCurrent() {
    return this.curr;
  }

  public CorrectionContextEntry getPrevious() {
    return this.prev;
  }

  public CorrectionContextEntry getPrevPrev() {
    return this.prevPrev;
  }

  public CorrectionContextEntry getNext() {
    return this.next;
  }

  @Override
  public String toString() {
    String str = new String();
    if (this.prevPrev != null) {
      str = this.prevPrev.term + ">" + this.prev.term + ">[" + this.curr.term + "]";
    } else if (this.prev != null) {
      str = this.prev.term + ">[" + this.curr.term + "]";
    } else if (this.curr != null) {
      str = "[" + this.curr.term + "]";
    }
    if (this.next != null) {
      str += ">" + this.next.term;
    }
    return str;
  }

  public void setPreviousCorrection(Entry<String, Double> correctedPrev) {
    this.correctedPrev = correctedPrev;
  }

  public Entry<String, Double> getPreviousCorrection() {
    return this.correctedPrev;
  }

}
