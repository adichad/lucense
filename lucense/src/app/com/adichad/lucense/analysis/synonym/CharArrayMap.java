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

package com.adichad.lucense.analysis.synonym;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A simple class that stores key Strings as char[]'s in a hash table. Note that
 * this is not a general purpose class. For example, it cannot remove items from
 * the map, nor does it resize its hash table to be smaller, etc. It is designed
 * to be quick to retrieve items by char[] keys without the necessity of
 * converting to a String first.
 */

public class CharArrayMap<V> extends AbstractMap<String, V> implements Map<String, V>, Cloneable, Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final static int INIT_SIZE = 2;

  private char[][] keys;

  private Object[] values;

  private int count;

  private final boolean ignoreCase;

  /**
   * Create map with enough capacity to hold startSize terms
   */
  public CharArrayMap(int initialCapacity, boolean ignoreCase) {
    this.ignoreCase = ignoreCase;
    int size = INIT_SIZE;
    // load factor of .75, inverse is 1.25, or x+x/4
    initialCapacity = initialCapacity + (initialCapacity >> 2);
    while (size <= initialCapacity)
      size <<= 1;
    this.keys = new char[size][];
    this.values = new Object[size];
  }

  public boolean ignoreCase() {
    return this.ignoreCase;
  }

  public V get(char[] key) {
    return get(key, 0, key.length);
  }

  public V get(char[] key, int off, int len) {
    return (V) this.values[getSlot(key, off, len)];
  }

  public V get(CharSequence key) {
    return (V) this.values[getSlot(key)];
  }

  @Override
  public V get(Object key) {
    return (V) this.values[getSlot(key)];
  }

  @Override
  public boolean containsKey(Object s) {
    return this.keys[getSlot(s)] != null;
  }

  @Override
  public boolean containsValue(Object value) {
    if (value == null) {
      // search for key with a null value
      for (int i = 0; i < this.keys.length; i++) {
        if ((this.keys[i] != null) && (this.values[i] == null))
          return true;
      }
      return false;
    }

    for (int i = 0; i < this.values.length; i++) {
      Object val = this.values[i];
      if ((val != null) && value.equals(val))
        return true;
    }
    return false;
  }

  private int getSlot(Object key) {
    if (key instanceof char[]) {
      char[] keyc = (char[]) key;
      return getSlot(keyc, 0, keyc.length);
    }
    return getSlot((CharSequence) key);
  }

  private int getSlot(char[] key, int off, int len) {
    int code = getHashCode(key, len);
    int pos = code & (this.keys.length - 1);
    char[] key2 = this.keys[pos];
    if ((key2 != null) && !equals(key, off, len, key2)) {
      final int inc = ((code >> 8) + code) | 1;
      do {
        code += inc;
        pos = code & (this.keys.length - 1);
        key2 = this.keys[pos];
      } while ((key2 != null) && !equals(key, off, len, key2));
    }
    return pos;
  }

  /** Returns true if the String is in the set */
  private int getSlot(CharSequence key) {
    int code = getHashCode(key);
    int pos = code & (this.keys.length - 1);
    char[] key2 = this.keys[pos];
    if ((key2 != null) && !equals(key, key2)) {
      final int inc = ((code >> 8) + code) | 1;
      do {
        code += inc;
        pos = code & (this.keys.length - 1);
        key2 = this.keys[pos];
      } while ((key2 != null) && !equals(key, key2));
    }
    return pos;
  }

  public V put(CharSequence key, V val) {
    return put(key.toString(), val); // could be more efficient
  }

  @Override
  public V put(String key, V val) {
    return put(key.toCharArray(), val);
  }

  /**
   * Add this key,val pair to the map. The char[] key is directly used, no copy
   * is made. If ignoreCase is true for this Map, the key array will be directly
   * modified. The user should never modify the key after calling this method.
   */
  public V put(char[] key, Object val) {
    if (this.ignoreCase)
      for (int i = 0; i < key.length; i++)
        key[i] = Character.toLowerCase(key[i]);
    int slot = getSlot(key, 0, key.length);
    if (this.keys[slot] == null)
      this.count++;
    Object prev = this.values[slot];
    this.keys[slot] = key;
    this.values[slot] = val;

    if (this.count + (this.count >> 2) >= this.keys.length) {
      rehash();
    }

    return (V) prev;
  }

  private boolean equals(char[] text1, int off, int len, char[] text2) {
    if (len != text2.length)
      return false;
    if (this.ignoreCase) {
      for (int i = 0; i < len; i++) {
        if (Character.toLowerCase(text1[off + i]) != text2[i])
          return false;
      }
    } else {
      for (int i = 0; i < len; i++) {
        if (text1[off + i] != text2[i])
          return false;
      }
    }
    return true;
  }

  private boolean equals(CharSequence text1, char[] text2) {
    int len = text1.length();
    if (len != text2.length)
      return false;
    if (this.ignoreCase) {
      for (int i = 0; i < len; i++) {
        if (Character.toLowerCase(text1.charAt(i)) != text2[i])
          return false;
      }
    } else {
      for (int i = 0; i < len; i++) {
        if (text1.charAt(i) != text2[i])
          return false;
      }
    }
    return true;
  }

  private void rehash() {
    final int newSize = 2 * this.keys.length;
    char[][] oldEntries = this.keys;
    Object[] oldValues = this.values;
    this.keys = new char[newSize][];
    this.values = new Object[newSize];

    for (int i = 0; i < oldEntries.length; i++) {
      char[] key = oldEntries[i];
      if (key != null) {
        // todo: could be faster... no need to compare keys on collision
        // since they are unique
        int newSlot = getSlot(key, 0, key.length);
        this.keys[newSlot] = key;
        this.values[newSlot] = oldValues[i];
      }
    }
  }

  private int getHashCode(char[] text, int len) {
    int code = 0;
    if (this.ignoreCase) {
      for (int i = 0; i < len; i++) {
        code = code * 31 + Character.toLowerCase(text[i]);
      }
    } else {
      for (int i = 0; i < len; i++) {
        code = code * 31 + text[i];
      }
    }
    return code;
  }

  private int getHashCode(CharSequence text) {
    int code;
    if (this.ignoreCase) {
      code = 0;
      int len = text.length();
      for (int i = 0; i < len; i++) {
        code = code * 31 + Character.toLowerCase(text.charAt(i));
      }
    } else {
      if (false && (text instanceof String)) {
        code = text.hashCode();
      } else {
        code = 0;
        int len = text.length();
        for (int i = 0; i < len; i++) {
          code = code * 31 + text.charAt(i);
        }
      }
    }
    return code;
  }

  @Override
  public int size() {
    return this.count;
  }

  @Override
  public boolean isEmpty() {
    return this.count == 0;
  }

  @Override
  public void clear() {
    this.count = 0;
    Arrays.fill(this.keys, null);
    Arrays.fill(this.values, null);
  }

  @Override
  public Set<Entry<String, V>> entrySet() {
    return new EntrySet();
  }

  /** Returns an EntryIterator over this Map. */
  public EntryIterator iterator() {
    return new EntryIterator();
  }

  /** public iterator class so efficient methods are exposed to users */
  public class EntryIterator implements Iterator<Map.Entry<String, V>> {
    int pos = -1;

    int lastPos;

    EntryIterator() {
      goNext();
    }

    private void goNext() {
      this.lastPos = this.pos;
      this.pos++;
      while ((this.pos < CharArrayMap.this.keys.length) && (CharArrayMap.this.keys[this.pos] == null))
        this.pos++;
    }

    @Override
    public boolean hasNext() {
      return this.pos < CharArrayMap.this.keys.length;
    }

    /** gets the next key... do not modify the returned char[] */
    public char[] nextKey() {
      goNext();
      return CharArrayMap.this.keys[this.lastPos];
    }

    /** gets the next key as a newly created String object */
    public String nextKeyString() {
      return new String(nextKey());
    }

    /** returns the value associated with the last key returned */
    public V currentValue() {
      return (V) CharArrayMap.this.values[this.lastPos];
    }

    /** sets the value associated with the last key returned */
    public V setValue(V value) {
      V old = (V) CharArrayMap.this.values[this.lastPos];
      CharArrayMap.this.values[this.lastPos] = value;
      return old;
    }

    /**
     * Returns an Entry<String,V> object created on the fly... use
     * nextCharArray() + currentValie() for better efficiency.
     */
    @Override
    public Map.Entry<String, V> next() {
      goNext();
      return new MapEntry(this.lastPos);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  private class MapEntry implements Map.Entry<String, V> {
    final int pos;

    MapEntry(int pos) {
      this.pos = pos;
    }

    public char[] getCharArr() {
      return CharArrayMap.this.keys[this.pos];
    }

    @Override
    public String getKey() {
      return new String(getCharArr());
    }

    @Override
    public V getValue() {
      return (V) CharArrayMap.this.values[this.pos];
    }

    @Override
    public V setValue(V value) {
      V old = (V) CharArrayMap.this.values[this.pos];
      CharArrayMap.this.values[this.pos] = value;
      return old;
    }

    @Override
    public String toString() {
      return getKey() + '=' + getValue();
    }
  }

  private class EntrySet extends AbstractSet<Map.Entry<String, V>> {
    @Override
    public EntryIterator iterator() {
      return new EntryIterator();
    }

    @Override
    public boolean contains(Object o) {
      if (!(o instanceof Map.Entry))
        return false;
      Map.Entry e = (Map.Entry) o;
      Object key = e.getKey();
      if (key == null)
        return false; // we don't support null keys
      Object val = e.getValue();
      Object v = get(key);
      return v == null ? val == null : v.equals(val);
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
      return CharArrayMap.this.count;
    }

    @Override
    public void clear() {
      CharArrayMap.this.clear();
    }
  }

  @Override
  public Object clone() {
    CharArrayMap<V> map = null;
    try {
      map = (CharArrayMap<V>) super.clone();
      map.keys = this.keys.clone();
      map.values = this.values.clone();
    } catch (CloneNotSupportedException e) {
      // impossible
    }
    return map;
  }
}
