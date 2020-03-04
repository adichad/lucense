package com.adichad.lucense.analysis.spelling.neo;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.util.Version;

import com.adichad.lucense.analysis.AnalyzerFactory;
import com.adichad.lucense.analysis.component.filter.LowerCaseFilterSource;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.PatternTokenizerSource;
import com.adichad.lucense.analysis.spelling.CorrectionParameters;
import com.adichad.lucense.analysis.spelling.SpellingCorrectionAttribute;

/**
 * Spews out spelling corrections for terms deamed incorrect/misplaced
 */

public final class ContextualSpellingCorrectingFilter extends TokenFilter {

  private SpellingCorrectionAttribute spellCorrAttr;

  private final CorrectionParameters params;

  private FixedSizeContextCorpus corpus;

  private int proximity;

  private LinkedList<CorrectedContextTerm> result;

  private Term cachedTerm;

  private CharTermAttribute termAtt;

  private PositionIncrementAttribute posIncrAtt;

  public ContextualSpellingCorrectingFilter(FixedSizeContextCorpus corpus, CorrectionParameters params,
      TokenStream input) {
    super(input);
    this.params = params;
    this.termAtt = addAttribute(CharTermAttribute.class);
    this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    this.spellCorrAttr = input.addAttribute(SpellingCorrectionAttribute.class);
    this.corpus = corpus;
    this.proximity = corpus.getProximity();
    this.result = new LinkedList<CorrectedContextTerm>();
    this.cachedTerm = new Term("label", "");
    
  }

  private final void updateResult(CorrectedContextTerm cct) {
    Map<Correction, Correction> corrs = new HashMap<Correction, Correction>();
    cct.posIncr = posIncrAtt.getPositionIncrement();
    cct.term = termAtt.toString();
    cct.corrs = corrs;
    
    result.addLast(cct);

    corpus.queryFilter(new FuzzyQuery(cachedTerm.createTerm(new String(cct.term)), 0.2f, 2, Integer.MAX_VALUE),
        cct.corrs);
    
    corpus.editDistanceFilter(cct.term, cct.corrs);

    corpus.contextFilter(cct.term, cct.posIncr, cct.corrs);
    
  }

  /**
   * Processes the next token and emits corrections if any
   */
  @Override
  public final boolean incrementToken() throws IOException {
    if(input.incrementToken()) {
      updateResult(new CorrectedContextTerm());
      return true;
    } else if(corpus.advanceNoInput())
      return true;
    corpus.close();
    int i=0;
    for(CorrectedContextTerm cc: result) {
      PriorityQueue<Correction> pq = new PriorityQueue<Correction>();
      for(Correction corr: cc.corrs.values()) {
        pq.add(corr);
      }
      String cstr = "";
      while(pq.size()>0) {
        cstr+=pq.remove()+" ";
      }
      System.out.println(cc.term+"["+(i+=cc.posIncr)+"]: "+cstr);
    }
    return false;
    
    /*
    CorrectedContextTerm cct = null;
    //if (result.size() > 0) {
      System.out.println("1");
      if (result.size() >= proximity) {
        System.out.println("2");
        cct = result.pollFirst();
        System.out.println(cct.term);
        if (input.incrementToken()) {
          System.out.println("3");
          updateResult(cct);
          return true;
        } else {
          System.out.println("4");
          if(corpus.advanceNoInput()) {
            System.out.println("5");
            return true;
          } else {
            System.out.println("6");
            for(CorrectedContextTerm cc: result) {
              PriorityQueue<Correction> pq = new PriorityQueue<Correction>();
              pq.addAll(cc.corrs.keySet());
              System.out.println(cc.term+": "+pq);
            }
            corpus.close();
            return false;
          }
        }
      } else {
        System.out.println("7");
        while(result.size()<proximity && input.incrementToken()) {
          System.out.println("8");
          cct = new CorrectedContextTerm();
          updateResult(cct);
        }
        if(result.size()<proximity) {
          System.out.println("9");
          if(corpus.advanceNoInput()) {
            System.out.println("10");
            return true;
          }
          else { 
            System.out.println("11");
            //System.out.println("result: "+result);
            for(CorrectedContextTerm cc: result) {
              PriorityQueue<Correction> pq = new PriorityQueue<Correction>();
              pq.addAll(cc.corrs.keySet());
              System.out.println(cc.term+": "+pq);
            }
            corpus.close();
            return false;
          }
        } else {
          System.out.println("12");
          cct = result.pollFirst();
          //System.out.println(cct.term);
          return true;
        }
          
      }
      
    /*} else {
      System.out.println("13");
      return false;
    }*/
    
  }

  public static void main(String[] args) {
    // analyzer definition
    try {
      List<TokenFilterSource> filterSources = new ArrayList<TokenFilterSource>();
      filterSources.add(new LowerCaseFilterSource(Version.LUCENE_33));
      filterSources.add(new ContextualSpellingCorrectingFilterSource("/home/adichad/sampleNSE/index/lang1", null));

      Analyzer an = AnalyzerFactory.createAnalyzer(new PatternTokenizerSource("[\\w#][\\w#+]*"), filterSources);

      TokenStream ts = an.tokenStream("", new StringReader("sale manger buisiness develiopment"));
      while (ts.incrementToken())
        ;
      ts.close();
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
