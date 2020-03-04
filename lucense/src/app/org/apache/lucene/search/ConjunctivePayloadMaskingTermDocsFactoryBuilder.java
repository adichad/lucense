/*
 * @(#)org.apache.lucene.search.ConjunctivePayloadMaskingTermDocsFactoryBuilder.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package org.apache.lucene.search;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositions;

public class ConjunctivePayloadMaskingTermDocsFactoryBuilder extends TermDocsFactoryBuilder {

  @Override
  public TermDocsFactory decode(DataInputStream dis) throws IOException {
    return new ConjunctivePayloadMaskingTermDocsFactory(dis.readByte());
  }

  private class ConjunctivePayloadMaskingTermDocsFactory extends TermDocsFactory {

    private byte mask;

    ConjunctivePayloadMaskingTermDocsFactory(byte mask) {
      this.mask = mask;
    }

    @Override
    public TermDocs wrapTermDocs(TermDocs termDocs) {
      if (termDocs instanceof TermPositions) {
        return new SelectedTermPositions((TermPositions) termDocs) {

          @Override
          protected boolean selectPosition(int doc, int freq, int pos, byte[] payload, int payloadLen) {
            if (payloadLen > 0) {
              return (payload[0] & mask) == 0;
            }
            return true;
          }

          @Override
          protected boolean selectPosition(int doc, int freq, int pos) {
            return true;
          }

          @Override
          protected boolean select(int doc, int freq, Iterator<Position> positions) {
            while (positions.hasNext()) {
              Position p = positions.next();
              if (selectPosition(doc, freq, p.pos(), p.payload(), p.payloadLen())) {
                return true;
              }
            }
            return false;
          }

          @Override
          protected boolean select(int doc, int freq) {
            return true;
          }
        };
      } else {
        return new SelectedTermDocs(termDocs) {

          @Override
          protected boolean selectPosition(int doc, int freq, int pos, byte[] payload, int payloadLen) {
            if (payloadLen > 0) {
              return (payload[0] & mask) == 0;
            }
            return true;
          }

          @Override
          protected boolean selectPosition(int doc, int freq, int pos) {
            return true;
          }

          @Override
          protected boolean select(int doc, int freq, Iterator<Position> positions) {
            while (positions.hasNext()) {
              Position p = positions.next();
              if (selectPosition(doc, freq, p.pos(), p.payload(), p.payloadLen())) {
                return true;
              }
            }
            return false;
          }

          @Override
          protected boolean select(int doc, int pos) {
            return true;
          }

        };
      }

    }

  }

}
