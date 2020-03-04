package org.apache.lucene.search;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.ToStringUtils;

/**
 * A Query that matches documents matching boolean combinations of other
 * queries, e.g. {@link TermQuery}s, {@link PhraseQuery}s or other
 * CustomBooleanQuerys.
 */
public class CustomBooleanQuery extends CustomQuery {
  private float boost = 0.0f;

  private static int maxClauseCount = 1024;

  @Override
  public float getBoost() {
    return this.boost;
  }

  @Override
  public void setBoost(float b) {
    this.boost = b;
  }

  /**
   * Thrown when an attempt is made to add more than
   * {@link #getMaxClauseCount()} clauses. This typically happens if a
   * PrefixQuery, FuzzyQuery, WildcardQuery, or TermRangeQuery is expanded to
   * many terms during search.
   */
  public static class TooManyClauses extends RuntimeException {
    public TooManyClauses() {}

    @Override
    public String getMessage() {
      return "maxClauseCount is set to " + maxClauseCount;
    }
  }

  /**
   * Return the maximum number of clauses permitted, 1024 by default. Attempts
   * to add more than the permitted number of clauses cause
   * {@link TooManyClauses} to be thrown.
   * 
   * @see #setMaxClauseCount(int)
   */
  public static int getMaxClauseCount() {
    return maxClauseCount;
  }

  /**
   * Set the maximum number of clauses permitted per CustomBooleanQuery. Default
   * value is 1024.
   */
  public static void setMaxClauseCount(int maxClauseCount) {
    if (maxClauseCount < 1)
      throw new IllegalArgumentException("maxClauseCount must be >= 1");
    CustomBooleanQuery.maxClauseCount = maxClauseCount;
  }

  private ArrayList<BooleanClause> clauses = new ArrayList<BooleanClause>();

  private boolean disableCoord;

  /** Constructs an empty boolean query. */
  public CustomBooleanQuery() {}

  /**
   * Constructs an empty boolean query.
   * 
   * {@link Similarity#coord(int,int)} may be disabled in scoring, as
   * appropriate. For example, this score factor does not make sense for most
   * automatically generated queries, like {@link WildcardQuery} and
   * {@link FuzzyQuery}.
   * 
   * @param disableCoord
   *          disables {@link Similarity#coord(int,int)} in scoring.
   */
  public CustomBooleanQuery(boolean disableCoord) {
    this.disableCoord = disableCoord;
  }

  /**
   * Returns true iff {@link Similarity#coord(int,int)} is disabled in scoring
   * for this query instance.
   * 
   * @see #CustomBooleanQuery(boolean)
   */
  public boolean isCoordDisabled() {
    return this.disableCoord;
  }

  // Implement coord disabling.
  // Inherit javadoc.
  @Override
  public Similarity getSimilarity(Searcher searcher) {
    Similarity result = super.getSimilarity(searcher);
    if (this.disableCoord) { // disable coord as requested
      result = new SimilarityDelegator(result) {
        @Override
        public float coord(int overlap, int maxOverlap) {
          return 1.0f;
        }
      };
    }
    return result;
  }

  /**
   * Specifies a minimum number of the optional BooleanClauses which must be
   * satisfied.
   * 
   * <p>
   * By default no optional clauses are necessary for a match (unless there are
   * no required clauses). If this method is used, then the specified number of
   * clauses is required.
   * </p>
   * <p>
   * Use of this method is totally independent of specifying that any specific
   * clauses are required (or prohibited). This number will only be compared
   * against the number of matching optional clauses.
   * </p>
   * <p>
   * EXPERT NOTE: Using this method may force collecting docs in order,
   * regardless of whether setAllowDocsOutOfOrder(true) has been called.
   * </p>
   * 
   * @param min
   *          the number of optional clauses that must match
   * @see #setAllowDocsOutOfOrder
   */
  public void setMinimumNumberShouldMatch(int min) {
    this.minNrShouldMatch = min;
  }

  protected int minNrShouldMatch = 0;

  /**
   * Gets the minimum number of the optional BooleanClauses which must be
   * satisfied.
   */
  public int getMinimumNumberShouldMatch() {
    return this.minNrShouldMatch;
  }

  /**
   * Adds a clause to a boolean query.
   * 
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  public void add(Query query, BooleanClause.Occur occur) {
    add(new BooleanClause(query, occur));
  }

  /**
   * Adds a clause to a boolean query.
   * 
   * @throws TooManyClauses
   *           if the new number of clauses exceeds the maximum clause number
   * @see #getMaxClauseCount()
   */
  public void add(BooleanClause clause) {
    if (this.clauses.size() >= maxClauseCount)
      throw new TooManyClauses();

    this.clauses.add(clause);
  }

  /** Returns the set of clauses in this query. */
  public BooleanClause[] getClauses() {
    return this.clauses.toArray(new BooleanClause[this.clauses.size()]);
  }

  /** Returns the list of clauses in this query. */
  public List<BooleanClause> clauses() {
    return this.clauses;
  }

  /**
   * Expert: the Weight for CustomBooleanQuery, used to normalize, score and
   * explain these queries.
   * 
   * <p>
   * NOTE: this API and implementation is subject to change suddenly in the next
   * release.
   * </p>
   */
  protected class BooleanWeight extends Weight {
    /** The Similarity implementation. */
    protected Similarity similarity;

    protected ArrayList<Weight> weights;

    public BooleanWeight(Searcher searcher) throws IOException {
      this.similarity = getSimilarity(searcher);
      this.weights = new ArrayList<Weight>(CustomBooleanQuery.this.clauses.size());
      for (int i = 0; i < CustomBooleanQuery.this.clauses.size(); i++) {
        BooleanClause c = CustomBooleanQuery.this.clauses.get(i);
        this.weights.add(c.getQuery().createWeight(searcher));
      }
    }

    @Override
    public Query getQuery() {
      return CustomBooleanQuery.this;
    }

    @Override
    public float getValue() {
      return getBoost();
    }

    @Override
    public float sumOfSquaredWeights() throws IOException {
      float sum = 0.0f;
      for (int i = 0; i < this.weights.size(); i++) {
        BooleanClause c = CustomBooleanQuery.this.clauses.get(i);
        Weight w = this.weights.get(i);
        // call sumOfSquaredWeights for all clauses in case of side effects
        float s = w.sumOfSquaredWeights(); // sum sub weights
        if (!c.isProhibited())
          // only add to sum for non-prohibited clauses
          sum += s;
      }

      return sum;
    }

    @Override
    public void normalize(float norm) {}

    @Override
    public Explanation explain(IndexReader reader, int doc) throws IOException {
      final int minShouldMatch = CustomBooleanQuery.this.getMinimumNumberShouldMatch();
      ComplexExplanation sumExpl = new ComplexExplanation();
      sumExpl.setDescription("sum of:");
      int coord = 0;
      int maxCoord = 0;
      float sum = 0.0f;
      boolean fail = false;
      int shouldMatchCount = 0;
      Iterator<BooleanClause> cIter = CustomBooleanQuery.this.clauses.iterator();
      for (Iterator<Weight> wIter = this.weights.iterator(); wIter.hasNext();) {
        Weight w = wIter.next();
        BooleanClause c = cIter.next();
        if (w.scorer(reader, true, true) == null) {
          continue;
        }
        Explanation e = w.explain(reader, doc);
        if (!c.isProhibited())
          maxCoord++;
        if (e.isMatch()) {
          if (!c.isProhibited()) {
            sumExpl.addDetail(e);
            sum += e.getValue();
            coord++;
          } else {
            Explanation r = new Explanation(0.0f, "match on prohibited clause (" + c.getQuery().toString() + ")");
            r.addDetail(e);
            sumExpl.addDetail(r);
            fail = true;
          }
          if (c.getOccur() == Occur.SHOULD)
            shouldMatchCount++;
        } else if (c.isRequired()) {
          Explanation r = new Explanation(0.0f, "no match on required clause (" + c.getQuery().toString() + ")");
          r.addDetail(e);
          sumExpl.addDetail(r);
          fail = true;
        }
      }
      if (fail) {
        sumExpl.setMatch(Boolean.FALSE);
        sumExpl.setValue(0.0f);
        sumExpl.setDescription("Failure to meet condition(s) of required/prohibited clause(s)");
        return sumExpl;
      } else if (shouldMatchCount < minShouldMatch) {
        sumExpl.setMatch(Boolean.FALSE);
        sumExpl.setValue(0.0f);
        sumExpl.setDescription("Failure to match minimum number " + "of optional clauses: " + minShouldMatch);
        return sumExpl;
      }

      sumExpl.setMatch(0 < coord ? Boolean.TRUE : Boolean.FALSE);
      sumExpl.setValue(sum);

      float coordFactor = this.similarity.coord(coord, maxCoord);
      if (coordFactor == 1.0f) // coord is no-op
        return sumExpl; // eliminate wrapper
      else {
        ComplexExplanation result = new ComplexExplanation(sumExpl.isMatch(), sum * coordFactor, "product of:");
        result.addDetail(sumExpl);
        result.addDetail(new Explanation(coordFactor, "coord(" + coord + "/" + maxCoord + ")"));
        return result;
      }
    }

    @Override
    public CustomScorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {
      List<CustomScorer> required = new ArrayList<CustomScorer>();
      List<CustomScorer> prohibited = new ArrayList<CustomScorer>();
      List<CustomScorer> optional = new ArrayList<CustomScorer>();
      Iterator<BooleanClause> cIter = CustomBooleanQuery.this.clauses.iterator();
      for (Iterator<Weight> wIter = this.weights.iterator(); wIter.hasNext();) {
        Weight w = wIter.next();
        BooleanClause c = cIter.next();
        CustomScorer subScorer = (CustomScorer) w.scorer(reader, true, false);
        if (subScorer == null) {
          if (c.isRequired()) {
            return null;
          }
        } else if (c.isRequired()) {
          required.add(subScorer);
        } else if (c.isProhibited()) {
          prohibited.add(subScorer);
        } else {
          optional.add(subScorer);
        }
      }

      // Check if we can return a BooleanScorer
      scoreDocsInOrder |= !allowDocsOutOfOrder; // until it is removed, factor
                                                // in the static setting.
      if (!scoreDocsInOrder && topScorer && (required.size() == 0) && (prohibited.size() < 32)) {
        return new CustomBooleanScorer(this.similarity, CustomBooleanQuery.this.minNrShouldMatch, optional, prohibited,
            this.getQuery().getBoost());
      }

      if ((required.size() == 0) && (optional.size() == 0)) {
        // no required and optional clauses.
        return null;
      } else if (optional.size() < CustomBooleanQuery.this.minNrShouldMatch) {
        // either >1 req scorer, or there are 0 req scorers and at least 1
        // optional scorer. Therefore if there are not enough optional scorers
        // no documents will be matched by the query
        return null;
      }
      // Return a BooleanScorer2
      return new CustomBooleanScorer2(this.similarity, CustomBooleanQuery.this.minNrShouldMatch, required, prohibited,
          optional, this.getQuery().getBoost());
    }

    @Override
    public boolean scoresDocsOutOfOrder() {
      int numProhibited = 0;
      for (Iterator<BooleanClause> cIter = CustomBooleanQuery.this.clauses.iterator(); cIter.hasNext();) {
        BooleanClause c = cIter.next();
        if (c.isRequired()) {
          return false; // BS2 (in-order) will be used by scorer()
        } else if (c.isProhibited()) {
          ++numProhibited;
        }
      }

      if (numProhibited > 32) { // cannot use BS
        return false;
      }

      // scorer() will return an out-of-order scorer if requested.
      return true;
    }

  }

  /**
   * Whether hit docs may be collected out of docid order.
   * 
   * @deprecated this will not be needed anymore, as
   *             {@link Weight#scoresDocsOutOfOrder()} is used.
   */
  @Deprecated
  private static boolean allowDocsOutOfOrder = true;

  /**
   * Expert: Indicates whether hit docs may be collected out of docid order.
   * 
   * <p>
   * Background: although the contract of the Scorer class requires that
   * documents be iterated in order of doc id, this was not true in early
   * versions of Lucene. Many pieces of functionality in the current Lucene code
   * base have undefined behavior if this contract is not upheld, but in some
   * specific simple cases may be faster. (For example: disjunction queries with
   * less than 32 prohibited clauses; This setting has no effect for other
   * queries.)
   * </p>
   * 
   * <p>
   * Specifics: By setting this option to true, docid N might be scored for a
   * single segment before docid N-1. Across multiple segments, docs may be
   * scored out of order regardless of this setting - it only applies to scoring
   * a single segment.
   * 
   * Being static, this setting is system wide.
   * </p>
   * 
   * @deprecated this is not needed anymore, as
   *             {@link Weight#scoresDocsOutOfOrder()} is used.
   */
  @Deprecated
  public static void setAllowDocsOutOfOrder(boolean allow) {
    allowDocsOutOfOrder = allow;
  }

  /**
   * Whether hit docs may be collected out of docid order.
   * 
   * @see #setAllowDocsOutOfOrder(boolean)
   * @deprecated this is not needed anymore, as
   *             {@link Weight#scoresDocsOutOfOrder()} is used.
   */
  @Deprecated
  public static boolean getAllowDocsOutOfOrder() {
    return allowDocsOutOfOrder;
  }

  /**
   * @deprecated Use {@link #setAllowDocsOutOfOrder(boolean)} instead.
   */
  @Deprecated
  public static void setUseScorer14(boolean use14) {
    setAllowDocsOutOfOrder(use14);
  }

  /**
   * @deprecated Use {@link #getAllowDocsOutOfOrder()} instead.
   */
  @Deprecated
  public static boolean getUseScorer14() {
    return getAllowDocsOutOfOrder();
  }

  @Override
  public Weight createWeight(Searcher searcher) throws IOException {
    return new BooleanWeight(searcher);
  }

  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    if ((this.minNrShouldMatch == 0) && (this.clauses.size() == 1)) { // optimize
                                                                      // 1-clause
      // queries
      BooleanClause c = this.clauses.get(0);
      if (!c.isProhibited()) { // just return clause

        Query query = c.getQuery().rewrite(reader); // rewrite first

        if (getBoost() != 1.0f) { // incorporate boost
          if (query == c.getQuery()) // if rewrite was no-op
            query = (Query) query.clone(); // then clone before boost
          query.setBoost(getBoost() * query.getBoost());
        }

        return query;
      }
    }

    CustomBooleanQuery clone = null; // recursively rewrite
    for (int i = 0; i < this.clauses.size(); i++) {
      BooleanClause c = this.clauses.get(i);
      Query query = c.getQuery().rewrite(reader);
      if (query != c.getQuery()) { // clause rewrote: must clone
        if (clone == null)
          clone = (CustomBooleanQuery) this.clone();
        clone.clauses.set(i, new BooleanClause(query, c.getOccur()));
      }
    }
    if (clone != null) {
      return clone; // some clauses rewrote
    } else
      return this; // no clauses rewrote
  }

  // inherit javadoc
  @Override
  public void extractTerms(Set<Term> terms) {
    for (Iterator<BooleanClause> i = this.clauses.iterator(); i.hasNext();) {
      BooleanClause clause = i.next();
      clause.getQuery().extractTerms(terms);
    }
  }

  @Override
  public Object clone() {
    CustomBooleanQuery clone = (CustomBooleanQuery) super.clone();
    clone.clauses = (ArrayList<BooleanClause>) this.clauses.clone();
    return clone;
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(String field) {
    StringBuffer buffer = new StringBuffer();
    boolean needParens = (getBoost() != 1.0) || (getMinimumNumberShouldMatch() > 0);
    if (needParens) {
      buffer.append("(");
    }

    for (int i = 0; i < this.clauses.size(); i++) {
      BooleanClause c = this.clauses.get(i);
      if (c.isProhibited())
        buffer.append("-");
      else if (c.isRequired())
        buffer.append("+");

      Query subQuery = c.getQuery();
      if (subQuery != null) {
        if (subQuery instanceof CustomBooleanQuery) { // wrap sub-bools in
                                                      // parens
          buffer.append("(");
          buffer.append(subQuery.toString(field));
          buffer.append(")");
        } else {
          buffer.append(subQuery.toString(field));
        }
      } else {
        buffer.append("null");
      }

      if (i != this.clauses.size() - 1)
        buffer.append(" ");
    }

    if (needParens) {
      buffer.append(")");
    }

    if (getMinimumNumberShouldMatch() > 0) {
      buffer.append('~');
      buffer.append(getMinimumNumberShouldMatch());
    }

    if (getBoost() != 1.0f) {
      buffer.append(ToStringUtils.boost(getBoost()));
    }

    return buffer.toString();
  }

  /** Returns true iff <code>o</code> is equal to this. */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CustomBooleanQuery))
      return false;
    CustomBooleanQuery other = (CustomBooleanQuery) o;
    return (this.getBoost() == other.getBoost()) && this.clauses.equals(other.clauses)
        && (this.getMinimumNumberShouldMatch() == other.getMinimumNumberShouldMatch());
  }

  /** Returns a hash code value for this object. */
  @Override
  public int hashCode() {
    return Float.floatToIntBits(getBoost()) ^ this.clauses.hashCode() + getMinimumNumberShouldMatch();
  }

}
