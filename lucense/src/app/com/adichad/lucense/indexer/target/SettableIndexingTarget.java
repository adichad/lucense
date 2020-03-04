package com.adichad.lucense.indexer.target;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;

import com.adichad.lucense.resource.IndexerConfigParser.FieldModifier;

public interface SettableIndexingTarget extends IndexingTarget {
  public abstract void setFieldModifiers(HashMap<String, FieldModifier> fieldModifiers2,
      LinkedHashMap<Pattern, FieldModifier> regexModifiers);

  public abstract void setAnalyzer(HashMap<String, Analyzer> namedAnalyzers);

  public abstract void setName(String value);

  public abstract void setPath(String value);

  public abstract void setIdField(String idfield);

  public abstract void setAnalyzerName(String value);

  public abstract void setFilterString(String value);

  public abstract void setDataSource(String value);

  public abstract void setAppend(boolean b);

  public abstract void setMaxFieldLength(int i);

  public abstract void setMaxBufferedDocs(int i);

  public abstract void setUseCompoundFile(boolean b);

  public abstract void setOptimize(boolean b);

  public abstract void addModifierName(String value);

  public abstract void setDuplicateUpdate(boolean b);

  public abstract void setMergeFactor(int mergeFactor);

  

}
