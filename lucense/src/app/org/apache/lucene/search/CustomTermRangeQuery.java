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
import java.text.Collator;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.ToStringUtils;

/**
 * A Query that matches documents within an range of terms.
 * 
 * <p>
 * This query matches the documents looking for terms that fall into the
 * supplied range according to {@link String#compareTo(String)}, unless a
 * <code>Collator</code> is provided. It is not intended for numerical ranges;
 * use {@link NumericRangeQuery} instead.
 * 
 * <p>
 * This query uses the
 * {@link MultiTermQuery#CONSTANT_SCORE_AUTO_REWRITE_DEFAULT} rewrite method.
 * 
 * @since 2.9
 */

public class CustomTermRangeQuery extends CustomMultiTermQuery {
  private String lowerTerm;

  private String upperTerm;

  private Collator collator;

  private String field;

  private boolean includeLower;

  private boolean includeUpper;

  private float boost = 0.0f;

  private int qpos;

  @Override
  public float getBoost() {
    return this.boost;
  }

  @Override
  public void setBoost(float b) {
    this.boost = b;
  }

  /**
   * Constructs a query selecting all terms greater/equal than
   * <code>lowerTerm</code> but less/equal than <code>upperTerm</code>.
   * 
   * <p>
   * If an endpoint is null, it is said to be "open". Either or both endpoints
   * may be open. Open endpoints may not be exclusive (you can't select all but
   * the first or last term without explicitly specifying the term to exclude.)
   * 
   * @param field
   *          The field that holds both lower and upper terms.
   * @param lowerTerm
   *          The term text at the lower end of the range
   * @param upperTerm
   *          The term text at the upper end of the range
   * @param includeLower
   *          If true, the <code>lowerTerm</code> is included in the range.
   * @param includeUpper
   *          If true, the <code>upperTerm</code> is included in the range.
   */
  public CustomTermRangeQuery(String field, String lowerTerm, String upperTerm, boolean includeLower,
      boolean includeUpper, int qpos) {
    this(field, lowerTerm, upperTerm, includeLower, includeUpper, null, qpos);
  }

  /**
   * Constructs a query selecting all terms greater/equal than
   * <code>lowerTerm</code> but less/equal than <code>upperTerm</code>.
   * <p>
   * If an endpoint is null, it is said to be "open". Either or both endpoints
   * may be open. Open endpoints may not be exclusive (you can't select all but
   * the first or last term without explicitly specifying the term to exclude.)
   * <p>
   * If <code>collator</code> is not null, it will be used to decide whether
   * index terms are within the given range, rather than using the Unicode code
   * point order in which index terms are stored.
   * <p>
   * <strong>WARNING:</strong> Using this constructor and supplying a non-null
   * value in the <code>collator</code> parameter will cause every single index
   * Term in the Field referenced by lowerTerm and/or upperTerm to be examined.
   * Depending on the number of index Terms in this Field, the operation could
   * be very slow.
   * 
   * @param lowerTerm
   *          The Term text at the lower end of the range
   * @param upperTerm
   *          The Term text at the upper end of the range
   * @param includeLower
   *          If true, the <code>lowerTerm</code> is included in the range.
   * @param includeUpper
   *          If true, the <code>upperTerm</code> is included in the range.
   * @param collator
   *          The collator to use to collate index Terms, to determine their
   *          membership in the range bounded by <code>lowerTerm</code> and
   *          <code>upperTerm</code>.
   * @param qpos
   */
  public CustomTermRangeQuery(String field, String lowerTerm, String upperTerm, boolean includeLower,
      boolean includeUpper, Collator collator, int qpos) {
    this.field = field;
    this.lowerTerm = lowerTerm;
    this.upperTerm = upperTerm;
    this.includeLower = includeLower;
    this.includeUpper = includeUpper;
    this.collator = collator;
    this.qpos = qpos;
  }

  /** Returns the field name for this query */
  public String getField() {
    return this.field;
  }

  /** Returns the lower value of this range query */
  public String getLowerTerm() {
    return this.lowerTerm;
  }

  /** Returns the upper value of this range query */
  public String getUpperTerm() {
    return this.upperTerm;
  }

  /** Returns <code>true</code> if the lower endpoint is inclusive */
  public boolean includesLower() {
    return this.includeLower;
  }

  /** Returns <code>true</code> if the upper endpoint is inclusive */
  public boolean includesUpper() {
    return this.includeUpper;
  }

  /** Returns the collator used to determine range inclusion, if any. */
  public Collator getCollator() {
    return this.collator;
  }

  @Override
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    return new TermRangeTermEnum(reader, this.field, this.lowerTerm, this.upperTerm, this.includeLower,
        this.includeUpper, this.collator);
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(String field) {
    StringBuffer buffer = new StringBuffer();
    if (!getField().equals(field)) {
      buffer.append(getField());
      buffer.append(":");
    }
    buffer.append(this.includeLower ? '[' : '{');
    buffer.append(this.lowerTerm != null ? this.lowerTerm : "*");
    buffer.append(" TO ");
    buffer.append(this.upperTerm != null ? this.upperTerm : "*");
    buffer.append(this.includeUpper ? ']' : '}');
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  // @Override
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((this.collator == null) ? 0 : this.collator.hashCode());
    result = prime * result + ((this.field == null) ? 0 : this.field.hashCode());
    result = prime * result + (this.includeLower ? 1231 : 1237);
    result = prime * result + (this.includeUpper ? 1231 : 1237);
    result = prime * result + ((this.lowerTerm == null) ? 0 : this.lowerTerm.hashCode());
    result = prime * result + ((this.upperTerm == null) ? 0 : this.upperTerm.hashCode());
    return result;
  }

  // @Override
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    CustomTermRangeQuery other = (CustomTermRangeQuery) obj;
    if (this.collator == null) {
      if (other.collator != null)
        return false;
    } else if (!this.collator.equals(other.collator))
      return false;
    if (this.field == null) {
      if (other.field != null)
        return false;
    } else if (!this.field.equals(other.field))
      return false;
    if (this.includeLower != other.includeLower)
      return false;
    if (this.includeUpper != other.includeUpper)
      return false;
    if (this.lowerTerm == null) {
      if (other.lowerTerm != null)
        return false;
    } else if (!this.lowerTerm.equals(other.lowerTerm))
      return false;
    if (this.upperTerm == null) {
      if (other.upperTerm != null)
        return false;
    } else if (!this.upperTerm.equals(other.upperTerm))
      return false;
    return true;
  }

  @Override
  public int getQueryPos() {
    return this.qpos;
  }

}
