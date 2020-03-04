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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.ToStringUtils;

/**
 * A Query that matches documents containing terms with a specified prefix. A
 * CustomPrefixQuery is built by QueryParser for input like <code>app*</code>.
 * 
 * <p>
 * This query uses the
 * {@link MultiTermQuery#CONSTANT_SCORE_AUTO_REWRITE_DEFAULT} rewrite method.
 */
public class CustomPrefixQuery extends CustomMultiTermQuery {
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

  private Term prefix;

  /**
   * Constructs a query for terms starting with <code>prefix</code>.
   * 
   * @param qpos
   */
  public CustomPrefixQuery(Term prefix, int qpos) {
    super(prefix); // will be removed in 3.0
    this.prefix = prefix;
    this.qpos = qpos;
  }

  /** Returns the prefix of this query. */
  public Term getPrefix() {
    return this.prefix;
  }

  @Override
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    return new PrefixTermEnum(reader, this.prefix);
  }

  /** Prints a user-readable version of this query. */
  @Override
  public String toString(String field) {
    StringBuffer buffer = new StringBuffer();
    if (!this.prefix.field().equals(field)) {
      buffer.append(this.prefix.field());
      buffer.append(":");
    }
    buffer.append(this.prefix.text());
    buffer.append('*');
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  // @Override
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((this.prefix == null) ? 0 : this.prefix.hashCode());
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
    CustomPrefixQuery other = (CustomPrefixQuery) obj;
    if (this.prefix == null) {
      if (other.prefix != null)
        return false;
    } else if (!this.prefix.equals(other.prefix))
      return false;
    return true;
  }

  @Override
  public int getQueryPos() {
    return this.qpos;
  }

}
