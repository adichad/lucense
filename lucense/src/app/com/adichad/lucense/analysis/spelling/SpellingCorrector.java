package com.adichad.lucense.analysis.spelling;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

import com.adichad.lucense.analysis.AnalyzerFactory;
import com.adichad.lucense.analysis.component.filter.ContextualSpellingCorrectingFilterSource;
import com.adichad.lucense.analysis.component.filter.LowerCaseFilterSource;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;
import com.adichad.lucense.analysis.component.tokenizer.WhitespaceTokenizerSource;
import com.adichad.lucense.analysis.spelling.SpellingCorrectionContext.CorrectionContextEntry;

public class SpellingCorrector {

  private Analyzer analyzer;

  public SpellingCorrector(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  public SpellingCorrectionAttribute getSpellingCorrections(String query) throws IOException {
    TokenStream ts = this.analyzer.tokenStream(null, new StringReader(query));
    SpellingCorrectionAttribute corrections = ts.addAttribute(SpellingCorrectionAttribute.class);
    ts.reset();
    while (ts.incrementToken())
      ;

    return corrections;
  }

  public static void main(String[] args) {
    try {
      System.out
          .println("usage: java -cp <classpath> -Xmx256m com.adichad.lucense.analysis.spelling.SpellingCorrector <corpus-file> <query-file>");
      CorrectionParameters params = new CorrectionParameters();
      params.editDistance = 3;
      params.prefixLen = 2;
      params.filterProbability = 0.0d;
      params.maxCorrections = 2;
      params.levenshteinPenaltyFactor = 7;
      TokenStreamSource tss = new WhitespaceTokenizerSource(Version.LUCENE_29);
      ArrayList<TokenFilterSource> filterSources = new ArrayList<TokenFilterSource>();
      filterSources.add(new LowerCaseFilterSource(Version.LUCENE_29));
      Analyzer queryAnl = AnalyzerFactory.createAnalyzer(tss, filterSources);

      /*
       * String stopfile = args[1]; System.out.println("stopwords: "+stopfile);
       * filterSources.add(new StopFilterSource(stopfile, true));
       */
      String file = args[0];
      System.out.println("corpus: " + file);
      filterSources.add(new ContextualSpellingCorrectingFilterSource(file, params));
      Analyzer anl = AnalyzerFactory.createAnalyzer(tss, filterSources);
      SpellingCorrector speller = new SpellingCorrector(anl);

      String queryFile = args[1];
      System.out.println("queries: " + queryFile);
      TokenStream ts = queryAnl.tokenStream(null, new FileReader(queryFile));
      TermAttribute termAtt = ts.addAttribute(TermAttribute.class);

      while (ts.incrementToken()) {
        SpellingCorrectionAttribute spellAtt = speller.getSpellingCorrections(termAtt.term());
        Map<CorrectionContextEntry, Map<String, Double>> corrMap = spellAtt.getSpellingCorrections();
        for (CorrectionContextEntry e : corrMap.keySet()) {
          System.out.println(termAtt.term() + "," + spellAtt.getBestCorrection(e).getKey());
        }
      }
      ts.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
