package org.apache.lucene.search;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;

public abstract class CustomScorer extends Scorer {

  protected CustomScorer(Similarity similarity) {
    super(similarity);
    // TODO Auto-generated constructor stub
  }

  protected Object2IntOpenHashMap<String>     currFieldLCS;

  protected int                               curLCSDoc                  = -1;

  Map<String, MatchTermPositions>             mtp                        = null;
  Map<String, MatchTermPositions>             femtp                        = null;
  Map<String, MatchTermPositions>             fe2mtp                        = null;
  private Object2IntOpenHashMap<String>       currNumwords;

  private int                                 curNumwordsDoc             = -1;

  private Object2BooleanOpenHashMap<String>   currFieldIsExact;

  private int                                 curIsExactDoc              = -1;

  private HashMap<String, MatchTermPositions> currFieldIsPartQueryFullExact;

  private int                                 curIsPartQueryFullExactDoc = -1;

  private Object2BooleanOpenHashMap<String>   currFieldIsFullExact;

  private int                                 curIsFullExactDoc          = -1;

  private Object2BooleanOpenHashMap<String>   currFieldIsAll;

  private int                                 curIsAllDoc                = -1;

  private HashMap<String, MatchTermPositions> currFieldIsAllExt;

  private int                                 curIsAllExtDoc             = -1;

  private int                                 bpmDoc                     = -1;

  private Object2IntOpenHashMap<String>       currFieldBPM;

  public int numwords(Set<String> scoreFields, String getfield)
      throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curNumwordsDoc) {
      this.currNumwords = new Object2IntOpenHashMap<String>();
      this.curNumwordsDoc = doc;
      int max = 0;
      for (String field : this.mtp.keySet()) {
        MatchTermPositions pos = this.mtp.get(field);
        if (!pos.alignDoc(this.docID())) {
          this.currNumwords.put(field, 0);
        } else {
          this.currNumwords.put(field, pos.currmpos.size());
        }
        if (pos.currmpos.size() > max)
          max = pos.currmpos.size();
      }
      this.currNumwords.put("@max", max);
    }
    return this.currNumwords.getInt(getfield);
  }

  public abstract void matchedTermPositions(
      Map<String, MatchTermPositions> mtp, Set<String> scoreFields) throws IOException;

  public int lcsLength(Set<String> scoreFields, String getfield)
      throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curLCSDoc) {
      this.curLCSDoc = doc;
      this.currFieldLCS = new Object2IntOpenHashMap<String>();
      int maxLCS = 0;
      for (String field : this.mtp.keySet()) {
        MatchTermPositions pos = this.mtp.get(field);
        int currLCS;
        if (!pos.alignDoc(doc)) {
          // System.out.println("currlcs("+field+"): 0");
          currLCS = 0;
          // System.out.println("ERROR aligning doc: "+doc+" for field: "+field);
        } else {
          currLCS = pos.maxLCSLen();
          // System.out.println("currlcs("+field+"): "+currLCS);
        }
        this.currFieldLCS.put(field, currLCS);
        if (maxLCS < currLCS) {
          maxLCS = currLCS;
        }
      }
      this.currFieldLCS.put("@max", maxLCS);
    }

    return this.currFieldLCS.getInt(getfield);
  }

  public int boostAtMinPos(Set<String> scoreFields, String getfield,
      int payloadPos) throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    MatchTermPositions pos = this.mtp.get(getfield);
    if (pos == null)
      return 0;

    int doc = this.docID();
    if (doc != this.bpmDoc) {
      this.bpmDoc = doc;
      this.currFieldBPM = new Object2IntOpenHashMap<>();

      // System.out.println("getfield: "+getfield+", scoreFields: "+scoreFields);
      if (!pos.alignDoc(doc)) {
        // System.out.println("no boostminpos for: "+getfield);
        this.currFieldBPM.put(getfield, 0);
      } else {
        // System.out.println("boostminpos for: "+getfield);
        this.currFieldBPM.put(getfield, pos.getBPM(payloadPos));
      }
    }

    // System.out.println(this.currFieldBPM);
    return this.currFieldBPM.getInt(getfield);
  }

  public boolean isExact(Set<String> scoreFields, String getfield)
      throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curIsExactDoc) {
      this.curIsExactDoc = doc;
      this.currFieldIsExact = new Object2BooleanOpenHashMap<String>();
      // boolean anyFieldIsExact = false ;
      for (String field : this.mtp.keySet()) {
        MatchTermPositions pos = this.mtp.get(field);
        boolean isExact = false;

        if (pos.alignDoc(doc) // docfield is a match
            && (pos.posSet.size() == pos.currmpos.size()) // docfield is all
                                                          // words
            // match
            && (pos.maxLCSLen() == pos.posSet.size()) // docfield is exact match
        ) {
          isExact = true;
        }
        this.currFieldIsExact.put(field, isExact);
      }
    }
    return this.currFieldIsExact.getBoolean(getfield);
  }

  public boolean isFullExact(Set<String> scoreFields, String getfield)
      throws IOException {
    if (this.femtp == null) {
      this.femtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.femtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curIsFullExactDoc) {
      this.curIsFullExactDoc = doc;
      this.currFieldIsFullExact = new Object2BooleanOpenHashMap<String>();
      // boolean anyFieldIsExact = false ;
      for (String field : this.femtp.keySet()) {
        MatchTermPositions pos = this.femtp.get(field);
        boolean isExact = false;
        //System.out.println("fullmatch field: "+field);
        if (pos.alignDoc(doc) // docfield is a match
            && (pos.posSet.size() == pos.currmpos.size()) // docfield is all
                                                          // words match
            && pos.isFullMatch() // docfield is exact match
        ) {
          isExact = true;
        }
        this.currFieldIsFullExact.put(field, isExact);
      }
    }
    return this.currFieldIsFullExact.getBoolean(getfield);
  }

  public boolean isFullExact(Set<String> scoreFields, String... getfields)
      throws IOException {
    if (this.fe2mtp == null) {
      this.fe2mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.fe2mtp, scoreFields);
    }
    int doc = this.docID();
    //System.out.println("________________________________________________");
    if (doc != this.curIsPartQueryFullExactDoc) {
      this.curIsPartQueryFullExactDoc = doc;
      this.currFieldIsPartQueryFullExact = new HashMap<>();

      for (String field : this.fe2mtp.keySet()) {
        MatchTermPositions pos = this.fe2mtp.get(field);
        
        //System.out.println("field: "+field+", pos: "+pos);
        if (pos.alignDoc(doc) // docfield is a match
            && (pos.isCandidateFullMatch()) // docfield is exact match
        ) {
          // System.out.println(": added");
          this.currFieldIsPartQueryFullExact.put(field, pos);
        }
        // else { System.out.println(": ignored"); }

      }
    }
    MatchTermPositions pos = null;
    IntOpenHashSet qpos = new IntOpenHashSet();
    IntOpenHashSet mpos = new IntOpenHashSet();
    
    for (String field : getfields) {
      if ((pos = currFieldIsPartQueryFullExact.get(field)) != null) {
        // System.out.println("field: "+field+", posset: "+pos.posSet+", currmpos: "+pos.currmpos);
        qpos.addAll(pos.posSet);
        mpos.addAll(pos.currmpos);
      } else
        return false;
    }
    if (mpos.isEmpty()) {
      return false;
    }
    
    return mpos.size() == qpos.size();

  }

  public boolean isAll(Set<String> scoreFields, String getfield)
      throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curIsAllDoc) {
      this.curIsAllDoc = doc;
      this.currFieldIsAll = new Object2BooleanOpenHashMap<String>();
      for (String field : this.mtp.keySet()) {
        MatchTermPositions pos = this.mtp.get(field);
        boolean isAll = false;
        if (pos.alignDoc(doc) // docfield is a match
            && (pos.posSet.size() == pos.currmpos.size()) // docfield is all
                                                          // words
        // match
        ) {
          isAll = true;
        }

        this.currFieldIsAll.put(field, isAll);
      }
      // System.out.println(doc+": "+currFieldIsAll);
    }

    return this.currFieldIsAll.getBoolean(getfield);
  }

  public boolean isAll(Set<String> scoreFields, String... getfields)
      throws IOException {
    if (this.mtp == null) {
      this.mtp = new HashMap<String, MatchTermPositions>();
      matchedTermPositions(this.mtp, scoreFields);
    }
    int doc = this.docID();
    if (doc != this.curIsAllExtDoc) {
      this.curIsAllExtDoc = doc;
      this.currFieldIsAllExt = new HashMap<String, MatchTermPositions>();
      for (String field : this.mtp.keySet()) {
        MatchTermPositions pos = this.mtp.get(field);
        if (pos.alignDoc(doc)) {// docfield is a match
          currFieldIsAllExt.put(field, pos);
        }
      }
      // System.out.println("________________________________");
    }
    MatchTermPositions pos = null;
    IntOpenHashSet qpos = new IntOpenHashSet();
    IntOpenHashSet mpos = new IntOpenHashSet();
    for (String field : getfields) {
      if ((pos = currFieldIsAllExt.get(field)) != null) {
        qpos.addAll(pos.posSet);
        mpos.addAll(pos.currmpos);
      }
    }
    if (mpos.isEmpty())
      return false;
    return mpos.size() == qpos.size();
  }

}

class MatchTermPos implements Comparable<MatchTermPos> {
  final int     qpos;

  TermPositions dpos;

  Term[]        terms;

  int           currdpos;

  int           currdposPos;
  byte[]        payloadBytes;

  public MatchTermPos(int qpos, TermPositions dpos, Term... terms) {
    this.qpos = qpos;
    this.dpos = dpos;
    this.terms = terms;
    this.currdposPos = 0;
    this.payloadBytes = new byte[] { (byte) 0 };
  }

  public boolean nextPosition() throws IOException {
    if (this.currdposPos < this.dpos.freq()) {
      this.currdpos = this.dpos.nextPosition();
      if (this.dpos.isPayloadAvailable())
        payloadBytes = this.dpos.getPayload(payloadBytes, 0);
      else {
        for (int i = 0; i < payloadBytes.length; ++i)
          payloadBytes[i] = 0;
      }
      ++this.currdposPos;
      //System.out.println(this);
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
    String s = "";
    for (Term term : terms)
      s += term + " ";
    return s + ": qpos=" + this.qpos + "; dpos=" + this.currdpos;
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

  int                           heapEnd     = 0;

  private int                   doc         = -1;
  private boolean               isAligned   = false;

  int                           initHeapEnd = 0;

  IntOpenHashSet                posSet;
  IntOpenHashSet                currmpos;

  private byte                  currDocLen  = 0;

  MatchTermPositions(int size) {
    this.pos = new ObjectArrayList<MatchTermPos>(size);
    posSet = new IntOpenHashSet(size);
    currmpos = new IntOpenHashSet(size);
  }
  
  @Override
  public String toString() {
    return this.pos+": "+posSet+", "+currmpos;
  }

  public int getBPM(int payloadPos) throws IOException {
    // System.out.println(this.pos);
    int bpm = 0;
    int minpos = Integer.MAX_VALUE;
    for (int i = 0; i < initHeapEnd; ++i) {
      MatchTermPos p = pos.get(i);

      int boost = 0;
      do {
        if (p.payloadBytes.length >= payloadPos + 4)
          boost = ByteBuffer.wrap(p.payloadBytes, payloadPos, 4).getInt();
      } while (p.nextPosition() && boost == 0);
      if (p.currdposPos != 0 && p.currdpos < minpos) {
        bpm = boost;
        minpos = p.currdpos;
        // System.out.println(p+": updated boost: "+boost);
      }
    }
    // System.out.println(bpm);
    return bpm;
  }

  MatchTermPositions() {
    this.pos = new ObjectArrayList<MatchTermPos>();
    posSet = new IntOpenHashSet();
    currmpos = new IntOpenHashSet();
  }

  public void add(int qpos, TermPositions dpos, Term... terms) {
    this.pos.add(new MatchTermPos(qpos, dpos, terms));
    posSet.add(qpos);
    /*
     * System.out.print("adding :"); for(Term term: terms) {
     * System.out.print(term+","); }
     * System.out.println(" qpos="+qpos+", dpos="+dpos);
     */
  }

  public boolean alignDoc(int doc) throws IOException {
    if (doc == this.doc) {
      return this.isAligned;
    }
    currmpos.clear();
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
          this.currDocLen = p.payloadBytes[0];
          //System.out.println("doc: "+doc+" matched term: "+p.terms[0]+", cdl: "+this.currDocLen+", dpos: "+p.currdpos);
        }
        //System.out.println(doc+": term: "+p.terms[0]+"("+p.qpos+")");
        currmpos.add(p.qpos);
        // System.out.println("currmpos: "+currmpos);

      } else {
        // System.out.println(doc+": unmatched term: "+p.terms[0]+"("+p.qpos+")");
        // set isExact = false
        // p.currdpos = -1;
      }

    }
    // System.out.println("pos after init: "+pos);
    this.initHeapEnd = this.heapEnd;

    if (this.heapEnd > 0) {
      this.isAligned = true;
      return true;
    }
    this.isAligned = false;
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
    // System.out.print(pos.terms[0]+"("+dpos.doc()+")=>");
    boolean hasNext = true;
    while (((dpos.doc() < doc) || (dpos.freq() == 0)) && (hasNext = dpos.next())) {
      if (dpos.doc() == doc) {
        pos.currdposPos = 0;
        pos.currdpos = -1;
        // System.out.println("Matched: "+pos.terms[0]);
        return true;
      }
    }
    if (hasNext && dpos.doc() == doc && dpos.freq() != 0) {
      pos.currdposPos = 0;
      pos.currdpos = -1;
      // System.out.println("outMatched: "+pos.terms[0]+"("+dpos.doc()+")");
      return true;
    }
    if(!hasNext)
      dpos.close();
    // System.out.println("UNmatched: "+pos.terms[0]);
    return false;
  }

  // protected void sortByQPos(ObjectArrayList<MatchTermPos> pos, int end,
  // MatchTermPos[] qspos) {
  //
  // for (int i = 0; i< end; i++) {
  // qspos[i] = pos.get(i);
  // }
  // ArrayUtil.quickSort(qspos, new Comparator<MatchTermPos>() {
  //
  // @Override
  // public int compare(MatchTermPos o1, MatchTermPos o2) {
  // // TODO Auto-generated method stub
  // return o1.qpos < o2.qpos ? -1 : (o1.qpos == o2.qpos ? 0 : 1);
  // }
  // });
  // }
  //
  // protected void sortByDPos(ObjectArrayList<MatchTermPos> pos, int end,
  // MatchTermPos[] dspos) {
  // for (int i = 0; i< end; i++) {
  // dspos[i] = pos.get(i);
  // }
  // ArrayUtil.quickSort(dspos, new Comparator<MatchTermPos>() {
  //
  // @Override
  // public int compare(MatchTermPos o1, MatchTermPos o2) {
  // // TODO Auto-generated method stub
  // return o1.currdpos < o2.currdpos ? -1 : (o1.currdpos == o2.currdpos ? 0
  // : 1);
  // }
  // });
  //
  // }

  // for longest common substring
  protected int currLCSLen() {
    int len = 1;
    int qdiff;
    int ddiff;

    for (int i = 0; i < this.initHeapEnd - 1; i++) {
      qdiff = (this.pos.get(i + 1).qpos - this.pos.get(i).qpos);
      ddiff = (this.pos.get(i + 1).currdpos - this.pos.get(i).currdpos);
      if (qdiff == ddiff && qdiff != 0) {
        len++;
        // System.out.println(pos.get(i).terms[0]+", "+pos.get(i+1).terms[0]+": cdl: "+this.currDocLen+", lcs: "+len);
      }
    }
    return len;
  }
  
  // for longest common subsequence
  protected int currLCSLen(MatchTermPos[] qspos, MatchTermPos[] dspos, int[][] c) {
    /*
     * for (int x = 1; x < this.initHeapEnd + 1; x++) { for (int y = 1; y <
     * this.initHeapEnd + 1; y++) { if (qspos[x - 1].term.equals(dspos[y -
     * 1].term)) c[x][y] = c[x - 1][y - 1] + 1; else c[x][y] = Math.max(c[x][y -
     * 1], c[x - 1][y]); } }
     */
    return c[this.initHeapEnd][this.initHeapEnd];
  }

  public int numwords() {
    return this.heapEnd;
  }

  public int maxLCSLen() throws IOException {
    // MatchTermPos[] qspos = new MatchTermPos[initHeapEnd];
    // MatchTermPos[] dspos = new MatchTermPos[initHeapEnd];
    // int[][] c = new int[this.initHeapEnd + 1][this.initHeapEnd + 1];
    // sortByQPos(this.pos, initHeapEnd, qspos);
    // sortByDPos(this.pos, initHeapEnd, dspos);
    int mlcs = currLCSLen();// currLCSLen(qspos, dspos, c);
    if (mlcs == this.initHeapEnd) {
      return this.initHeapEnd;
    }

    MatchTermPos p = pop();

    while (p != null) {
      if (p.nextPosition()) {
        push(p);
        // if (p.payloadBytes[0] < this.currDocLen)
        this.currDocLen = p.payloadBytes[0];
        // System.out.println("maxLCSLen matched term: "+p.terms[0]+", cdl: "+this.currDocLen+", dpos: "+p.currdpos);
        // sortByDPos(this.pos, initHeapEnd, dspos);
      } else
        this.pos.set(this.heapEnd, p);
      int clcs = currLCSLen();// currLCSLen(qspos, dspos, c);

      if (clcs == this.initHeapEnd)
        return this.initHeapEnd;
      if (clcs > mlcs)
        mlcs = clcs;
      p = pop();
    }

    return mlcs;
  }

  public boolean isFullMatch() throws IOException {
    // MatchTermPos[] qspos = new MatchTermPos[initHeapEnd];
    // MatchTermPos[] dspos = new MatchTermPos[initHeapEnd];
    // int[][] c = new int[this.initHeapEnd + 1][this.initHeapEnd + 1];
    // sortByQPos(this.pos, initHeapEnd, qspos);
    // sortByDPos(this.pos, initHeapEnd, dspos);
    int mlcs = currLCSLen();// currLCSLen(qspos, dspos, c);
    if (mlcs == this.currmpos.size() && mlcs == this.currDocLen) {
      // System.out.println("yes: "+mlcs+", "+this.currmpos+", "+this.currDocLen);
      return true;
    }

    MatchTermPos p = pop();
    // if(p!=null)
    // System.out.println("field: "+p.terms[0].field());
    while (p != null) {
      if (p.nextPosition()) {
        push(p);
        this.currDocLen = p.payloadBytes[0];
        // System.out.println("isFullMatch matched term: "+p.terms[0]+", cdl: "+this.currDocLen+", dpos: "+p.currdpos);
        // System.out.println("["+p.terms[0]+"],");
        // sortByDPos(this.pos, initHeapEnd, dspos);
      } else
        this.pos.set(this.heapEnd, p);
      mlcs = currLCSLen();// currLCSLen(qspos, dspos, c);

      if (mlcs == this.currmpos.size() && mlcs == this.currDocLen) {
        // System.out.println("yes: "+mlcs+", "+this.currmpos+", "+this.currDocLen);
        return true;
      }
      p = pop();
    }
    if (mlcs == this.currmpos.size() && mlcs == this.currDocLen) {
      // System.out.println("yes: "+mlcs+", "+this.currmpos+", "+this.currDocLen);
      return true;
    }
    // System.out.println("no: "+mlcs+", "+this.currmpos+", "+this.currDocLen);
    return false;
  }

  public boolean isCandidateFullMatch() throws IOException {
    // MatchTermPos[] qspos = new MatchTermPos[initHeapEnd];
    // MatchTermPos[] dspos = new MatchTermPos[initHeapEnd];
    // int[][] c = new int[this.initHeapEnd + 1][this.initHeapEnd + 1];
    // sortByQPos(this.pos, initHeapEnd, qspos);
    // sortByDPos(this.pos, initHeapEnd, dspos);
    //System.out.println("in cand match");
    int mlcs = currLCSLen();// currLCSLen(qspos, dspos, c);
    
    if (mlcs >= this.currDocLen) {
      //System.out.println("1. yes: "+mlcs+", "+this.currmpos+", "+this.currDocLen);
      return true;
    }

    MatchTermPos p = pop();
    //System.out.println("1. yes: "+mlcs+", "+this.currmpos+", "+this.currDocLen+", payload: "+p.payloadBytes[0]+", qpos: "+p.qpos+", dpos"+p.currdpos);
    while (p != null) {
      //System.out.println("in while");
      if (p.nextPosition()) {
        push(p);
        //if (p.payloadBytes[0] < this.currDocLen)
          this.currDocLen = p.payloadBytes[0];
        //System.out.println("isCandFullMatch matched term: "+p.terms[0]+", cdl: "+this.currDocLen+", dpos: "+p.currdpos);
        // System.out.println("["+p.terms[0]+"],");
        // sortByDPos(this.pos, initHeapEnd, dspos);
      } else
        this.pos.set(this.heapEnd, p);
      int clcs = currLCSLen();// currLCSLen(qspos, dspos, c);

      //if (clcs > mlcs)
        mlcs = clcs;
      if (mlcs >= this.currDocLen) {
        //System.out.println("2. yes: "+mlcs+", "+this.currmpos+", "+this.currDocLen);
        return true;
      }
      p = pop();
    }
    if (mlcs >= this.currDocLen) {
      //System.out.println("3. yes: "+mlcs+", "+this.currmpos+", "+this.currDocLen);
      return true;
    }
    //System.out.println("cand no: "+mlcs+", "+this.currmpos+", "+this.currDocLen);
    return false;
  }

}
