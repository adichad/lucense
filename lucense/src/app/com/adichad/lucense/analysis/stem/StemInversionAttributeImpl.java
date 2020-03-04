package com.adichad.lucense.analysis.stem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.util.AttributeImpl;

public class StemInversionAttributeImpl extends AttributeImpl implements StemInversionAttribute {

  /**
   *   
   */
  private static final long serialVersionUID = 1L;

  protected Map<String, Set<String>> inversions;

  private String original;

  private InvertingStemmer last;

  private boolean lock;

  public StemInversionAttributeImpl() {
    this.inversions = new HashMap<String, Set<String>>();
  }

  @Override
  public void clear() {
    // inversions.clear();
  }

  @Override
  public void copyTo(AttributeImpl target) {
    if (target instanceof StemInversionAttribute) {
      // target.clear();
      ((StemInversionAttributeImpl) target).inversions.putAll(this.inversions);
    }
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof StemInversionAttribute)
      return this.inversions.equals(((StemInversionAttributeImpl) other).inversions);
    return false;
  }

  @Override
  public int hashCode() {
    return this.inversions.hashCode();
  }

  private void lock() {
    this.lock = true;
  }

  private void unlock() {
    this.lock = false;
  }

  private boolean locked() {
    return this.lock;
  }

  @Override
  public String addStemInversion(String original, String inversion, InvertingStemmer stemmer) {
    if (!locked()) {
      this.original = original;
      lock();
    }
    if (!this.inversions.containsKey(this.original))
      this.inversions.put(this.original, new HashSet<String>());
    this.inversions.get(this.original).add(inversion);

    if (stemmer == this.last) {
      unlock();
      Iterator<InvertingStemmer> it = chain.iterator();
      it.next();
      while (it.hasNext()) {
        InvertingStemmer is = it.next();
        is.addInversions(this.inversions.get(this.original));
      }
    }
    return this.original;
  }

  @Override
  public Map<String, Set<String>> getStemInversions() {
    return this.inversions;
  }

  LinkedList<InvertingStemmer> chain = new LinkedList<InvertingStemmer>();

  @Override
  public void setLast(InvertingStemmer stemmer) {
    this.chain.addFirst(stemmer);
    this.last = stemmer;
  }

  @Override
  public void addSynonymInversion(String original, String inversion) {
    if (!this.inversions.containsKey(original))
      this.inversions.put(original, new HashSet<String>());
    this.inversions.get(original).add(inversion);
  }

}
