package com.cleartrip.sw.search.query;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.CustomBooleanQuery;
import org.apache.lucene.search.CustomFuzzyQuery;
import org.apache.lucene.search.CustomMultiTermQuery;
import org.apache.lucene.search.CustomPhraseQuery;
import org.apache.lucene.search.CustomPrefixQuery;
import org.apache.lucene.search.CustomTermQuery;
import org.apache.lucene.search.CustomTermRangeQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermDocsFactory;
import org.apache.lucene.search.WildcardQuery;

public class CustomQueryFactory extends QueryFactory {

  private final TermDocsFactory              tdf;
  private final Map<String, TermDocsFactory> fieldTermDocsFactories;

  public CustomQueryFactory(TermDocsFactory tdf,
      Map<String, TermDocsFactory> fieldTermDocsFactories) throws IOException {
    this.tdf = tdf;
    this.fieldTermDocsFactories = fieldTermDocsFactories;
  }

  public Query newBooleanQuery(boolean disableCoord) {
    return new CustomBooleanQuery(disableCoord);
  }

  private final TermDocsFactory tdf(String field) {
    TermDocsFactory tdf = fieldTermDocsFactories.get(field);
    if (tdf == null)
      return this.tdf;
    return tdf;
  }

  public Query newTermQuery(Term term, int pos) {
    return new CustomTermQuery(term, pos, tdf(term.field()));
  }

  public Query newPhraseQuery(String field) {
    return new CustomPhraseQuery(tdf(field));
  }

  public Query newMultiPhraseQuery() {
    return new MultiPhraseQuery();
  }

  public Query newPrefixQuery(Term prefix, int pos) {
    CustomPrefixQuery query = new CustomPrefixQuery(prefix, pos,
        tdf(prefix.field()));
    query
        .setRewriteMethod(CustomMultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  public Query newFuzzyQuery(Term term, float minimumSimilarity,
      int prefixLength, int pos) {
    // CustomFuzzyQuery doesn't yet allow constant score rewrite
    return new CustomFuzzyQuery(term, minimumSimilarity, prefixLength, pos,
        tdf(term.field()));
  }

  public Query newRangeQuery(String field, String part1, String part2,
      boolean inclusive, int pos) {
    final CustomTermRangeQuery query = new CustomTermRangeQuery(field, part1,
        part2, inclusive, inclusive, pos, tdf(field));
    query
        .setRewriteMethod(CustomMultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  public Query newMatchAllDocsQuery() {
    return new MatchAllDocsQuery();
  }

  public Query newWildcardQuery(Term t) {
    WildcardQuery query = new WildcardQuery(t);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  @Override
  public Query newIntRangeQuery(String field, int precisionStep, int min,
      int max, boolean minInclusive, boolean maxInclusive) {
    NumericRangeQuery<Integer> query = NumericRangeQuery.newIntRange(field,
        precisionStep, min, max, minInclusive, maxInclusive);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  @Override
  public Query newFloatRangeQuery(String field, int precisionStep, float min,
      float max, boolean minInclusive, boolean maxInclusive) {
    NumericRangeQuery<Float> query = NumericRangeQuery.newFloatRange(field,
        precisionStep, min, max, minInclusive, maxInclusive);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  @Override
  public Query newDoubleRangeQuery(String field, int precisionStep, double min,
      double max, boolean minInclusive, boolean maxInclusive) {
    NumericRangeQuery<Double> query = NumericRangeQuery.newDoubleRange(field,
        precisionStep, min, max, minInclusive, maxInclusive);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  @Override
  public BooleanClause[] getClauses(Query bq) {
    return ((CustomBooleanQuery) bq).getClauses();
  }

  @Override
  public void add(Query bq, Query ptq, Occur occur) {
    ((CustomBooleanQuery) bq).add(ptq, occur);
  }

  @Override
  public void add(Query bq, BooleanClause bc) {
    ((CustomBooleanQuery) bq).add(bc);
  }

  @Override
  public void addClauses(Query q, Query src) {
    for (BooleanClause clause : ((CustomBooleanQuery) src).getClauses())
      ((CustomBooleanQuery) q).add(clause);
  }

  @Override
  public void addPhraseTerm(Query q, Term t) {
    ((CustomPhraseQuery) q).add(t);
  }

}
