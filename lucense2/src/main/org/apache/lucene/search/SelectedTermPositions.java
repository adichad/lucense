/*
 * @(#)org.apache.lucene.search.SelectedTermPositions.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package org.apache.lucene.search;

import java.io.IOException;

import org.apache.lucene.index.TermPositions;

public abstract class SelectedTermPositions extends SelectedTermDocs implements TermPositions {

  private Position currentPosition;

  public SelectedTermPositions(TermPositions termPositions) {
    super(termPositions);
  }

  @Override
  protected abstract boolean selectPosition(int doc, int freq, int pos, byte[] payload, int payloadLen);

  @Override
  protected abstract boolean selectPosition(int doc, int freq, int pos);

  @Override
  public int nextPosition() {
    this.currentPosition = positions.removeHead();
    return currentPosition.pos();
  }

  @Override
  public int getPayloadLength() {
    return this.currentPosition.payloadLen();
  }

  @Override
  public byte[] getPayload(byte[] data, int offset) throws IOException {
    return this.currentPosition.payload();
  }

  @Override
  public boolean isPayloadAvailable() {
    return this.currentPosition.payloadLen() > 0;
  }

}
