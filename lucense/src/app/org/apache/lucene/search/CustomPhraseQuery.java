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
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Explanation.IDFExplanation;
import org.apache.lucene.util.ToStringUtils;

/**
 * A Query that matches documents containing a particular sequence of terms. A
 * CustomPhraseQuery is built by QueryParser for input like
 * <code>"new york"</code>.
 * 
 * <p>
 * This query may be combined with other terms or queries with a
 * {@link BooleanQuery}.
 */
public class CustomPhraseQuery extends CustomQuery {
  private float boost = 0.0f;

  @Override
  public float getBoost() {
    return this.boost;
  }

  @Override
  public void setBoost(float b) {
    this.boost = b;
  }

  private String field;

  private ArrayList terms = new ArrayList(4);

  private ArrayList positions = new ArrayList(4);

  private ArrayList qpositions = new ArrayList(4);

  private int maxPosition = 0;

  private int slop = 0;

  private TermDocsFactory termDocsFactory;

  /** Constructs an empty phrase query. */
  public CustomPhraseQuery(TermDocsFactory factory) {
    this.termDocsFactory = factory;
  }

  /**
   * Sets the number of other words permitted between words in query phrase. If
   * zero, then this is an exact phrase search. For larger values this works
   * like a <code>WITHIN</code> or <code>NEAR</code> operator.
   * 
   * <p>
   * The slop is in fact an edit-distance, where the units correspond to moves
   * of terms in the query phrase out of position. For example, to switch the
   * order of two words requires two moves (the first move places the words atop
   * one another), so to permit re-orderings of phrases, the slop must be at
   * least two.
   * 
   * <p>
   * More exact matches are scored higher than sloppier matches, thus search
   * results are sorted by exactness.
   * 
   * <p>
   * The slop is zero by default, requiring exact matches.
   */
  public void setSlop(int s) {
    this.slop = s;
  }

  /** Returns the slop. See setSlop(). */
  public int getSlop() {
    return this.slop;
  }

  /**
   * Adds a term to the end of the query phrase. The relative position of the
   * term is the one immediately after the last term added.
   */
  public void add(Term term, int qpos) {
    int position = 0;
    if (this.positions.size() > 0)
      position = ((Integer) this.positions.get(this.positions.size() - 1)).intValue() + 1;

    add(term, position, qpos);
  }

  /**
   * Adds a term to the end of the query phrase. The relative position of the
   * term within the phrase is specified explicitly. This allows e.g. phrases
   * with more than one term at the same position or phrases with gaps (e.g. in
   * connection with stopwords).
   * 
   * @param term
   * @param position
   */
  public void add(Term term, int position, int qpos) {
    if (this.terms.size() == 0)
      this.field = term.field();
    else if (term.field() != this.field)
      throw new IllegalArgumentException("All phrase terms must be in the same field: " + term);

    this.terms.add(term);
    this.positions.add(new Integer(position));
    this.qpositions.add(new Integer(qpos));
    if (position > this.maxPosition)
      this.maxPosition = position;
  }

  /** Returns the set of terms in this phrase. */
  public Term[] getTerms() {
    return (Term[]) this.terms.toArray(new Term[0]);
  }

  /**
   * Returns the relative positions of terms in this phrase.
   */
  public int[] getPositions() {
    int[] result = new int[this.positions.size()];
    for (int i = 0; i < this.positions.size(); i++)
      result[i] = ((Integer) this.positions.get(i)).intValue();
    return result;
  }

  public int[] getQueryPositions() {
    int[] result = new int[this.qpositions.size()];
    for (int i = 0; i < this.qpositions.size(); i++)
      result[i] = ((Integer) this.qpositions.get(i)).intValue();
    return result;
  }

  private class PhraseWeight extends Weight {
    private Similarity similarity;

    private float value;

    private float idf;

    private float queryNorm;

    private float queryWeight;

    private IDFExplanation idfExp;

    private TermDocsFactory termDocsFactory;

    public PhraseWeight(Searcher searcher, TermDocsFactory termDocsFactory) throws IOException {
      this.similarity = getSimilarity(searcher);

      this.idfExp = this.similarity.idfExplain(CustomPhraseQuery.this.terms, searcher);
      this.idf = this.idfExp.getIdf();
      this.termDocsFactory = termDocsFactory;
    }

    @Override
    public String toString() {
      return "weight(" + CustomPhraseQuery.this + ")";
    }

    @Override
    public Query getQuery() {
      return CustomPhraseQuery.this;
    }

    @Override
    public float getValue() {
      return this.value;
    }

    @Override
    public float sumOfSquaredWeights() {
      return 1f;
    }

    @Override
    public void normalize(float queryNorm) {}

    @Override
    public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer) throws IOException {
      if (CustomPhraseQuery.this.terms.size() == 0) // optimize zero-term case
        return null;

      TermPositions[] tps = new TermPositions[CustomPhraseQuery.this.terms.size()];
      TermPositions[] tps2 = new TermPositions[CustomPhraseQuery.this.terms.size()];
      for (int i = 0; i < CustomPhraseQuery.this.terms.size(); i++) {
        TermPositions p = reader.termPositions((Term) CustomPhraseQuery.this.terms.get(i));
        TermPositions p2 = reader.termPositions((Term) CustomPhraseQuery.this.terms.get(i));
        if (p == null) {
          System.out.println("p is null");
          return null;
        }

        if (this.termDocsFactory != null) {
          p = (TermPositions) this.termDocsFactory.wrapTermDocs(p);
          p2 = (TermPositions) this.termDocsFactory.wrapTermDocs(p2);
        }
        tps[i] = p;
        tps2[i] = p2;
      }

      if (CustomPhraseQuery.this.slop == 0) // optimize exact case
        return new CustomExactPhraseScorer(this, tps, getPositions(), this.similarity,
            reader.norms(CustomPhraseQuery.this.field), CustomPhraseQuery.this.field, CustomPhraseQuery.this.terms,
            getQueryPositions(), tps2);
      else
        return new CustomSloppyPhraseScorer(this, tps, getPositions(), this.similarity, CustomPhraseQuery.this.slop,
            reader.norms(CustomPhraseQuery.this.field), CustomPhraseQuery.this.field, CustomPhraseQuery.this.terms,
            getQueryPositions(), tps2);

    }

    @Override
    public Explanation explain(IndexReader reader, int doc) throws IOException {

      Explanation result = new Explanation();
      result.setDescription("weight(" + getQuery() + " in " + doc + "), product of:");

      StringBuffer docFreqs = new StringBuffer();
      StringBuffer query = new StringBuffer();
      query.append('\"');
      docFreqs.append(this.idfExp.explain());
      for (int i = 0; i < CustomPhraseQuery.this.terms.size(); i++) {
        if (i != 0) {
          query.append(" ");
        }

        Term term = (Term) CustomPhraseQuery.this.terms.get(i);

        query.append(term.text());
      }
      query.append('\"');

      Explanation idfExpl = new Explanation(this.idf, "idf(" + CustomPhraseQuery.this.field + ":" + docFreqs + ")");

      // explain query weight
      Explanation queryExpl = new Explanation();
      queryExpl.setDescription("queryWeight(" + getQuery() + "), product of:");

      Explanation boostExpl = new Explanation(getBoost(), "boost");
      if (getBoost() != 1.0f)
        queryExpl.addDetail(boostExpl);
      queryExpl.addDetail(idfExpl);

      Explanation queryNormExpl = new Explanation(this.queryNorm, "queryNorm");
      queryExpl.addDetail(queryNormExpl);

      queryExpl.setValue(boostExpl.getValue() * idfExpl.getValue() * queryNormExpl.getValue());

      result.addDetail(queryExpl);

      // explain field weight
      Explanation fieldExpl = new Explanation();
      fieldExpl.setDescription("fieldWeight(" + CustomPhraseQuery.this.field + ":" + query + " in " + doc
          + "), product of:");

      Scorer scorer = scorer(reader, true, false);
      if (scorer == null) {
        return new Explanation(0.0f, "no matching docs");
      }
      Explanation tfExpl = explain(reader, doc);
      fieldExpl.addDetail(tfExpl);
      fieldExpl.addDetail(idfExpl);

      Explanation fieldNormExpl = new Explanation();
      byte[] fieldNorms = reader.norms(CustomPhraseQuery.this.field);
      float fieldNorm = fieldNorms != null ? Similarity.decodeNorm(fieldNorms[doc]) : 1.0f;
      fieldNormExpl.setValue(fieldNorm);
      fieldNormExpl.setDescription("fieldNorm(field=" + CustomPhraseQuery.this.field + ", doc=" + doc + ")");
      fieldExpl.addDetail(fieldNormExpl);

      fieldExpl.setValue(tfExpl.getValue() * idfExpl.getValue() * fieldNormExpl.getValue());

      result.addDetail(fieldExpl);

      // combine them
      result.setValue(queryExpl.getValue() * fieldExpl.getValue());

      if (queryExpl.getValue() == 1.0f)
        return fieldExpl;

      return result;
    }
  }

  @Override
  public Weight createWeight(Searcher searcher) throws IOException {
    if (this.terms.size() == 1) { // optimize one-term case
      Term term = (Term) this.terms.get(0);
      Query termQuery = new CustomTermQuery(term, (Integer) this.qpositions.get(0), this.termDocsFactory);
      termQuery.setBoost(getBoost());
      return termQuery.createWeight(searcher);
    }
    return new PhraseWeight(searcher, this.termDocsFactory);
  }

  /**
   * @see org.apache.lucene.search.Query#extractTerms(java.util.Set)
   */
  @Override
  public void extractTerms(Set queryTerms) {
    queryTerms.addAll(this.terms);
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(String f) {
    StringBuffer buffer = new StringBuffer();
    if ((this.field != null) && !this.field.equals(f)) {
      buffer.append(this.field);
      buffer.append(":");
    }

    buffer.append("\"");
    String[] pieces = new String[this.maxPosition + 1];
    for (int i = 0; i < this.terms.size(); i++) {
      int pos = ((Integer) this.positions.get(i)).intValue();
      String s = pieces[pos];
      if (s == null) {
        s = ((Term) this.terms.get(i)).text();
      } else {
        s = s + "|" + ((Term) this.terms.get(i)).text();
      }
      pieces[pos] = s;
    }
    for (int i = 0; i < pieces.length; i++) {
      if (i > 0) {
        buffer.append(' ');
      }
      String s = pieces[i];
      if (s == null) {
        buffer.append('?');
      } else {
        buffer.append(s);
      }
    }
    buffer.append("\"");

    if (this.slop != 0) {
      buffer.append("~");
      buffer.append(this.slop);
    }

    buffer.append(ToStringUtils.boost(getBoost()));

    return buffer.toString();
  }

  /** Returns true iff <code>o</code> is equal to this. */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof CustomPhraseQuery))
      return false;
    CustomPhraseQuery other = (CustomPhraseQuery) o;
    return (this.getBoost() == other.getBoost()) && (this.slop == other.slop) && this.terms.equals(other.terms)
        && this.positions.equals(other.positions);
  }

  /** Returns a hash code value for this object. */
  @Override
  public int hashCode() {
    return Float.floatToIntBits(getBoost()) ^ this.slop ^ this.terms.hashCode() ^ this.positions.hashCode();
  }

}
