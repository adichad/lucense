/*
 * @(#)org.apache.lucene.search.SelectedTermDocs.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package org.apache.lucene.search;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermPositions;

public abstract class SelectedTermDocs implements TermDocs {

  protected TermDocs termDocs;

  private int currFreq;

  private int currDoc;

  protected PositionList positions = new PositionList();

  protected class PositionList {
    private Position head, tail;

    private int size;

    public PositionList() {
      head = null;
      tail = null;
      size = 0;
    }

    public void append(Position pos) {
      if (tail == null) {
        head = tail = pos;
      } else {
        tail.next = pos;
        tail = pos;
      }
      ++size;
    }

    public Position removeHead() {
      if (head == null)
        return head;
      if (head == tail) {
        Position temp = head;
        head = null;
        tail = null;
        size = 0;
        return temp;
      }
      Position temp = head;
      head = head.next;
      --size;
      return temp;
    }

    public void clear() {
      head = null;
      tail = null;
      size = 0;
    }

    public Iterator<Position> iterator() {
      return new Iterator<Position>() {
        Position current = head;

        @Override
        public boolean hasNext() {
          return current != null;
        }

        @Override
        public Position next() {
          Position temp = current;
          current = current.next;
          return temp;
        }

        @Override
        public void remove() {}

      };
    }
  }

  protected class Position {
    private int pos;

    private byte[] payload;

    private Position next;

    private int payloadLen;

    public Position(int pos, byte[] payload, int payloadLen) {
      this.pos = pos;
      this.payload = payload;
      this.payloadLen = payloadLen;
      this.next = null;
    }

    public int pos() {
      return pos;
    }

    public int payloadLen() {
      return payloadLen;
    }

    public byte[] payload() {
      return payload;
    }

  }

  public SelectedTermDocs(TermDocs termDocs) {
    this.termDocs = termDocs;
  }

  public void seek(Term term) throws IOException {
    this.termDocs.seek(term);
  }

  public void seek(TermEnum termEnum) throws IOException {
    this.termDocs.seek(termEnum);
  }

  public int freq() {
    return this.currFreq;
  }

  public boolean next() throws IOException {
    int nextDoc = -1;
    do {
      if (!termDocs.next())
        return false;
      nextDoc = termDocs.doc();
    } while (!selectDoc(termDocs));
    return nextDoc != -1;
  }

  protected boolean selectDoc(TermDocs termDocs) throws IOException {
    this.currDoc = termDocs.doc();
    this.currFreq = termDocs.freq();

    if (termDocs instanceof TermPositions) {
      TermPositions termPositions = (TermPositions) termDocs;
      this.positions.clear();
      for (int i = 0; i < currFreq; i++) {
        int cpos = termPositions.nextPosition();
        Position pos;
        if (termPositions.isPayloadAvailable()) {
          int payloadLen = termPositions.getPayloadLength();
          byte[] payload = termPositions.getPayload(null, 0);
          if (selectPosition(currDoc, currFreq, cpos, payload, payloadLen)) {
            pos = new Position(cpos, payload, payloadLen);
            positions.append(pos);
          }
        } else {
          if (selectPosition(currDoc, currFreq, cpos)) {
            pos = new Position(cpos, null, 0);
            positions.append(pos);
          }
        }
      }
      this.currFreq = positions.size;
      return select(currDoc, currFreq, positions.iterator());
    } else {
      return select(currDoc, currFreq);
    }
  }

  public boolean skipTo(int target) throws IOException {
    int nextDoc = -1;
    if (termDocs.skipTo(target)) {
      if (!selectDoc(termDocs)) {
        if (next())
          return false;
      }
      nextDoc = termDocs.doc();
    }
    return nextDoc != -1;
  }

  protected abstract boolean select(int doc, int freq, Iterator<Position> positions);

  protected abstract boolean select(int doc, int freq);

  protected boolean selectPosition(int doc, int freq, int pos, final byte[] payload, int payloadLen) {
    return true;
  }

  protected boolean selectPosition(int doc, int freq, int pos) {
    return true;
  }

  @Override
  public int doc() {
    return currDoc;
  }

  @Override
  public int read(int[] docs, int[] freqs) throws IOException {
    int count;
    for (count = 0; count < docs.length && next(); count++) {
      docs[count] = doc();
      freqs[count] = freq();
    }
    return count;
  }

  @Override
  public void close() throws IOException {
    termDocs.close();
  }
}
