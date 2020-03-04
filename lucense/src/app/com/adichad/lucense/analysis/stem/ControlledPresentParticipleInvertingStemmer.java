package com.adichad.lucense.analysis.stem;

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

/*

 Porter stemmer in Java. The original paper is in

 Porter, 1980, An algorithm for suffix stripping, Program, Vol. 14,
 no. 3, pp 130-137,

 See also http://www.tartarus.org/~martin/PorterStemmer/index.html

 Bug 1 (reported by Gonzalo Parra 16/10/99) fixed as marked below.
 Tthe words 'aed', 'eed', 'oed' leave k at 'a' for step 3, and b[k-1]
 is then out outside the bounds of b.

 Similarly,

 Bug 2 (reported by Steve Dyrdahl 22/2/00) fixed as marked below.
 'ion' by itself leaves j = -1 in the test for 'ion' in step 5, and
 b[j] is then outside the bounds of b.

 Release 3.

 [ This version is derived from Release 3, modified by Brian Goetz to
 optimize for fewer object creations.  ]

 */

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * Stemmer, implementing the Porter Stemming Algorithm
 * 
 * The Stemmer class transforms a word into its root form. The input word can be
 * provided a character at time (by calling add()), or at once by calling one of
 * the various stem(something) methods.
 */

public class ControlledPresentParticipleInvertingStemmer implements InvertingStemmer {
  private char[] b;

  private int i, /* offset into b */
  j, k, k0;

  private boolean dirty = false;

  private Set<String> blackList;

  private int minLen;

  private static final int INC = 50; /*
                                      * unit of size whereby b is increased
                                      */

  private static final int EXTRA = 1;

  private ControlledPresentParticipleStemmer plainStemmer;

  private StemInversionAttribute stemAtt;

  private String original;

  public ControlledPresentParticipleInvertingStemmer(StemInversionAttribute stemAtt) {
    this.b = new char[INC];
    this.i = 0;
    this.blackList = new HashSet<String>();
    this.minLen = 1;
    this.stemAtt = stemAtt;
    this.stemAtt.setLast(this);
  }

  public ControlledPresentParticipleInvertingStemmer(Set<String> blackList, StemInversionAttribute stemAtt) {
    this(stemAtt);
    if (blackList != null) {
      this.blackList = blackList;
    }
    this.plainStemmer = new ControlledPresentParticipleStemmer(blackList, this.minLen);
  }

  public ControlledPresentParticipleInvertingStemmer(Set<String> blackList, int minLen, StemInversionAttribute stemAtt) {
    this(blackList, stemAtt);
    this.minLen = minLen;
    this.plainStemmer = new ControlledPresentParticipleStemmer(blackList, minLen);
  }

  public ControlledPresentParticipleInvertingStemmer(int minLen, StemInversionAttribute stemAtt) {
    this(stemAtt);
    this.minLen = minLen;
    this.plainStemmer = new ControlledPresentParticipleStemmer(this.blackList, minLen);
  }

  /**
   * reset() resets the stemmer so it can stem another word. If you invoke the
   * stemmer by calling add(char) and then stem(), you must call reset() before
   * starting another word.
   */
  public void reset() {
    this.i = 0;
    this.dirty = false;
  }

  /**
   * Add a character to the word being stemmed. When you are finished adding
   * characters, you can call stem(void) to process the word.
   */
  public void add(char ch) {

    if (this.b.length <= this.i + EXTRA) {
      char[] new_b = new char[this.b.length + INC];
      System.arraycopy(this.b, 0, new_b, 0, this.b.length);
      this.b = new_b;
    }
    this.b[this.i++] = ch;
    // for(int u = 0;u<b.length;u++ )
    // System.out.print(b[u]);
    // System.out.println();
  }

  /**
   * After a word has been stemmed, it can be retrieved by toString(), or a
   * reference to the internal buffer can be retrieved by getResultBuffer and
   * getResultLength (which is generally more efficient.)
   */
  @Override
  public String toString() {
    return new String(this.b, 0, this.i);
  }

  /**
   * Returns the length of the word resulting from the stemming process.
   */
  public int getResultLength() {
    return this.i;
  }

  /**
   * Returns a reference to a character buffer containing the results of the
   * stemming process. You also need to consult getResultLength() to determine
   * the length of the result.
   */
  public char[] getResultBuffer() {
    return this.b;
  }

  /* cons(i) is true <=> b[i] is a consonant. */

  private final boolean cons(int i) {
    switch (this.b[i]) {
    case 'a':
    case 'e':
    case 'i':
    case 'o':
    case 'u':
      return false;
    case 'y':
      return (i == this.k0) ? true : !cons(i - 1);
    default:
      return true;
    }
  }

  private final boolean ends(String s) {
    int l = s.length();
    int o = this.k - l + 1;
    if (o < this.k0)
      return false;
    for (int i = 0; i < l; i++)
      if (this.b[o + i] != s.charAt(i))
        return false;
    this.j = this.k - l;
    return true;
  }

  /*
   * step1() gets rid of plurals and -ed or -ing. e.g. caresses -> caress ponies
   * -> poni ties -> ti caress -> caress cats -> cat feed -> feed agreed ->
   * agree disabled -> disable matting -> mat mating -> mate meeting -> meet
   * milling -> mill messing -> mess meetings -> meet
   */

  private final boolean step1() {
    boolean flag = false;
    if (ends("ing")) {
      this.k -= 3;
      flag = true;
    }

    return flag;
  }

  /* step6() removes a final -e if m() > 1. */

  private final void step6() {
    this.j = this.k;
    int syllables = m();

    if (!((this.b[this.k] == 'w') || (this.b[this.k] == 'x') || (this.b[this.k] == 'y'))) {
      if (cons(this.k)) {
        if (this.k > 0) {
          if (this.b[this.k] == this.b[this.k - 1] && !(this.b[this.k] == 'l' && syllables == 1)) { // chop
                                                                                                    // double
                                                                                                    // consonent
            // other than s
            if (this.b[this.k] != 's')
              this.k--;
          } else if (!cons(this.k - 1)) {
            if (this.b[this.k] == 's' || this.b[this.k] == 'g' || this.b[this.k] == 'p')
              this.b[++this.k] = 'e';
            else if (this.b[this.k - 1] == 'u' && this.b[this.k] == 'r')
              this.b[++this.k] = 'e';
            else if (this.k >= 2) {
              if ((this.b[this.k - 2] != 'o') && (this.b[this.k - 1] == 'u')) {
                this.b[++this.k] = 'e';
              } else if ((this.b[this.k - 2] != 'e' && this.b[this.k - 2] != 'o') && (this.b[this.k - 1] == 'a')) {
                this.b[++this.k] = 'e';
              } else if ((this.b[this.k - 2] != 'o' && this.b[this.k - 2] != 'i') && (this.b[this.k - 1] == 'o')
                  && this.b[this.k] != 'n' && syllables <= 2) {
                this.b[++this.k] = 'e';
              } else if ((this.b[this.k - 2] != 'a') && (this.b[this.k - 1] == 'i')
                  && (syllables != 2 || this.b[this.k] != 't')) {
                this.b[++this.k] = 'e';
              }
            }
          } else if (((this.b[this.k] == 'k') && (this.b[this.k - 1] == 'c') && (syllables > 1))) {
            this.k--;
          } else if (this.b[this.k - 1] != 'l' && this.b[this.k] == 'l') { // penultimate
                                                                           // is
                                                                           // a
                                                                           // consonent
            this.b[++this.k] = 'e';
          } else if (this.b[this.k - 1] == 'r'
              && (this.b[this.k] == 's' || this.b[this.k] == 'g' || this.b[this.k] == 'v')) { // penultimate
                                                                                              // is
                                                                                              // an
                                                                                              // r<x>
            this.b[++this.k] = 'e';
          }

        }
      }
    } else if (this.k == 1) {
      this.b[this.k++] = 'i';
      this.b[this.k] = 'e';
    }
  }

  /*
   * m() measures the number of consonant sequences between k0 and j. if c is a
   * consonant sequence and v a vowel sequence, and <..> indicates arbitrary
   * presence, <c><v> gives 0 <c>vc<v> gives 1 <c>vcvc<v> gives 2 <c>vcvcvc<v>
   * gives 3 ....
   */

  private final int m() {
    int n = 0;
    int i = this.k0;
    while (true) {
      if (i > this.j)
        return n;
      if (!cons(i))
        break;
      i++;
    }
    i++;
    while (true) {
      while (true) {
        if (i > this.j)
          return n;
        if (cons(i))
          break;
        i++;
      }
      i++;
      n++;
      while (true) {
        if (i > this.j)
          return n;
        if (!cons(i))
          break;
        i++;
      }
      i++;
    }
  }

  /**
   * Stem a word provided as a String. Returns the result as a String.
   */
  public String stem(String s) {
    if (stem(s.toCharArray(), s.length()))
      return toString();
    else
      return s;
  }

  /**
   * Stem a word contained in a char[]. Returns true if the stemming process
   * resulted in a word different from the input. You can retrieve the result
   * with getResultLength()/getResultBuffer() or toString().
   */
  public boolean stem(char[] word) {
    return stem(word, word.length);
  }

  /**
   * Stem a word contained in a portion of a char[] array. Returns true if the
   * stemming process resulted in a word different from the input. You can
   * retrieve the result with getResultLength()/getResultBuffer() or toString().
   */
  public boolean stem(char[] wordBuffer, int offset, int wordLen) {
    reset();
    if (this.b.length < wordLen) {
      char[] new_b = new char[wordLen + EXTRA];
      this.b = new_b;
    }
    System.arraycopy(wordBuffer, offset, this.b, 0, wordLen);
    this.i = wordLen;
    return stem(0);
  }

  /**
   * Stem a word contained in a leading portion of a char[] array. Returns true
   * if the stemming process resulted in a word different from the input. You
   * can retrieve the result with getResultLength()/getResultBuffer() or
   * toString().
   */
  public boolean stem(char[] word, int wordLen) {
    return stem(word, 0, wordLen);
  }

  /**
   * Stem the word placed into the Stemmer buffer through calls to add().
   * Returns true if the stemming process resulted in a word different from the
   * input. You can retrieve the result with getResultLength()/getResultBuffer()
   * or toString().
   */
  public boolean stem() {
    return stem(0);
  }

  public boolean stem(int i0) {
    this.original = new String(this.b, 0, this.i);
    this.k = this.i - 1;
    this.k0 = i0;
    if (this.blackList.contains(this.original)) {
      addOriginal();
      return false;
    } else if (this.i < this.minLen) {
      addInversions();
      return false;
    }

    if (this.k > this.k0 + 1) {
      if (step1())
        step6();
    }
    // Also, a word is considered dirty if we lopped off letters
    // Thanks to Ifigenia Vairelles for pointing this out.
    if (this.i != this.k + 1)
      this.dirty = true;
    this.i = this.k + 1;
    addInversions();
    return this.dirty;
  }

  private void set(char[] c, int offset, String val) {
    for (int y = offset, x = 0; x < val.length(); y++, x++) {
      c[y] = val.charAt(x);
    }
  }

  private Set<String> generateInversions() {

    Set<String> possibles = new HashSet<String>(3);
    char c[] = new char[this.b.length + 4];
    System.arraycopy(this.b, 0, c, 0, this.i);

    if (this.b[this.k] == 'e') {
      if (k > 0) {
        if ((this.b[this.k - 1] == 'e') || (this.b[this.k - 1] == 'y') || (this.b[this.k - 1] == 'o')) {
          set(c, this.i, "ing");
          possibles.add(new String(c, 0, this.i + 3));
        } else if (cons(this.k - 1)) {
          set(c, this.i, "ing");
          possibles.add(new String(c, 0, this.i + 3));
          set(c, this.i - 1, "ing");
          possibles.add(new String(c, 0, this.i + 2));
        } else if (this.b[this.k - 1] == 'i') {
          set(c, this.i - 2, "ying");
          possibles.add(new String(c, 0, this.i + 2));
        } else {
          set(c, this.i, "ing");
          possibles.add(new String(c, 0, this.i + 3));
          set(c, this.i - 1, "ing");
          possibles.add(new String(c, 0, this.i + 2));
        }
      }
    } else if (this.b[this.k] == 'c') {
      set(c, this.i, "king");
      possibles.add(new String(c, 0, this.i + 4));
    } else if (this.b[this.k] == 'y') {
      set(c, this.i, "ing");
      possibles.add(new String(c, 0, this.i + 3));
    } else if ((this.k > 0) && !cons(this.k - 1)) {
      if ((this.b[this.k] == 'w') || (this.b[this.k] == 'x') || (this.b[this.k] == 'y')) {
        set(c, this.i, "ing");
        possibles.add(new String(c, 0, this.i + 3));
      } else if ((this.k > 1) && cons(this.k - 2)) {
        set(c, this.i, this.b[this.k] + "ing");
        possibles.add(new String(c, 0, this.i + 4));
        set(c, this.i, "ing");
        possibles.add(new String(c, 0, this.i + 3));
      } else {
        set(c, this.i, "ing");
        possibles.add(new String(c, 0, this.i + 3));
      }
    } else {
      set(c, this.i, "ing");
      possibles.add(new String(c, 0, this.i + 3));
    }
    return possibles;
  }

  private void addOriginal() {
    this.original = this.stemAtt.addStemInversion(this.original, this.original, this);
  }

  private void addInversions() {
    String stem = new String(this.b, 0, this.i);
    Set<String> possibles = generateInversions();
    for (String possible : possibles)
      if (possible.length() >= this.minLen && this.plainStemmer.stem(possible).equals(stem))
        this.original = this.stemAtt.addStemInversion(this.original, possible, this);
    this.original = this.stemAtt.addStemInversion(this.original, stem, this);
  }

  /**
   * Test program for demonstrating the Stemmer. It reads a file and stems each
   * word, writing the result to standard out. Usage: Stemmer file-name
   */
  public static void main(String[] args) {

    Set<String> exclude = new HashSet<String>();
    StemInversionAttribute att = new StemInversionAttributeImpl();
    ControlledPresentParticipleInvertingStemmer s = new ControlledPresentParticipleInvertingStemmer(exclude, 5, att);

    for (int i = 0; i < args.length; i++) {
      try {
        InputStream in = new FileInputStream(args[i]);
        byte[] buffer = new byte[1024];
        int bufferLen, offset, ch;

        bufferLen = in.read(buffer);
        offset = 0;
        s.reset();

        while (true) {
          if (offset < bufferLen)
            ch = buffer[offset++];
          else {
            bufferLen = in.read(buffer);
            offset = 0;
            if (bufferLen < 0)
              ch = -1;
            else
              ch = buffer[offset++];
          }

          if (Character.isLetter((char) ch)) {
            s.add(Character.toLowerCase((char) ch));
          } else {
            s.stem();
            System.out.print(s.toString());
            s.reset();
            if (ch < 0)
              break;
            else {
              System.out.print((char) ch);
            }
          }
        }
        System.out.println(att.getStemInversions());
        in.close();
      } catch (IOException e) {
        System.out.println("error reading " + args[i]);
      }
    }
  }

  @Override
  public void addInversions(Set<String> candidates) {

    Set<String> invs = new HashSet<String>();
    for (String candidate : candidates) {
      this.b = new char[candidate.length() + 4];
      System.arraycopy(candidate.toCharArray(), 0, b, 0, candidate.length());
      this.i = candidate.length();
      this.k0 = 0;
      this.k = i;

      Set<String> curr = generateInversions();
      for (String possible : curr)
        if (candidates.contains(this.plainStemmer.stem(possible)))
          invs.add(possible);
    }
    candidates.addAll(invs);

  }
}
