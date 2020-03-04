package com.cleartrip.sw.search.facets;

import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntSortedMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.math3.stat.clustering.Cluster;
import org.apache.commons.math3.stat.clustering.Clusterable;
import org.apache.commons.math3.stat.clustering.EuclideanIntegerPoint;
import org.apache.commons.math3.stat.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;

public class HistogramFaceterFactory extends SearchFaceterFactory {
  private Term         term;
  private final String field;
  private final String type;
  private final int    width;

  public HistogramFaceterFactory(Map<String, ?> params, Properties env) {
    super(params, env);
    this.field = (String) params.get("field");
    this.type = (String) params.get("type");
    this.width = ((Integer) params.get("width"));
    if (this.width < 1)
      throw new IllegalArgumentException(
          "invalid width. expected >= 1, received: " + this.width);

  }

  private static class IntHistogramFaceter extends SearchFaceter {

    private final String              field;
    private int[]                     vals;
    private Int2IntSortedMap          counts;
    private final int                 width;
    private int                       totalCount   = 0;
    DescriptiveStatistics             stats        = new DescriptiveStatistics();
    Collection<EuclideanIntegerPoint> clusterables = new ArrayList<>();

    public IntHistogramFaceter(final Collector next, final String field,
        final int width) {
      super(next);
      this.field = field;
      counts = new Int2IntAVLTreeMap();
      counts.defaultReturnValue(0);
      this.width = width;
    }

    @Override
    public void collect(int doc) throws IOException {
      stats.addValue(vals[doc]);
      clusterables.add(new EuclideanIntegerPoint(new int[] { vals[doc] }));
      counts.put(vals[doc], counts.get(vals[doc]) + 1);
      ++totalCount;
      if (next != null)
        next.collect(doc);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      this.vals = FieldCache.DEFAULT.getInts(reader, this.field);
      super.setNextReader(reader, docBase);
    }

    @Override
    public void setScorer(Scorer scorer) throws IOException {
      super.setScorer(scorer);
    }

    @Override
    public String getFacetJson(IndexSearcher searcher, FacetsJsonTopDocs facetsJsonTopDocs)
        throws CorruptIndexException, IOException {

      int minVal = counts.firstIntKey();
      int maxVal = counts.lastIntKey();
      StringBuilder sb = new StringBuilder();
      sb.append("{ \"total_count\": ").append(totalCount).append(",");
      sb.append("\"min_val\": ").append(stats.getMin()).append(",");
      sb.append("\"max_val\": ").append(stats.getMax()).append(",");
      sb.append("\"mean\": \"").append(stats.getMean()).append("\",");
      sb.append("\"variance\": \"").append(stats.getVariance()).append("\",");
      sb.append("\"standard_deviation\": \"")
          .append(stats.getStandardDeviation()).append("\",");
      sb.append("\"skewedness\": \"").append(stats.getSkewness()).append("\",");
      sb.append("\"kurtosis\": \"").append(stats.getKurtosis()).append("\",");
      sb.append("\"pop_variance\": \"").append(stats.getPopulationVariance())
          .append("\",");
      sb.append("\"geometric_mean\": \"").append(stats.getGeometricMean())
          .append("\",");
      sb.append("\"histogram(").append(width).append(")\": {\n");
      int currLow = minVal;
      int currHigh = minVal + width;
      while (currLow <= maxVal) {
        Int2IntMap currCounts = counts.subMap(currLow, currHigh);
        String currKey = "[" + currLow + ", " + currHigh + ")";
        int count = 0;
        for (int val : currCounts.values()) {
          count += val;
        }
        if (count > 0)
          sb.append("\"").append(currKey).append("\": ").append(count)
              .append(",");
        currLow = currHigh;
        currHigh += width;
      }
      sb.deleteCharAt(sb.length() - 1).append("},");
      sb.append("\"bar_count\":").append(counts.size()).append(",");
      KMeansPlusPlusClusterer<EuclideanIntegerPoint> clusterer = new KMeansPlusPlusClusterer<EuclideanIntegerPoint>(
          new Random());
      List<Cluster<EuclideanIntegerPoint>> clusters = clusterer.cluster(
          clusterables, Math.min(5, clusterables.size()), 200);
      sb.append("\"clusters\": [\n");
      for (Cluster<EuclideanIntegerPoint> cluster : clusters) {
        sb.append("{ ");
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (EuclideanIntegerPoint point : cluster.getPoints()) {
          int p = point.getPoint()[0];
          if (p > max)
            max = p;
          if (p < min)
            min = p;
        }
        sb.append("\"min\": ").append(min).append(",");
        sb.append("\"max\": ").append(max).append(",");
        sb.append("\"count\": ").append(cluster.getPoints().size());
        sb.append("},");

      }
      sb.deleteCharAt(sb.length() - 1).append("]");

      sb.append("}");
      return sb.toString();
    }
  }

  @Override
  public SearchFaceter createFaceter(Collector c) {
    return new IntHistogramFaceter(c, field, width);
  }

  @Override
  public Query createFilter(String[] vals) {
    BooleanQuery q = new BooleanQuery();
    for (String val : vals)
      q.add(new TermQuery(this.term.createTerm(val)), Occur.SHOULD);
    return q;
  }
}
