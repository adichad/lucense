package org.apache.lucene.search;

import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositions;

public class ConjunctivePayloadMaskingTermDocsFactory extends TermDocsFactory {

  private final byte mask;
  private final int  payloadOffset;

  public ConjunctivePayloadMaskingTermDocsFactory(Map<String, ?> params) {
    super(params);
    this.mask = (byte)((int)((Integer) params.get("mask")));
    this.payloadOffset = (Integer) params.get("payloadOffset");
  }

  @Override
  public TermDocs wrapTermDocs(TermDocs termDocs) {
    if (termDocs instanceof TermPositions) {
      return new SelectedTermPositions((TermPositions) termDocs) {

        @Override
        protected boolean selectPosition(int doc, int freq, int pos,
            byte[] payload, int payloadLen) {
          if (payloadLen > payloadOffset) {
            return (payload[payloadOffset] & mask) == 0;
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
        protected boolean selectPosition(int doc, int freq, int pos,
            byte[] payload, int payloadLen) {
          if (payloadLen > payloadOffset) {
            return (payload[payloadOffset] & mask) == 0;
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
