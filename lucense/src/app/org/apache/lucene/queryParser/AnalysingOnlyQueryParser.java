package org.apache.lucene.queryParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.Version;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class AnalysingOnlyQueryParser extends QueryParser {

  private Map<String, List<Attribute>> atts = new HashMap<String, List<Attribute>>();

  private Set<Class<? extends Attribute>> attClasses = new HashSet<Class<? extends Attribute>>();

  private Object2IntOpenHashMap<String> qlenFieldMap = new Object2IntOpenHashMap<String>();

  private Map<String, List<String>> termFieldMap = new HashMap<String, List<String>>();

  protected AnalysingOnlyQueryParser(QueryParserTokenManager tm) {
    super(tm);
  }

  public AnalysingOnlyQueryParser(Version matchVersion, String f, Analyzer a) {
    super(matchVersion, f, a);
  }

  protected AnalysingOnlyQueryParser(CharStream cs) {
    super(cs);
  }

  public void addAttributeClass(Class<? extends Attribute> clazz) {
    this.attClasses.add(clazz);
  }

  public Map<String, List<Attribute>> getAttributes() {
    return this.atts;
  }

  public Object2IntOpenHashMap<String> getQueryLengthMap() {
    return this.qlenFieldMap;
  }

  /**
   * @exception ParseException
   *              throw in overridden method to disallow
   */
  @Override
  protected Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
    // Use the analyzer to get all the tokens, and then build a TermQuery,
    // PhraseQuery, or nothing based on the term count

    TokenStream source;
    try {
      source = this.analyzer.reusableTokenStream(field, new StringReader(queryText));
      source.reset();
    } catch (IOException e) {
      source = this.analyzer.tokenStream(field, new StringReader(queryText));
    }
    CachingTokenFilter buffer = new CachingTokenFilter(source);
    CharTermAttribute termAtt = null;
    PositionIncrementAttribute posIncrAtt = null;
    int numTokens = 0;

    boolean success = false;
    try {
      buffer.reset();
      success = true;
    } catch (IOException e) {
      // success==false if we hit an exception
    }
    if (success) {
      if (buffer.hasAttribute(CharTermAttribute.class)) {
        termAtt = buffer.getAttribute(CharTermAttribute.class);
      }
      if (buffer.hasAttribute(PositionIncrementAttribute.class)) {
        posIncrAtt = buffer.getAttribute(PositionIncrementAttribute.class);
      }
      if (!this.atts.containsKey(field))
        this.atts.put(field, new ArrayList<Attribute>());
      for (Class<? extends Attribute> clazz : this.attClasses) {
        if (buffer.hasAttribute(clazz)) {
          this.atts.get(field).add(buffer.getAttribute(clazz));
        }
      }
    }

    int positionCount = 0;
    boolean severalTokensAtSamePosition = false;

    boolean hasMoreTokens = false;
    if (termAtt != null) {
      try {
        hasMoreTokens = buffer.incrementToken();
        while (hasMoreTokens) {
          if (!termFieldMap.containsKey(field))
            termFieldMap.put(field, new LinkedList<String>());
          termFieldMap.get(field).add(termAtt.toString());
          numTokens++;
          int positionIncrement = (posIncrAtt != null) ? posIncrAtt.getPositionIncrement() : 1;
          if (positionIncrement != 0)
            positionCount += positionIncrement;
          else
            severalTokensAtSamePosition = true;

          hasMoreTokens = buffer.incrementToken();
        }

      } catch (IOException e) {
        // ignore
      }
      if (positionCount > 0) {
        if (this.qlenFieldMap.containsKey(field))
          this.qlenFieldMap.put(field, positionCount + this.qlenFieldMap.getInt(field));
        else
          this.qlenFieldMap.put(field, positionCount);
      }
    }
    try {
      // rewind the buffer stream
      buffer.reset();

      // close original stream - all tokens buffered
      source.close();
    } catch (IOException e) {
      // ignore
    }

    if (numTokens == 0) {

      return null;
    } else if (numTokens == 1) {
      String term = null;
      try {
        boolean hasNext = buffer.incrementToken();
        assert hasNext == true;
        term = termAtt.toString();
      } catch (IOException e) {
        // safe to ignore, because we know the number of tokens
      }
      return newTermQuery(new Term(field, term));
    } else {
      if (severalTokensAtSamePosition) {
        if (positionCount == 1) {
          // no phrase query:
          for (int i = 0; i < numTokens; i++) {
            try {
              boolean hasNext = buffer.incrementToken();
              assert hasNext == true;
            } catch (IOException e) {
              // safe to ignore, because we know the number of tokens
            }

          }
          return null;
        } else {
          // phrase query:
          List<Term> multiTerms = new ArrayList<Term>();
          int position = -1;
          for (int i = 0; i < numTokens; i++) {
            String term = null;
            int positionIncrement = 1;
            try {
              boolean hasNext = buffer.incrementToken();
              assert hasNext == true;
              term = termAtt.toString();
              if (posIncrAtt != null) {
                positionIncrement = posIncrAtt.getPositionIncrement();
              }
            } catch (IOException e) {
              // safe to ignore, because we know the number of tokens
            }

            if ((positionIncrement > 0) && (multiTerms.size() > 0)) {
              multiTerms.clear();
            }
            position += positionIncrement;
            multiTerms.add(new Term(field, term));
          }
          return null;
        }
      } else {
        int position = -1;

        for (int i = 0; i < numTokens; i++) {
          int positionIncrement = 1;

          try {
            boolean hasNext = buffer.incrementToken();
            assert hasNext == true;
            if (posIncrAtt != null) {
              positionIncrement = posIncrAtt.getPositionIncrement();
            }
          } catch (IOException e) {
            // safe to ignore, because we know the number of tokens
          }

          if (this.enablePositionIncrements) {
            position += positionIncrement;
          }
        }
        return null;
      }
    }
  }

  @Override
  protected BooleanQuery newBooleanQuery(boolean disableCoord) {
    return null;
  }

  @Override
  protected BooleanClause newBooleanClause(Query q, BooleanClause.Occur occur) {
    return null;
  }

  @Override
  protected Query newTermQuery(Term term) {
    return null;
  }

  @Override
  protected PhraseQuery newPhraseQuery() {
    return null;
  }

  @Override
  protected MultiPhraseQuery newMultiPhraseQuery() {
    return null;
  }

  @Override
  protected Query newPrefixQuery(Term prefix) {
    return null;
  }

  @Override
  protected Query newFuzzyQuery(Term term, float minimumSimilarity, int prefixLength) {
    return null;
  }

  @Override
  protected Query newRangeQuery(String field, String part1, String part2, boolean inclusive) {
    return null;
  }

  @Override
  protected Query newMatchAllDocsQuery() {
    return null;
  }

  @Override
  protected Query newWildcardQuery(Term t) {
    return null;
  }

  public Map<String, List<String>> analyzedTerms() {
    return termFieldMap;
  }

}
