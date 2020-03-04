package com.cleartrip.sw.search.query.processors;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.apache.lucene.spatial.DistanceUtils;
import org.apache.lucene.util.Version;

import com.cleartrip.sw.search.map.ViewPort;
import com.cleartrip.sw.search.query.QueryFactory;
import com.cleartrip.sw.search.query.processors.URLQueryProcessor.CDSearchParameters;
import com.cleartrip.sw.search.searchj.SearchParameters;

public class URLQueryParser extends CustomQueryParser {

  public URLQueryParser(Version version, Analyzer analyzer,
      String defaultField, QueryFactory qf) {
    super(version, analyzer, defaultField, qf);
  }

  @Override
  public Query parse(SearchParameters searchParams) throws ParseException,
      IOException {
    CDSearchParameters params = (CDSearchParameters) searchParams;
    Map<String, String[]> qParams = params.queryParams;
    Query bq = qf.newBooleanQuery(true);
    double latitude = 0, longitude = 0, distanceKM = 0;
    String latField = null, longField = null;
    for (String paramName : qParams.keySet()) {

      String[] namesAndWeights = paramName.split(",");
      String[] vals = qParams.get(paramName);

      Query bout = qf.newBooleanQuery(true);
      for (int i = 0; i < namesAndWeights.length; i++) {
        String[] temp = namesAndWeights[i].split("\\^");
        String field = temp[0];
        float weight = 1.0f;
        if (temp.length > 1)
          weight = Float.parseFloat(temp[1]);
        Query bin = qf.newBooleanQuery(true);
        bin.setBoost(weight);
        for (String val : vals) {
          if (field.startsWith("lat")) {
            latitude = Float.parseFloat(val);
            latField = field;
          } else if (field.startsWith("long")) {
            longitude = Float.parseFloat(val);
            longField = field;
          } else if (field.equals("dist")) {
            distanceKM = Float.parseFloat(val);
          } else {
            String[] subvals = val.split("\\s*\"?\\s*,\\s*\"?\\s*");
            Query subbin = qf.newBooleanQuery(true);
            for (String subval : subvals) {
              if (!subval.equals("")) {
                TokenStream ts = this.analyzer.tokenStream(field,
                    new StringReader(subval));
                CharTermAttribute termAtt = ts
                    .addAttribute(CharTermAttribute.class);
                int pos = 0;
                Query subsubbin = qf.newBooleanQuery(true);
                while (ts.incrementToken()) {
                  pos++;
                  Term t = new Term(field, termAtt.toString());
                  Query q = params.makeFuzzy ? (qf.newFuzzyQuery(t,
                      params.fuzzySimilarity, 1, pos)) : qf
                      .newTermQuery(t, pos);
                  qf.add(subsubbin, q, Occur.MUST);
                }
                ts.close();
                if (qf.getClauses(subsubbin).length > 0)
                  qf.add(subbin, subsubbin, Occur.SHOULD);
              }
            }
            if (qf.getClauses(subbin).length > 0)
              qf.add(bq, subbin, Occur.MUST);
          }
        }
        if (qf.getClauses(bin).length > 0)
          qf.add(bout, bin, Occur.SHOULD);
      }
      if (qf.getClauses(bout).length > 0)
        qf.add(bq, bout, Occur.MUST);
    }
    if (latField != null && longField != null) {
      double minlat = latitude - 0.5f, maxlat = latitude + 0.5f, minlng = longitude - 0.5f, maxlng = longitude + 0.5f;
      if (distanceKM > 0) {
        double[] result = new double[2];
        double latRads = latitude * DistanceUtils.DEGREES_TO_RADIANS;
        double lngRads = longitude * DistanceUtils.DEGREES_TO_RADIANS;
        result = DistanceUtils.pointOnBearing(latRads, lngRads, distanceKM,
            DistanceUtils.DEG_90_AS_RADS, result,
            DistanceUtils.EARTH_MEAN_RADIUS_KM);
        maxlng = result[1] * DistanceUtils.RADIANS_TO_DEGREES;
        result = DistanceUtils.pointOnBearing(latRads, lngRads, distanceKM,
            DistanceUtils.DEG_270_AS_RADS, result,
            DistanceUtils.EARTH_MEAN_RADIUS_KM);
        minlng = result[1] * DistanceUtils.RADIANS_TO_DEGREES;

        result = DistanceUtils.pointOnBearing(latRads, lngRads, distanceKM,
            DistanceUtils.DEG_180_AS_RADS, result,
            DistanceUtils.EARTH_MEAN_RADIUS_KM);
        minlat = result[0] * DistanceUtils.RADIANS_TO_DEGREES;
        result = DistanceUtils.pointOnBearing(latRads, lngRads, distanceKM, 0,
            result, DistanceUtils.EARTH_MEAN_RADIUS_KM);
        maxlat = result[0] * DistanceUtils.RADIANS_TO_DEGREES;

      }
      Query q = qf.newFloatRangeQuery(latField, 4,
          new Double(minlat).floatValue(), new Double(maxlat).floatValue(),
          true, true);
      qf.add(bq, q, Occur.MUST);
      q = qf.newFloatRangeQuery(longField, 4, new Double(minlng).floatValue(),
          new Double(maxlng).floatValue(), true, true);
      qf.add(bq, q, Occur.MUST);
    }
    if (!params.viewPort.equals(new ViewPort(90, -180, -90, 180, 0)))
      qf.add(bq, params.viewPort.newFilter(qf), Occur.MUST);
    return bq;
  }

}
