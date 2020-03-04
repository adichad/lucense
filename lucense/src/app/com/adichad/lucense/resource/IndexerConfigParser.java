package com.adichad.lucense.resource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.util.Version;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

import com.adichad.lucense.analysis.AnalyzerFactory;
import com.adichad.lucense.analysis.component.AnalyzerComponentFactory;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;
import com.adichad.lucense.expression.ExpressionFactory;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.StringLucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;
import com.adichad.lucense.expression.parse.ParseException;
import com.adichad.lucense.indexer.FieldTransformer;
import com.adichad.lucense.indexer.FieldTransformerFactory;
import com.adichad.lucense.indexer.source.DataSource;
import com.adichad.lucense.indexer.target.LuceneIndexDefinition;
import com.adichad.lucense.indexer.target.Neo4JTextIndexDefinition;
import com.adichad.lucense.indexer.target.SettableIndexingTarget;
import com.adichad.lucense.request.Request.FieldType;

public class IndexerConfigParser implements ConfigurationNodeVisitor {

  class AnalyzerSource {
    private String name;

    private String tokenizerType;

    private String tokenCharDef;

    private boolean morphInject;

    private HashMap<String, AnalyzerSource> fieldAnalyzerSource;

    private String file;

    private String filterSubtype;

    private String filterType;

    private List<TokenFilterSource> tokenFilterSources;

    private TokenStreamSource tokenStreamSource;

    public int minLen = 0;

    public int prefixLen = -1;

    public int editDistance = -1;

    public double filterProbability = -1d;

    public double penaltyFactor = 5d;

    public int maxCorrections = -1;

    public boolean replace = true;

    public Version luceneVersion = Version.LUCENE_33;

    public AnalyzerSource() {
      this.tokenFilterSources = new ArrayList<TokenFilterSource>();

    }

    public Analyzer getAnalyzer() throws IOException {
      if ((this.fieldAnalyzerSource != null) && (this.fieldAnalyzerSource.size() > 0)) {
        HashMap<String, Analyzer> fieldAnalyzers = new HashMap<String, Analyzer>();
        for (String field : this.fieldAnalyzerSource.keySet()) {
          fieldAnalyzers.put(field, this.fieldAnalyzerSource.get(field).getAnalyzer());
        }
        return AnalyzerFactory.createNestedAnalyzer(this.luceneVersion, this.tokenStreamSource,
            this.tokenFilterSources, fieldAnalyzers);
      }

      return AnalyzerFactory.createAnalyzer(this.tokenStreamSource, this.tokenFilterSources);
    }
  }

  public class FieldModifier {
    private String name;

    private String field;

    private FieldType type;

    private Field.Index indexMode;

    private Field.Store storeMode;

    private String transformString;

    private StringLucenseExpression transform;

    private Field.TermVector termv;

    private HashMap<Context, StringLucenseExpression> transformMap;

    private HashMap<String, FieldType> fieldTypes;

    private boolean omitNorms;

    public ScriptableObject scope = null;

    public FieldTransformer transformer;

    public FieldModifier() {
      this.indexMode = Field.Index.NOT_ANALYZED;
      this.storeMode = Field.Store.NO;
      this.termv = Field.TermVector.NO;
      this.type = FieldType.TYPE_STRING;
      this.transformMap = new HashMap<Context, StringLucenseExpression>();
      this.omitNorms = true;
    }

    public Store getStoreMode() {
      return this.storeMode;
    }

    public Field.Index getIndexMode() {
      return this.indexMode;
    }

    public void addContext(Context cx) throws ParseException {
      if (this.transformer == null) {
        this.transform = (StringLucenseExpression) ExpressionFactory.getExpressionFromString(this.transformString,
            this.type, cx, this.scope, null, new HashMap<String, LucenseExpression>(), new ValueSources(
                new HashMap<String, IntValueSource>(), new HashMap<String, FloatValueSource>(),
                new HashMap<String, DoubleValueSource>(), new HashMap<String, BooleanValueSource>(),
                new HashMap<String, StringValueSource>()), null);
        if (this.transform != null) {

          synchronized (this.transformMap) {
            this.transformMap.put(cx, this.transform);
          }
        }
      }
    }
    
    public StringLucenseExpression getTransformExpression(Context cx) throws ParseException {
      StringLucenseExpression transform = null;
      if(transformString!=null)
        transform = (StringLucenseExpression) ExpressionFactory.getExpressionFromString(this.transformString,
            this.type, cx, this.scope, null, new HashMap<String, LucenseExpression>(), new ValueSources(
                new HashMap<String, IntValueSource>(), new HashMap<String, FloatValueSource>(),
                new HashMap<String, DoubleValueSource>(), new HashMap<String, BooleanValueSource>(),
                new HashMap<String, StringValueSource>()), null);
      return transform;
    }

    public Field.TermVector getTermVector() {
      return this.termv;
    }

    public String getFieldName() {
      return this.field;
    }

    public FieldType getFieldType() {
      return this.type;
    }

    public String getTransformString() {
      return this.transformString;
    }

    public StringLucenseExpression getTransform(Context cx) {
      return this.transformMap.get(cx);
    }

    public void setFieldTypes(HashMap<String, FieldType> fieldTypes) {
      this.fieldTypes = fieldTypes;
    }

    public boolean getOmitNorms() {
      return this.omitNorms;
    }
  }

  private DataSource ds = null;

  private AnalyzerSource an = null;

  private AnalyzerSource fan = null;

  private FieldModifier modifier = null;

  private LuceneIndexDefinition index;

  private HashMap<String, AnalyzerSource> fieldAnalyzers;

  private boolean readingField;

  private Map<String, DataSource> namedDataSources;

  private HashMap<String, Analyzer> namedAnalyzers;

  private HashMap<String, FieldModifier> fieldModifiers;

  private HashMap<String, SettableIndexingTarget> namedIndexes;

  private StringWriter prehistoricLog;

  private String varname;

  private String vartype;

  private HashSet<Substitution> varsubstitutions;

  private String varqueryname;

  private int pos;

  private String sourcename;

  private boolean configureLogging;

  private Context cx;

  private String scopeName;

  private ScriptableObject scope;

  private Map<String, ScriptableObject> scopeMap;

  private FieldTransformer transformer;

  private LinkedHashMap<Pattern, FieldModifier> regexModifiers;

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  IndexerConfigParser(StringWriter prehistoricLog, Context cx) {
    this.namedDataSources = new HashMap<String, DataSource>();
    this.namedAnalyzers = new HashMap<String, Analyzer>();
    this.fieldModifiers = new HashMap<String, FieldModifier>();
    this.regexModifiers = new LinkedHashMap<Pattern, FieldModifier>();
    this.namedIndexes = new HashMap<String, SettableIndexingTarget>();
    this.prehistoricLog = prehistoricLog;
    this.configureLogging = true;
    this.cx = cx;
    this.scopeMap = new HashMap<String, ScriptableObject>();
    this.scope = cx.initStandardObjects();
    this.transformer = null;
  }

  private IndexerConfigParser() {
    // this(null);
  }

  public IndexerConfigParser(StringWriter prehistoricLog, boolean configureLogging, Context cx) {
    this(prehistoricLog, cx);
    this.configureLogging = configureLogging;
  }

  IndexerResourceManager parse(String configPath) throws ConfigurationException {
    XMLConfiguration config = new XMLConfiguration(configPath);
    config.getRootNode().visit(this);
    for (SettableIndexingTarget index : this.namedIndexes.values()) {
      index.setFieldModifiers(this.fieldModifiers, this.regexModifiers);
      index.setAnalyzer(this.namedAnalyzers);
    }

    return new IndexerResourceManager(this.namedDataSources, this.namedIndexes);
  }

  @Override
  public boolean terminate() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void visitAfterChildren(ConfigurationNode node) {
    try {
      String name = node.getName();
      if (node.getParentNode() != null) {
        String value = null;
        if (node.getValue() != null)
          value = node.getValue().toString();
        String parname = node.getParentNode().getName();
        if (parname.equals("datasource")) {
          if (this.ds == null)
            this.ds = new DataSource();
          if (name.equals("query")) {
            this.ds.setQuery(value, this.varsubstitutions);// dsquery
            this.varsubstitutions = new HashSet<Substitution>();
          } else if (name.equals("name")) {
            this.ds.setName(value);// dsname
          } else if (name.equals("type")) {
            this.ds.setType(value);// dstype
          } else if (name.equals("pre-query")) {
            this.ds.setPreQuery(value, this.varsubstitutions);// dsprequery
            this.varsubstitutions = new HashSet<Substitution>();
          } else if (name.equals("post-query")) {
            this.ds.setPostQuery(value, this.varsubstitutions);// dspostquery =
            this.varsubstitutions = new HashSet<Substitution>();
          } else if (name.equals("var-query")) {
            this.ds.addVarQuery(this.varqueryname, this.varsubstitutions, value);
            this.varsubstitutions = new HashSet<Substitution>();
          } else if (name.equals("batch-step")) {
            this.ds.setBatchStep(Integer.parseInt(value));// dsbatchstep =
                                                          // Integer.parseInt(value);
          } else if (name.equals("host")) {
            this.ds.setHost(value);// dshost = value;
          } else if (name.equals("port")) {
            this.ds.setPort(Integer.parseInt(value));// dsport =
                                                     // Integer.parseInt(value);
          } else if (name.equals("username")) {
            this.ds.setUser(value);// dsuser = value;
          } else if (name.equals("password")) {
            this.ds.setPassword(value);// dbpass = value;
          } else if (name.equals("database")) {
            this.ds.setDatabase(value);// dsdb = value;
          } else if (name.equals("path")) {
            this.ds.setPath(value);// dspath = value;
          } else if (name.equals("filetype") || name.equals("sd-model")) {
            this.ds.setFileType(value);// dsfiletype = value;
          } else if (name.equals("subdirs")) {
            if (value != null) {
              value = value.toLowerCase();
              this.ds.setSubDirs(value.equals("true") ? true : false);// subdirs
                                                                      // =
            }
          } else if (name.equals("id-field")) {
            this.ds.setIdField(value);
          }
        } else if (parname.equals("var-query")) {
          if (name.equals("name")) {
            this.varqueryname = value;
          } else if (name.equals("var-substitution")) {
            this.varsubstitutions.add(new Substitution(this.pos, this.sourcename, this.vartype, this.varname));
            this.vartype = null;
            this.varname = null;
            this.pos = 0;
            this.sourcename = null;
          }
        } else if (parname.equals("query") || parname.equals("post-query") || parname.equals("pre-query")) {
          if (name.equals("var-substitution")) {
            this.varsubstitutions.add(new Substitution(this.pos, this.sourcename, this.vartype, this.varname));
            this.vartype = null;
            this.varname = null;
            this.pos = 0;
            this.sourcename = null;
          }
        } else if (parname.equals("var-substitution")) {
          if (name.equals("pos")) {
            this.pos = Integer.parseInt(value);
          } else if (name.equals("source")) {
            this.sourcename = value;
          } else if (name.equals("type")) {
            this.vartype = value;
          } else if (name.equals("var")) {
            this.varname = value;
          }
        } else if (parname.equals("indexer")) {
          if (name.equals("datasource")) {
            this.namedDataSources.put(this.ds.getName(), this.ds);
            this.ds = null;
          } else if (name.equals("analyzer")) {
            this.an.tokenStreamSource = AnalyzerComponentFactory.getTokenStreamSource(this.an.luceneVersion,
                this.an.tokenizerType, this.an.tokenCharDef);
            this.an.fieldAnalyzerSource = this.fieldAnalyzers;
            this.namedAnalyzers.put(this.an.name, this.an.getAnalyzer());
            this.an = null;
            this.fieldAnalyzers = null;
          } else if (name.equals("regex-field-modifier")) {
            if (this.regexModifiers == null) {
              this.regexModifiers = new LinkedHashMap<Pattern, FieldModifier>();
            }
            this.regexModifiers.put(Pattern.compile(this.modifier.field), this.modifier);
            this.modifier = null;
          } else if (name.equals("field-modifier")) {
            if (this.fieldModifiers == null) {
              this.fieldModifiers = new HashMap<String, FieldModifier>();
            }
            this.fieldModifiers.put(this.modifier.name, this.modifier);
            this.modifier = null;
          } else if (name.equals("index")) {
            this.namedIndexes.put(this.index.getName(), this.index);
            this.index = null;
          } else if (name.equals("langindex")) {
            this.namedIndexes.put(this.index.getName(), this.index);
            this.index = null;
          } else if (name.equals("scope")) {
            if ((this.scopeName != null) && (this.scope != null)) {
              this.scope.sealObject();
              this.scopeMap.put(this.scopeName, this.scope);
              this.scopeName = null;
              this.scope = this.cx.initStandardObjects();
            }
          } else if (name.equals("log-properties")) {
            if (configureLogging) {
              // fix that bloody leak!
              Logger.getRootLogger().removeAllAppenders();
              Logger.getLogger("ErrorLogger").removeAllAppenders();
              Logger.getLogger("StatusLogger").removeAllAppenders();
              Properties properties = new Properties();
              try {
                properties.load(new FileInputStream(value));
                PropertyConfigurator.configure(properties);
              } catch (Exception e) {
                errorLogger.log(Level.FATAL, "Logging config failure: " + e);
                System.out.println(this.prehistoricLog);
                System.exit(1);
              }

              HashMap<Appender, Layout> appenderLayoutMap = new HashMap<Appender, Layout>();
              for (Enumeration<?> apps = statusLogger.getAllAppenders(); apps.hasMoreElements();) {
                Appender app = (Appender) apps.nextElement();
                appenderLayoutMap.put(app, app.getLayout());
                app.setLayout(new PatternLayout("%m"));
              }

              for (Appender app : appenderLayoutMap.keySet()) {
                app.setLayout(appenderLayoutMap.get(app));
              }
            }
            // we now know how to write, so flush prehistoric log
            statusLogger.log(Level.INFO, this.prehistoricLog.toString());

          }
        } else if (parname.equals("analyzer")) {
          if (this.an == null) {
            this.an = new AnalyzerSource();
          }
          if (name.equals("tokenizer")) {
            this.an.tokenCharDef = value;
          } else if (name.equals("lucene-version")) {
            this.an.luceneVersion = Version.valueOf(value);
          } else if (name.equals("name")) {
            this.an.name = value;
          } else if (name.equals("tokenfilter")) {
            TokenFilterSource tfs = AnalyzerComponentFactory.getTokenFilterSource(this.an.luceneVersion,
                this.an.filterType, this.an.filterSubtype, this.an.morphInject, this.an.file, this.an.minLen,
                this.an.prefixLen, this.an.editDistance, this.an.filterProbability, this.an.penaltyFactor,
                this.an.maxCorrections, this.an.replace);
            if (tfs != null)
              this.an.tokenFilterSources.add(tfs);
          } else if (name.equals("field")) {
            if (this.fieldAnalyzers == null) {
              this.fieldAnalyzers = new HashMap<String, AnalyzerSource>();
            }
            this.fan.tokenStreamSource = AnalyzerComponentFactory.getTokenStreamSource(this.fan.luceneVersion,
                this.fan.tokenizerType, this.fan.tokenCharDef);
            this.fieldAnalyzers.put(this.fan.name, this.fan);
            this.fan = null;
          }
        } else if (parname.equals("field")) {
          if (this.fan == null) {
            this.fan = new AnalyzerSource();
          }
          if (name.equals("tokenizer")) {
            this.fan.tokenCharDef = value;
          } else if (name.equals("lucene-version")) {
            this.fan.luceneVersion = Version.valueOf(value);
          } else if (name.equals("name")) {
            this.fan.name = value;
          } else if (name.equals("tokenfilter")) {
            TokenFilterSource tfs = AnalyzerComponentFactory.getTokenFilterSource(this.fan.luceneVersion,
                this.fan.filterType, this.fan.filterSubtype, this.fan.morphInject, this.fan.file, this.fan.minLen,
                this.fan.prefixLen, this.fan.editDistance, this.fan.filterProbability, this.fan.penaltyFactor,
                this.fan.maxCorrections, this.fan.replace);
            if (tfs != null)
              this.fan.tokenFilterSources.add(tfs);
          }
        } else if (parname.equals("tokenizer")) {
          if (this.readingField) {
            if (this.fan == null) {
              this.fan = new AnalyzerSource();
            }
            if (name.equals("type")) {
              this.fan.tokenizerType = value;
            }
          } else {
            if (this.an == null) {
              this.an = new AnalyzerSource();
            }
            if (name.equals("type")) {
              this.an.tokenizerType = value;
            }
          }
        } else if (parname.equals("tokenfilter")) {
          if (this.readingField) {
            if (this.fan == null)
              this.fan = new AnalyzerSource();
            if (name.equals("type")) {
              this.fan.filterType = value;
            } else if (name.equals("subtype")) {
              this.fan.filterSubtype = value;
            } else if (name.equals("file")) {
              this.fan.file = value;
            } else if (name.equals("inject") || name.equals("mark-payload")) {
              this.fan.morphInject = value.equals("true") ? true : false;
            } else if (name.equals("min-length") || name.equals("max-length")) {
              this.fan.minLen = Integer.valueOf(value);
            } else if (name.equals("prefix-length")) {
              this.fan.prefixLen = Integer.valueOf(value);
            } else if (name.equals("edit-distance")) {
              this.fan.editDistance = Integer.valueOf(value);
            } else if (name.equals("filter-probability")) {
              this.fan.filterProbability = Double.valueOf(value);
            } else if (name.equals("levenshtein-penalty-factor")) {
              this.fan.penaltyFactor = Double.valueOf(value);
            } else if (name.equals("max-corrections")) {
              this.fan.maxCorrections = Integer.valueOf(value);
            } else if (name.equals("replace-token") || name.equals("incr-pos")) {
              this.fan.replace = Boolean.valueOf(value);
            }
          } else {
            if (this.an == null)
              this.an = new AnalyzerSource();
            if (name.equals("type")) {
              this.an.filterType = value;
            } else if (name.equals("subtype")) {
              this.an.filterSubtype = value;
            } else if (name.equals("file")) {
              this.an.file = value;
            } else if (name.equals("inject") || name.equals("mark-payload")) {
              this.an.morphInject = value.equals("true") ? true : false;
            } else if (name.equals("min-length") || name.equals("max-length")) {
              this.an.minLen = Integer.valueOf(value);
            } else if (name.equals("prefix-length")) {
              this.an.prefixLen = Integer.valueOf(value);
            } else if (name.equals("edit-distance")) {
              this.an.editDistance = Integer.valueOf(value);
            } else if (name.equals("filter-probability")) {
              this.an.filterProbability = Double.valueOf(value);
            } else if (name.equals("levenshtein-penalty-factor")) {
              this.an.penaltyFactor = Double.valueOf(value);
            } else if (name.equals("max-corrections")) {
              this.an.maxCorrections = Integer.valueOf(value);
            } else if (name.equals("replace-token") || name.equals("incr-pos")) {
              this.an.replace = Boolean.valueOf(value);
            }
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
        } else if (parname.equals("transform")) {
          if (name.equals("scope")) {
            this.scope = scopeMap.get(value);
          } else if (name.equals("class")) {
            this.transformer = ((FieldTransformerFactory) ((Class<?>) Class.forName(value)).getDeclaredConstructor()
                .newInstance()).getFieldTransformer(((ConfigurationNode) node.getParentNode().getChildren("params")
                .get(0)));
          }
        } else if (parname.equals("field-modifier")||parname.equals("regex-field-modifier")) {
          if (this.modifier == null) {
            this.modifier = new FieldModifier();
          }
          if (name.equals("name")) {
            this.modifier.name = value;
          } else if (name.equals("field")) {
            this.modifier.field = value;
          } else if (name.equals("type")) {
            this.modifier.type = FieldType.getFieldType(value);
          } else if (name.equals("transform")) {
            if ((value != null) && !value.trim().equals("")) {
              this.modifier.transformString = value;
              this.modifier.scope = this.scope;
            } else {
              this.modifier.transformer = this.transformer;
            }
          } else if (name.equals("omit-norms")) {
            if (value.trim().toLowerCase().equals("false"))
              this.modifier.omitNorms = false;
            else
              this.modifier.omitNorms = true;
          } else if (name.equals("store")) {
            Field.Store mode = Field.Store.NO;
            value = value.toLowerCase();
            if (value.equals("yes"))
              mode = Field.Store.YES;
            else if (value.equals("no"))
              mode = Field.Store.NO;
            this.modifier.storeMode = mode;
          } else if (name.equals("index-mode")) {
            Field.Index mode = Field.Index.ANALYZED_NO_NORMS;
            value = value.toLowerCase();
            if (value.equals("analyzed"))
              mode = Field.Index.ANALYZED;
            else if (value.equals("analyzed_no_norms"))
              mode = Field.Index.ANALYZED_NO_NORMS;
            else if (value.equals("no"))
              mode = Field.Index.NO;
            else if (value.equals("not_analyzed"))
              mode = Field.Index.NOT_ANALYZED;
            else if (value.equals("not_analyzed_no_norms"))
              mode = Field.Index.NOT_ANALYZED_NO_NORMS;
            this.modifier.indexMode = mode;
          } else if (name.equals("term-vector")) {
            Field.TermVector termv = Field.TermVector.YES;
            value = value.toLowerCase();
            if (value.equals("no"))
              termv = Field.TermVector.NO;
            else if (value.equals("with_offsets"))
              termv = Field.TermVector.WITH_OFFSETS;
            else if (value.equals("with_positions"))
              termv = Field.TermVector.WITH_POSITIONS;
            else if (value.equals("with_positions_offsets"))
              termv = Field.TermVector.WITH_POSITIONS_OFFSETS;
            else if (value.equals("yes"))
              termv = Field.TermVector.YES;
            this.modifier.termv = termv;
          }

        } else if (parname.equals("langindex")) {
          if (this.index == null)
            this.index = new Neo4JTextIndexDefinition();
          if (name.equals("name")) {
            this.index.setName(value);
          } else if (name.equals("path")) {
            this.index.setPath(value);
          } else if (name.equals("analyzer")) {
            this.index.setAnalyzerName(value);
          } else if (name.equals("filter")) {
            this.index.setFilterString(value);
          } else if (name.equals("datasource")) {
            this.index.setDataSource(value);
          } else if (name.equals("index-mode")) {
            if (value != null) {
              value = value.toLowerCase();
              this.index.setAppend(value.equals("append") ? true : false);
            }
          } else if (name.equals("max-field-length")) {
            if (value != null)
              value = value.toLowerCase();
            this.index
                .setMaxFieldLength(value.equals("max_field_length") ? Integer.MAX_VALUE : Integer.parseInt(value));
          } else if (name.equals("max-buffered-docs")) {
            this.index.setMaxBufferedDocs(Integer.parseInt(value));
          } else if (name.equals("proximity")) {
            if (value != null) {
              this.index.setMergeFactor(Integer.parseInt(value));
            }
          } else if (name.equals("optimize")) {
            if (value != null) {
              value = value.toLowerCase();
              this.index.setOptimize(value.equals("true") ? true : false);
            }
          } else if (name.equals("on-duplicate-key")) {
            if (value != null) {
              value = value.toLowerCase();
              this.index.setDuplicateUpdate(value.equals("insert") ? false : true);
            }
          } else if (name.equals("id-field")) {
            this.index.setIdField(value);
          }
        } else if (parname.equals("index")) {
          if (this.index == null) {
            this.index = new LuceneIndexDefinition();
          }
          if (name.equals("name")) {
            this.index.setName(value);
          } else if (name.equals("path")) {
            this.index.setPath(value);
          } else if (name.equals("analyzer")) {
            this.index.setAnalyzerName(value);
          } else if (name.equals("filter")) {
            this.index.setFilterString(value);
          } else if (name.equals("datasource")) {
            this.index.setDataSource(value);
          } else if (name.equals("index-mode")) {
            if (value != null) {
              value = value.toLowerCase();
              this.index.setAppend(value.equals("append") ? true : false);
            }
          } else if (name.equals("max-field-length")) {
            if (value != null)
              value = value.toLowerCase();
            this.index
                .setMaxFieldLength(value.equals("max_field_length") ? Integer.MAX_VALUE : Integer.parseInt(value));
          } else if (name.equals("max-buffered-docs")) {
            this.index.setMaxBufferedDocs(Integer.parseInt(value));
          } else if (name.equals("use-compound-file")) {
            if (value != null)
              value = value.toLowerCase();
            this.index.setUseCompoundFile(value.equals("true") ? true : false);
          } else if (name.equals("merge-factor")) {
            if (value != null) {
              this.index.setMergeFactor(Integer.parseInt(value));
            }
          } else if (name.equals("optimize")) {
            if (value != null) {
              value = value.toLowerCase();
              this.index.setOptimize(value.equals("true") ? true : false);
            }
          } else if (name.equals("on-duplicate-key")) {
            if (value != null) {
              value = value.toLowerCase();
              this.index.setDuplicateUpdate(value.equals("insert") ? false : true);
            }
          } else if (name.equals("id-field")) {
            this.index.setIdField(value);
          }
        } else if (parname.equals("field-modifiers")) {
          if (name.equals("field-modifier")) {
            this.index.addModifierName(value);
          }
        }
      }
    } catch (Exception e) {
      errorLogger.log(Level.ERROR, e);
      e.printStackTrace();
    }
  }

  @Override
  public void visitBeforeChildren(ConfigurationNode node) {
    String name = node.getName();
    if (node.getParentNode() != null) {
      String parname = node.getParentNode().getName();
      try {
        if (parname.equals("analyzer")) {
          if (name.equals("field")) {
            this.readingField = true;
          } else
            this.readingField = false;
        }
      } catch (Exception e) {
        errorLogger.log(Level.ERROR, e);
      }
    }
  }

}
