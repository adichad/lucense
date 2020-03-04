package com.adichad.lucense.analysis.component;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.util.Version;

import com.adichad.lucense.analysis.component.filter.ContextualSpellingCorrectingFilterSource;
import com.adichad.lucense.analysis.component.filter.ControlledPluralsInvertingStemFilterSource;
import com.adichad.lucense.analysis.component.filter.ControlledPluralsStemFilterSource;
import com.adichad.lucense.analysis.component.filter.ControlledPresentParticipleInvertingStemFilterSource;
import com.adichad.lucense.analysis.component.filter.ControlledPresentParticipleStemFilterSource;
import com.adichad.lucense.analysis.component.filter.LowerCaseFilterSource;
import com.adichad.lucense.analysis.component.filter.PhoneticFilterSource;
import com.adichad.lucense.analysis.component.filter.PorterStemFilterSource;
import com.adichad.lucense.analysis.component.filter.ShingleFilterSource;
import com.adichad.lucense.analysis.component.filter.StopFilterSource;
import com.adichad.lucense.analysis.component.filter.SynonymFilterSource;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource.CharNormalizerType;
import com.adichad.lucense.analysis.component.tokenizer.KeywordTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.PatternTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.StandardTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenizingCharsetTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.WhitespaceTokenizerSource;
import com.adichad.lucense.analysis.spelling.CorrectionParameters;

public class AnalyzerComponentFactory {
  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  public static TokenStreamSource getTokenStreamSource(Version matchVersion, String tokenizerType, String tokenCharDef) {
    if (tokenizerType.equals("whitespace"))
      return new WhitespaceTokenizerSource(matchVersion);
    if (tokenizerType.equals("charset"))
      return new CharTokenizerSource(matchVersion, tokenCharDef, CharNormalizerType.NONE);
    if (tokenizerType.equals("standard"))
      return new StandardTokenizerSource(matchVersion);
    if (tokenizerType.equals("tokenizing-charset"))
      return new TokenizingCharsetTokenizerSource(matchVersion, tokenCharDef);
    if (tokenizerType.equals("pattern"))
      return new PatternTokenizerSource(tokenCharDef);
    if (tokenizerType.equals("keyword")) {
      return new KeywordTokenizerSource();
    }
    return null;
  }

  public static TokenFilterSource getTokenFilterSource(Version matchVersion, String filterType, String filterSubtype,
      boolean morphInject, String file, int minLen, int prefixLen, int editDistance, double filterProbability,
      double penaltyFactor, int maxCorrections, boolean replace) {
    if (filterType.equals("stopword")) {
      try {
        return new StopFilterSource(matchVersion, file, morphInject);
      } catch (FileNotFoundException e) {
        errorLogger.log(Level.WARN, "Stopwords file not found: " + e);
      } catch (Exception e) {
        errorLogger.log(Level.WARN, e.getMessage());
      }
    }

    if (filterType.equals("synonym")) {
      try {
        return new SynonymFilterSource(file, replace);
      } catch (FileNotFoundException e) {
        errorLogger.log(Level.WARN, "Synonyms file not found: " + e);
      } catch (Exception e) {
        errorLogger.log(Level.WARN, e.getMessage());
      }
    }
    if (filterType.equals("lowercase"))
      return new LowerCaseFilterSource(matchVersion);
    if (filterType.equals("phonetic"))
      return new PhoneticFilterSource(filterSubtype, morphInject);
    if (filterType.equals("stemporter"))
      return new PorterStemFilterSource();
    if (filterType.equals("stemplurals"))
      return new ControlledPluralsStemFilterSource(matchVersion, file, minLen, replace, morphInject);
    if (filterType.equals("stemplurals-inversions"))
      return new ControlledPluralsInvertingStemFilterSource(matchVersion, file, minLen, replace);
    if (filterType.equals("stempresentparticiples"))
      return new ControlledPresentParticipleStemFilterSource(matchVersion, file, minLen, replace, morphInject);
    if (filterType.equals("stempresentparticiples-inversions"))
      return new ControlledPresentParticipleInvertingStemFilterSource(matchVersion, file, minLen, replace);
    if (filterType.equals("shingle"))
      return new ShingleFilterSource(minLen);
    if (filterType.equals("spellcorrect")) {
      CorrectionParameters params = new CorrectionParameters();
      params.editDistance = editDistance;
      params.prefixLen = prefixLen;
      params.filterProbability = filterProbability;
      params.maxCorrections = maxCorrections;
      params.levenshteinPenaltyFactor = penaltyFactor;
      try {
        return new ContextualSpellingCorrectingFilterSource(file, params);
      } catch (IOException e) {
        errorLogger.log(Level.WARN, e.getMessage());
      }
    }
    return null;
  }

}
