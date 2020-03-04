package com.adichad.lucense.resource;

//import com.adichad.lucense.grouping.GrouperFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.configuration.tree.ConfigurationNodeVisitor;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.DefaultTermDocsFactoryBuilder;
import org.apache.lucene.search.TermDocsFactoryBuilder;
import org.apache.lucene.util.Version;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.adichad.lucense.analysis.AnalyzerFactory;
import com.adichad.lucense.analysis.component.AnalyzerComponentFactory;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;
import com.adichad.lucense.analysis.spelling.SpellingCorrector;
import com.adichad.lucense.bitmap.HoleManager;
import com.adichad.lucense.connection.DefaultServerConnectionManager;
import com.adichad.lucense.connection.PooledServerConnectionManager;
import com.adichad.lucense.connection.ServerConnectionManager;
import com.adichad.lucense.expression.fieldSource.ValueSourceFactory;
import com.adichad.lucense.grouping.GrouperFactory;
import com.adichad.lucense.request.RequestFactory.RequestType;

class SearchConfigParser implements ConfigurationNodeVisitor {

  // private GrouperFactory grouper;
  private HashMap<String, GrouperFactory> grouperFactories;

  private String indexName;

  private String indexPath;

  private boolean storeOnRAM;

  private Set<String> allowedHosts;

  private String socketType;

  private int socketTimeout;

  private int portNumber;

  private String tokenizerType;

  private String tokenCharDef;

  private String fieldName;

  private String defaultTokenCharDef;

  private String defaultTokenizerType;

  private boolean readingField;

  private HashMap<String, Analyzer> fieldAnalyzerMap;

  private String schemaName;

  private boolean morphInject;

  private boolean defaultMorphInject;

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private StringWriter prehistoricLog;

  private ServerConnectionManager serverConnectionManager;

  //private Map<String, Set<IndexReader>> indexnameReaderMap;

  private Map<String, Float> readerBoosts;

  private Map<String, Map<String, Analyzer>> schemaAnalyzerMap;

  private Map<String, Map<String, TermDocsFactoryBuilder>> analyzerTDFBMap;

  private HashMap<String, String> indexnameSchemaMap;

  private HashMap<String, Set<String>> schemaIndexNameMap;

  private Map<RequestType, Set<String>> authorizationMap;

  private int maxConcurrancy;

  private int maxQueueSize;

  private String masterHost;

  private int masterPort;

  private SearchResourceManager searchResourceManager;

  private RequestType requestType;

  private long maxSearchTimeout;

  private String scopeName;

  private Map<String, Scriptable> scopeMap;

  private ScriptableObject scope;

  private Context cx;

  //private Set<IndexReader> readers;

  private Set<String> readernames;

  private Analyzer schemaAnalyzer;

  private HashSet<String> expressionFields;

  private HashMap<String, HashSet<String>> schemaExpressionFieldMap;

  private float indexBoost;

  private TokenStreamSource tokenStreamSource;

  private List<TokenFilterSource> tokenFilterSources;

  private TokenStreamSource defaultTokenStreamSource;

  private List<TokenFilterSource> defaultTokenFilterSources;

  private String filterType;

  private String filterSubtype;

  private String file;

  private String defaultFilterType;

  private String defaultFilterSubtype;

  private String defaultFile;

  private int minLen;

  private int defaultMinLen;

  private int editDistance = -1;

  private int defaultPrefixLen = -1;

  private double filterProbability = -1d;

  private double defaultFilterProbability = -1d;

  private int prefixLen = -1;

  private int defaultEditDistance = -1;

  private HashMap<String, SpellingCorrector> spellingCorrectors;

  private int maxCorrections = -1;

  private int defaultMaxCorrections = -1;

  private boolean defaultReplace = true;

  private boolean replace = true;

  private double penaltyFactor = 5d;

  private double defaultPenaltyFactor = 5d;

  private Set<String> indexPaths;

  private String facetConfig;

  private HashMap<String, String> schemaFacetConfigMap;

  private String grouperName;

  private HashMap<String, Map<String, GrouperFactory>> schemaGrouperFactoryMap;

  private IndexerResourceManager schemaIndexResourceManager;

  private String writeConfigName;

  private boolean isRealtime;

  private Map<String, String> indexWriteConfigMap;

  private Map<String, IndexerResourceManager> schemaIRMMap;

  private boolean forceCreate;

  private String analyzerName = "default";

  private Map<String, Analyzer> schemaNamedAnalyzers;

  private Map<String, TermDocsFactoryBuilder> TDFBMap;

  private TermDocsFactoryBuilder TDFB;

  private HashMap<String, String> curAuxEnvConfig = new HashMap<String, String>();

  private int curMiniHoleSize = HoleManager.DEF_HOLE_SIZE;

  private String curCellDictName;

  private HashMap<String, String> curHoleDBConfig = new HashMap<String, String>();

  private HashMap<String, String> curAuxDBConfig = new HashMap<String, String>();

  private HashMap<String, String> curMainDBConfig = new HashMap<String, String>();

  private String curAuxIndexName;

  private HashMap<String, AuxIndexManagerConfig> auxIndexManagerConfigMap = new HashMap<String, AuxIndexManagerConfig>();

  private String auxLucenseField;

  private Version luceneVersion = Version.LUCENE_33;

  private Version defaultLuceneVersion = Version.LUCENE_33;

  private Version schemaLuceneVersion = Version.LUCENE_33;

  private Map<String, Set<IndexReaderConfig>> indexnameReaderConfigMap;

  private SearchResourceManager oldSRM;

  SearchConfigParser(int portNumber, StringWriter prehistoricLog, Context cx) {
    //this.indexnameReaderMap = new HashMap<String, Set<IndexReader>>();
    this.indexnameReaderConfigMap = new HashMap<String, Set<IndexReaderConfig>>();
    this.readerBoosts = new HashMap<String, Float>();
    this.indexnameSchemaMap = new HashMap<String, String>();
    this.schemaIndexNameMap = new HashMap<String, Set<String>>();
    this.schemaFacetConfigMap = new HashMap<String, String>();
    this.schemaAnalyzerMap = new HashMap<String, Map<String, Analyzer>>();
    this.analyzerTDFBMap = new HashMap<String, Map<String, TermDocsFactoryBuilder>>();
    this.schemaNamedAnalyzers = new HashMap<String, Analyzer>();
    this.TDFBMap = new HashMap<String, TermDocsFactoryBuilder>();
    this.schemaGrouperFactoryMap = new HashMap<String, Map<String, GrouperFactory>>();
    this.schemaIRMMap = new HashMap<String, IndexerResourceManager>();

    this.fieldAnalyzerMap = new HashMap<String, Analyzer>();
    this.grouperFactories = new HashMap<String, GrouperFactory>();
    this.authorizationMap = new HashMap<RequestType, Set<String>>();
    this.spellingCorrectors = new HashMap<String, SpellingCorrector>();
    this.maxConcurrancy = 30;
    this.maxSearchTimeout = 5000;
    this.portNumber = portNumber;
    this.allowedHosts = new HashSet<String>();
    this.scopeMap = new HashMap<String, Scriptable>();
    this.cx = cx;
    this.scope = cx.initStandardObjects();
    this.prehistoricLog = prehistoricLog;
//    this.readers = new HashSet<IndexReader>();
    this.readernames = new HashSet<String>();
    this.schemaIndexResourceManager = null;
    this.tokenFilterSources = new ArrayList<TokenFilterSource>();
    this.tokenStreamSource = null;
    this.defaultTokenFilterSources = new ArrayList<TokenFilterSource>();
    this.defaultTokenStreamSource = null;
    this.indexWriteConfigMap = new HashMap<String, String>();
    this.indexPaths = new HashSet<String>();
    this.schemaExpressionFieldMap = new HashMap<String, HashSet<String>>();
    this.expressionFields = new HashSet<String>();
    this.expressionFields.add("_score");
    this.expressionFields.add("_docid");
    this.expressionFields.add("numwords");
    this.expressionFields.add("_indexboost");
    cleanupIndexInfo();
  }
  
  //auxIndexManagerMap, scopeMap, schemaIRMMap, readerMap, spellingCorrector*, 
  SearchResourceManager parse(SearchResourceManager old) throws Exception {
    XMLConfiguration config = new XMLConfiguration(old.getConfigPath());
    this.oldSRM = old;
    config.getRootNode().visit(this);
    this.searchResourceManager = new SearchResourceManager(this.indexnameReaderConfigMap, this.readerBoosts,
        this.indexnameSchemaMap, this.schemaIndexNameMap, this.schemaFacetConfigMap, this.schemaAnalyzerMap,
        this.analyzerTDFBMap, this.schemaGrouperFactoryMap, this.schemaExpressionFieldMap, this.schemaIRMMap,
        this.indexWriteConfigMap, this.spellingCorrectors, this.serverConnectionManager, this.authorizationMap,
        this.scopeMap, this.maxConcurrancy, this.maxQueueSize, this.maxSearchTimeout, this.masterHost, this.masterPort,
        this.portNumber, this.auxIndexManagerConfigMap, old.getConfigPath(), old);
    return this.searchResourceManager;
  }
  
  SearchResourceManager parse(String configPath) throws Exception {
    XMLConfiguration config = new XMLConfiguration(configPath);
    config.getRootNode().visit(this);

    this.searchResourceManager = new SearchResourceManager(this.indexnameReaderConfigMap, this.readerBoosts,
        this.indexnameSchemaMap, this.schemaIndexNameMap, this.schemaFacetConfigMap, this.schemaAnalyzerMap,
        this.analyzerTDFBMap, this.schemaGrouperFactoryMap, this.schemaExpressionFieldMap, this.schemaIRMMap,
        this.indexWriteConfigMap, this.spellingCorrectors, this.serverConnectionManager, this.authorizationMap,
        this.scopeMap, this.maxConcurrancy, this.maxQueueSize, this.maxSearchTimeout, this.masterHost, this.masterPort,
        this.portNumber, this.auxIndexManagerConfigMap, configPath);
    //TODO:change this constructor call
    return this.searchResourceManager;
  }

  @Override
  public boolean terminate() {
    return false;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void visitAfterChildren(ConfigurationNode node) {
    String name = node.getName();
    if (node.getParentNode() != null) {
      String value = null;
      if (node.getValue() != null)
        value = node.getValue().toString();
      String parname = node.getParentNode().getName();
      try {
        if (parname.equals("index-schema")) {
          if (name.equals("name")) {
            this.schemaName = value;
          } else if (name.equals("lucene-version")) {
            this.schemaLuceneVersion = Version.valueOf(value);
          } else if (name.equals("analyzer")) {
            this.defaultTokenStreamSource = AnalyzerComponentFactory.getTokenStreamSource(this.defaultLuceneVersion,
                this.defaultTokenizerType, this.defaultTokenCharDef);
            this.schemaAnalyzer = AnalyzerFactory.createNestedAnalyzer(this.defaultLuceneVersion,
                this.defaultTokenStreamSource, this.defaultTokenFilterSources, this.fieldAnalyzerMap);
            this.schemaNamedAnalyzers.put(this.analyzerName, this.schemaAnalyzer);
            this.TDFBMap.put(this.analyzerName, this.TDFB == null ? new DefaultTermDocsFactoryBuilder() : this.TDFB);
            this.analyzerName = "default";
            this.defaultTokenStreamSource = null;
            this.defaultTokenFilterSources = new ArrayList<TokenFilterSource>();
            this.TDFB = null;
            this.defaultLuceneVersion = Version.LUCENE_33;
          } else if (name.equals("index")) {
            
            Set<IndexReaderConfig> currreaderconfigs = new HashSet<IndexReaderConfig>();
            currreaderconfigs.add(new IndexReaderConfig(this.storeOnRAM, this.forceCreate, this.indexPath, defaultLuceneVersion, schemaAnalyzer, this.indexName));
            this.indexnameReaderConfigMap.put(this.indexName, currreaderconfigs);
            
            //this.indexnameReaderMap.put(this.indexName, curreaders);
            this.readernames.add(this.indexName);
            //this.readers.add(reader);
            this.readerBoosts.put(indexName, this.indexBoost);//
            this.indexWriteConfigMap.put(this.indexName, this.writeConfigName);
            cleanupIndexInfo();
          } else if (name.equals("expression-fields")) {
            Analyzer a = new WhitespaceAnalyzer(this.schemaLuceneVersion);
            TokenStream ts = a.tokenStream(null, new StringReader(value));
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            while (ts.incrementToken()) {
              this.expressionFields.add(termAtt.toString());
            }
            ts.end();
            ts.close();
          } else if (name.equals("facet-config")) {
            this.facetConfig = value;
          } else if (name.equals("grouper")) {
            Class<?> gfc = Class.forName(value);
            Constructor<?> cons = gfc.getDeclaredConstructor();
            GrouperFactory gf = (GrouperFactory) cons.newInstance();
            this.grouperFactories.put(this.grouperName, gf);
          } else if (name.equals("indexer-config")) {
            IndexerConfigParser icp = new IndexerConfigParser(prehistoricLog, false, cx);
            this.schemaIndexResourceManager = icp.parse(value);
          }
        } else if (parname.equals("index")) {
          if (name.equals("name")) {
            this.indexName = value;
          } else if (name.equals("boost")) {
            this.indexBoost = Float.parseFloat(value);
          } else if (name.equals("path")) {
            this.indexPath = value;
            this.indexPaths.add(value);
          } else if (name.equals("store")) {
            if (value.equals("ram"))
              this.storeOnRAM = true;
            else
              this.storeOnRAM = false;
          } else if (name.equals("live-update")) {
            // TODO: implement!
          } else if (name.equals("write-config")) {
            this.writeConfigName = value;
          } else if (name.equals("realtime")) {
            this.isRealtime = value.equals("true") ? true : false;
          } else if (name.equals("force-create")) {
            this.forceCreate = value.equals("true") ? true : false;
          }
        } else if (parname.equals("grouper")) {
          if (name.equals("name")) {
            this.grouperName = value;
          }
        } else if (parname.equals("spelling-corrector")) {
          if (name.equals("name")) {
            this.schemaName = value;
          } else if (name.equals("analyzer")) {
            this.defaultTokenStreamSource = AnalyzerComponentFactory.getTokenStreamSource(this.defaultLuceneVersion,
                this.defaultTokenizerType, this.defaultTokenCharDef);
            this.schemaAnalyzer = AnalyzerFactory.createNestedAnalyzer(this.defaultLuceneVersion,
                this.defaultTokenStreamSource, this.defaultTokenFilterSources, this.fieldAnalyzerMap);
            this.defaultTokenStreamSource = null;
            this.defaultTokenFilterSources = new ArrayList<TokenFilterSource>();
          }
        } else if (parname.equals("analyzer")) {
          if (name.equals("name")) {
            this.analyzerName = value;
          } else if (name.equals("lucene-version")) {
            this.defaultLuceneVersion = Version.valueOf(value);
          } else if (name.equals("termdocs-builder")) {
            Class<?> tdfb = Class.forName(value);
            Constructor<?> cons = tdfb.getDeclaredConstructor();
            this.TDFB = (TermDocsFactoryBuilder) cons.newInstance();
          } else if (name.equals("field")) {
            this.tokenStreamSource = AnalyzerComponentFactory.getTokenStreamSource(this.luceneVersion,
                this.tokenizerType, this.tokenCharDef);
            Analyzer analyzer = AnalyzerFactory.createAnalyzer(this.tokenStreamSource, this.tokenFilterSources);
            this.fieldAnalyzerMap.put(this.fieldName, analyzer);
            this.tokenStreamSource = null;
            this.tokenFilterSources = new ArrayList<TokenFilterSource>();
            this.luceneVersion = Version.LUCENE_33;
          } else if (name.equals("tokenizer")) {
            this.defaultTokenCharDef = value;
          } else if (name.equals("tokenfilter")) {
            TokenFilterSource tfs = AnalyzerComponentFactory.getTokenFilterSource(this.defaultLuceneVersion,
                this.defaultFilterType, this.defaultFilterSubtype, this.defaultMorphInject, this.defaultFile,
                this.defaultMinLen, this.defaultPrefixLen, this.defaultEditDistance, this.defaultFilterProbability,
                this.defaultPenaltyFactor, this.defaultMaxCorrections, this.defaultReplace);
            if (tfs != null)
              this.defaultTokenFilterSources.add(tfs);
            cleanupDefaultFilterInfo();
          }

        } else if (parname.equals("tokenizer")) {
          if (name.equals("type")) {
            if (this.readingField)
              this.tokenizerType = value;
            else
              this.defaultTokenizerType = value;
          }
        } else if (parname.equals("tokenfilter")) {
          if (name.equals("type")) {
            if (this.readingField)
              this.filterType = value;
            else
              this.defaultFilterType = value;
          } else if (name.equals("subtype")) {
            if (this.readingField)
              this.filterSubtype = value;
            else
              this.defaultFilterSubtype = value;
          } else if (name.equals("inject")) {
            if (this.readingField)
              this.morphInject = value.equals("true") ? true : false;
            else
              this.defaultMorphInject = value.equals("true") ? true : false;
          } else if (name.equals("file")) {
            if (this.readingField)
              this.file = value;
            else
              this.defaultFile = value;
          } else if (name.equals("min-length") || name.equals("max-length")) {
            if (this.readingField)
              this.minLen = Integer.valueOf(value);
            else
              this.defaultMinLen = Integer.valueOf(value);
          } else if (name.equals("prefix-length")) {
            if (this.readingField)
              this.prefixLen = Integer.valueOf(value);
            else
              this.defaultPrefixLen = Integer.valueOf(value);
          } else if (name.equals("edit-distance")) {
            if (this.readingField)
              this.editDistance = Integer.valueOf(value);
            else
              this.defaultEditDistance = Integer.valueOf(value);
          } else if (name.equals("filter-probability")) {
            if (this.readingField)
              this.filterProbability = Double.valueOf(value);
            else
              this.defaultFilterProbability = Double.valueOf(value);
          } else if (name.equals("max-corrections")) {
            if (this.readingField)
              this.maxCorrections = Integer.valueOf(value);
            else
              this.defaultMaxCorrections = Integer.valueOf(value);
          } else if (name.equals("levenshtein-penalty-factor")) {
            if (this.readingField)
              this.penaltyFactor = Double.valueOf(value);
            else
              this.defaultPenaltyFactor = Double.valueOf(value);
          } else if (name.equals("replace-token") || name.equals("incr-pos")) {
            if (this.readingField)
              this.replace = Boolean.valueOf(value);
            else
              this.defaultReplace = Boolean.valueOf(value);
          }
        } else if (parname.equals("field")) {
          if (name.equals("name")) {
            this.fieldName = value;
          } else if (name.equals("lucene-version")) {
            this.luceneVersion = Version.valueOf(value);
          } else if (name.equals("tokenizer")) {
            this.tokenCharDef = value;
          } else if (name.equals("tokenfilter")) {
            TokenFilterSource tfs = AnalyzerComponentFactory.getTokenFilterSource(this.luceneVersion, this.filterType,
                this.filterSubtype, this.morphInject, this.file, this.minLen, this.prefixLen, this.editDistance,
                this.filterProbability, this.penaltyFactor, this.maxCorrections, this.replace);
            if (tfs != null)
              this.tokenFilterSources.add(tfs);
            cleanupFilterInfo();
          }
        } else if (parname.equals("searcher")) {
          if (name.equals("max-concurrancy")) {
            this.maxConcurrancy = Integer.parseInt(value);
          } else if (name.equals("aux-index")) {
            auxIndexManagerConfigMap.put(curAuxIndexName, new AuxIndexManagerConfig(curAuxEnvConfig, curMiniHoleSize,
                curCellDictName, curHoleDBConfig, curAuxDBConfig, curMainDBConfig, this.auxLucenseField, this.curAuxIndexName
                ));
            
            curMiniHoleSize = HoleManager.DEF_HOLE_SIZE;
            curAuxEnvConfig = new HashMap<String, String>();
            curHoleDBConfig = new HashMap<String, String>();
            curAuxDBConfig = new HashMap<String, String>();
            curMainDBConfig = new HashMap<String, String>();
            curCellDictName = null;
            curAuxIndexName = null;
            this.auxLucenseField = null;
          } else if (name.equals("max-queue-size")) {
            this.maxQueueSize = Integer.parseInt(value);
          } else if (name.equals("max-search-timeout")) {
            this.maxSearchTimeout = Long.parseLong(value);
          } else if (name.equals("connection")) {
            if (this.socketType.equals("persistent")) {
              this.serverConnectionManager = new PooledServerConnectionManager(this.portNumber, this.socketTimeout,
                  this.allowedHosts);
            } else {
              if(this.oldSRM==null)
                this.serverConnectionManager = new DefaultServerConnectionManager(this.portNumber, this.socketTimeout,
                    this.allowedHosts);
              else
                this.serverConnectionManager = new DefaultServerConnectionManager((DefaultServerConnectionManager)this.oldSRM.getConnectionManager(), this.socketTimeout,
                    this.allowedHosts);
            }
          } else if (name.equals("score-fields")) { // TODO: REMOVE THIS CRAP!

            Analyzer a = new WhitespaceAnalyzer(this.schemaLuceneVersion);
            TokenStream ts = a.tokenStream(null, new StringReader(value));
            CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
            while (ts.incrementToken()) {
              ValueSourceFactory.scoreFields.add(termAtt.toString());
            }
            ts.end();
            ts.close();
          } else if (name.equals("log-properties")) {
            // fixed that bloody leak, phew!
            Logger.getRootLogger().removeAllAppenders();
            Logger.getLogger("QueryLogger").removeAllAppenders();
            Logger.getLogger("ErrorLogger").removeAllAppenders();
            Logger.getLogger("StatusLogger").removeAllAppenders();

            Properties properties = new Properties();
            try {
              properties.setProperty("port", Integer.toString(this.portNumber));
              properties.load(new FileInputStream(value));
              PropertyConfigurator.configure(properties);
            } catch (Exception e) {
              errorLogger.log(Level.FATAL, "Logging config failure: " + e);
              System.out.println(this.prehistoricLog);
              System.exit(1);
            }

            HashMap<Appender, Layout> appenderLayoutMap = new HashMap<Appender, Layout>();
            for (Enumeration<Appender> apps = statusLogger.getAllAppenders(); apps.hasMoreElements();) {
              Appender app = apps.nextElement();
              appenderLayoutMap.put(app, app.getLayout());
              app.setLayout(new PatternLayout("%m"));
            }
            // we now know how to write, so flush prehistoric log
            statusLogger.log(Level.INFO, this.prehistoricLog.toString());

            for (Appender app : appenderLayoutMap.keySet()) {
              app.setLayout(appenderLayoutMap.get(app));
            }
          } else if (name.equals("scope")) {
            if ((this.scopeName != null) && (this.scope != null)) {
              this.scope.sealObject();
              this.scopeMap.put(this.scopeName, this.scope);
              this.scopeName = null;
              this.scope = this.cx.initStandardObjects();
            }
          } else if (name.equals("index-schema")) {
            for (String readername : this.readernames) {
              this.indexnameSchemaMap.put(readername, this.schemaName);
            }
            if (this.facetConfig != null) {
              try {
                File infile = new File(this.facetConfig);
                for (String indexPath : this.indexPaths) {
                  FileReader in = new FileReader(infile);
                  try {
                    File outfile = new File(indexPath + File.separator + "bobo.spring");
                    if (!outfile.exists()) {
                      FileWriter out = new FileWriter(outfile, false);
                      int c;
                      while ((c = in.read()) != -1)
                        out.write(c);
                      out.close();
                    }
                  } catch (IOException e) {
                    errorLogger.log(Level.WARN, "Error copying facet configuration: " + e);
                  }
                  in.close();
                }
              } catch (IOException e) {
                errorLogger.log(Level.WARN, "Error reading facet configuration: " + e);
              }
            }
            this.schemaIndexNameMap.put(this.schemaName, this.readernames);
            this.schemaIRMMap.put(this.schemaName, this.schemaIndexResourceManager);
            this.schemaFacetConfigMap.put(this.schemaName, this.facetConfig);
            this.indexnameSchemaMap.put(this.schemaName, this.schemaName);
            //this.indexnameReaderMap.put(this.schemaName, this.readers);
            this.schemaAnalyzerMap.put(this.schemaName, this.schemaNamedAnalyzers);
            this.analyzerTDFBMap.put(this.schemaName, this.TDFBMap);
            this.schemaGrouperFactoryMap.put(this.schemaName, this.grouperFactories);
            this.fieldAnalyzerMap = new HashMap<String, Analyzer>();
            this.grouperFactories = new HashMap<String, GrouperFactory>();
            this.readernames = new HashSet<String>();
            //this.readers = new HashSet<IndexReader>();
            this.schemaIndexResourceManager = null;
            this.schemaExpressionFieldMap.put(this.schemaName, this.expressionFields);
            this.expressionFields = new HashSet<String>();
            this.expressionFields.add("_score");
            this.expressionFields.add("_docid");
            this.expressionFields.add("numwords");
            this.schemaNamedAnalyzers = new HashMap<String, Analyzer>();
            this.TDFBMap = new HashMap<String, TermDocsFactoryBuilder>();
            this.analyzerName = "default";
            this.indexPaths.clear();
            this.schemaLuceneVersion = Version.LUCENE_33;
          } else if (name.equals("spelling-corrector")) {
            this.spellingCorrectors.put(this.schemaName, new SpellingCorrector(this.schemaAnalyzer));
            statusLogger.log(Level.INFO, "loaded spelling corpus: " + this.schemaName);
          }

        } else if (parname.equals("scope")) {
          if (name.equals("name")) {
            this.scopeName = value;
          } else if (name.equals("script")) {
            if (value != null) {
              this.cx.compileReader(new BufferedReader(new FileReader(value)), value, 1, null)
                  .exec(this.cx, this.scope);
            }
          }

        } else if (parname.equals("connection")) {
          if (name.equals("allowed-host")) {
            this.allowedHosts.add(value);
            this.allowedHosts.add("/" + value);
            if (!this.authorizationMap.containsKey(this.requestType)) {
              this.authorizationMap.put(this.requestType, new HashSet<String>());
            }
            this.authorizationMap.get(this.requestType).add(value);
            this.authorizationMap.get(this.requestType).add("/" + value);
          }
        } else if (parname.equals("allowed-host")) {
          if (name.equals("request-type")) {
            this.requestType = RequestType.getRequestType(value);
          }
        } else if (parname.equals("socket")) {
          if (name.equals("type")) {
            this.socketType = value;
          } else if (name.equals("timeoutms")) {
            this.socketTimeout = Integer.parseInt(value);
          }
        } else if (parname.equals("master")) {
          if (name.equals("host")) {
            this.masterHost = value;
          } else if (name.equals("port")) {
            this.masterPort = Integer.parseInt(value);
          }
        } else if (parname.equals("aux-index")) {
          if (name.equals("name")) {
            curAuxIndexName = value;
          } else if (name.equals("min-hole-size")) {
            curMiniHoleSize = Integer.parseInt(value);
          } else if (name.equals("cell-dictionary")) {
            curCellDictName = value;
          } else if (name.equals("lucene-field")) {
            this.auxLucenseField = value;
          }

        } else if (parname.equals("env")) {
          curAuxEnvConfig.put(name, value);
        } else if (parname.equals("hole-db-config")) {
          curHoleDBConfig.put(name, value);
        } else if (parname.equals("aux-db-config")) {
          curAuxDBConfig.put(name, value);
        } else if (parname.equals("main-db-config")) {
          curMainDBConfig.put(name, value);
        }
      } catch (Exception e) {
        errorLogger.log(Level.ERROR, e);
        e.printStackTrace();
      }
    }
  }

  private void cleanupDefaultFilterInfo() {
    this.defaultEditDistance = -1;
    this.defaultFilterProbability = -1d;
    this.defaultPenaltyFactor = 5;
    this.defaultPrefixLen = -1;
    this.defaultMaxCorrections = -1;
    this.defaultReplace = true;
    this.defaultMorphInject = false;
  }

  private void cleanupFilterInfo() {
    this.editDistance = -1;
    this.filterProbability = -1d;
    this.penaltyFactor = 5;
    this.prefixLen = -1;
    this.maxCorrections = -1;
    this.replace = true;
    this.morphInject = false;
  }

  private void cleanupIndexInfo() {
    this.storeOnRAM = false;
    this.indexName = null;
    this.indexPath = null;
    this.indexBoost = 0;
    this.writeConfigName = null;
    this.forceCreate = false;

  }

  @Override
  public void visitBeforeChildren(ConfigurationNode node) {
    String name = node.getName();
    if (node.getParentNode() != null) {
      try {
        String parname = node.getParentNode().getName();
        if (parname.equals("analyzer")) {
          if (name.equals("field")) {
            this.readingField = true;
          } else
            this.readingField = false;
        }
      } catch (Exception e) {
        errorLogger.log(Level.ERROR, e);
        e.printStackTrace();
      }
    }
  }
}
