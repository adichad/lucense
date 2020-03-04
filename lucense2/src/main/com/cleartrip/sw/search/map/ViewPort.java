package com.cleartrip.sw.search.map;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

import com.cleartrip.sw.search.query.QueryFactory;

public class ViewPort {
  public double north;
  public double west;
  public double south;
  public double east;
  public int    zoom;

  public ViewPort(double north, double west, double south, double east, int zoom) {
    this.north = north;
    this.south = south;
    this.east = east;
    this.west = west;
    this.zoom = zoom;
  }

  public Query newFilter(QueryFactory qf) {
    Query q = qf.newBooleanQuery(true);
    qf.add(q, NumericRangeQuery.newDoubleRange("long_ce_small_world", 4, west,
        east, true, false), Occur.MUST);
    qf.add(q, NumericRangeQuery.newDoubleRange("lat_ce_small_world", 4, south,
        north, true, false), Occur.MUST);
    Query zero = qf.newBooleanQuery(true);
    qf.add(zero, NumericRangeQuery.newDoubleRange("long_ce_small_world", 4, 0d,
        0d, true, true), Occur.MUST);
    qf.add(zero, NumericRangeQuery.newDoubleRange("lat_ce_small_world", 4, 0d,
        0d, true, true), Occur.MUST);
    qf.add(q, zero, Occur.MUST_NOT);
    return q;
  }

  static final FieldSelector   fs      = new FieldSelector() {

                                         @Override
                                         public FieldSelectorResult accept(
                                             String arg0) {
                                           if (arg0
                                               .equals("str_lat_ce_small_world")
                                               || arg0
                                                   .equals("str_long_ce_small_world"))
                                             return FieldSelectorResult.LOAD;
                                           return FieldSelectorResult.NO_LOAD;
                                         }

                                       };
  public static final ViewPort DEFAULT = new ViewPort(90, -180, -90, 180, 0);

  public boolean filterAfterSort(ScoreDoc sd, IndexSearcher s)
      throws CorruptIndexException, IOException {
    Document doc = s.doc(sd.doc, fs);
    // System.out.println("lat:"+doc.get("str_lat_ce_small_world")+", lng:"+doc.get("str_long_ce_small_world"));
    double lat;
    double lng;
    if (south <= (lat = Double.valueOf(doc.get("str_lat_ce_small_world")))
        && lat <= north
        && west <= (lng = Double.valueOf(doc.get("str_long_ce_small_world")))
        && lng <= east)
      return true;
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ViewPort) {
      ViewPort vp = (ViewPort) obj;
      return east == vp.east && west == vp.west && north == vp.north
          && south == vp.south;
    }
    return false;
  }

  public String toString() {
    return "[" + zoom + "][(" + south + "," + west + ")-(" + north + "," + east
        + ")]";
  }

  public void snapToGrid(ViewPort fullView, double latStep, double lngStep) {
    this.west = fullView.west
        + Math.floor((this.west - fullView.west) / lngStep) * lngStep;
    this.east = fullView.east
        - Math.floor((fullView.east - this.east) / lngStep) * lngStep;
    this.south = fullView.south
        + Math.floor((this.south - fullView.south) / latStep) * latStep;
    this.north = fullView.north
        - Math.floor((fullView.north - this.north) / latStep) * latStep;

  }
}