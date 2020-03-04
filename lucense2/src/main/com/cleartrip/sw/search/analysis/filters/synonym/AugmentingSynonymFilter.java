package com.cleartrip.sw.search.analysis.filters.synonym;

import java.util.Set;

import org.apache.lucene.analysis.TokenStream;

public class AugmentingSynonymFilter extends SynonymFilter {

  public AugmentingSynonymFilter(TokenStream in, SynonymMap<String, String> map) {
    super(in, map);

  }

  protected boolean emit(Set<EmitEntry> limit, int incrPos) {
    if (this.emitBuffer.isEmpty())
      return false;
    Set<EmitEntry> currList = this.emitBuffer.peekFirst();
    if (currList == limit)
      return false;

    if (currList.isEmpty()) {
      this.emitBuffer.poll();
      this.startOffsetBuffer.poll();
      this.endOffsetBuffer.poll();
      if (this.emitBuffer.isEmpty() || (this.emitBuffer.peekFirst() == limit))
        return false;
      currList = this.emitBuffer.peekFirst();
      incrPos = 1;
    }

    EmitEntry w = currList.iterator().next();
    this.termAtt.setEmpty().append(w.toString());
    this.offAtt.setOffset(this.startOffsetBuffer.peekFirst(), this.endOffsetBuffer.peekFirst());
    currList.remove(w);
    this.posAtt.setPositionIncrement(incrPos);
    // System.out.println("Emitting : "+w.toString()+" POSN : "+incrPos);
    return true;
  }

}
