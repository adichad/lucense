/**
 * 
 */
package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.AnalysingOnlyQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.SimpleScoringQueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.CustomSimilarity;
import org.apache.lucene.search.HighPriorityTimeLimitingCollector;
import org.apache.lucene.search.IndexBoostCollector;
import org.apache.lucene.search.IndexBoostComparatorSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LCSLengthCollector;
import org.apache.lucene.search.LCSLengthComparatorSource;
import org.apache.lucene.search.NumwordsCollector;
import org.apache.lucene.search.NumwordsComparatorSource;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryLenComparatorSource;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermDocsFactory;
import org.apache.lucene.search.TermDocsFactoryBuilder;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.Version;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.browseengine.bobo.api.BrowseRequest;
import com.browseengine.bobo.api.FacetSpec;
import com.browseengine.bobo.api.FacetSpec.FacetSortSpec;

import com.adichad.lucense.analysis.stem.StemInversionAttribute;
import com.adichad.lucense.bitmap.AuxIndexFilteringCollectorFactory;
import com.adichad.lucense.exception.InvalidOffsetException;
import com.adichad.lucense.exception.LucenseException;
import com.adichad.lucense.exception.LucenseParseException;
import com.adichad.lucense.exception.UnknownException;
import com.adichad.lucense.expression.BooleanExpressionCollector;
import com.adichad.lucense.expression.BooleanLucenseExpression;
import com.adichad.lucense.expression.DoubleExpressionCollector;
import com.adichad.lucense.expression.DoubleLucenseExpression;
import com.adichad.lucense.expression.ExpressionCollector;
import com.adichad.lucense.expression.ExpressionComparatorSource;
import com.adichad.lucense.expression.ExpressionFactory;
import com.adichad.lucense.expression.ExpressionFilteringCollector;
import com.adichad.lucense.expression.FloatExpressionCollector;
import com.adichad.lucense.expression.FloatLucenseExpression;
import com.adichad.lucense.expression.IntExpressionCollector;
import com.adichad.lucense.expression.IntLucenseExpression;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.StringExpressionCollector;
import com.adichad.lucense.expression.StringLucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;
import com.adichad.lucense.grouping.Grouper;
import com.adichad.lucense.grouping.GroupingCollector;
import com.adichad.lucense.request.search.SearchResultRowFiller;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.ExceptionResult;
import com.adichad.lucense.result.SearchResult;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author adichad
 */
public class SearchRequest extends Request implements Runnable {
  private SearchResourceManager searchResourceManager;

  private boolean closeSock;

  private String query;

  private List<String> indexes;

  private String comment;

  private String appname;

  private int offset;

  private int limit;

  private int maxmatches;

  private SortMode sortmode;

  private String sortby;

  private String[] defaultfields;

  private ScorerType scorerType;

  private String expscore;

  // private Grouper[] groupers;
  private List<Grouper> groupers;

  private Map<BooleanLucenseExpression, Boolean> expressionFilters;

  private HashMap<String, Integer> fieldWeights;

  private Query luceneQuery;

  private int resultCount;

  private Date startTime;

  private Date endTime;

  private long timeTaken;

  private Sort sort;

  private Map<String, FieldType> selectedFields;

  private BrowseRequest browseRequest;

  /*
   * private Date parseStart; private Date parseEnd; private double
   * parseTimeTaken; private Date searchEnd; private Date searchStart; private
   * double searchTimeTaken; private Date fillStart; private Date fillEnd;
   * private double fillTimeTaken;
   */
  private int searchTimeout;

  private HashMap<String, FieldType> fieldTypes;

  private Context cx;

  private Scriptable scope;

  private HashSet<String> expressionFields;

  private String highlightPluStemmedQuery;

  private boolean getPluStemmedHighlightables;

  private static Logger queryLogger = Logger.getLogger("QueryLogger");

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private Map<String, Float> readerBoosts = null;

  private HashMap<String, Object2IntOpenHashMap<String>> externalValSource;

  private Map<String, LucenseExpression> namedExprs;

  private Map<String, IntValueSource> intValueSources;

  private Map<String, FloatValueSource> floatValueSources;

  private Map<String, DoubleValueSource> doubleValueSources;

  private Map<String, BooleanValueSource> booleanValueSources;

  private Map<String, StringValueSource> stringValueSources;

  private ValueSources valueSources;

  private String queryAnalyzerName;

  private TermDocsFactoryBuilder termDocsFactoryBuilder;

  private TermDocsFactory termDocsFactory;

  private LinkedList<AuxIndexFilteringCollectorFactory> auxFilters;

  private Version luceneVersion;

  public SearchRequest(Socket sock, int version, int id) {
    super(sock, version, id);
  }

  @Override
  protected void readFrom(InputStream in) throws Exception {
    DataInputStream dis = new DataInputStream(in);

    if (this.version > 0) {
      this.offset = dis.readInt();
      this.limit = dis.readInt();
      this.maxmatches = dis.readInt();
      this.searchTimeout = dis.readInt();

      this.luceneVersion = Version.valueOf(readString(dis));
      this.query = readString(dis);

      int len = dis.readInt();
      this.indexes = new Vector<String>();
      this.expressionFields = new HashSet<String>();
      for (int i = 0; i < len; i++) {
        String indexName = readString(dis);
        this.indexes.add(indexName);
        HashSet<String> idxExprFields = this.searchResourceManager.getExpressionFields(indexName);
        if (idxExprFields != null) {
          this.expressionFields.addAll(idxExprFields);
        }
      }

      this.queryAnalyzerName = readString(dis);

      Set<Analyzer> analyzers = this.searchResourceManager.getAnalyzers(this.indexes, this.queryAnalyzerName);
      Analyzer analyzer = analyzers.iterator().next();
      AnalysingOnlyQueryParser qparser = new AnalysingOnlyQueryParser(this.luceneVersion, "", analyzer);
      qparser.addAttributeClass(StemInversionAttribute.class);
      qparser.parse(this.query);

      this.termDocsFactoryBuilder = this.searchResourceManager.termDocsFactoryBuilder(this.indexes,
          this.queryAnalyzerName);
      this.termDocsFactory = this.termDocsFactoryBuilder.decode(dis);

      this.externalValSource = new HashMap<String, Object2IntOpenHashMap<String>>();
      this.externalValSource.put("_qlen", qparser.getQueryLengthMap());

      this.scope = this.searchResourceManager.getScopeByName(readString(dis));
      len = dis.readInt();
      this.selectedFields = new HashMap<String, FieldType>();
      for (int i = 0; i < len; i++) {
        String field = readString(dis);
        this.selectedFields.put(field, FieldType.getFieldType(dis.readByte()));
      }

      len = dis.readInt();
      this.fieldWeights = new HashMap<String, Integer>();
      for (int i = 0; i < len; i++) {
        String field = readString(dis);
        this.fieldWeights.put(field, dis.readInt());
      }
      len = dis.readInt();
      this.defaultfields = new String[len];
      for (int i = 0; i < len; i++) {
        this.defaultfields[i] = readString(dis);
      }

      this.scorerType = ScorerType.getScorerType(dis.readByte());
      this.expscore = readString(dis);

      len = dis.readInt();
      this.fieldTypes = new HashMap<String, FieldType>();
      for (int i = 0; i < len; i++) {
        FieldType type = FieldType.getFieldType(dis.readByte());
        int mlen = dis.readInt();
        for (int j = 0; j < mlen; j++) {
          String field = readString(dis);
          this.fieldTypes.put(field, type);
        }
      }

      this.namedExprs = new HashMap<String, LucenseExpression>();
      this.intValueSources = new HashMap<String, IntValueSource>();
      this.floatValueSources = new HashMap<String, FloatValueSource>();
      this.doubleValueSources = new HashMap<String, DoubleValueSource>();
      this.booleanValueSources = new HashMap<String, BooleanValueSource>();
      this.stringValueSources = new HashMap<String, StringValueSource>();
      this.valueSources = new ValueSources(this.intValueSources, this.floatValueSources, this.doubleValueSources,
          this.booleanValueSources, this.stringValueSources);
      len = dis.readInt();
      for (int i = 0; i < len; i++) {
        String name = readString(dis);
        String expr = readString(dis);
        FieldType type = FieldType.getFieldType(dis.readByte());

        this.namedExprs.put(name, ExpressionFactory.getExpressionFromString(expr, type, this.cx, this.scope,
            this.externalValSource, this.namedExprs, this.valueSources, searchResourceManager));
      }

      len = dis.readInt();
      if (len > 0) {
        this.readerBoosts = new HashMap<String, Float>();
        for (int i = 0; i < len; i++) {
          String dir = this.searchResourceManager.getIndexDir(readString(dis));
          Float boost = Float.parseFloat(readString(dis));
          if (dir != null)
            this.readerBoosts.put(dir, boost);
        }
      }
      if (this.readerBoosts == null)
        this.readerBoosts = this.searchResourceManager.getReaderBoosts();
      else {
        Map<String, Float> boosts = new HashMap<String, Float>(this.searchResourceManager.getReaderBoosts());
        boosts.putAll(this.readerBoosts);
        this.readerBoosts = boosts;
      }

      this.expressionFilters = new HashMap<BooleanLucenseExpression, Boolean>();
      len = dis.readInt();
      for (int i = 0; i < len; i++) {
        String expr = readString(dis);
        this.expressionFilters.put((BooleanLucenseExpression) ExpressionFactory.getExpressionFromString(expr,
            FieldType.TYPE_BOOLEAN, this.cx, this.scope, this.externalValSource, this.namedExprs, this.valueSources,
            searchResourceManager), dis.readByte() != 0);
      }

      this.auxFilters = new LinkedList<AuxIndexFilteringCollectorFactory>();
      len = dis.readInt();
      for (int i = 0; i < len; i++) {
        String indexName = readString(dis);
        byte[] rowid = readStringInBytes(dis);
        boolean exclude = dis.readByte() != 0;
        auxFilters.add(new AuxIndexFilteringCollectorFactory(searchResourceManager.getAuxIndexer(indexName), rowid,
            exclude));
      }

      this.sortmode = SortMode.getSortMode(dis.readByte());
      if (this.sortmode == SortMode.SORT_SQL) {
        len = dis.readInt();
        SortField[] sortfields = new SortField[len];
        for (int i = 0; i < len; i++) {
          String sortfield = readString(dis);
          FieldType sorterType = FieldType.getFieldType(dis.readByte());
          if (sortfield.equals("@score")) {
            sortfields[i] = SortField.FIELD_SCORE;
            dis.readByte();
          } else if (sortfield.equals("@indexboost")) {
            sortfields[i] = new SortField(sortfield, new IndexBoostComparatorSource(this.readerBoosts),
                dis.readByte() != 0);
          } else if (sortfield.equals("@docid")) {
            sortfields[i] = SortField.FIELD_DOC;
            dis.readByte();
          } else if (sortfield.startsWith("@expr")) {
            sortfield = sortfield.substring(5);
            sortfields[i] = new SortField(sortfield, new ExpressionComparatorSource(sorterType, this.cx, this.scope,
                this.externalValSource, this.namedExprs, this.intValueSources, this.floatValueSources,
                this.doubleValueSources, this.booleanValueSources, this.stringValueSources, searchResourceManager),
                dis.readByte() != 0);
          } else if (sortfield.startsWith("@numwords")) {
            sortfield = sortfield.substring(10, sortfield.length() - 1);
            sortfields[i] = new SortField(sortfield, new NumwordsComparatorSource(), dis.readByte() != 0);
          } else if (sortfield.startsWith("@lcslen")) {
            sortfield = sortfield.substring(8, sortfield.length() - 1);
            sortfields[i] = new SortField(sortfield, new LCSLengthComparatorSource(), dis.readByte() != 0);
          } else if (sortfield.startsWith("@qlen")) {
            sortfield = sortfield.substring(6, sortfield.length() - 1);
            sortfields[i] = new SortField(sortfield, new QueryLenComparatorSource(this.externalValSource.get("_qlen")),
                dis.readByte() != 0);
          } else {
            sortfields[i] = new SortField(sortfield, FieldType.getSortFieldType(sorterType), dis.readByte() != 0);
          }
        }
        this.sort = new Sort(sortfields);
      } else {
        this.sortby = readString(dis);
      }

      this.groupers = decodeNewGroupers(dis);
      // System.out.println(groupers);

      // fast grouper api
      this.browseRequest = null;
      len = dis.readInt();
      if (len > 0) {
        this.browseRequest = new BrowseRequest();
        for (int i = 0; i < len; i++) {
          FacetSpec spec = new FacetSpec();
          spec.setOrderBy(getFacetSortSpec(dis.readByte()));
          if (spec.getOrderBy().equals(FacetSortSpec.OrderByCustom)) {
            errorLogger.log(Level.WARN,
                "custom facet sorting not yet supported. falling back to descending facet-count sort.");
            spec.setOrderBy(FacetSortSpec.OrderHitsDesc);
          }
          spec.setMinHitCount(dis.readInt());
          spec.setMaxCount(dis.readInt());

          this.browseRequest.setFacetSpec(readString(dis), spec);
        }
      }

      if (dis.readByte() != 0) {
        this.getPluStemmedHighlightables = true;
        this.highlightPluStemmedQuery = readString(dis);
        if ((this.highlightPluStemmedQuery == null) || this.highlightPluStemmedQuery.trim().equals(""))
          this.highlightPluStemmedQuery = this.query;
      }
      this.appname = readString(dis);
      this.comment = readString(dis);
    }
  }

  private List<Grouper> decodeNewGroupers(DataInputStream dis) throws Exception, IOException,
      com.adichad.lucense.expression.parse.ParseException {
    int len = dis.readInt();

    List<Grouper> groupers = new ArrayList<Grouper>(len);
    for (int i = 0; i < len; i++) {
      String className = readString(dis);
      Grouper g = this.searchResourceManager.getGrouper(this.indexes, className, dis, this.cx, this.scope,
          this.externalValSource, this.namedExprs, this.valueSources, this.searchResourceManager, this.readerBoosts,
          this.fieldTypes, this.expressionFields, this.scorerType);
      groupers.add(g);
    }
    return groupers;
  }

  private FacetSortSpec getFacetSortSpec(byte readByte) {
    switch (readByte) {
    case 0:
      return FacetSortSpec.OrderHitsDesc;
    case 1:
      return FacetSortSpec.OrderValueAsc;
    case 2:
      return FacetSortSpec.OrderByCustom;
    default:
      return FacetSortSpec.OrderHitsDesc;
    }
  }

  @Override
  protected void sendTo(OutputStream out) throws IOException {
    // TODO Auto-generated method stub
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Request#process(com.adichad.lucense.request
   * .ServerContext, java.util.concurrent.ExecutorService)
   */
  @Override
  public void process(SearchResourceManager searchResourceManager, ExecutorService executor, boolean closeSock) {
    try {
      this.searchResourceManager = searchResourceManager;
      this.closeSock = closeSock;
      executor.submit(this);
    } catch (Exception e) {
      errorLogger.log(Level.ERROR, e);
    }
  }

  @Override
  public void run() {
    IndexSearcher searcher = null;
    try {
      this.cx = Context.enter();
      this.cx.setOptimizationLevel(1);
      readFrom(this.sock.getInputStream());

      if (this.maxmatches < this.offset) {
        throw new InvalidOffsetException(this.offset, this.maxmatches);
      }
      this.startTime = new Date();
      // this.parseStart = new Date();
      long searchTimeout = Math.min(this.searchResourceManager.getMaxSearchTimeout(), this.searchTimeout);

      Set<Analyzer> analyzers = this.searchResourceManager.getAnalyzers(this.indexes, queryAnalyzerName);
      Analyzer analyzer = analyzers.iterator().next();
      if (this.scorerType == ScorerType.SCORE_LCSFIELD) {
        // queryLogger.log(Level.DEBUG, "Inside LCSFIELD");
        SimpleScoringQueryParser parser = new SimpleScoringQueryParser(this.luceneVersion, "", analyzer);
        parser.setTermDocsFactory(this.termDocsFactory);
        this.luceneQuery = parser.parse(this.query);
      } else if (this.scorerType == ScorerType.SCORE_BOOLFIELD) {
        // queryLogger.log(Level.DEBUG, "Inside BOOLFIELD");
        SimpleScoringQueryParser parser = new SimpleScoringQueryParser(this.luceneVersion, "", analyzer);
        this.luceneQuery = parser.parse(this.query);
      } else {
        // queryLogger.log(Level.DEBUG, "Inside DEFAULT");
        QueryParser parser = new QueryParser(this.luceneVersion, "", analyzer);
        this.luceneQuery = parser.parse(this.query);
      }
      // this.parseEnd = new Date();
      // this.parseTimeTaken = (float) (parseEnd.getTime() -
      // parseStart.getTime()) / 1000.0;
      searcher = this.searchResourceManager.getSearcher(this.indexes);
      searcher.setSimilarity(new CustomSimilarity(this.luceneQuery));

      SearchResult sresult = new SearchResult(this.id);
      SearchResultRowFiller filler = null;

      Set<String> fetchFields = this.selectedFields.keySet();

      boolean needNumwords = false;
      boolean needLCS = false;
      boolean needQLen = false;
      ArrayList<String> numwordsFields = new ArrayList<String>();
      ArrayList<String> lcsFields = new ArrayList<String>();
      ArrayList<String> qlenFields = new ArrayList<String>();

      Map<String, LucenseExpression> exprs = new HashMap<String, LucenseExpression>();
      for (String field : fetchFields) {
        if (field.startsWith("@numwords")) {
          numwordsFields.add(field.substring(10, field.length() - 1));
          needNumwords = true;
        } else if (field.startsWith("@lcslen")) {
          lcsFields.add(field.substring(8, field.length() - 1));
          needLCS = true;
        } else if (field.startsWith("@qlen")) {
          qlenFields.add(field.substring(6, field.length() - 1));
          needQLen = true;
        } else if (field.startsWith("@expr")) {
          LucenseExpression expr = ExpressionFactory.getExpressionFromString(field.substring(5),
              this.selectedFields.get(field), this.cx, this.scope, this.externalValSource, this.namedExprs,
              this.valueSources, searchResourceManager);
          exprs.put(field, expr);
        }
      }

      boolean needScore = fetchFields.contains("@score");
      boolean needDocID = fetchFields.contains("@docid");
      boolean needIndexBoost = fetchFields.contains("@indexboost");

      TopFieldCollector tfc = TopFieldCollector.create(this.sort, this.offset + this.limit, true,
          this.scorerType != ScorerType.SCORE_NONE, false, true);
      Collector c = tfc;

      NumwordsCollector nc = null;
      IndexBoostCollector ibc = null;
      LCSLengthCollector lcsc = null;
      GroupingCollector gc = null;
      Map<String, ExpressionCollector> ecs = new HashMap<String, ExpressionCollector>(exprs.size());
      for (String str : exprs.keySet()) {
        ExpressionCollector ec = null;
        switch (exprs.get(str).getType()) {
        case TYPE_INT:
          ec = new IntExpressionCollector((IntLucenseExpression) exprs.get(str), c);
          break;
        case TYPE_FLOAT:
          ec = new FloatExpressionCollector((FloatLucenseExpression) exprs.get(str), c);
          break;
        case TYPE_DOUBLE:
          ec = new DoubleExpressionCollector((DoubleLucenseExpression) exprs.get(str), c);
          break;
        case TYPE_BOOLEAN:
          ec = new BooleanExpressionCollector((BooleanLucenseExpression) exprs.get(str), c);
          break;
        case TYPE_STRING:
          ec = new StringExpressionCollector((StringLucenseExpression) exprs.get(str), c);
          break;

        }
        ecs.put(str, ec);
        c = ec;
      }
      if (needIndexBoost) {
        ibc = new IndexBoostCollector(c, this.searchResourceManager.getReaderBoosts());
        c = ibc;
      }
      if (needLCS) {
        lcsc = new LCSLengthCollector(lcsFields, c);
        c = lcsc;
      }
      if (needNumwords) {
        nc = new NumwordsCollector(numwordsFields, c);
        c = nc;
      }
      if (this.groupers.size() > 0) {
        gc = new GroupingCollector(c, this.groupers);
        c = gc;
      }
      for (BooleanLucenseExpression expression : this.expressionFilters.keySet()) {
        c = new ExpressionFilteringCollector(c, expression, this.expressionFilters.get(expression));
      }
      for (AuxIndexFilteringCollectorFactory auxFilterSource : auxFilters) {
        c = auxFilterSource.wrap(c);
      }
      c = new HighPriorityTimeLimitingCollector(c, searchTimeout);
      searcher.search(this.luceneQuery, null, c);

      if (this.browseRequest != null) {
        this.browseRequest.setQuery(this.luceneQuery);
        sresult.addBoboGroupings(this.searchResourceManager.getBrowser(this.indexes).browse(this.browseRequest));
      }

      sresult.setSelectFields(fetchFields.toArray(new String[0]));
      for (Grouper grouper : this.groupers) {
        grouper.fillGroupings(sresult);
      }

      // this.searchTimeTaken = (float) (searchEnd.getTime() - searchStart
      // .getTime()) / 1000.0;

      TopFieldDocs tfd = (TopFieldDocs) tfc.topDocs();
      this.resultCount = tfd.totalHits;
      // this.fillStart = new Date();
      if (needNumwords)
        filler = new SearchResultRowFiller.NumwordsFiller(nc, tfd, filler);
      if (needLCS)
        filler = new SearchResultRowFiller.LCSLenFiller(lcsc, tfd, filler);
      if (needQLen)
        filler = new SearchResultRowFiller.QLenFiller(qlenFields, this.externalValSource.get("_qlen"), filler);
      if (needScore)
        filler = new SearchResultRowFiller.ScoreFiller(tfd, filler);
      if (needDocID)
        filler = new SearchResultRowFiller.DocIDFiller(tfd, filler);
      if (needIndexBoost)
        filler = new SearchResultRowFiller.IndexBoostFiller(ibc, tfd, filler);
      for (String str : exprs.keySet()) {
        filler = new SearchResultRowFiller.ExpressionFiller(str, ecs.get(str), tfd, filler);
      }
      if (!fetchFields.isEmpty())
        filler = new SearchResultRowFiller.StoredFieldFiller(tfd, searcher, fetchFields, filler);

      if (filler != null) {
        Document[] retdocs = new Document[Math.max(Math.min(this.limit, tfd.totalHits - this.offset), 0)];
        for (int i = this.offset; i < Math.min(this.offset + this.limit, tfd.totalHits); i++)
          retdocs[i - this.offset] = filler.fill(null, i);
        sresult.setResults(retdocs);
      }

      // this.fillEnd = new Date();
      // this.fillTimeTaken = (float) (fillEnd.getTime() - fillStart.getTime())
      // / 1000.0;

      sresult.setDisplayCount(this.resultCount);

      if (this.getPluStemmedHighlightables) {
        AnalysingOnlyQueryParser qparser = new AnalysingOnlyQueryParser(this.luceneVersion, "", analyzer);
        qparser.addAttributeClass(StemInversionAttribute.class);
        qparser.parse(this.highlightPluStemmedQuery);
        Map<String, List<Attribute>> attMap = qparser.getAttributes();
        Map<String, Map<String, Set<String>>> inversions = new HashMap<String, Map<String, Set<String>>>();
        for (String field : attMap.keySet()) {
          List<Attribute> atts = attMap.get(field);
          for (Attribute att : atts) {
            if (att instanceof StemInversionAttribute) {
              if (!inversions.containsKey(field))
                inversions.put(field, new HashMap<String, Set<String>>());
              Map<String, Set<String>> currInvs = ((StemInversionAttribute) att).getStemInversions();
              if (!currInvs.isEmpty()) {
                Map<String, Set<String>> ci = inversions.get(field);
                for (String orig : currInvs.keySet()) {
                  if (!ci.containsKey(orig))
                    ci.put(orig, new HashSet<String>());
                  ci.get(orig).addAll(currInvs.get(orig));
                }
              }
            }
          }
        }
        sresult.addHighlightables(inversions);
      }

      sresult.writeTo(this.sock.getOutputStream());

      this.endTime = new Date();
      this.timeTaken = this.endTime.getTime() - this.startTime.getTime();

      queryLogger.log(Level.INFO, toString());
    } catch (LucenseException e1) {
      errorLogger.log(Level.ERROR, e1 + " [" + this.toString() + "]");
      if (!this.sock.isClosed()) {
        try {
          ExceptionResult result = new ExceptionResult(e1, this.id);
          result.writeTo(this.sock.getOutputStream());
        } catch (IOException e2) {
          errorLogger.log(Level.ERROR, e2 + " [" + this.toString() + "]");
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
      StringWriter o = new StringWriter();
      e.printStackTrace(new PrintWriter(o));
      errorLogger.log(Level.ERROR, e + " [" + this.toString() + "] " + o.toString());
      if (!this.sock.isClosed()) {
        try {
          ExceptionResult result;
          if (e instanceof ParseException) {
            result = new ExceptionResult(new LucenseParseException(e), this.id);
          } else {
            result = new ExceptionResult(new UnknownException(e), this.id);
          }
          result.writeTo(this.sock.getOutputStream());
        } catch (IOException e2) {
          errorLogger.log(Level.ERROR, e2 + " [" + this.toString() + "]");
        }
      }
    } finally {
      if (searcher != null)
        this.searchResourceManager.removeRequest(searcher);
      if (!this.sock.isClosed()) {
        try {
          if (this.closeSock) {
            this.sock.shutdownOutput();
            this.sock.close();
          }
        } catch (IOException e) {
          errorLogger.log(Level.ERROR, e + " [" + this.toString() + "]");
        }
      }
      Context.exit();
    }
  }

  @Override
  public String toString() {
    StringBuilder buff = new StringBuilder();
    Formatter formatter = new Formatter(buff);

    long secs = this.timeTaken / (1000L);
    long msecs = this.timeTaken % (1000L);

    buff.append(secs);
    buff.append(".");
    formatter.format("%03d", msecs);
    // formatter.format("%3.3f", time);
    buff.append(" sec ");/*
                          * buff.append("("); formatter.format("%3.3f",
                          * parseTimeTaken); buff.append(",");
                          * formatter.format("%3.3f", searchTimeTaken);
                          * buff.append(","); formatter.format("%3.3f",
                          * fillTimeTaken); buff.append(") ");
                          */

    buff.append("[").append(this.scorerType);

    buff.append("/").append(this.groupers == null ? "(null)" : this.groupers.size()).append("/")
        .append(this.expressionFilters == null ? "(null)" : this.expressionFilters.size()).append("/")
        .append(this.sortmode).append("/").append(this.queryAnalyzerName).append(" ");
    buff.append("(").append(this.offset).append(",").append(this.limit).append(")/").append(this.maxmatches)
        .append(" ");
    buff.append(this.resultCount);
    buff.append("] ");
    buff.append("[");
    int i = 0;
    for (String index : this.indexes) {
      buff.append(index);
      if (i < this.indexes.size() - 1) {
        buff.append(",");
      }
      i++;
    }
    buff.append("] ");
    buff.append("[").append(this.appname).append("] ");
    buff.append("[").append(this.sock == null ? "(null)" : this.sock.getInetAddress()).append(":")
        .append(this.sock == null ? "(null)" : this.sock.getPort()).append("] ");
    buff.append("[").append(this.id).append("] ");

    buff.append("[").append(this.comment).append("] ");
    buff.append("[").append(this.query).append("] ");

    return buff.toString();
  }
}
