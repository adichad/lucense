package com.cleartrip.sw.search.query.processors;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.spatial.DistanceUtils;
import org.apache.lucene.util.Version;

import com.cleartrip.sw.search.map.ViewPort;
import com.cleartrip.sw.search.query.QueryFactory;
import com.cleartrip.sw.search.query.processors.StrictNLPQueryProcessor.StrictNLPSearchParameters;
import com.cleartrip.sw.search.query.processors.SuggestQueryProcessor.SuggestSearchParameters;
import com.cleartrip.sw.search.searchj.SearchParameters;
import com.cleartrip.sw.search.sort.GeoDistanceComparatorSource;

public class SuggestQueryParser extends CustomQueryParser {

  private final Map<String, FieldQueryFiller> fqFillers;

  private static class LookBeforeDelvingFiller extends FieldQueryFiller {

    LookBeforeDelvingFiller(Analyzer an, String field, List<Term> baseTerms,
        Occur occur, Occur subOccur, QueryFactory qf) {
      super(an, field, baseTerms, occur, subOccur, qf);
    }

    @Override
    boolean fillToken(String token, Set<String> fillFields, int pos,
        IndexReader reader, Occur occur, Query bq, boolean fuzzy,
        int fuzzyThreshold, float fuzzySimilarity) throws IOException {
      boolean filled = false;
      for (Term term : baseTerms) {
        if (fillFields.contains(term.field())) {
          Term t = term.createTerm(token);
          TermEnum readerTerms = reader.terms(t);
          Term readerTerm = readerTerms.term();
          if (readerTerm != null && readerTerm.field().equals(t.field())
              && readerTerm.text().equals(token)) {
            qf.add(
                bq,
                fuzzy ? qf.newTermXorFuzzyQuery(reader, t,
                    fuzzySimilarity <= 0.01f ? getMinSimilarity(t.text())
                        : fuzzySimilarity, 2, pos, fuzzyThreshold) : qf
                    .newTermQuery(t, pos), occur);
            filled = true;
          }
        }
      }
      return filled;
    }
  }

  public SuggestQueryParser(Version version, Analyzer an,
      String defaultField, QueryFactory qf) {
    super(version, an, defaultField, qf);
    fqFillers = new HashMap<>();
    Term t = new Term("place_type");
    List<Term> baseTerms = new ArrayList<>(1);
    baseTerms.add(t);
    FieldQueryFiller filler = new LookBeforeDelvingFiller(an, "place_type",
        baseTerms, Occur.SHOULD, Occur.SHOULD, qf);
    fqFillers.put("place_type", filler);

    baseTerms = new ArrayList<>(1);
    baseTerms.add(new Term("themes"));
    filler = new LookBeforeDelvingFiller(an, "themes", baseTerms, Occur.SHOULD,
        Occur.SHOULD, qf);
    fqFillers.put("themes", filler);

    baseTerms = new ArrayList<>(3);
    baseTerms.add(new Term("name"));
    baseTerms.add(new Term("aliases"));
    baseTerms.add(new Term("geo_path_aliases"));
    baseTerms.add(new Term("gp_aliases_boosted"));
    fqFillers.put("name", new BasicFieldQueryFiller(an, "name", baseTerms,
        Occur.MUST, Occur.SHOULD, qf));
    fqFillers.put("gp_aliases_boosted", new BasicFieldQueryFiller(an,
        "gp_aliases_boosted", baseTerms, Occur.MUST, Occur.SHOULD, qf));
  }

  private static abstract class FieldQueryFiller {
    protected final List<Term>   baseTerms;
    private final Occur          occur;
    protected final QueryFactory qf;
    private final Analyzer       analyzer;
    protected final String       field;
    private final Occur          subOccur;

    FieldQueryFiller(Analyzer an, String field, List<Term> baseTerms,
        Occur occur, Occur subOccur, QueryFactory qf) {
      this.analyzer = an;
      this.baseTerms = baseTerms;
      this.field = field;
      this.occur = occur;
      this.subOccur = subOccur;
      this.qf = qf;
    }

    final int fill(String qstr, Set<String> fields, IndexReader reader,
        Query bq, boolean fuzzy, int fuzzyThreshold, float fuzzySimilarity)
        throws IOException {
      return fill(qstr, fields, reader, this.occur, this.subOccur, bq, fuzzy,
          fuzzyThreshold, fuzzySimilarity);
    }

    final int fill(String qstr, Set<String> fields, IndexReader reader,
        Occur occur, Occur subOccur, Query bq, boolean fuzzy,
        int fuzzyThreshold, float fuzzySimilarity) throws IOException {
      StringReader qReader = new StringReader(qstr);
      // System.out.println("filling for: "+fields);
      TokenStream ts = this.analyzer.tokenStream(field, qReader);
      CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
      PositionIncrementAttribute posIncrAtt = ts
          .addAttribute(PositionIncrementAttribute.class);
      int matched = 0;
      int pos = 0;
      while (ts.incrementToken()) {
        Query subq = qf.newBooleanQuery(true);
        // System.out.println(termAtt.toString());
        if (fillToken(termAtt.toString(), fields,
            pos += posIncrAtt.getPositionIncrement(), reader, subOccur, subq,
            fuzzy, fuzzyThreshold, fuzzySimilarity))
          matched++;
        if (qf.getClauses(subq).length > 0)
          qf.add(bq, subq, occur);
      }
      ts.close();
      qReader.close();
      return matched;
    }

    abstract boolean fillToken(String token, Set<String> fillFields, int pos,
        IndexReader reader, Occur occur, Query bq, boolean fuzzy,
        int fuzzyThreshold, float fuzzySimilarity) throws IOException;
  }

  private static final class BasicFieldQueryFiller extends FieldQueryFiller {

    BasicFieldQueryFiller(Analyzer an, String field, List<Term> baseTerms,
        Occur occur, Occur subOccur, QueryFactory qf) {
      super(an, field, baseTerms, occur, subOccur, qf);
    }

    @Override
    boolean fillToken(String token, Set<String> fillFields, int pos,
        IndexReader reader, Occur occur, Query bq, boolean fuzzy,
        int fuzzyThreshold, float fuzzySimilarity) throws IOException {
      // System.out.println("fillToken for: "+fillFields+": "+token+": "+baseTerms);
      boolean filled = false;
      for (Term term : baseTerms) {
        if (fillFields.contains(term.field())) {
          Term t = term.createTerm(token);
          qf.add(
              bq,
              fuzzy ? qf.newTermXorFuzzyQuery(reader, t,
                  fuzzySimilarity <= 0.01f ? getMinSimilarity(t.text())
                      : fuzzySimilarity, 2, pos, fuzzyThreshold) : qf
                  .newTermQuery(t, pos), occur);
          filled = true;
          // System.out.println("filled: "+bq);
        }
      }
      return filled;
    }

  }

  public Query parseStructured(SearchParameters searchParams)
      throws ParseException, IOException {
    SuggestSearchParameters params = (SuggestSearchParameters) searchParams;
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
            latitude = Double.parseDouble(val);
            latField = field;
          } else if (field.startsWith("long")) {
            longitude = Double.parseDouble(val);
            longField = field;
          } else if (field.equals("dist")) {
            distanceKM = Double.parseDouble(val);
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
              qf.add(bin, subbin, Occur.MUST);
          }
        }
        if (qf.getClauses(bin).length > 0)
          qf.add(bout, bin, Occur.SHOULD);
      }
      if (qf.getClauses(bout).length > 0)
        qf.add(bq, bout, Occur.MUST);
    }
    if (params.coordinateQuery != null) {
      latField = params.latField;
      longField = params.longField;
      QueryParser parser = new QueryParser(this.version, "", this.analyzer);
      ScoreDoc[] sds = params.searcher.search(
          parser.parse(params.coordinateQuery), 1).scoreDocs;
      if (sds.length > 0) {
        int doc = sds[0].doc;
        IndexReader reader = params.searcher.getIndexReader();
        latitude = FieldCache.DEFAULT.getDoubles(reader, latField)[doc];
        longitude = FieldCache.DEFAULT.getDoubles(reader, longField)[doc];
      }

      if (params.geoSort && params.geoSortBuckets == null) {
        LinkedList<SortField> sorters = new LinkedList<>();

        for (SortField sf : params.sort.getSort())
          sorters.add(sf);

        if (params.geoSortBucketInterval > 0.0d) {
          DoubleArrayList list = new DoubleArrayList();
          for (double d = 0.0d; d < params.dist; d += params.geoSortBucketInterval) {
            list.add(d);
          }
          params.geoSortBuckets = list.toDoubleArray();
        } else {
          params.geoSortBuckets = params.defaultBuckets;
        }

        sorters.addFirst(new SortField("geo", new GeoDistanceComparatorSource(
            latField, longField, latitude, longitude, params.geoSortBuckets)));
        params.sort.setSort(sorters.toArray(new SortField[sorters.size()]));

      }

    }
    if (latField != null && longField != null) {
      double minlat = latitude - 0.5f, maxlat = latitude + 0.5f, minlng = longitude - 0.5f, maxlng = longitude + 0.5f;
      if (distanceKM > 0.0d) {
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
      Query q = qf.newDoubleRangeQuery(latField, 4, minlat, maxlat, true, true);
      qf.add(bq, q, Occur.MUST);
      q = qf.newDoubleRangeQuery(longField, 4, minlng, maxlng, true, true);
      qf.add(bq, q, Occur.MUST);
    }
    if (!params.viewPort.equals(ViewPort.DEFAULT))
      qf.add(bq, params.viewPort.newFilter(qf), Occur.MUST);
    return bq;
  }

  @Override
  public Query parse(SearchParameters searchParams) throws ParseException,
      IOException {
    SuggestSearchParameters params = (SuggestSearchParameters) searchParams;
    IndexSearcher searcher = params.searcher;
    IndexReader reader = searcher.getIndexReader();
    params.queryString = params.queryString.toLowerCase();
    String[] qparts = params.queryString.split("\\b(?:in|inside|within)\\b");
    Query queryFinal = qf.newBooleanQuery(true);
    if (qparts.length > 1) {
      // in query
      Set<String> geoFields = new HashSet<>();
      geoFields.add("geo_path_aliases");
      Set<String> geoBoosted = new HashSet<>();
      geoBoosted.add("gp_aliases_boosted");
      // Query subq = qf.newBooleanQuery(true);
      for (int i = 1; i < qparts.length; i++) {
        // System.out.println("qpart: " + qparts[i]);
        Query subsubq = qf.newBooleanQuery(true);
        fqFillers.get("name").fill(qparts[i], geoFields, reader, params.occur,
            params.subOccur, subsubq, true, params.fuzzyThreshold,
            params.fuzzySimilarity);
        if (qf.getClauses(subsubq).length > 0)
          qf.add(queryFinal, new BooleanClause(subsubq, Occur.MUST));
        Query boostq = qf.newBooleanQuery(true);
        fqFillers.get("gp_aliases_boosted").fill(qparts[i], geoBoosted, reader,
            Occur.SHOULD, Occur.SHOULD, boostq, false, 100,
            params.fuzzySimilarity);
        if (qf.getClauses(boostq).length > 0)
          qf.add(queryFinal, new BooleanClause(boostq, Occur.SHOULD));
      }

      String qstr = qparts[0];
      Query subsubq = qf.newBooleanQuery(true);

      int typesMatched = fqFillers.get("place_type")
          .fill(qstr, params.fields, reader, subsubq, false,
              params.fuzzyThreshold, params.fuzzySimilarity);
      int themesMatched = fqFillers.get("themes")
          .fill(qstr, params.fields, reader, subsubq, false,
              params.fuzzyThreshold, params.fuzzySimilarity);

      Query tempq = qf.newBooleanQuery(true);
      int namesMatched = fqFillers.get("name").fill(qstr, params.fields,
          reader, params.occur, params.subOccur, tempq, true,
          params.fuzzyThreshold, params.fuzzySimilarity);
      if (typesMatched != namesMatched && themesMatched != namesMatched)
        qf.addClauses(subsubq, tempq);

      if (qf.getClauses(subsubq).length > 0)
        qf.add(queryFinal, subsubq, Occur.MUST);

    } else {
      qparts = params.queryString.split("\\b(?:near|nearby)\\b");
      if (qparts.length > 1) {
        // near query
        Query latlongGetter = qf.newBooleanQuery(true);
        String qstr = qparts[1];
        Query subq = qf.newBooleanQuery(true);

        // Query subsubq = qf.newBooleanQuery(true);
        int typesMatched = fqFillers.get("place_type").fill(qstr,
            params.fields, reader, subq, false, params.fuzzyThreshold,
            params.fuzzySimilarity);
        int themesMatched = fqFillers.get("themes").fill(qstr, params.fields,
            reader, subq, false, params.fuzzyThreshold, params.fuzzySimilarity);

        Query tempq = qf.newBooleanQuery(true);
        int namesMatched = fqFillers.get("name").fill(qstr, params.fields,
            reader, params.occur, params.subOccur, tempq, true,
            params.fuzzyThreshold, params.fuzzySimilarity);
        if ((typesMatched != namesMatched || typesMatched != 1)
            && (themesMatched != namesMatched || themesMatched != 1))
          if (qf.getClauses(tempq).length > 0)
            qf.add(subq, tempq, Occur.MUST);

        if (qf.getClauses(subq).length > 0)
          qf.add(latlongGetter, subq, Occur.MUST);

        // System.out.println(params.sort);
        TopFieldCollector tfd = TopFieldCollector.create(params.sort, 1, true,
            false, false, true);
        searcher.search(latlongGetter, tfd);
        ScoreDoc[] sds = tfd.topDocs(0, 1).scoreDocs;
        if (sds.length == 0)
          return qf.newTermQuery(new Term("", ""), 0);
        int doc = sds[0].doc;
        /*
         * System.out.println("doc: [" + FieldCache.DEFAULT.getStrings(reader,
         * "name_as_id")[doc] + "] subquery: " + latlongGetter);
         */
        LatLong latLong = new LatLong();
        Query g = getGeoQuery(doc, reader, latLong);
        double latitude = latLong.latitude;
        double longitude = latLong.longitude;
        LinkedList<SortField> sorters = new LinkedList<>();

        for (SortField sf : params.sort.getSort())
          sorters.add(sf);

        sorters.addFirst(new SortField("geo", new GeoDistanceComparatorSource(
            "lat_ce_small_world", "long_ce_small_world", latitude, longitude,
            new double[] { 3, 10, 20, 40, 80 })));

        params.sort.setSort(sorters.toArray(new SortField[sorters.size()]));

        qstr = qparts[0];
        subq = qf.newBooleanQuery(true);

        // subsubq = qf.newBooleanQuery(true);
        typesMatched = fqFillers.get("place_type").fill(qstr, params.fields,
            reader, subq, false, params.fuzzyThreshold, params.fuzzySimilarity);
        themesMatched = fqFillers.get("themes").fill(qstr, params.fields,
            reader, subq, false, params.fuzzyThreshold, params.fuzzySimilarity);

        tempq = qf.newBooleanQuery(true);
        namesMatched = fqFillers.get("name").fill(qstr, params.fields, reader,
            params.occur, params.subOccur, tempq, true, params.fuzzyThreshold,
            params.fuzzySimilarity);

        if ((typesMatched != namesMatched || typesMatched != 1)
            && (themesMatched != namesMatched || themesMatched != 1))
          if (qf.getClauses(tempq).length > 0)
            qf.add(subq, tempq, Occur.MUST);

        if (qf.getClauses(subq).length > 0)
          qf.add(queryFinal, subq, Occur.MUST);

        qf.add(queryFinal, g, Occur.MUST);
      } else {
        // basic query
        String qstr = qparts[0];
        Query subq = qf.newBooleanQuery(true);

        int typesMatched = fqFillers.get("place_type").fill(qstr,
            params.fields, reader, subq, false, params.fuzzyThreshold,
            params.fuzzySimilarity);
        int themesMatched = fqFillers.get("themes").fill(qstr, params.fields,
            reader, subq, false, params.fuzzyThreshold, params.fuzzySimilarity);

        Query boostq = qf.newBooleanQuery(true);
        if (params.fields.contains("geo_path_aliases")) {
          Set<String> fields = new HashSet<>();
          fields.add("gp_aliases_boosted");
          fqFillers.get("gp_aliases_boosted").fill(qstr, fields, reader,
              Occur.SHOULD, Occur.SHOULD, boostq, false, 100,
              params.fuzzySimilarity);
          if (qf.getClauses(boostq).length > 0) {
            qf.add(subq, boostq, Occur.SHOULD);
          }

        }

        Query tempq = qf.newBooleanQuery(true);
        int namesMatched = fqFillers.get("name").fill(qstr, params.fields,
            reader, params.occur, params.subOccur, tempq, true,
            params.fuzzyThreshold, params.fuzzySimilarity);

        if ((typesMatched != namesMatched || typesMatched != 1)
            && (themesMatched != namesMatched || themesMatched != 1))
          if (qf.getClauses(tempq).length > 0) {
            qf.add(subq, tempq, Occur.MUST);
          }

        // }
        if (qf.getClauses(subq).length > 0)
          qf.add(queryFinal, subq, Occur.MUST);
      }
    }
    qf.add(queryFinal,
        qf.newTermQuery(new Term("place_type_geo_planet", "Zip"), 0),
        Occur.MUST_NOT);
    /*
     * Query fsub = qf.newBooleanQuery(true);
     * 
     * qf.add(fsub, qf.newTermQuery(new Term("_id", "4FCD49C3E44D78692F265428"),
     * 0), Occur.SHOULD); qf.add(fsub, qf.newTermQuery(new Term("_id",
     * "4FCD3FD0E44D78692F23AD65"), 0), Occur.SHOULD); qf.add(queryFinal, fsub,
     * Occur.MUST);
     */
    for (Query filter : params.facetFilters.values()) {
      qf.add(queryFinal, new BooleanClause(filter, Occur.MUST));
    }
    if (!params.queryParams.isEmpty()
        || !params.viewPort.equals(ViewPort.DEFAULT))
      qf.add(queryFinal, parseStructured(params), Occur.MUST);

    // Query idq = qf.newBooleanQuery(true);
    // qf.add(idq, qf.newTermQuery(new Term("_id",
    // "4FCD3FCDE44D78692F22E3AC"), 0), Occur.SHOULD);
    // qf.add(idq, qf.newTermQuery(new Term("_id",
    // "4FCD3FCDE44D78692F22EC8A"), 0), Occur.SHOULD);
    // qf.add(queryFinal, idq, Occur.MUST);

    // System.out.println(queryFinal);
    return queryFinal;
  }

  public static class LatLong {
    double latitude;
    double longitude;
  }

  public Query getGeoQuery(int doc, IndexReader reader, LatLong latLong)
      throws IOException {
    // String placeType = FieldCache.DEFAULT.getStrings(reader,
    // "place_type")[doc];
    // String[] fieldSuffixes = new String[] {"geo_planet", "ct_hotels",
    // "ct_airports", "expedia_poi"};
    Query bbox = qf.newBooleanQuery(false);
    double minLat, maxLat, minLong, maxLong;
    minLat = FieldCache.DEFAULT.getDoubles(reader, "lat_sw_small_world")[doc];
    maxLat = FieldCache.DEFAULT.getDoubles(reader, "lat_ne_small_world")[doc];
    minLong = FieldCache.DEFAULT.getDoubles(reader, "long_sw_small_world")[doc];
    maxLong = FieldCache.DEFAULT.getDoubles(reader, "long_ne_small_world")[doc];

    if (minLat == 91.0 || minLat == 0.0) {
      minLat = FieldCache.DEFAULT.getDoubles(reader, "lat_ce_small_world")[doc];
    }
    if (maxLat == 91.0 || maxLat == 0.0) {
      maxLat = FieldCache.DEFAULT.getDoubles(reader, "lat_ce_small_world")[doc];
    }
    if (minLong == 181.0 || minLong == 0.0) {
      minLong = FieldCache.DEFAULT.getDoubles(reader, "long_ce_small_world")[doc];
    }
    if (maxLong == 181.0 || maxLong == 0.0) {
      maxLong = FieldCache.DEFAULT.getDoubles(reader, "long_ce_small_world")[doc];
    }

    Query latQuery = qf.newDoubleRangeQuery("lat_ce_small_world", 4,
        minLat - 0.5f, maxLat + 0.5f, true, true);
    Query longQuery = qf.newDoubleRangeQuery("long_ce_small_world", 4,
        minLong - 0.5f, maxLong + 0.5f, true, true);
    Query ceQuery = qf.newBooleanQuery(false);

    qf.add(ceQuery, latQuery, Occur.MUST);
    qf.add(ceQuery, longQuery, Occur.MUST);
    qf.add(bbox, new BooleanClause(ceQuery, Occur.SHOULD));
    latLong.latitude = (minLat + maxLat) / 2.0f;
    latLong.longitude = (minLong + maxLong) / 2.0f;
    // bbox.setBoost(1000000f);
    return bbox;
  }

  private static float getMinSimilarity(String string) {
    if (string.length() > 10)
      return 0.6f;
    if (string.length() > 7)
      return 0.7f;
    if (string.length() > 3)
      return 0.8f;
    return 0.9f;
  }

}
