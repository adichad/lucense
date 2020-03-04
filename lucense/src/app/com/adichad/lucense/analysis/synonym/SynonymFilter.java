package com.adichad.lucense.analysis.synonym;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import com.adichad.lucense.analysis.AnalyzerFactory;
import com.adichad.lucense.analysis.component.filter.SynonymFilterSource;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.WhitespaceTokenizerSource;

/**
 * SynonymFilter handles multi-token synonyms with variable position increment
 * offsets.
 */

public abstract class SynonymFilter extends TokenFilter {

  protected class EmitEntry {
    int span;

    boolean starting;

    String label;

    public EmitEntry(int span, String label, boolean starting) {
      this.label = label;
      this.span = span;
      this.starting = starting;
    }

    public boolean isStarting() {
      return this.starting;
    }

    @Override
    public String toString() {
      return this.label;// + ((this.span != 0) ? "[" + this.span + "]" : "");
    }
  }

  protected final SynonymMap<String, String> map;

  protected Set<SynonymMap<String, String>> matchedPrefixes;

  protected CharTermAttribute termAtt;

  protected PositionIncrementAttribute posAtt;

  protected OffsetAttribute offAtt;

  protected LinkedList<LinkedHashSet<EmitEntry>> emitBuffer;

  protected LinkedList<Integer> startOffsetBuffer;

  protected LinkedList<Integer> endOffsetBuffer;

  protected LinkedHashSet<EmitEntry> emitLimit;

  public SynonymFilter(TokenStream in, SynonymMap<String, String> map) {
    super(in);
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posAtt = addAttribute(PositionIncrementAttribute.class);
    this.offAtt = addAttribute(OffsetAttribute.class);
    this.map = map;

    // System.out.println("Synonym Map : "+map);

    this.matchedPrefixes = new HashSet<SynonymMap<String, String>>();
    this.matchedPrefixes.add(map);
    this.emitBuffer = new LinkedList<LinkedHashSet<EmitEntry>>();
    this.startOffsetBuffer = new LinkedList<Integer>();
    this.endOffsetBuffer = new LinkedList<Integer>();
    this.emitLimit = null;
  }

  @Override
  public final boolean incrementToken() throws IOException { // System.out.println("\n\n Ab mujhe bulaya-- Augmenting!!!");
    if (emit(this.emitLimit, 0))
      return true;
    while (!enqueueEmit(this.input.incrementToken()))
      ;
    return emit(this.emitLimit, 1);
  }

  protected final boolean enqueueEmit(boolean haveInput) {
    if (haveInput) {
      LinkedHashSet<EmitEntry> ls = new LinkedHashSet<EmitEntry>();
      EmitEntry e = new EmitEntry(1, this.termAtt.toString(), true);
      ls.add(e);
      this.emitBuffer.add(ls);
      this.startOffsetBuffer.add(this.offAtt.startOffset());
      this.endOffsetBuffer.add(this.offAtt.endOffset());
    }

    Set<SynonymMap<String, String>> removableMatches = new HashSet<SynonymMap<String, String>>();
    Set<SynonymMap<String, String>> addableMatches = new HashSet<SynonymMap<String, String>>();
    boolean emit = true;
    for (SynonymMap<String, String> m : this.matchedPrefixes) {
      if (haveInput && m.containsSubmap(this.termAtt.toString())) {
        addableMatches.add(m.getSubmap(this.termAtt.toString()));
        if ((m != this.map)
            || ((this.matchedPrefixes.size() == 1) && ((this.emitLimit == null) || m.containsSubmap(this.emitLimit
                .iterator().next().label))))
          emit = false;
        else if (((this.emitLimit == null) || !m.containsSubmap(this.emitLimit.iterator().next().label)))
          this.emitLimit = this.emitBuffer.descendingIterator().next();
      }

      if (m != this.map) {
        removableMatches.add(m);
        if (m.payload.size() != 0)
          fillEmit(m, haveInput);
        if (haveInput)
          this.emitLimit = this.emitBuffer.descendingIterator().next();
      }
    }
    if (!haveInput || addableMatches.isEmpty())
      this.emitLimit = null;

    this.matchedPrefixes.removeAll(removableMatches);
    this.matchedPrefixes.addAll(addableMatches);
    return emit;
  }

  protected abstract boolean emit(Set<EmitEntry> limit, int incrPos);

  protected final void fillEmit(SynonymMap<String, String> m, boolean skipLastEntry) {
    Iterator<LinkedHashSet<EmitEntry>> eit = this.emitBuffer.descendingIterator();
    Set<String> rootWords = new HashSet<String>();

    if (skipLastEntry)
      this.emitLimit = eit.next();
    else
      this.emitLimit = null;

    LinkedHashSet<EmitEntry> currSyns = eit.next();
    SynonymMap<String, String> sm = m;
    rootWords = sm.payload;

    int span = sm.span;
    for (String s : rootWords)
      currSyns.add(new EmitEntry(span, s, false));

    int depth = sm.span;
    while ((sm.parent != this.map) && eit.hasNext()) {
      currSyns = eit.next();
      depth--;
      ArrayList<EmitEntry> removables = new ArrayList<EmitEntry>();
      for (EmitEntry d : currSyns) {
        String s = d.label.substring(1);
        if (rootWords.contains(s))
          if (d.isStarting() || (depth >= d.span))
            removables.add(d);
      }
      currSyns.removeAll(removables);
      sm = sm.parent;
    }

    for (String s : rootWords)
      currSyns.add(new EmitEntry(span, s, true));
    /*
     * System.out.println("filled: " + emitBuffer + "(skipping: " + (emitLimit
     * == null ? "null" : emitLimit.iterator().next()) + ")");
     */
  }

  public static void main(String[] args) {
    try {
      String query = "adi";
      query = "adi ms net sql";
      query = "ms net sql java";
      query = "adi  ms net sql java";
      query = "adi ms net sql java ms smthn u love mere";
      query = "adi adi ms net sql ms ms net java adi";
      query = "adi adi ms net sql ms adi ms net java adi";
      query = "adi adi ms net sql ms adi ms net java adi ms net sql java";

      query = "engineer mud engineer";

      // query = "mud engineer engineer";
      // query = "mud engineer engineer papa";
      query = "ASNT";
      query = "non destructive testing";
      query = "papa american society of non destructive testing ASNT";
      // query =
      // "adi adi ms net sql ms adi ms net sql java adi ms net sql java1";
      // query =
      // "adi adi ms net sql ms adi ms net sql java adi ms net sql java1 adi";

      List<TokenFilterSource> filterSources = new ArrayList<TokenFilterSource>();
      filterSources.add(new SynonymFilterSource("/home/adichad/nirmapplyNSE/current/etc/synonymFile.in", true));
      // filterSources.add(new
      // SynonymFilterSource("/home/adichad/svnco/SearchPlatform/lucense/branches/adichad/env/adichad/synonymTest.in",
      // false));
      Analyzer an = AnalyzerFactory.createAnalyzer(new WhitespaceTokenizerSource(Version.LUCENE_33), filterSources);
      TokenStream ts = an.tokenStream("", new StringReader(query));

      CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
      PositionIncrementAttribute posAttr = (PositionIncrementAttribute) ts
          .addAttribute(PositionIncrementAttribute.class);
      OffsetAttribute offAttr = ts.addAttribute(OffsetAttribute.class);

      int i = 0;
      while (ts.incrementToken()) {
        System.out.println("[" + (i += posAttr.getPositionIncrement()) + "] [" + offAttr.startOffset() + "-"
            + offAttr.endOffset() + ")\t" + termAtt.toString());
      }
    } catch (Exception e) {
      e.printStackTrace(System.out);
    }
  }
}
