package com.cleartrip.sw.search.sort;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.spatial.DistanceUtils;

public class GeoDistanceComparatorSource extends FieldComparatorSource {

  private double   latitude;
  private double   longitude;
  private String   latField;
  private String   longField;
  private double[] bucketPoints;

  public GeoDistanceComparatorSource(String latField, String longField,
      double latitude, double longitude, double[] bucketPoints) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.latField = latField;
    this.longField = longField;
    this.bucketPoints = bucketPoints;
  }

  private static class GeoDistanceComparator extends FieldComparator<Integer> {

    private int[]    dist;
    private double[] lats;
    private double[] longs;
    private double   latitude;
    private double   longitude;
    private int      bottom;
    private String   latField;
    private String   longField;
    // private IndexReader reader;
    private double[] bucketPoints;

    public GeoDistanceComparator(String latField, String longField,
        double latitude, double longitude, double[] bucketPoints, int numHits) {
      this.latField = latField;
      this.longField = longField;
      this.latitude = DistanceUtils.DEGREES_TO_RADIANS * latitude;
      this.longitude = DistanceUtils.DEGREES_TO_RADIANS * longitude;
      this.dist = new int[numHits];
      this.bucketPoints = bucketPoints;
    }

    private int bucket(double d) {
      for (int i = 0; i < bucketPoints.length; i++) {
        if (d < bucketPoints[i])
          return i;
      }
      return bucketPoints.length;
    }

    @Override
    public int compare(int slot1, int slot2) {
      return dist[slot1] < dist[slot2] ? -1 : (dist[slot1] > dist[slot2] ? 1
          : 0);
    }

    @Override
    public void setBottom(int slot) {
      this.bottom = dist[slot];

    }

    @Override
    public int compareBottom(int doc) throws IOException {
      int cdist = bucket(DistanceUtils.haversine(
          DistanceUtils.DEGREES_TO_RADIANS * lats[doc],
          DistanceUtils.DEGREES_TO_RADIANS * longs[doc], latitude, longitude,
          DistanceUtils.EARTH_EQUATORIAL_RADIUS_KM));
      // System.out.println(FieldCache.DEFAULT.getStrings(reader,
      // "name_as_id")[doc]+": "+cdist);
      return cdist > bottom ? -1 : (cdist < bottom ? 1 : 0);
    }

    @Override
    public void copy(int slot, int doc) throws IOException {
      dist[slot] = bucket(DistanceUtils.haversine(
          DistanceUtils.DEGREES_TO_RADIANS * lats[doc],
          DistanceUtils.DEGREES_TO_RADIANS * longs[doc], latitude, longitude,
          DistanceUtils.EARTH_EQUATORIAL_RADIUS_KM));
      // System.out.println(FieldCache.DEFAULT.getStrings(reader,
      // "name_as_id")[doc]+": "+dist[slot]);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      lats = FieldCache.DEFAULT.getDoubles(reader, latField);
      longs = FieldCache.DEFAULT.getDoubles(reader, longField);
      // this.reader = reader;
    }

    @Override
    public Integer value(int slot) {
      return dist[slot];
    }

  }

  @Override
  public FieldComparator<Integer> newComparator(String fieldname, int numHits,
      int sortPos, boolean reversed) throws IOException {
    return new GeoDistanceComparator(latField, longField, latitude, longitude,
        bucketPoints, numHits);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("(" + latField + "," + longField
        + "): (" + latitude + "," + longitude + ") > [  ");
    for(double bucketPoint: bucketPoints) {
      sb.append(bucketPoint).append(", ");
    }
    sb.setLength(sb.length()-2);
    sb.append("]");
    return sb.toString();
  }
}
