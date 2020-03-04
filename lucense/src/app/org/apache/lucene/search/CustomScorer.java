package org.apache.lucene.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class CustomScorer extends Scorer {

  public CustomScorer(Similarity similarity) {
    super(similarity);
  }

  protected Map<String, Integer> currFieldLCS;

  protected int curLCSDoc = -1;

  Map<String, MatchTermPositions> mtp = null;

  private Map<String, Integer> currNumwords;

  private int curNumwordsDoc = -1;

  private Map<String, Boolean> currFieldIsExact;

  private int curIsExactDoc = -1;

  private Map<String, Boolean> currFieldIsAll;

  private int curIsAllDoc = -1;

  public Map<String, Integer> numwords(Set<String> scoreFields) throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curNumwordsDoc) {
      this.currNumwords = new HashMap<String, Integer>();
      this.curNumwordsDoc = doc;
      int max = 0;
      for (String field : this.mtp.keySet()) {
        MatchTermPositions pos = this.mtp.get(field);
        if (!pos.alignDoc(this.docID())) {
          this.currNumwords.put(field, 0);
        } else {
          this.currNumwords.put(field, pos.initHeapEnd);
        }
        if (pos.initHeapEnd > max)
          max = pos.initHeapEnd;
      }
      this.currNumwords.put("@max", max);
    }
    return this.currNumwords;
  }

  public abstract void matchedTermPositions(Map<String, MatchTermPositions> mtp, Set<String> scoreFields);

  public Map<String, Integer> lcsLength(Set<String> scoreFields) throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curLCSDoc) {
      this.curLCSDoc = doc;
      this.currFieldLCS = new HashMap<String, Integer>();
      int maxLCS = 0;
      for (String field : this.mtp.keySet()) {
        MatchTermPositions pos = this.mtp.get(field);
        int currLCS;
        if (!pos.alignDoc(doc)) {
          currLCS = 0;
          // System.out.println("ERROR aligning doc: "+doc+" for field: "+field);
        } else {
          currLCS = pos.maxLCSLen();
        }
        this.currFieldLCS.put(field, currLCS);
        if (maxLCS < currLCS) {
          maxLCS = currLCS;
        }
      }
      this.currFieldLCS.put("@max", maxLCS);
    }

    return this.currFieldLCS;
  }

  public Map<String, Boolean> isExact(Set<String> scoreFields) throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curIsExactDoc) {
      this.curIsExactDoc = doc;
      this.currFieldIsExact = new HashMap<String, Boolean>();
      // boolean anyFieldIsExact = false ;
      for (String field : this.mtp.keySet()) {
        MatchTermPositions pos = this.mtp.get(field);
        boolean isExact = false;

        if (pos.alignDoc(doc) // docfield is a match
            && (pos.pos.size() == pos.initHeapEnd) // docfield is all words
                                                   // match
            && (pos.maxLCSLen() == pos.pos.size()) // docfield is exact match
        ) {
          isExact = true;
        }
        this.currFieldIsExact.put(field, isExact);
      }
    }
    return this.currFieldIsExact;
  }

  public Map<String, Boolean> isAll(Set<String> scoreFields) throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curIsAllDoc) {
      this.curIsAllDoc = doc;
      this.currFieldIsAll = new HashMap<String, Boolean>();
      for (String field : this.mtp.keySet()) {
        MatchTermPositions pos = this.mtp.get(field);
        boolean isAll = false;
        if (pos.alignDoc(doc) // docfield is a match
            && (pos.pos.size() == pos.initHeapEnd) // docfield is all words
                                                   // match
        ) {
          isAll = true;
        }
        this.currFieldIsAll.put(field, isAll);
      }
      // System.out.println(doc+": "+currFieldIsAll);
    }

    return this.currFieldIsAll;
  }
}

class MatchTermPos implements Comparable<MatchTermPos> {
  int qpos;

  TermPositions dpos;

  Term term;

  int currdpos;

  int currdposPos;

  public MatchTermPos(int qpos, TermPositions dpos, Term term) {
    this.qpos = qpos;
    this.dpos = dpos;
    this.term = term;
    this.currdposPos = 0;
  }

  public boolean nextPosition() throws IOException {
    if (this.currdposPos < this.dpos.freq()) {
      this.currdpos = this.dpos.nextPosition();
      ++this.currdposPos;
      return true;
    }
    return false;
  }

  public int freq() {
    return this.dpos.freq();
  }

  public int doc() {
    return this.dpos.doc();
  }

  @Override
  public String toString() {
    return this.term + ": qpos=" + this.qpos + "; dpos=" + this.currdpos;
  }

  @Override
  public int compareTo(MatchTermPos arg) {
    if (this.qpos - this.currdpos > arg.qpos - arg.currdpos)
      return -1;
    if (this.qpos - this.currdpos < arg.qpos - arg.currdpos)
      return 1;
    if (this.currdpos > arg.currdpos)
      return 1;
    if (this.currdpos < arg.currdpos)
      return -1;
    if (this.qpos > arg.qpos)
      return -1;
    if (this.qpos < arg.qpos)
      return 1;
    return 0;
  }

}

class MatchTermPositions {
  ObjectArrayList<MatchTermPos> pos;

  int heapEnd = 0;

  private int doc = -1;

  int initHeapEnd = 0;

  MatchTermPositions(int size) {
    this.pos = new ObjectArrayList<MatchTermPos>(size);
  }

  MatchTermPositions() {
    this.pos = new ObjectArrayList<MatchTermPos>();
  }

  public void add(int qpos, TermPositions dpos, Term term) {
    // System.out.println("adding "+term+", qpos="+qpos+", dpos="+dpos);
    this.pos.add(new MatchTermPos(qpos, dpos, term));
  }

  public boolean alignDoc(int doc) throws IOException {
    if (doc == this.doc)
      return true;
    this.doc = doc;
    this.heapEnd = 0;
    for (int i = 0; i < this.pos.size(); ++i) {
      MatchTermPos p = this.pos.get(i);
      if (advanceToDoc(doc, p)) {
        if (i != this.heapEnd) {
          this.pos.set(i, this.pos.get(this.heapEnd));
          this.pos.set(this.heapEnd, p);
        }
        if (p.nextPosition()) {
          bubbleUp();
          ++this.heapEnd;
        }
      } else {
        // set isExact = false
        // p.currdpos = -1;
      }

    }
    // System.out.println("pos after init: "+pos);
    this.initHeapEnd = this.heapEnd;

    if (this.heapEnd > 0)
      return true;
    return false;
  }

  protected void bubbleUp() throws IOException {
    MatchTermPos cpos = this.pos.get(this.heapEnd);
    int i = this.heapEnd;
    int j = (i - 1) >> 1;
    while ((i > 0) && (this.pos.get(j).compareTo(cpos) > 0)) {
      this.pos.set(i, this.pos.get(j));
      this.pos.set(j, cpos);
      i = j;
      j = (i - 1) >> 1;
    }
  }

  protected void bubbleDown() {
    MatchTermPos cpos = this.pos.get(0);
    int i = 0;
    int j = 1;
    while (j < this.heapEnd) {
      if (cpos.compareTo(this.pos.get(j)) > 0) {
        if ((j + 1) < this.heapEnd) {
          if (this.pos.get(j).compareTo(this.pos.get(j + 1)) > 0) {
            this.pos.set(i, this.pos.get(j + 1));
            this.pos.set(j + 1, cpos);
            i = j + 1;
          } else {
            this.pos.set(i, this.pos.get(j));
            this.pos.set(j, cpos);
            i = j;
          }
        } else {
          this.pos.set(i, this.pos.get(j));
          this.pos.set(j, cpos);
          i = j;
        }
      } else if ((j + 1) < this.heapEnd) {
        if (cpos.compareTo(this.pos.get(j + 1)) > 0) {
          this.pos.set(i, this.pos.get(j + 1));
          this.pos.set(j + 1, cpos);
          i = j + 1;
        } else
          break;
      } else
        break;
      j = (i << 1) + 1;
    }
  }

  protected MatchTermPos pop() {
    if (this.heapEnd == 0)
      return null;
    MatchTermPos cpos = this.pos.get(0);
    this.pos.set(0, this.pos.get(--this.heapEnd));
    bubbleDown();
    return cpos;
  }

  protected void push(MatchTermPos cpos) throws IOException {
    this.pos.set(this.heapEnd, cpos);
    bubbleUp();
    ++this.heapEnd;
  }

  protected boolean advanceToDoc(int doc, MatchTermPos pos) throws IOException {
    TermPositions dpos = pos.dpos;

    while (((dpos.doc() < doc) || (dpos.freq() == 0)) && dpos.next()) {
      if (dpos.doc() == doc) {
        pos.currdposPos = 0;
        pos.currdpos = -1;
        return true;
      }
    }
    if (dpos.doc() == doc) {
      pos.currdposPos = 0;
      pos.currdpos = -1;
      return true;
    }

    return false;
  }

  protected int currLCSLen(int i) {
    if (i < this.initHeapEnd - 1) {
      // System.out.println("checking link "+i+": next.qpos="+pos.get(i+1).qpos+", this.qpos="+pos.get(i).qpos+", next.dpos="+pos.get(i+1).currdpos+", this.dpos="+pos.get(i).currdpos);
      if ((this.pos.get(i + 1).qpos - this.pos.get(i).qpos) == (this.pos.get(i + 1).currdpos - this.pos.get(i).currdpos)) {
        // System.out.println("+1");
        return 1 + currLCSLen(i + 1);
      } else {
        return currLCSLen(i + 1);
      }
    }
    if (i == this.initHeapEnd - 1)
      return 1;
    return 0;
  }

  protected int currLCSLen() {
    return currLCSLen(0);
  }

  public int numwords() {
    return this.heapEnd;
  }

  public int maxLCSLen() throws IOException {
    int mlcs = currLCSLen();
    if (mlcs == this.initHeapEnd) {
      return this.initHeapEnd;
    }

    MatchTermPos p = pop();

    while (p != null) {
      if (p.nextPosition())
        push(p);
      else
        this.pos.set(this.heapEnd, p);
      int clcs = currLCSLen();

      if (clcs == this.initHeapEnd)
        return this.initHeapEnd;
      if (clcs > mlcs)
        mlcs = clcs;
      p = pop();
    }

    return mlcs;
  }

}
