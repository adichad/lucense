package com.cleartrip.sw.search.index;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.index.MergePolicy;
import org.apache.lucene.index.TieredMergePolicy;

public class TieredMergePolicySource extends MergePolicySource {

  private final MergePolicy policy;

  public TieredMergePolicySource(Map<String, ?> params, Properties env) {
    TieredMergePolicy p = new TieredMergePolicy();
    if (params.containsKey("forceMergeDeletesPctAllowed")) {
      p.setForceMergeDeletesPctAllowed((Double) params
          .get("forceMergeDeletesPctAllowed"));
    }
    policy = p;

  }

  @Override
  public MergePolicy acquire() {
    // TODO Auto-generated method stub
    return this.policy;
  }

  @Override
  public void release(MergePolicy policy) {
    policy.close();
  }

}
