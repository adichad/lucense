package com.adichad.lucense.analysis.synonym;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;

public class ReplacingSynonymFilter extends SynonymFilter {

  LinkedList<EmitEntry> finalOut = new LinkedList<EmitEntry>();

  LinkedList<Integer> endOffsetFinalOut = new LinkedList<Integer>();

  LinkedList<Integer> startOffsetFinalOut = new LinkedList<Integer>();

  int slop = 0;

  public ReplacingSynonymFilter(TokenStream in, SynonymMap<String, String> map) {
    super(in, map);
  }

  public int getSlop() {
    return slop;
  }

  @Override
  protected boolean emit(Set<EmitEntry> limit, int incrPos) {
    if (this.emitBuffer.isEmpty() || this.emitBuffer.peekFirst() == limit) {
      if (this.finalOut.isEmpty())
        return false;
      else {
        if (!finalOut.isEmpty()) {
          EmitEntry w = finalOut.removeFirst();
          int startOffset = startOffsetFinalOut.removeFirst();
          int endOffset = endOffsetFinalOut.removeFirst();
          this.termAtt.setEmpty().append(w.label);
          this.offAtt.setOffset(startOffset, endOffset);
          this.posAtt.setPositionIncrement(1);
          return true;
        } else {
          return false;
        }
      }
    } else {
      LinkedHashSet<EmitEntry> currList;
      int i = 0;
      while (!this.emitBuffer.isEmpty() && this.emitBuffer.peekFirst() != limit) {
        currList = this.emitBuffer.removeFirst();
        int startOffset = startOffsetBuffer.removeFirst();
        int endOffset = endOffsetBuffer.removeFirst();
        EmitEntry w = getMaxStartingEntry(currList);
        if (finalOut.isEmpty() || finalOut.peekLast().span < (w.span + i)) {
          finalOut.add(w);
          startOffsetFinalOut.add(startOffset);
          endOffsetFinalOut.add(endOffset);
        }
        i++;
      }
      if (!finalOut.isEmpty()) {
        EmitEntry w = finalOut.removeFirst();
        int startOffset = startOffsetFinalOut.removeFirst();
        int endOffset = endOffsetFinalOut.removeFirst();
        this.termAtt.setEmpty().append(w.label);
        this.offAtt.setOffset(startOffset, endOffset);
        this.posAtt.setPositionIncrement(1);
        return true;
      } else {
        return false;
      }
    }
  }

  protected void processNextEmission(EmitEntry item) {

  }

  protected boolean vomit(EmitEntry w) {
    this.termAtt.setEmpty().append(w.label);
    this.offAtt.setOffset(this.startOffsetBuffer.peekFirst(), this.endOffsetBuffer.peekFirst());

    this.posAtt.setPositionIncrement(1);
    this.emitBuffer.removeFirst();
    this.endOffsetBuffer.removeFirst();
    this.startOffsetBuffer.removeFirst();

    return true;
  }

  protected EmitEntry getMaxStartingEntry(LinkedHashSet<EmitEntry> currList) {
    int maxSpan = -1;
    EmitEntry maxEntry = null;
    if (hasStartingSplWord(currList)) {
      EmitEntry orig = currList.iterator().next();
      currList.remove(orig);
    }
    for (EmitEntry e : currList) {
      if (e.starting && maxSpan < e.span) {
        maxSpan = e.span;
        maxEntry = e;
      }
    }
    return maxEntry;
  }

  private boolean hasStartingSplWord(LinkedHashSet<EmitEntry> currList) {
    if (currList.isEmpty() || currList.size() == 1)
      return false;
    Iterator<EmitEntry> it = currList.iterator();
    it.next();
    while (it.hasNext()) {
      if (it.next().starting)
        return true;
    }
    return false;
  }
}
