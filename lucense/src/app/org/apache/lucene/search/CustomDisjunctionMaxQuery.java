package org.apache.lucene.search;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

/**
 * A query that generates the union of documents produced by its subqueries, and
 * that scores each document with the maximum score for that document as
 * produced by any subquery, plus a tie breaking increment for any additional
 * matching subqueries. This is useful when searching for a word in multiple
 * fields with different boost factors (so that the fields cannot be combined
 * equivalently into a single search field). We want the primary score to be the
 * one associated with the highest boost, not the sum of the field scores (as
 * BooleanQuery would give). If the query is "albino elephant" this ensures that
 * "albino" matching one field and "elephant" matching another gets a higher
 * score than "albino" matching both fields. To get this result, use both
 * BooleanQuery and CustomDisjunctionMaxQuery: for each term a
 * CustomDisjunctionMaxQuery searches for it in each field, while the set of
 * these CustomDisjunctionMaxQuery's is combined into a BooleanQuery. The tie
 * breaker capability allows results that include the same term in multiple
 * fields to be judged better than results that include this term in only the
 * best of those multiple fields, without confusing this with the better case of
 * two different terms in the multiple fields.
 */
public class CustomDisjunctionMaxQuery extends CustomQuery {
  private float boost = 0.0f;

  @Override
  public float getBoost() {
    return this.boost;
  }

  @Override
  public void setBoost(float b) {
    this.boost = b;
  }

  /* The subqueries */
  private ArrayList<Query> disjuncts = new ArrayList<Query>();

  /**
   * Creates a new empty CustomDisjunctionMaxQuery. Use add() to add the
   * subqueries.
   * 
   * @param tieBreakerMultiplier
   *          the score of each non-maximum disjunct for a document is
   *          multiplied by this weight and added into the final score. If
   *          non-zero, the value should be small, on the order of 0.1, which
   *          says that 10 occurrences of word in a lower-scored field that is
   *          also in a higher scored field is just as good as a unique word in
   *          the lower scored field (i.e., one that is not in any higher scored
   *          field.
   */
  public CustomDisjunctionMaxQuery() {}

  /**
   * Creates a new CustomDisjunctionMaxQuery
   * 
   * @param disjuncts
   *          a Collection<Query> of all the disjuncts to add
   * @param tieBreakerMultiplier
   *          the weight to give to each matching non-maximum disjunct
   */
  public CustomDisjunctionMaxQuery(Collection<Query> disjuncts) {
    add(disjuncts);
  }

  /**
   * Add a subquery to this disjunction
   * 
   * @param query
   *          the disjunct added
   */
  public void add(Query query) {
    this.disjuncts.add(query);
  }

  /**
   * Add a collection of disjuncts to this disjunction via Iterable<Query>
   */
  public void add(Collection<Query> disjuncts) {
    this.disjuncts.addAll(disjuncts);
  }

  /** An Iterator<Query> over the disjuncts */
  public Iterator<Query> iterator() {
    return this.disjuncts.iterator();
  }

  /**
   * Expert: the Weight for CustomDisjunctionMaxQuery, used to normalize, score
   * and explain these queries.
   * 
   * <p>
   * NOTE: this API and implementation is subject to change suddenly in the next
   * release.
   * </p>
   */
  protected class CustomDisjunctionMaxWeight extends Weight {
    /** The Similarity implementation. */
    protected Similarity similarity;

    /** The Weights for our subqueries, in 1-1 correspondence with disjuncts */
    protected ArrayList<Weight> weights = new ArrayList<Weight>(); // The
                                                                   // Weight's
                                                                   // for our
                                                                   // subqueries,
                                                                   // in 1-1
                                                                   // correspondence
                                                                   // with
                                                                   // disjuncts

    /*
     * Construct the Weight for this Query searched by searcher. Recursively
     * construct subquery weights.
     */
    public CustomDisjunctionMaxWeight(Searcher searcher) throws IOException {
      this.similarity = searcher.getSimilarity();
      for (Iterator<Query> iter = CustomDisjunctionMaxQuery.this.disjuncts.iterator(); iter.hasNext();) {
        this.weights.add((iter.next()).createWeight(searcher));
      }
    }

    /* Return our associated CustomDisjunctionMaxQuery */
    @Override
    public Query getQuery() {
      return CustomDisjunctionMaxQuery.this;
    }

    /* Return our boost */
    @Override
    public float getValue() {
      return 1f;
    }

    /*
     * Compute the sub of squared weights of us applied to our subqueries. Used
     * for normalization.
     */
    @Override
    public float sumOfSquaredWeights() throws IOException {
      return 1;
    }

    /* Apply the computed normalization factor to our subqueries */
    @Override
    public void normalize(float norm) {}

    /* Create the scorer used to score our associated CustomDisjunctionMaxQuery */
    @Override
    public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {
      Scorer[] scorers = new Scorer[this.weights.size()];
      int idx = 0;
      for (Iterator<Weight> iter = this.weights.iterator(); iter.hasNext();) {
        Weight w = iter.next();
        // lazy about the downcast right now: adichad
        Scorer subScorer = w.scorer(reader, true, false);
        if ((subScorer != null) && (subScorer.nextDoc() != DocIdSetIterator.NO_MORE_DOCS)) {
          scorers[idx++] = subScorer;
        }
      }
      if (idx == 0)
        return null; // all scorers did not have documents
      CustomDisjunctionMaxScorer result = new CustomDisjunctionMaxScorer(this, this.similarity, scorers, idx);
      return result;
    }

    /* Explain the score we computed for doc */
    @Override
    public Explanation explain(IndexReader reader, int doc) throws IOException {
      if (CustomDisjunctionMaxQuery.this.disjuncts.size() == 1)
        return (this.weights.get(0)).explain(reader, doc);
      ComplexExplanation result = new ComplexExplanation();
      float max = 0.0f;
      result.setDescription("max of:");
      for (Iterator<Weight> iter = this.weights.iterator(); iter.hasNext();) {
        Explanation e = (iter.next()).explain(reader, doc);
        if (e.isMatch()) {
          result.setMatch(Boolean.TRUE);
          result.addDetail(e);

          max = Math.max(max, e.getValue());
        }
      }
      result.setValue(max);
      return result;
    }

  } // end of CustomDisjunctionMaxWeight inner class

  /* Create the Weight used to score us */
  @Override
  public Weight createWeight(Searcher searcher) throws IOException {
    return new CustomDisjunctionMaxWeight(searcher);
  }

  /**
   * Optimize our representation and our subqueries representations
   * 
   * @param reader
   *          the IndexReader we query
   * @return an optimized copy of us (which may not be a copy if there is
   *         nothing to optimize)
   */
  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    int numDisjunctions = this.disjuncts.size();
    if (numDisjunctions == 1) {
      Query singleton = this.disjuncts.get(0);
      Query result = singleton.rewrite(reader);
      if (getBoost() != 1.0f) {
        if (result == singleton)
          result = (Query) result.clone();
        result.setBoost(getBoost() * result.getBoost());
      }
      return result;
    }
    CustomDisjunctionMaxQuery clone = null;
    for (int i = 0; i < numDisjunctions; i++) {
      Query clause = this.disjuncts.get(i);
      Query rewrite = clause.rewrite(reader);
      if (rewrite != clause) {
        if (clone == null)
          clone = (CustomDisjunctionMaxQuery) this.clone();
        clone.disjuncts.set(i, rewrite);
      }
    }
    if (clone != null)
      return clone;
    else
      return this;
  }

  /**
   * Create a shallow copy of us -- used in rewriting if necessary
   * 
   * @return a copy of us (but reuse, don't copy, our subqueries)
   */
  @Override
  public Object clone() {
    CustomDisjunctionMaxQuery clone = (CustomDisjunctionMaxQuery) super.clone();
    clone.disjuncts = (ArrayList<Query>) this.disjuncts.clone();
    return clone;
  }

  // inherit javadoc
  @Override
  public void extractTerms(Set<Term> terms) {
    for (Iterator<Query> iter = this.disjuncts.iterator(); iter.hasNext();) {
      (iter.next()).extractTerms(terms);
    }
  }

  /**
   * Prettyprint us.
   * 
   * @param field
   *          the field to which we are applied
   * @return a string that shows what we do, of the form
   *         "(disjunct1 | disjunct2 | ... | disjunctn)^boost"
   */
  @Override
  public String toString(String field) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("(");
    int numDisjunctions = this.disjuncts.size();
    for (int i = 0; i < numDisjunctions; i++) {
      Query subquery = this.disjuncts.get(i);
      if (subquery instanceof BooleanQuery) { // wrap sub-bools in parens
        buffer.append("(");
        buffer.append(subquery.toString(field));
        buffer.append(")");
      } else
        buffer.append(subquery.toString(field));
      if (i != numDisjunctions - 1)
        buffer.append(" | ");
    }
    buffer.append(")");
    if (getBoost() != 1.0) {
      buffer.append("^");
      buffer.append(getBoost());
    }
    return buffer.toString();
  }

  /**
   * Return true iff we represent the same query as o
   * 
   * @param o
   *          another object
   * @return true iff o is a CustomDisjunctionMaxQuery with the same boost and
   *         the same subqueries, in the same order, as us
   */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CustomDisjunctionMaxQuery))
      return false;
    CustomDisjunctionMaxQuery other = (CustomDisjunctionMaxQuery) o;
    return (this.getBoost() == other.getBoost()) && this.disjuncts.equals(other.disjuncts);
  }

  /**
   * Compute a hash code for hashing us
   * 
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return Float.floatToIntBits(getBoost()) + this.disjuncts.hashCode();
  }

}
