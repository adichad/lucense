package com.cleartrip.sw.search.context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermDocsFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockFactory;
import org.apache.lucene.util.Version;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import com.adichad.lucense.expression.BooleanExpressionTree;
import com.adichad.lucense.expression.ExpressionCollectorFactory;
import com.adichad.lucense.expression.ExpressionComparatorSourceFactory;
import com.adichad.lucense.expression.ExpressionFactory;
import com.adichad.lucense.expression.ExpressionFilteringCollectorFactory;
import com.adichad.lucense.expression.ExpressionTree;
import com.cleartrip.sw.analytics.AnalyticsProcessor;
import com.cleartrip.sw.search.analysis.GenericAnalyzer;
import com.cleartrip.sw.search.analysis.charfilters.CharFilterSource;
import com.cleartrip.sw.search.analysis.filters.TokenFilterSource;
import com.cleartrip.sw.search.analysis.tokenizers.TokenizerSource;
import com.cleartrip.sw.search.context.ResourceManagerConfig.AnalyticsProcessorConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.AnalyzerConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.CharFilterConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.DataManagerConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.ExecuterServiceFactoryConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.ExpressionConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.ExpressionFilterConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.FaceterConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.FieldSelectorConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.FieldTemplateConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.FilterConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.IndexerConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.LookupFactoryConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.MergePolicyConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.QueryFactoryConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.QueryParserConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.QueryProcessorConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.ReturnFormatConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.SchemaConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.ScopeConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.SearcherFactoryConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.SearcherWarmingStrategyConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.SimilarityConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.SorterConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.SuggestCollectorConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.SuggestProcessorConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.TermDocsFactoryConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.TokenFilterConfig;
import com.cleartrip.sw.search.context.ResourceManagerConfig.TokenizerConfig;
import com.cleartrip.sw.search.data.DataManager;
import com.cleartrip.sw.search.data.JsonDataMapper;
import com.cleartrip.sw.search.executors.ExecuterServiceFactory;
import com.cleartrip.sw.search.facets.SearchFaceterFactory;
import com.cleartrip.sw.search.filters.SearchFilterFactory;
import com.cleartrip.sw.search.index.MergePolicySource;
import com.cleartrip.sw.search.query.QueryFactory;
import com.cleartrip.sw.search.query.processors.QueryParserFactory;
import com.cleartrip.sw.search.query.processors.QueryProcessor;
import com.cleartrip.sw.search.schema.FieldTemplate;
import com.cleartrip.sw.search.schema.Schema;
import com.cleartrip.sw.search.searchj.CustomFieldSelector;
import com.cleartrip.sw.search.searchj.ReturnFieldFormat;
import com.cleartrip.sw.search.similarity.SimilaritySource;
import com.cleartrip.sw.search.util.Constants;
import com.cleartrip.sw.suggest.LookupFactory;
import com.cleartrip.sw.suggest.SuggestionCollector;
import com.cleartrip.sw.suggest.SuggestionProcessor;

public class ResourceManager {

  private final Map<String, DataManager>                         dataManagers;
  private final Map<String, Analyzer>                            analyzers;
  private final Map<String, TokenizerSource>                     tokenizers;
  private final Map<String, TokenFilterSource>                   tokenFilters;
  private final Map<String, CharFilterSource>                    charFilters;
  private final Map<String, Schema>                              schemata;
  private final Map<String, FieldTemplate>                       fieldTemplates;
  private final Map<String, IndexWriter>                         indexWriters;
  private final Map<String, QueryProcessor>                      queryProcs;
  private final Map<String, AnalyticsProcessor>                  analyticsProcessors;
  private final Map<String, QueryParserFactory>                  queryParsers;
  private final Map<String, Scriptable>                          scopes;
  private final Map<String, ExpressionTree>                      expressions;
  private final Map<String, SortFieldFactory>                    sorters;
  private final Map<String, SimilaritySource>                    similarities;
  private final Map<String, CustomFieldSelector>                 fieldSelectors;
  private final Map<String, ReturnFieldFormat>                   fieldFormats;
  private final Map<String, SearchFaceterFactory>                faceters;
  private final Map<String, SearchFilterFactory>                 filters;
  private final Map<String, ExpressionFilteringCollectorFactory> expressionFilters;
  private final Map<String, QueryFactory>                        queryFactories;
  private final Map<String, TermDocsFactory>                     termDocsFactories;
  private final Map<String, MergePolicySource>                   mergePolicies;
  private final Map<String, CustomSearcherFactory>               searcherFactories;
  private final Map<String, SearcherWarmingStrategy>             searcherWarmingStrategies;
  private final Map<String, ExecutorService>                     executorServices;
  private final Map<String, LookupFactory>                       lookupFactories;
  private final Map<String, SuggestionProcessor>                 suggestionProcessors;
  private final Map<String, SuggestionCollector>                 suggestionCollectors;

  public ResourceManager(ResourceManager in, ResourceManagerConfig rmc,
      Properties env) throws Exception {
    Map<String, DataManagerConfig> dataManagerConfigs = rmc.getDataManagers();
    Map<String, SchemaConfig> schemaConfigs = rmc.getSchemata();
    Map<String, FieldTemplateConfig> templateConfigs = rmc.getFieldTemplates();
    Map<String, IndexerConfig> indexerConfigs = rmc.getIndexWriters();
    Map<String, QueryProcessorConfig> queryProcessorConfigs = rmc
        .getQueryProcessors();
    Map<String, AnalyticsProcessorConfig> analyticsProcessorConfigs = rmc
        .getAnalyticsProcessors();
    Map<String, QueryParserConfig> queryParserConfigs = rmc.getQueryParsers();
    Map<String, SorterConfig> sorterConfigs = rmc.getSorters();
    Map<String, SimilarityConfig> similarityConfigs = rmc.getSimilarities();
    Map<String, FieldSelectorConfig> fieldSelectorConfigs = rmc
        .getFieldSelectors();
    Map<String, ReturnFormatConfig> returnFormatConfigs = rmc
        .getReturnFormats();
    Map<String, FaceterConfig> faceterConfigs = rmc.getFaceters();
    Map<String, FilterConfig> filterConfigs = rmc.getFilters();
    Map<String, ExpressionFilterConfig> expressionFilterConfigs = rmc
        .getExpressionFilters();
    Map<String, ExpressionConfig> expressionConfigs = rmc.getExpressions();
    Map<String, ScopeConfig> scopeConfigs = rmc.getScopes();
    Map<String, QueryFactoryConfig> queryFactoryConfigs = rmc
        .getQueryFactories();
    Map<String, TermDocsFactoryConfig> termDocsFactoryConfigs = rmc
        .getTermDocsFactories();
    Map<String, MergePolicyConfig> mergePolicyConfigs = rmc.getMergePolicies();
    Map<String, SearcherFactoryConfig> searcherFactoryConfigs = rmc
        .getSearcherFactories();
    Map<String, ExecuterServiceFactoryConfig> executorServiceFactoryConfigs = rmc
        .getExecuterServiceFactories();
    Map<String, SearcherWarmingStrategyConfig> searcherWarmingStrategyConfigs = rmc
        .getSearcherWarmingStrategies();
    Map<String, LookupFactoryConfig> lookupFactoryConfigs = rmc.getLookupFactories();
    Map<String, SuggestProcessorConfig> suggestionProcessorConfigs = rmc
        .getSuggestProcessors();
    Map<String, SuggestCollectorConfig> suggestionCollectorConfigs = rmc
        .getSuggestCollectors();

    analyzers = new HashMap<>();
    tokenizers = new HashMap<>();
    tokenFilters = new HashMap<>();
    charFilters = new HashMap<>();
    schemata = new HashMap<>(schemaConfigs.size());
    fieldTemplates = new HashMap<>(templateConfigs.size());
    indexWriters = new HashMap<>(indexerConfigs.size());
    queryProcs = new HashMap<>(queryProcessorConfigs.size());
    analyticsProcessors = new HashMap<>(analyticsProcessorConfigs.size());
    queryParsers = new HashMap<>(queryParserConfigs.size());
    sorters = new HashMap<>(sorterConfigs.size());
    similarities = new HashMap<>(similarityConfigs.size());
    fieldSelectors = new HashMap<>(fieldSelectorConfigs.size());
    faceters = new HashMap<>(faceterConfigs.size());
    filters = new HashMap<>(filterConfigs.size());
    expressionFilters = new HashMap<>(expressionFilterConfigs.size());
    expressions = new HashMap<>(expressionConfigs.size());
    scopes = new HashMap<>(scopeConfigs.size());
    dataManagers = new HashMap<>(dataManagerConfigs.size());
    fieldFormats = new HashMap<>(returnFormatConfigs.size());
    queryFactories = new HashMap<>(queryFactoryConfigs.size());
    termDocsFactories = new HashMap<>(termDocsFactoryConfigs.size());
    mergePolicies = new HashMap<>(mergePolicyConfigs.size());
    searcherFactories = new HashMap<>(searcherFactoryConfigs.size());
    searcherWarmingStrategies = new HashMap<>(
        searcherWarmingStrategyConfigs.size());
    executorServices = new HashMap<>(executorServiceFactoryConfigs.size());
    lookupFactories = new HashMap<>(lookupFactoryConfigs.size());
    suggestionProcessors = new HashMap<>(suggestionProcessorConfigs.size());
    suggestionCollectors = new HashMap<>(suggestionCollectorConfigs.size());

    for (Map.Entry<String, DataManagerConfig> entry : dataManagerConfigs
        .entrySet()) {
      String dataManagerName = entry.getKey();
      DataManagerConfig dataManagerConf = entry.getValue();
      if (!dataManagers.containsKey(dataManagerName)) {

        Schema schema = ensureAndGetSchema(schemaConfigs, templateConfigs,
            dataManagerConf.getSchema());

        String indexerName = dataManagerConf.getIndexWriter();
        if (!indexWriters.containsKey(indexerName)) {
          IndexerConfig indexerConf = indexerConfigs.get(indexerName);
          Analyzer a = getAnalyzer(indexerConf.getAnalyzer(), rmc, env);
          String path = env.getProperty("path.data");
          String fullPath = path + File.separator + indexerConf.getIndexName();
          LockFactory lockFactory = (LockFactory) Class
              .forName(indexerConf.getLockFactory()).getConstructor()
              .newInstance();

          Directory dir = (Directory) Class.forName(indexerConf.getDirType())
              .getConstructor(File.class, LockFactory.class)
              .newInstance(new File(fullPath), lockFactory);

          Version v = Version.valueOf(indexerConf.getIndexVersion());

          IndexWriterConfig iwc = new IndexWriterConfig(v, a);
          iwc.setOpenMode(OpenMode.valueOf(indexerConf.getOpenMode()));
          iwc.setRAMBufferSizeMB(Integer.parseInt(env.getProperty(indexerConf
              .getMemUsageEnv())));

          String similarityName = indexerConf.getSimilarity();

          iwc.setSimilarity(ensureAndGetSimilaritySource(similarityConfigs,
              similarityName).getSimilarity());

          iwc.setMergePolicy(ensureAndGetMergePolicySource(mergePolicyConfigs,
              indexerConf.getMergePolicy(), env).acquire());

          // TODO: get more iwc settings from json

          IndexWriter w = new IndexWriter(dir, iwc);
          indexWriters.put(indexerName, w);
        }
        IndexWriter indexer = indexWriters.get(indexerName);

        Map<String, ?> params = dataManagerConf.getParams();
        Map<String, String> qprocNames = dataManagerConf.getQueryProcessors();
        Map<String, QueryProcessor> myQueryProcs = new HashMap<>(
            qprocNames.size());
        for (Map.Entry<String, String> e : qprocNames.entrySet()) {
          String canonicalName = e.getKey();
          String queryProcName = e.getValue();

          myQueryProcs.put(
              canonicalName,
              ensureQueryProcessor(rmc, env, queryProcessorConfigs,
                  queryParserConfigs, sorterConfigs, similarityConfigs,
                  fieldSelectorConfigs, faceterConfigs, filterConfigs,
                  queryProcName, expressionConfigs, expressionFilterConfigs,
                  scopeConfigs, returnFormatConfigs, queryFactoryConfigs,
                  termDocsFactoryConfigs));

        }

        Map<String, String> aprocNames = dataManagerConf
            .getAnalyticsProcessors();
        Map<String, AnalyticsProcessor> myAnalyticsProcs = new HashMap<>(
            aprocNames.size());
        for (Map.Entry<String, String> e : aprocNames.entrySet()) {
          String canonicalName = e.getKey();
          String analyticsProcName = e.getValue();

          myAnalyticsProcs.put(
              canonicalName,
              ensureAnalyticsProcessor(rmc, env, analyticsProcessorConfigs,
                  analyticsProcName));

        }

        String searcherFactoryName = dataManagerConf.getSearcherFactory();

        dataManagers.put(
            dataManagerName,
            new DataManager(indexer, schema, new JsonDataMapper(), params,
                myQueryProcs, myAnalyticsProcs,
                ensureAndGetSuggestionProcessors(
                    dataManagerConf.getSuggestProcessors(),
                    suggestionProcessorConfigs, suggestionCollectorConfigs,
                    rmc, env), ensureAndGetSearcherFactory(searcherFactoryName,
                    searcherFactoryConfigs, similarityConfigs,
                    executorServiceFactoryConfigs,
                    searcherWarmingStrategyConfigs, schemaConfigs,
                    templateConfigs, lookupFactoryConfigs, env)));

      }
    }

  }

  private Map<String, SuggestionProcessor> ensureAndGetSuggestionProcessors(
      Map<String, String> suggestProcessorMap,
      Map<String, SuggestProcessorConfig> suggestionProcessorConfigs,
      Map<String, SuggestCollectorConfig> suggestionCollectorConfigs,
      ResourceManagerConfig rmc, Properties env) throws Exception {
    Map<String, SuggestionProcessor> res = new HashMap<>();
    for (String name : suggestProcessorMap.keySet()) {
      String sname = suggestProcessorMap.get(name);
      if (!suggestionProcessors.containsKey(sname)) {
        SuggestProcessorConfig conf = suggestionProcessorConfigs.get(sname);

        suggestionProcessors
            .put(sname,
                (SuggestionProcessor) (Class.forName(conf.getType())
                    .getConstructor(SuggestionCollector.class)
                    .newInstance(ensureAndGetSuggestionCollector(
                        conf.getCollector(), suggestionCollectorConfigs, rmc,
                        env))));
      }
      res.put(name, suggestionProcessors.get(sname));
    }
    return res;
  }

  private SuggestionCollector ensureAndGetSuggestionCollector(String name,
      Map<String, SuggestCollectorConfig> suggestionCollectorConfigs,
      ResourceManagerConfig rmc, Properties env) throws Exception {
    if (!suggestionCollectors.containsKey(name)) {
      SuggestCollectorConfig conf = suggestionCollectorConfigs.get(name);

      List<String> sorterNames = conf.getSorterChain();
      List<SortFieldFactory> mySorters = new ArrayList<>(sorterNames.size());
      for (String sorterName : sorterNames) {
        mySorters.add(ensureAndGetSorter(rmc, env, rmc.getSorters(),
            rmc.getExpressions(), rmc.getScopes(), sorterName));
      }

			suggestionCollectors.put(name,
					(SuggestionCollector) (Class.forName(conf.getType())
							.getConstructor(List.class, List.class, List.class, Map.class)
							.newInstance(conf.getFields(),
									conf.getPrePopulatedFields(), mySorters,
									conf.getParams())));
    }
    return suggestionCollectors.get(name);
  }

  private Schema ensureAndGetSchema(Map<String, SchemaConfig> schemaConfigs,
      Map<String, FieldTemplateConfig> templateConfigs, String schemaName) {
    if (!schemata.containsKey(schemaName)) {
      SchemaConfig schemaConf = schemaConfigs.get(schemaName);
      String idField = schemaConf.getIdField();

      Map<String, FieldTemplate> requiredFieldTemplates = bindFieldTemplates(
          templateConfigs, schemaConf.getRequiredFieldTemplates());
      Map<String, FieldTemplate> optionalFieldTemplates = bindFieldTemplates(
          templateConfigs, schemaConf.getOptionalFieldTemplates());
      Map<Pattern, FieldTemplate> patternFieldTemplates = bindPatternFieldTemplates(
          templateConfigs, schemaConf.getPatternFieldTemplates());
      schemata.put(schemaName, new Schema(idField, requiredFieldTemplates,
          optionalFieldTemplates, patternFieldTemplates));
    }

    return schemata.get(schemaName);
  }

  private CustomSearcherFactory ensureAndGetSearcherFactory(
      String name,
      Map<String, SearcherFactoryConfig> configs,
      Map<String, SimilarityConfig> similarityConfigs,
      Map<String, ExecuterServiceFactoryConfig> executorServiceConfigs,
      Map<String, SearcherWarmingStrategyConfig> searcherWarmingStrategyConfigs,
      Map<String, SchemaConfig> schemaConfigs,
      Map<String, FieldTemplateConfig> templateConfigs,
      Map<String, LookupFactoryConfig> lookupFactoryConfigs, Properties env)
      throws InstantiationException, IllegalAccessException,
      InvocationTargetException, NoSuchMethodException, ClassNotFoundException,
      IllegalArgumentException, SecurityException {
    if (!searcherFactories.containsKey(name)) {
      SearcherFactoryConfig conf = configs.get(name);
      searcherFactories.put(
          name,
          new CustomSearcherFactory(ensureAndGetSimilaritySource(
              similarityConfigs, conf.getSearchSimilarity()).getSimilarity(),
              ensureAndGetExecutorService(executorServiceConfigs,
                  conf.getExecutorServiceFactory(), env),
              ensureAndGetSearcherWarmingStrategy(
                  searcherWarmingStrategyConfigs,
                  conf.getSearcherWarmingStrategy(), schemaConfigs,
                  templateConfigs), ensureAndGetLookupFactories(
                  conf.getLookupFactories(), lookupFactoryConfigs, env)));
    }
    return searcherFactories.get(name);
  }

  private Map<String, LookupFactory> ensureAndGetLookupFactories(
      Map<String, String> lfmap, Map<String, LookupFactoryConfig> lookupFactoryConfigs, Properties env)
      throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException,
      NoSuchMethodException, SecurityException, ClassNotFoundException {
    Map<String, LookupFactory> res = new HashMap<>();
    for (String field : lfmap.keySet()) {
      String lookupName = lfmap.get(field);
      if (!lookupFactories.containsKey(lookupName)) {
//    	System.out.println("lookupname:" + lookupName + "\nLookupfactory:" + lookupFactoryConfigs.get(lookupName).getType());
//        System.out.println("Path.Lookup:" + env.getProperty(lookupFactoryConfigs.get(lookupName).getPathEnv()));
    	lookupFactories.put(lookupName,
            (LookupFactory) (Class
                .forName(lookupFactoryConfigs.get(lookupName).getType()).getConstructor(String.class)
                .newInstance(env.getProperty(lookupFactoryConfigs.get(lookupName).getPathEnv()))));
      }
      res.put(field, lookupFactories.get(lookupName));
    }
    return res;
  }

  private SearcherWarmingStrategy ensureAndGetSearcherWarmingStrategy(
      Map<String, SearcherWarmingStrategyConfig> searcherWarmingStrategyConfigs,
      String name, Map<String, SchemaConfig> schemaConfigs,
      Map<String, FieldTemplateConfig> templateConfigs) {
    if (!searcherWarmingStrategies.containsKey(name)) {
      SearcherWarmingStrategyConfig conf = searcherWarmingStrategyConfigs
          .get(name);

      searcherWarmingStrategies.put(name, new SearcherWarmingStrategy(
          ensureAndGetSchema(schemaConfigs, templateConfigs, conf.getSchema()),
          conf.getFieldCacheWarmables()));
    }

    return searcherWarmingStrategies.get(name);
  }

  private ExecutorService ensureAndGetExecutorService(
      Map<String, ExecuterServiceFactoryConfig> executorServiceConfigs,
      String name, Properties env) throws InstantiationException,
      IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException,
      ClassNotFoundException {
    if (!executorServices.containsKey(name)) {
      ExecuterServiceFactoryConfig conf = executorServiceConfigs.get(name);
      executorServices.put(name,
          ((ExecuterServiceFactory) (Class.forName(conf.getType())
              .getConstructor(Map.class, Properties.class).newInstance(
              conf.getParams(), env))).newExecutorService());
    }
    return executorServices.get(name);
  }

  private MergePolicySource ensureAndGetMergePolicySource(
      Map<String, MergePolicyConfig> mergePolicyConfigs,
      String mergePolicyName, Properties env) throws InstantiationException,
      IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException,
      ClassNotFoundException {
    if (!mergePolicies.containsKey(mergePolicyName)) {
      MergePolicyConfig mergePolicyConfig = mergePolicyConfigs
          .get(mergePolicyName);

      MergePolicySource mergePolicySource = (MergePolicySource) Class
          .forName(mergePolicyConfig.getType())
          .getConstructor(Map.class, Properties.class)
          .newInstance(mergePolicyConfig.getParams(), env);
      mergePolicies.put(mergePolicyName, mergePolicySource);
    }
    return mergePolicies.get(mergePolicyName);
  }

  protected SimilaritySource ensureAndGetSimilaritySource(
      Map<String, SimilarityConfig> similarityConfigs, String similarityName)
      throws InstantiationException, IllegalAccessException,
      InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    if (!similarities.containsKey(similarityName)) {
      SimilarityConfig simConfig = similarityConfigs.get(similarityName);

      SimilaritySource simSource = (SimilaritySource) Class
          .forName(simConfig.getType()).getConstructor(Map.class)
          .newInstance(simConfig.getParams());
      similarities.put(similarityName, simSource);
    }
    return similarities.get(similarityName);
  }

  private AnalyticsProcessor ensureAnalyticsProcessor(
      ResourceManagerConfig rmc, Properties env,
      Map<String, AnalyticsProcessorConfig> analyticsProcessorConfigs,
      String analyticsProcName) throws InstantiationException,
      IllegalAccessException, IllegalArgumentException,
      InvocationTargetException, NoSuchMethodException, SecurityException,
      ClassNotFoundException {
    if (!analyticsProcessors.containsKey(analyticsProcName)) {
      AnalyticsProcessorConfig aProcConfig = analyticsProcessorConfigs
          .get(analyticsProcName);

      analyticsProcessors.put(
          analyticsProcName,
          (AnalyticsProcessor) Class.forName(aProcConfig.getType())
              .getConstructor(Map.class, Properties.class)
              .newInstance(aProcConfig.getParams(), env));
    }
    return analyticsProcessors.get(analyticsProcName);

  }

  private ExpressionFilteringCollectorFactory ensureExpressionFilter(
      ResourceManagerConfig rmc, Properties env,
      Map<String, ExpressionFilterConfig> expressionFilterConfigs,
      Map<String, ExpressionConfig> expressionConfigs,
      Map<String, ScopeConfig> scopeConfigs, String expressionFilterName)
      throws Exception {
    if (!expressionFilters.containsKey(expressionFilterName)) {
      ExpressionFilterConfig exprFilterConfig = expressionFilterConfigs
          .get(expressionFilterName);
      BooleanExpressionTree expr = (BooleanExpressionTree) ensureExpression(
          rmc, env, expressionConfigs, scopeConfigs,
          exprFilterConfig.getExpression());
      boolean exclude = exprFilterConfig.getExclude();
      ExpressionFilteringCollectorFactory efcf = new ExpressionFilteringCollectorFactory(
          expr, exclude);
      expressionFilters.put(expressionFilterName, efcf);
    }
    return expressionFilters.get(expressionFilterName);
  }

  private ExpressionTree ensureExpression(ResourceManagerConfig rmc,
      Properties env, Map<String, ExpressionConfig> expressionConfigs,
      Map<String, ScopeConfig> scopeConfigs, String expressionName)
      throws Exception {
    if (!expressions.containsKey(expressionName)) {
      ExpressionConfig expressionConfig = expressionConfigs.get(expressionName);
      ExpressionTree tree = ExpressionFactory.getExpressionTreeFromString(
          expressionConfig.getExpression(),
          ensureScope(rmc, env, scopeConfigs,
              expressionConfig.getJavaScriptScope()));
      expressions.put(expressionName, tree);
    }
    return expressions.get(expressionName);
  }

  private Scriptable ensureScope(ResourceManagerConfig rmc, Properties env,
      Map<String, ScopeConfig> scopeConfigs, String scopeName)
      throws FileNotFoundException, IOException {
    if (scopeName == null)
      return null;
    if (!scopes.containsKey(scopeName)) {
      ScopeConfig scopeConfig = scopeConfigs.get(scopeName);
      String fileName = env.getProperty(Constants.ETC_PATH_KEY)
          + File.separator + scopeConfig.getJavaScriptFile();
      Context cx = Context.enter();
      try {
        ScriptableObject scope = cx.initStandardObjects();
        cx.compileReader(new BufferedReader(new FileReader(fileName)),
            fileName, 1, null).exec(cx, scope);
        scope.sealObject();
        this.scopes.put(scopeName, scope);
      } finally {
        Context.exit();
      }
    }
    return this.scopes.get(scopeName);
  }

  private QueryProcessor ensureQueryProcessor(ResourceManagerConfig rmc,
      Properties env, Map<String, QueryProcessorConfig> queryProcessorConfigs,
      Map<String, QueryParserConfig> queryParserConfigs,
      Map<String, SorterConfig> sorterConfigs,
      Map<String, SimilarityConfig> similarityConfigs,
      Map<String, FieldSelectorConfig> fieldSelectorConfigs,
      Map<String, FaceterConfig> faceterConfigs,
      Map<String, FilterConfig> filterConfigs, String queryProcName,
      Map<String, ExpressionConfig> expressionConfigs,
      Map<String, ExpressionFilterConfig> expressionFilterConfigs,
      Map<String, ScopeConfig> scopeConfigs,
      Map<String, ReturnFormatConfig> returnFormatConfigs,
      Map<String, QueryFactoryConfig> queryFactoryConfigs,
      Map<String, TermDocsFactoryConfig> termDocsFactoryConfigs)
      throws InstantiationException, IllegalAccessException,
      InvocationTargetException, NoSuchMethodException, ClassNotFoundException,
      Exception {
    if (!queryProcs.containsKey(queryProcName)) {
      QueryProcessorConfig qProcConfig = queryProcessorConfigs
          .get(queryProcName);

      List<String> parserNames = qProcConfig.getParsers();
      Map<String, QueryParserFactory> myParsers = new HashMap<>(
          parserNames.size());
      for (String qParserName : parserNames) {
        // String qParserName = qProcConfig.getParser();

        if (!queryParsers.containsKey(qParserName)) {
          QueryParserConfig parserConf = queryParserConfigs.get(qParserName);
          QueryFactory qf = ensureAndGetQueryFactory(
              parserConf.getQueryFactory(), queryFactoryConfigs,
              termDocsFactoryConfigs);

          QueryParserFactory parserFactory = (QueryParserFactory) Class
              .forName(parserConf.getType())
              .getConstructor(Version.class, Analyzer.class, Map.class,
                  QueryFactory.class)
              .newInstance(Version.valueOf(parserConf.getMatchVersion()),
                  getAnalyzer(parserConf.getAnalyzer(), rmc, env),
                  parserConf.getParams(), qf);
          queryParsers.put(qParserName, parserFactory);
        }
        myParsers.put(qParserName, queryParsers.get(qParserName));
      }

      Map<String, List<String>> exprFilterNamesMap = qProcConfig
          .getExpressionFilterChains();

      Map<String, List<ExpressionFilteringCollectorFactory>> myExprFiltersMap = null;
      if (exprFilterNamesMap != null) {
        myExprFiltersMap = new HashMap<>(exprFilterNamesMap.size());
        for (String chainName : exprFilterNamesMap.keySet()) {
          List<String> exprFilterNames = exprFilterNamesMap.get(chainName);
          List<ExpressionFilteringCollectorFactory> myExprFilters = null;
          if (exprFilterNames != null) {
            myExprFilters = new ArrayList<>(exprFilterNames.size());
            for (String exprFilterName : exprFilterNames) {
              myExprFilters.add(ensureExpressionFilter(rmc, env,
                  expressionFilterConfigs, expressionConfigs, scopeConfigs,
                  exprFilterName));
            }
            myExprFiltersMap.put(chainName, myExprFilters);
          }
        }
      }
      Map<String, List<String>> sorterChainsMap = qProcConfig.getSorterChains();
      Map<String, List<SortFieldFactory>> mySorterChainsMap = new HashMap<>(
          sorterChainsMap.size());
      for (String chainName : sorterChainsMap.keySet()) {
        List<String> sorterNames = sorterChainsMap.get(chainName);
        List<SortFieldFactory> mySorters = new ArrayList<>(sorterNames.size());
        for (String sorterName : sorterNames) {
          mySorters.add(ensureAndGetSorter(rmc, env, sorterConfigs,
              expressionConfigs, scopeConfigs, sorterName));
        }
        mySorterChainsMap.put(chainName, mySorters);
      }
      List<SortFieldFactory> defaultSorterChain = mySorterChainsMap
          .get(qProcConfig.getDefaultSorterChain());
      String defaultFieldSelectorName = qProcConfig.getDefaultFieldSelector();
      if (!this.fieldSelectors.containsKey(defaultFieldSelectorName)) {
        FieldSelectorConfig fSelConfig = fieldSelectorConfigs
            .get(defaultFieldSelectorName);
        Map<String, ReturnFieldFormat> nameFields = new HashMap<>();
        Map<String, String> fieldNameConfs = new HashMap<>(
            fSelConfig.getNames());
        for (String fname : fieldNameConfs.keySet()) {
          String formatName = fieldNameConfs.get(fname);
          if (!fieldFormats.containsKey(formatName)) {
            ReturnFormatConfig rfc = returnFormatConfigs.get(formatName);
            fieldFormats.put(formatName, ReturnFieldFormat.newFormatter(rfc));
          }
          nameFields.put(fname, fieldFormats.get(formatName));
        }
        Map<String, String> patterns = fSelConfig.getPatterns();

        Map<Pattern, ReturnFieldFormat> fieldPatterns = new HashMap<>(
            patterns.size());
        for (String pat : patterns.keySet()) {
          String formatName = patterns.get(pat);
          if (!fieldFormats.containsKey(formatName)) {
            ReturnFormatConfig rfc = returnFormatConfigs.get(formatName);
            fieldFormats.put(formatName, ReturnFieldFormat.newFormatter(rfc));
          }
          fieldPatterns.put(Pattern.compile(pat), fieldFormats.get(formatName));
        }
        Map<String, ExpressionCollectorFactory> exprs = new HashMap<>();
        Map<String, String> expressionNames = fSelConfig.getExpressions();
        for (String asName : expressionNames.keySet()) {
          String exprName = expressionNames.get(asName);
          ExpressionTree tree = ensureExpression(rmc, env, expressionConfigs,
              scopeConfigs, exprName);
          exprs.put(asName, new ExpressionCollectorFactory(tree));
        }
        this.fieldSelectors.put(defaultFieldSelectorName,
            new CustomFieldSelector(nameFields, fieldPatterns, exprs));
      }
      CustomFieldSelector defaultFieldSelector = this.fieldSelectors
          .get(defaultFieldSelectorName);

      Map<String, CustomFieldSelector> myFieldSelectors = new HashMap<>();
      for (String fieldSelectorName : qProcConfig.getFieldSelectors()) {
        if (!this.fieldSelectors.containsKey(fieldSelectorName)) {
          FieldSelectorConfig fSelConfig = fieldSelectorConfigs
              .get(fieldSelectorName);
          Map<String, ReturnFieldFormat> nameFields = new HashMap<>();
          Map<String, String> fieldNameConfs = new HashMap<>(
              fSelConfig.getNames());
          for (String fname : fieldNameConfs.keySet()) {
            String formatName = fieldNameConfs.get(fname);
            if (!fieldFormats.containsKey(formatName)) {
              ReturnFormatConfig rfc = returnFormatConfigs.get(formatName);
              fieldFormats.put(formatName, ReturnFieldFormat.newFormatter(rfc));
            }
            nameFields.put(fname, fieldFormats.get(formatName));
          }
          Map<String, String> patterns = fSelConfig.getPatterns();

          Map<Pattern, ReturnFieldFormat> fieldPatterns = new HashMap<>(
              patterns.size());
          for (String pat : patterns.keySet()) {
            String formatName = patterns.get(pat);
            if (!fieldFormats.containsKey(formatName)) {
              ReturnFormatConfig rfc = returnFormatConfigs.get(formatName);
              fieldFormats.put(formatName, ReturnFieldFormat.newFormatter(rfc));
            }
            fieldPatterns.put(Pattern.compile(pat),
                fieldFormats.get(formatName));
          }
          Map<String, ExpressionCollectorFactory> exprs = new HashMap<>();
          Map<String, String> expressionNames = fSelConfig.getExpressions();
          for (String asName : expressionNames.keySet()) {
            String exprName = expressionNames.get(asName);
            ExpressionTree tree = ensureExpression(rmc, env, expressionConfigs,
                scopeConfigs, exprName);
            exprs.put(asName, new ExpressionCollectorFactory(tree));
          }
          this.fieldSelectors.put(fieldSelectorName, new CustomFieldSelector(
              nameFields, fieldPatterns, exprs));
        }
        myFieldSelectors.put(fieldSelectorName,
            fieldSelectors.get(fieldSelectorName));
      }

      Map<String, String> faceterNames = qProcConfig.getFaceters();
      Map<String, SearchFaceterFactory> myFaceters = null;
      if (faceterNames != null) {
        myFaceters = new HashMap<>(faceterNames.size());
        for (String faceterName : faceterNames.keySet()) {
          String faceterRef = faceterNames.get(faceterName);
          if (!faceters.containsKey(faceterRef)) {
            FaceterConfig faceterConf = faceterConfigs.get(faceterRef);
            SearchFaceterFactory faceter = (SearchFaceterFactory) Class
                .forName(faceterConf.getType())
                .getConstructor(Map.class, Properties.class)
                .newInstance(faceterConf.getParams(), env);
            faceters.put(faceterRef, faceter);
          }
          myFaceters.put(faceterName, faceters.get(faceterRef));
        }
      }

      List<String> filterNames = qProcConfig.getFilters();
      List<SearchFilterFactory> myFilters = null;
      if (filterNames != null) {
        myFilters = new ArrayList<SearchFilterFactory>(filterNames.size());
        for (String filterName : filterNames) {
          if (!filters.containsKey(filterName)) {
            FilterConfig filterConf = filterConfigs.get(filterName);
            SearchFilterFactory filter = (SearchFilterFactory) Class
                .forName(filterConf.getType())
                .getConstructor(Map.class, Properties.class)
                .newInstance(filterConf.getParams(), env);
            filters.put(filterName, filter);
          }
          myFilters.add(filters.get(filterName));
        }
      }

      String catchProcessorName = qProcConfig.getCatchProcessor();
      QueryProcessor catchProcessor = null;
      if (catchProcessorName != null) {
        catchProcessor = ensureQueryProcessor(rmc, env, queryProcessorConfigs,
            queryParserConfigs, sorterConfigs, similarityConfigs,
            fieldSelectorConfigs, faceterConfigs, filterConfigs,
            catchProcessorName, expressionConfigs, expressionFilterConfigs,
            scopeConfigs, returnFormatConfigs, queryFactoryConfigs,
            termDocsFactoryConfigs);
      }

      Set<String> scoreFields = new HashSet<>();
      scoreFields.addAll(qProcConfig.getScoreFields());
      queryProcs.put(
          queryProcName,
          (QueryProcessor) Class
              .forName(qProcConfig.getType())
              .getConstructor(Map.class, QueryParserFactory.class, Map.class,
                  List.class, QueryProcessor.class, CustomFieldSelector.class,
                  Map.class, Map.class, List.class, Map.class, Set.class,
                  Map.class, Properties.class)
              .newInstance(myParsers,
                  myParsers.get(qProcConfig.getDefaultParser()),
                  mySorterChainsMap, defaultSorterChain, catchProcessor,
                  defaultFieldSelector, myFieldSelectors, myFaceters,
                  myFilters, myExprFiltersMap, scoreFields,
                  qProcConfig.getParams(), env));
    }
    return queryProcs.get(queryProcName);
  }

  private SortFieldFactory ensureAndGetSorter(ResourceManagerConfig rmc,
      Properties env, Map<String, SorterConfig> sorterConfigs,
      Map<String, ExpressionConfig> expressionConfigs,
      Map<String, ScopeConfig> scopeConfigs, String sorterName)
      throws Exception {
    if (!sorters.containsKey(sorterName)) {
      SorterConfig sorterConf = sorterConfigs.get(sorterName);
      switch (sorterConf.getType()) {
      case "SCORE": {
        sorters.put(sorterName, new SortFieldFactory(SortField.FIELD_SCORE));
        break;
      }
      case "DOC": {
        sorters.put(sorterName, new SortFieldFactory(SortField.FIELD_DOC));
        break;
      }
      case "FIELD": {
        Map<String, ?> sorterParams = sorterConf.getParams();
        boolean descending = (Boolean) sorterParams.get("descending");
        String field = (String) sorterParams.get("field");
        String typeName = (String) sorterParams.get("type");
        int type;
        switch (typeName) {
        case "BYTE": {
          type = SortField.BYTE;
          break;
        }
        case "INT": {
          type = SortField.INT;
          break;
        }
        case "LONG": {
          type = SortField.LONG;
          break;
        }
        case "STRING": {
          type = SortField.STRING;
          break;
        }
        case "FLOAT": {
          type = SortField.FLOAT;
          break;
        }
        case "DOUBLE": {
          type = SortField.DOUBLE;
          break;
        }
        case "SHORT": {
          type = SortField.SHORT;
          break;
        }
        case "STRING_VAL": {
          type = SortField.STRING_VAL;
          break;
        }
        case "CUSTOM": {
          type = SortField.CUSTOM;
          break;
        }
        default: {
          type = 0;
        }
        }
        sorters.put(sorterName, new SortFieldFactory(new SortField(field, type,
            descending)));
        break;
      }
      case "EXPRESSION": {
        Map<String, ?> sorterParams = sorterConf.getParams();
        boolean descending = (Boolean) sorterParams.get("descending");
        String expressionName = (String) sorterParams.get("expression");
        ExpressionComparatorSourceFactory factory = new ExpressionComparatorSourceFactory(
            this.ensureExpression(rmc, env, expressionConfigs, scopeConfigs,
                expressionName));
        sorters.put(sorterName, new SortFieldFactory(factory, descending,
            expressionName));
        break;
      }
      }
    }
    return sorters.get(sorterName);
  }

  private QueryFactory ensureAndGetQueryFactory(String queryFactoryName,
      Map<String, QueryFactoryConfig> queryFactoryConfigs,
      Map<String, TermDocsFactoryConfig> tdfConfigs)
      throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException,
      NoSuchMethodException, SecurityException, ClassNotFoundException {

    if (!this.queryFactories.containsKey(queryFactoryName)) {

      QueryFactoryConfig qfc = queryFactoryConfigs.get(queryFactoryName);
      TermDocsFactory tdf = ensureAndGetTermDocsFactory(
          qfc.getTermDocsFactory(), tdfConfigs);
      Map<String, String> fieldTDFConfigs = qfc.getFieldTermDocsFactories();
      Map<String, TermDocsFactory> fieldTDFs = new HashMap<>(
          fieldTDFConfigs.size());
      for (String field : fieldTDFConfigs.keySet()) {
        fieldTDFs
            .put(
                field,
                ensureAndGetTermDocsFactory(fieldTDFConfigs.get(field),
                    tdfConfigs));
      }
      this.queryFactories.put(
          queryFactoryName,
          (QueryFactory) Class.forName(qfc.getType())
              .getConstructor(TermDocsFactory.class, Map.class)
              .newInstance(tdf, fieldTDFs));
    }
    return this.queryFactories.get(queryFactoryName);
  }

  private TermDocsFactory ensureAndGetTermDocsFactory(
      String termDocsFactoryName, Map<String, TermDocsFactoryConfig> tdfConfigs)
      throws InstantiationException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException,
      NoSuchMethodException, SecurityException, ClassNotFoundException {
    if (!this.termDocsFactories.containsKey(termDocsFactoryName)) {
      TermDocsFactoryConfig tdfc = tdfConfigs.get(termDocsFactoryName);
      this.termDocsFactories.put(
          termDocsFactoryName,
          (TermDocsFactory) Class.forName(tdfc.getType())
              .getConstructor(Map.class).newInstance(tdfc.getParams()));
    }
    return this.termDocsFactories.get(termDocsFactoryName);
  }

  private Map<String, FieldTemplate> bindFieldTemplates(
      Map<String, FieldTemplateConfig> templateConfigs,
      Map<String, String> fieldNames) {
    Map<String, FieldTemplate> fieldTemps = new HashMap<>(fieldNames.size());
    for (String fieldName : fieldNames.keySet()) {
      String templateName = fieldNames.get(fieldName);
      if (!fieldTemplates.containsKey(templateName)) {
        FieldTemplateConfig templateConfig = templateConfigs.get(templateName);
        fieldTemplates.put(templateName,
            FieldTemplateFactory.createTemplate(templateConfig));
      }
      fieldTemps.put(fieldName, fieldTemplates.get(templateName));
    }
    return fieldTemps;
  }

  private Map<Pattern, FieldTemplate> bindPatternFieldTemplates(
      Map<String, FieldTemplateConfig> templateConfigs,
      Map<String, String> fieldNames) {
    Map<Pattern, FieldTemplate> fieldTemps = new HashMap<>(fieldNames.size());
    for (String fieldName : fieldNames.keySet()) {
      String templateName = fieldNames.get(fieldName);
      if (!fieldTemplates.containsKey(templateName)) {
        FieldTemplateConfig templateConfig = templateConfigs.get(templateName);
        fieldTemplates.put(templateName,
            FieldTemplateFactory.createTemplate(templateConfig));
      }
      fieldTemps.put(Pattern.compile(fieldName),
          fieldTemplates.get(templateName));
    }
    return fieldTemps;
  }

  private Analyzer getAnalyzer(String anName, ResourceManagerConfig rmc,
      Properties env) throws Exception {
    Map<String, AnalyzerConfig> analyzerConfigs = rmc.getAnalyzers();
    Map<String, CharFilterConfig> charFilterConfigs = rmc.getCharFilters();
    Map<String, TokenizerConfig> tokenizerConfigs = rmc.getTokenizers();
    Map<String, TokenFilterConfig> filterConfigs = rmc.getTokenFilters();

    if (!analyzers.containsKey(anName)) {
      AnalyzerConfig analyzerConf = analyzerConfigs.get(anName);

      List<String> charFilterNames = analyzerConf.getCharFilters();
      List<CharFilterSource> cFilters = null;
      if (charFilterNames != null) {
        cFilters = new ArrayList<>(charFilterNames.size());
        for (String charFilterName : charFilterNames) {
          if (!charFilters.containsKey(charFilterName)) {
            CharFilterConfig charFilterConf = charFilterConfigs
                .get(charFilterName);
            charFilters.put(
                charFilterName,
                (CharFilterSource) Class.forName(charFilterConf.getType())
                    .getConstructor(Map.class, Properties.class)
                    .newInstance(charFilterConf.getParams(), env));
          }
          cFilters.add(charFilters.get(charFilterName));
        }
      }

      String tokenizerName = analyzerConf.getTokenizer();
      if (!tokenizers.containsKey(tokenizerName)) {
        TokenizerConfig tokenizerConf = tokenizerConfigs.get(tokenizerName);
        tokenizers.put(
            tokenizerName,
            (TokenizerSource) Class.forName(tokenizerConf.getType())
                .getConstructor(Map.class, Properties.class)
                .newInstance(tokenizerConf.getParams(), env));
      }
      TokenizerSource tokenizer = tokenizers.get(tokenizerName);

      List<String> filterNames = analyzerConf.getTokenFilters();
      List<TokenFilterSource> filters = null;
      if (filterNames != null) {
        filters = new ArrayList<>(filterNames.size());
        for (String filterName : filterNames) {
          if (!tokenFilters.containsKey(filterName)) {
            TokenFilterConfig filterConf = filterConfigs.get(filterName);
            tokenFilters.put(
                filterName,
                (TokenFilterSource) Class.forName(filterConf.getType())
                    .getConstructor(Map.class, Properties.class)
                    .newInstance(filterConf.getParams(), env));
          }
          filters.add(tokenFilters.get(filterName));
        }
      }
      int posIncrGap = analyzerConf.getPositionIncrementGap();
      int offsetGap = analyzerConf.getOffsetGap();
      Analyzer a = new GenericAnalyzer(cFilters, tokenizer, filters,
          posIncrGap, offsetGap);
      Map<String, String> fieldConfs = analyzerConf.getFieldAnalyzers();
      if (fieldConfs != null && !fieldConfs.isEmpty()) {
        Map<String, Analyzer> fieldAnalyzers = new HashMap<>(fieldConfs.size());
        for (String field : fieldConfs.keySet()) {
          String fanName = fieldConfs.get(field);
          fieldAnalyzers.put(field, getAnalyzer(fanName, rmc, env));
        }
        a = new PerFieldAnalyzerWrapper(a, fieldAnalyzers);
      }
      analyzers.put(anName, a);
    }
    return analyzers.get(anName);
  }

  public void destroy() throws CorruptIndexException, IOException {
    for (DataManager d : dataManagers.values())
      d.close();
    for (ExecutorService e : executorServices.values())
      e.shutdown();
  }

  public DataManager dataWriter(String name) {
    return dataManagers.get(name);
  }

  public Analyzer analyzer(String name) {
    return analyzers.get(name);
  }
}
