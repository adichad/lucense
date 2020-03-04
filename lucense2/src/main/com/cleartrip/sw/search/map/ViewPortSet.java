package com.cleartrip.sw.search.map;

import it.unimi.dsi.fastutil.doubles.Double2ObjectAVLTreeMap;

import java.io.IOException;
import java.util.LinkedList;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.util.PriorityQueue;

public class ViewPortSet extends Collector {
  private final String                                                              latField;
  private final String                                                              lngField;
  private final Double2ObjectAVLTreeMap<Double2ObjectAVLTreeMap<TopFieldCollector>> viewPortFinder;
  private float[]                                                                   lats;
  private float[]                                                                   lngs;
  private int numViewPorts;

  public static ViewPortSet wrap(Sort sort, int numHits, String latField,
      String lngField, ViewPort... viewPorts) throws IOException {
    return new ViewPortSet(sort, numHits, latField, lngField, viewPorts);
  }

  private ViewPortSet(Sort sort, int numHits, String latField, String lngField,
      ViewPort... viewPorts) throws IOException {
    this.latField = latField;
    this.lngField = lngField;
    this.numViewPorts = viewPorts.length;
    this.viewPortFinder = new Double2ObjectAVLTreeMap<>();
    for (ViewPort vp : viewPorts) {
      Double2ObjectAVLTreeMap<TopFieldCollector> m;
      if (!viewPortFinder.containsKey(vp.south)) {
        m = new Double2ObjectAVLTreeMap<>();
        viewPortFinder.put(vp.south, m);
      } else {
        m = viewPortFinder.get(vp.south);
      }
      if (!m.containsKey(vp.west)) {
        m.put(vp.west,
            TopFieldCollector.create(sort, numHits, true, true, true, false));
      }
    }

  }

  private void collect(int doc, double lat, double lng) throws IOException {
    Double2ObjectAVLTreeMap<TopFieldCollector> m = (Double2ObjectAVLTreeMap<TopFieldCollector>) viewPortFinder
        .get(viewPortFinder.headMap(lat).firstDoubleKey()).headMap(lng);
    m.get(m.firstDoubleKey()).collect(doc);

  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {

  }

  @Override
  public void collect(int doc) throws IOException {
    collect(doc, lats[doc], lngs[doc]);
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.lats = FieldCache.DEFAULT.getFloats(reader, this.latField);
    this.lngs = FieldCache.DEFAULT.getFloats(reader, this.lngField);
  }

  @Override
  public boolean acceptsDocsOutOfOrder() {
    return true;
  }

  private static class TopDocsPriorityQueue extends PriorityQueue<TopFieldDocs> {

    public TopDocsPriorityQueue(int size) {
      initialize(size);
    }

    @Override
    protected boolean lessThan(TopFieldDocs a, TopFieldDocs b) {
      return a.totalHits < b.totalHits;
    }

  }

  public TopDocs topDocs(int offset, int limit) {
    //int numHits = offset+limit;
    TopDocsPriorityQueue tdq = new TopDocsPriorityQueue(numViewPorts);
    LinkedList<ScoreDoc> sds = new LinkedList<>();
    int totalHits = 0;
    for(Double2ObjectAVLTreeMap<TopFieldCollector> m: viewPortFinder.values()) {
      for(TopFieldCollector tfc: m.values()) {
        TopFieldDocs tfd;
        if((tfd = (TopFieldDocs)tfc.topDocs()).totalHits>0) {
          tdq.add(tfd);
          totalHits+=tfd.totalHits;
        }
      }
    }
    //LinkedList<TopFieldDocs> tfds = new LinkedList<>();
    while(tdq.size()>0) {
      TopFieldDocs tfd = tdq.pop();
      //tfds.addFirst(tfd);
      int lim = new Double(Math.ceil((float) Math.min(offset + limit, totalHits)
          * (float) tfd.totalHits / (float) totalHits)).intValue();
      //System.out.println("total found: "+tfd.totalHits+"/"+total+", selecting: "+lim);
      for (int i = 0; i < lim; i++) {
        sds.add(tfd.scoreDocs[i]);
      }
      
    }
    int lim = Math.max(Math.min(limit, sds.size()-offset), 0);
    while(sds.size()>lim)
      sds.removeFirst();
    return new TopDocs(totalHits, sds.toArray(new ScoreDoc[sds.size()]), Float.NaN);
    
  }

}
