package com.cleartrip.sw.search.context;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public abstract class ResourceManagerConfig {

  public static abstract class FaceterConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class FilterConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class ScopeConfig {
    public abstract String getJavaScriptFile();
  }

  public static abstract class ExpressionConfig {
    public abstract String getExpression();

    public abstract String getJavaScriptScope();
  }

  public static abstract class ExpressionFilterConfig {
    public abstract String getExpression();

    public abstract boolean getExclude();
  }

  public static abstract class SorterConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class TermDocsFactoryConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class QueryFactoryConfig {
    public abstract String getType();

    public abstract String getTermDocsFactory();

    public abstract Map<String, String> getFieldTermDocsFactories();
  }

  public static abstract class QueryParserConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();

    public abstract String getMatchVersion();

    public abstract String getAnalyzer();

    public abstract String getQueryFactory();
  }

  public static abstract class SimilarityConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class QueryProcessorConfig {
    public abstract String getType();

    public abstract List<String> getParsers();

    public abstract String getDefaultParser();

    public abstract Map<String, List<String>> getSorterChains();

    public abstract String getDefaultSorterChain();

    public abstract Map<String, String> getFaceters();

    public abstract List<String> getFilters();

    public abstract Map<String, List<String>> getExpressionFilterChains();

    public abstract List<String> getScoreFields();

    public abstract String getCatchProcessor();

    public abstract String getDefaultFieldSelector();

    public abstract List<String> getFieldSelectors();

    public abstract Map<String, ?> getParams();

  }

  public static abstract class AnalyticsProcessorConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class SearcherWarmingStrategyConfig {
    public abstract String getSchema();

    public abstract List<String> getFieldCacheWarmables();

  }

  public static abstract class ExecuterServiceFactoryConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class SearcherFactoryConfig {
    public abstract String getSearchSimilarity();

    public abstract String getSearcherWarmingStrategy();

    public abstract String getExecutorServiceFactory();

    public abstract Map<String, String> getLookupFactories();

  }

  public static abstract class DataManagerConfig {
    public abstract String getIndexWriter();

    public abstract String getSchema();

    public abstract Map<String, String> getQueryProcessors();

    public abstract Map<String, String> getAnalyticsProcessors();

    public abstract Map<String, String> getSuggestProcessors();

    public abstract String getSearcherFactory();

    public abstract Map<String, ?> getParams();

  }

  public static abstract class CharFilterConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class TokenizerConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class TokenFilterConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class AnalyzerConfig {
    public abstract List<String> getCharFilters();

    public abstract String getTokenizer();

    public abstract List<String> getTokenFilters();

    public abstract Map<String, String> getFieldAnalyzers();

    public abstract int getPositionIncrementGap();

    public abstract int getOffsetGap();
  }

  public static abstract class FieldTemplateConfig {
    public abstract String getType();

    public abstract String getIndexed();

    public abstract String getStored();

    public abstract String getTermVector();

    public abstract boolean getInternName();

    public abstract int getPrecisionStep();

    public abstract String getNumericType();

    public abstract float getDefaultBoost();

    public abstract String getIndexOptions();

    public abstract Object getDefaultValue();

    public abstract String getCardinality();

  }

  public static abstract class IndexerConfig {
    public abstract String getIndexName();

    public abstract String getDirType();

    public abstract String getMergePolicy();

    public abstract String getLockFactory();

    public abstract String getIndexVersion();

    public abstract String getAnalyzer();

    public abstract String getOpenMode();

    public abstract String getMemUsageEnv();

    public abstract String getSimilarity();

  }

  public static abstract class MergePolicyConfig {
    public abstract String getType();

    public abstract Map<String, ?> getParams();
  }

  public static abstract class SchemaConfig {
    public abstract String getIdField();

    public abstract Map<String, String> getRequiredFieldTemplates();

    public abstract Map<String, String> getOptionalFieldTemplates();

    public abstract Map<String, String> getPatternFieldTemplates();
  }

  public static abstract class ReturnFormatConfig {
    public abstract String getType();

    public abstract String getCardinality();
  }

  public static abstract class FieldSelectorConfig {
    public abstract Map<String, String> getNames();

    public abstract Map<String, String> getPatterns();

    public abstract Map<String, String> getExpressions();
  }

  public static abstract class SuggestCollectorConfig {
    public abstract String getType();

    public abstract List<String> getSorterChain();

    public abstract List<String> getFields();
    
    public abstract List<String> getPrePopulatedFields();
    
    public abstract Map<String, ?> getParams();
  }

  public static abstract class SuggestProcessorConfig {
    public abstract String getType();

    public abstract String getCollector();
  }
  
  public static abstract class LookupFactoryConfig {
	  public abstract String getType();
	  
	  public abstract String getPathEnv();
  }

  public abstract Map<String, CharFilterConfig> getCharFilters();

  public abstract Map<String, TokenizerConfig> getTokenizers();

  public abstract Map<String, TokenFilterConfig> getTokenFilters();

  public abstract Map<String, AnalyzerConfig> getAnalyzers();

  public abstract Map<String, FieldTemplateConfig> getFieldTemplates();

  public abstract Map<String, SchemaConfig> getSchemata();

  public abstract Map<String, DataManagerConfig> getDataManagers();

  public abstract Map<String, IndexerConfig> getIndexWriters();

  public abstract Map<String, ScopeConfig> getScopes();

  public abstract Map<String, ExpressionConfig> getExpressions();

  public abstract Map<String, SorterConfig> getSorters();

  public abstract Map<String, QueryParserConfig> getQueryParsers();

  public abstract Map<String, QueryProcessorConfig> getQueryProcessors();

  public abstract Map<String, AnalyticsProcessorConfig> getAnalyticsProcessors();

  public abstract Map<String, SimilarityConfig> getSimilarities();

  public abstract Map<String, FieldSelectorConfig> getFieldSelectors();

  public abstract Map<String, ReturnFormatConfig> getReturnFormats();

  public abstract Map<String, FaceterConfig> getFaceters();

  public abstract Map<String, FilterConfig> getFilters();

  public abstract Map<String, ExpressionFilterConfig> getExpressionFilters();

  public abstract Map<String, QueryFactoryConfig> getQueryFactories();

  public abstract Map<String, TermDocsFactoryConfig> getTermDocsFactories();

  public abstract Map<String, MergePolicyConfig> getMergePolicies();

  public abstract Map<String, SearcherWarmingStrategyConfig> getSearcherWarmingStrategies();

  public abstract Map<String, ExecuterServiceFactoryConfig> getExecuterServiceFactories();

  public abstract Map<String, SearcherFactoryConfig> getSearcherFactories();

  public abstract Map<String, LookupFactoryConfig> getLookupFactories();

  public abstract Map<String, SuggestCollectorConfig> getSuggestCollectors();

  public abstract Map<String, SuggestProcessorConfig> getSuggestProcessors();

  public ResourceManager deriveResourceManager(ResourceManager in,
      Properties env) throws Exception {
    ResourceManager rm = new ResourceManager(in, this, env);
    return rm;
  }

}
