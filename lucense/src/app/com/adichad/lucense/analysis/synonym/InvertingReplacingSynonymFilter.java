package com.adichad.lucense.analysis.synonym;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import com.adichad.lucense.analysis.AnalyzerFactory;
import com.adichad.lucense.analysis.component.filter.InvertingReplacingSynonymFilterSource;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.WhitespaceTokenizerSource;
import com.adichad.lucense.analysis.stem.StemInversionAttribute;

/**
 * SynonymFilter handles multi-token synonyms with variable position increment
 * offsets.
 */

public class InvertingReplacingSynonymFilter extends ReplacingSynonymFilter {

  private final HashMap<String, HighlightObject> highlightObj;

  private StemInversionAttribute invAtt;

  public InvertingReplacingSynonymFilter(TokenStream in, SynonymMap<String, String> map,
      HashMap<String, HighlightObject> highlightObj) {
    super(in, map);
    this.highlightObj = highlightObj;
    this.invAtt = addAttribute(StemInversionAttribute.class);
  }

  protected void processNextEmission(EmitEntry item) {
    HighlightObject obj = highlightObj.get(item.label);
    Set<String> list = obj.entries;
    for (String s : list) {
      invAtt.addSynonymInversion(item.label, s);
    }
  }

  public static void main(String[] args) {
    try {
      String query = "adi";
      query = "adi ms net sql";
      query = "ms net sql java";
      query = "adi  ms net sql java";
      query = "adi ms net sql java ms";
      query = "adi adi ms net sql ms ms net java adi";
      query = "adi adi ms net sql ms adi ms net java adi";
      query = "adi adi ms net sql ms adi ms net java adi ms net sql java1";
      query = "adi adi ms net sql ms adi ms net sql java adi ms net sql java1";
      query = "adi adi ms net sql ms adi ms net sql java adi ms net sql java adi";

      List<TokenFilterSource> filterSources = new ArrayList<TokenFilterSource>();
      filterSources.add(new InvertingReplacingSynonymFilterSource(
          "/home/adichad/svnco/SearchPlatform/lucense/branches/adichad/env/adichad/synonymTest.in"));
      Analyzer an = AnalyzerFactory.createAnalyzer(new WhitespaceTokenizerSource(Version.LUCENE_33), filterSources);
      TokenStream ts = an.tokenStream("", new StringReader(query));

      CharTermAttribute termAtt = (CharTermAttribute) ts.addAttribute(CharTermAttribute.class);
      PositionIncrementAttribute posAttr = (PositionIncrementAttribute) ts
          .addAttribute(PositionIncrementAttribute.class);
      OffsetAttribute offAttr = (OffsetAttribute) ts.addAttribute(OffsetAttribute.class);

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