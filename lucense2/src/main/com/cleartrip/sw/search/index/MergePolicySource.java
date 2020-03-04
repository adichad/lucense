package com.cleartrip.sw.search.index;

import org.apache.lucene.index.MergePolicy;

public abstract class MergePolicySource {
  public abstract MergePolicy acquire();

  public abstract void release(MergePolicy policy);
}
