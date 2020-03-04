package com.adichad.lucense.analysis;

import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.util.Version;

import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;
import com.adichad.lucense.analysis.component.tokenizer.WhitespaceTokenizerSource;

public class AnalyzerFactory {

  public static Analyzer createAnalyzer(TokenStreamSource tokenStreamSource, List<TokenFilterSource> filterSources) {
    return new CustomizedAnalyzer(tokenStreamSource, filterSources);
  }

  public static Analyzer createNestedAnalyzer(Version matchVersion, TokenStreamSource tokenStreamSource,
      List<TokenFilterSource> filterSources, Map<String, Analyzer> fieldAnalyzerMap) {
    if (tokenStreamSource == null) {
      tokenStreamSource = new WhitespaceTokenizerSource(matchVersion);
    }
    Analyzer analyzer = createAnalyzer(tokenStreamSource, filterSources);
    if (fieldAnalyzerMap.size() > 0) {
      PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(analyzer);
      for (String field : fieldAnalyzerMap.keySet())
        wrapper.addAnalyzer(field, fieldAnalyzerMap.get(field));
      return wrapper;
    } else
      return analyzer;
  }

}
