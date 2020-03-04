<?php
/**
 * @file lucenseapi.php
 * API for php applications to query lucense searchd
 */

# sort types
define("SORT_SQL",              0);
define("SORT_CMP",              1);

# order by
define("ORDER_DESC",            1);
define("ORDER_ASC",             0);

# facet order by
define("ORDER_COUNT_DESC",      0);
define("ORDER_VAL_ASC",         1);
define("ORDER_CUSTOM",          2);

# scorer types
define("SCORE_DEFAULT",         0);
define("SCORE_LCSFIELD",        1);
define("SCORE_BOOLFIELD",       2);
define("SCORE_EXPRESSION",      3);
define("SCORE_NONE",            4);

# query combinators
define("OP_AND",            "AND");
define("OP_OR",              "OR");

# query modifiers
define("PROXIMITY",           "~");
define("BOOST",               "^");
define("EXACT",              "\"");
define("FIELD",               ":");
define("EXCLUDE",           "NOT");
define("GROUPSTART",          "(");
define("GROUPEND",            ")");
define("RANGE_START",         "[");
define("RANGE_END",           "]");
define("RANGE_TO",           "TO");

# result types
define("ERRORRESULT",            255);
define("CONNERRORRESULT",        254);
define("PINGRESULT",               0);
define("STATUSRESULT",             1);
define("SEARCHRESULT",             2);
define("REGISTERWORKERRESULT",     3);
define("LOADINDEXES",              4);
define("UNLOADINDEXES",            5);
define("GCRESULT",                 6);
define("SPELLINGCORRECTIONRESULT", 7);
define("STEMINVERSIONRESULT",      8);
define("OPENINDEXWRITER",          9);
define("REPLACEDOCUMENTS",        10);
define("CLOSEINDEXWRITER",        11);
define("DELETEDOCUMENTS",         12);
define("ANALYZEQUERY",            13);
define("TERMSTATS",               14);
define("COMMANDADDTOAUX",         15);
define("COMMANDDELETEFROMAUX",    16);
define("COMMANDOPTIMIZEAUX",      17);
define("RELOADCONF",              18);
define("COMMANDUPDATEAUX_CELLWISE", 19);

# error types
define("TOOMANYREQUESTS",       1);
define("NOVALIDINDEXFOUND",     2);
define("UNKNOWNREQUESTTYPE",    3);
define("UNKNOWNMORPHOLOGY",     4);
define("UNKNOWNEXCEPTION",     -1);
define("CONNECTIONERROR",      -2);
define("OK",                    0);

# expression field types
define("STRING",                0);
define("INT",                   1);
define("DOUBLE",                2);
define("FLOAT",                 3);
define("BOOLEAN",               4);

# api communication protocol version
define("VERSION",        1);

# Default Lucene Match Version
define("DEFAULT_LUCENE_VERSION", "LUCENE_33");


/**
 * PayloadFilterParamEncoder extend to encode dynamic parameters
 * payload based document and term-position filter implementations.
 * See com.adichad.lucense.payload for more details
 */

interface PayloadFilterParamEncoder {
  public function encode($params);
}

class DefaultPayloadFilterParamEncoder implements PayloadFilterParamEncoder {
  public function encode($params) {
    return "";
  }
}

class SingleByteConjunctiveMaskingPayloadFilterParamEncoder implements PayloadFilterParamEncoder {
  const EXCLUDE_STOPWORDS = 4;
  const EXCLUDE_PARTICIPLE_STEMS = 2;
  const EXCLUDE_PLURAL_STEMS = 1;

  public function encode($params) {
    assert(is_int($params));
    assert($params<256);
    assert($params>=0);//1 byte
    return pack("C",$params);
  }
}

/**
 * GroupParamEncoder: interface to be implemented by custom grouping requests
 */

interface GroupParamEncoder {
  public function encode($grouper);
}

class GenericDoAllGroupParamEncoder implements GroupParamEncoder {
  public function encode($params) {
    assert(is_array($params));
    $req = pack("N", count($params));
    foreach($params as $gname=>$grouper) {
      $req .= pack("N", strlen($gname)).$gname;
      $req .= pack("N", count($grouper["groupfields"]));
      foreach ($grouper["groupfields"] as $groupfield=>$type) {
        $req .= pack("N",strlen($groupfield)).$groupfield;
        $req .= pack("C",$type);
      }
      $req .= pack("N", count($grouper["sortfields"]));
      foreach ($grouper["sortfields"] as $sortfield=>$arr) {
        $order = $arr['order'];
        $type = $arr['type'];
        $req .= pack("N",strlen($sortfield)).$sortfield;
        $req .= pack("C", $type);
        $req .= pack("C", $order);
      }
      $req .= pack("N",$grouper["groupoffset"]);
      $req .= pack("N",$grouper["grouplimit"]);
      $req .= pack("N",strlen($grouper["where"])).$grouper["where"];
      $req .= pack("N",strlen($grouper["having"])).$grouper["having"];

      $req .= pack("N", count($grouper["select"]));
      foreach ($grouper["select"] as $selectfield=>$arr) {
        $val = $arr["val"];
        $type = $arr["type"];
        $req .= pack("N",strlen($selectfield)).$selectfield;
        $req .= pack("N",strlen($val)).$val;
        $req .= pack("C", $type);
      }


      $req .= $this->encode($grouper["subgroupers"]);
      $req .= pack("C", $grouper["getresults"]);
    }
    return $req;
  }
}


/**
 *
 * LucenseClient: create objects of this class to set and fire Queries against
 * running com.adichad.lucense.searchd.SearchServerStarter instances
 *
 */
class LucenseClient {

  protected $error;
  protected $errortype;
  protected $connerror;

  protected $host;
  protected $port;
  protected $sockTimeout;

  protected $query;
  protected $indexes;
  protected $analyzername;
  protected $comment;

  protected $newGroupers;
  protected $faceters;
  protected $highlightedStoredFields;
  protected $StoredFieldHighlighting;
  protected $fieldweights;
  protected $sortmode;
  protected $sortby;
  protected $selectFields;
  protected $appname;
  protected $offset;
  protected $limit;
  protected $maxmatches;
  protected $timeout;
  protected $scorer;
  protected $expscore;
  protected $searchResults;
  protected $corrections;
  protected $steminversions;
  protected $filters;
  protected $auxfilters;
  protected $defaultFields;
  protected $fieldTypes;
  protected $mozillaScope;
  protected $getHighlightables;
  protected $highlightQuery;
  protected $readerBoosts;
  protected $namedExprs;
  protected $fieldWiseHighlightableWords;
  protected $fieldWiseHighlightablePhrases;
  protected $payloadFilterCriteria;
  protected $payloadFilterParamEncoder;
  protected $luceneVersion;
  protected $_serverInfo;
  protected $_retries;

  public function __construct () {
    $this->error = "";
    $this->errortype = OK;
    $this->connerror = false;

    $this->host = "";
    $this->port = 0;
    $this->sockTimeout = 35;
    $this->query = "";
    $this->indexes = array("*");
    $this->analyzername = "default";
    $this->comment = "";
    $this->newGroupers = array();
    $this->faceters = array();
    $this->fieldweights = array();
    $this->sortmode = SORT_SQL;
    $this->sortby = array("@score"=>ORDER_DESC);
    $this->selectFields = array("@docid"=>INT,"@score"=>FLOAT);
    $this->appname = "DumbApp";
    $this->offset = 0;
    $this->limit = 20;
    $this->maxmatches = 1000;
    $this->timeout = 30000;
    $this->scorer = SCORE_DEFAULT;
    $this->expscore = "";
    $this->searchResults = array();
    $this->corrections = array();
    $this->steminversions = array();
    $this->filters = array();
    $this->auxfilters = array();
    $this->defaultFields = array();
    $this->fieldTypes = array();
    $this->mozillaScope = "";
    $this->getHighlightables = false;
    $this->highlightQuery = "";
    $this->readerBoosts = array();
    $this->namedExprs = array();
    $this->highlightedStoredFields = array();
    $this->StoredFieldHighlighting = false;
    $this->fieldWiseHighlightableWords = array();
    $this->fieldWiseHighlightablePhrases = array();
    $this->payloadFilterCriteria = null;
    $this->payloadFilterParamEncoder = new DefaultPayloadFilterParamEncoder();
    $this->luceneVersion = DEFAULT_LUCENE_VERSION;
    $this->_serverInfo = array() ;
    $this->_retries = 1;
  }

  /*
   * SetServer: specify which searchd instance to connect to
   * @param $host searchd ip
   * @param $port searchd port
   */
  public function SetServer ( $host="localhost", $port=6000 ) {
    assert ( is_string ($host) );
    assert ( strlen ($host) > 0 );
    assert ( is_int ($port) );
    assert ( $port > 0 );

    $this->host = $host;
    $this->port = $port;
  }

  public function SetServers ( $arrServerInfo,$retries=1) {

    assert ( is_array ($arrServerInfo) );
    $len = count($arrServerInfo) ;
    for($i=0; $i<$len; $i++)
    {
      $server  = $arrServerInfo[$i] ;
      assert(is_array($server)) ;
      assert(is_string($server['host']));
      assert(is_int($server['port']));
    }
    assert(is_int($retries)) ;
    $this->_serverInfo =  $arrServerInfo ;
    $this->_retries    = $retries ;
  }

  /*
   * SetLuceneVersion: set the match Version to be used by analyzers and query parser
   */
  public function SetLuceneVersion($version = DEFAULT_LUCENE_VERSION) {
    assert(is_string($version) && strlen($version)>0);
    $this->luceneVersion = $version;
  }

  /*
   * SetAppName: specify application name to be logged with query
   */
  public function SetAppName ( $appname ) {
    assert ( is_string ($appname));
    assert ( strlen($appname)>0 );
    $this->appname = $appname;
  }

  /*
   * SetExpressionScore: specify the named, config-defined scope to use for any javascript evaluations
   */
  public function SetExpressionScope ( $mozillaScope ) {
    assert ( is_string ($mozillaScope) );
    assert ( strlen($mozillaScope) > 0);
    $this->mozillaScope = $mozillaScope;
  }

  public function SetNamedExpression ( $name, $expr, $type ) {
    assert( is_string($name) && strlen($name>0));
    assert( is_string($expr) && strlen($expr>0));
    assert ($type == STRING || $type == INT || $type == DOUBLE || $type == FLOAT || $type == BOOLEAN);
    $this->namedExprs[$name] = array($expr, $type);
  }

  /*
   * SetLimits: specify mysql-like offset and limit constraints on the returned records
   */
  public function SetLimits ($offset, $limit, $maxmatches=1000) {
    assert ( is_int ($offset) );
    assert ( $offset>=0 );
    assert ( is_int ($limit) );
    assert ( $limit>=0 );
    assert ( is_int ($maxmatches) );
    assert ( $maxmatches>=0 );
    $this->offset = $offset;
    $this->limit = $limit;
    $this->maxmatches = $maxmatches;
  }

  /*
   * SetTimeout: set the time (millisecs) after which a request will time out and flow will return to the application
   */
  public function SetTimeout ( $timeout ) {
    assert(is_long($timeout));
    assert($timeout>=0);
    $this->timeout = $timeout;
    $this->socketTimeout = 5+($timeout/1000);
  }

  /*
   * SetSelect: specify the stored fields/expressions that will be returned for each matched record
   */
  public function SetSelect ( $selectFields ) {
    assert (is_array ($selectFields));
    foreach($selectFields as $field=>$type) {
      assert (is_string($field) && strlen($field)>0);
      assert ($type == STRING || $type == INT || $type == DOUBLE || $type == FLOAT || $type == BOOLEAN);
    }
    $this->selectFields = $selectFields;
  }

  /*
   * SetFieldTypes: specify default field types to be used in javascript expression evaluations
   */
  public function SetFieldTypes ( $types ) {
    assert (is_array ($types));
    foreach( $types as $type=>$fields ) {
      assert ($type == STRING || $type == INT || $type == DOUBLE || $type == FLOAT || $type == BOOLEAN);
      assert (is_array ($fields));
      foreach($fields as $field) {
        assert (is_string($field));
      }
    }
    $this->fieldTypes = $types;
  }

  /*
   * SetIndexBoosts: specify the value of the @indexboost variable for records from a particular lucene index
   */
  public function SetIndexBoosts ( $boostArray ) {
    assert (is_array ($boostArray));
    foreach($boostArray as $dir=>$boost) {
      assert (is_string($dir) && strlen($dir)>0);
      assert (is_float($boost));
    }
    $this->readerBoosts = $boostArray;
  }

  /*
   * SetSort: specify the comparator chain to be used for sorting matched records
   */
  public function SetSort ( $sortmode, $sortby ) {
    assert (($sortmode==SORT_CMP && is_string($sortby))
      ||($sortmode==SORT_SQL && is_array($sortby)));
    if($sortmode==SORT_SQL) {
      foreach($sortby as $field=>$arr) {
        $order = $arr['order'];
        $type = $arr['type'];
        assert(is_string($field) && ($order == ORDER_DESC|| $order == ORDER_ASC));
        assert($type==STRING || $type==DOUBLE || $type==FLOAT || $type==INT);
      }
    }
    $this->sortmode = $sortmode;
    $this->sortby = $sortby;
  }


  /*
   * SetFieldWeights: specify field-wise weights used in LCSField scoring
   */
  public function SetFieldWeights ( $fieldweights ) {
    assert (is_array($fieldweights));
    foreach ($fieldweights as $field=>$weight) {
      assert(is_string($field) && strlen($field)>0);
      assert(is_int($weight) && $weight > 0);
    }
    $this->fieldweights = $fieldweights;
  }

  /**
   * AddNewGrouper
   * @deprecated use AddGrouper instead
   * */
  public function AddNewGrouper( $className, $encoder, $groupArray = array()) {
    $this->AddGrouper( $className, $encoder, $groupArray);
  }

  /* AddGrouper: Extensible Group By functionality over search results
   * @param string            className  named handle for the decoding class as specified
   *                                     in search.xml
   * @param GroupParamEncoder encoder    encoder that defines the format in which
   *                                     params are sent over the socket
   * @param array             groupArray array of parameter values specifying the
   *                                     the grouping function
   *
   * */
  public function AddGrouper( $className, $encoder, $groupArray = array()) {
    assert (is_string($className));
    assert ($encoder instanceof GroupParamEncoder);
    assert (is_array($groupArray));

    $this->newGroupers[] = array($className, $encoder, $groupArray);
  }

  private function EncodeNewGroupers($groupers) {
    $req = pack("N", count($groupers));
    foreach ($groupers as $grouper) {
      $req .= pack("N", strlen($grouper[0])).$grouper[0];
      $req .= $grouper[1]->encode($grouper[2]);
    }
    return $req;
  }

  public function ResetGroupers () {
    unset ($this->newGroupers);
    $this->newGroupers = array();
  }

  /*
   * AddFaceter: specify bobo-browse faceting criteria
   */
  public function AddFaceter ( $fname, $sortby=ORDER_COUNT_DESC, $facetmincount=0, $facetlimit=10 ) {
    assert (is_string($fname));
    assert ($sortby == ORDER_COUNT_DESC || $sortby == ORDER_VAL_ASC || $sortby == ORDER_CUSTOM);
    assert (is_int($facetmincount) && $facetmincount>=0);
    assert (is_int($facetlimit) && $facetlimit>0);

    $this->faceters[$fname] = array();
    $this->faceters[$fname]["sortby"] = $sortby;
    $this->faceters[$fname]["facetmincount"] = $facetmincount;
    $this->faceters[$fname]["facetlimit"] = $facetlimit;
  }

  public function ResetFaceters () {
    unset ($this->faceters);
    $this->faceters = array();
  }

  /*
   * SetScorer: set the scoring mode to calculate @score, @numwords, @lcslen variables
   */
  public function SetScorer ($scorer, $expscore="") {
    assert ($scorer==SCORE_DEFAULT
      ||$scorer==SCORE_LCSFIELD
      ||$scorer==SCORE_BOOLFIELD
      ||$scorer==SCORE_EXPRESSION
      ||$scorer==SCORE_NONE);
    assert ($scorer!=SCORE_EXPRESSION||(is_string($expscore)&&strlen($expscore)>0));
    $this->scorer = $scorer;
    $this->expscore = $expscore;
  }

  public function SetDefaultFields($fields) {
    assert(is_array($fields));
    foreach($fields as $field) {
      assert(is_string($field) && strlen($field)>0);
    }
    $this->defaultFields = $fields;
  }

  /*
   * SetFilterExpression: add a boolean expression over index fields/score vars to filter records on
   */
  public function SetFilterExpression($expression, $exclude=false) {
    assert(is_string($expression) && strlen($expression)>0);
    $this->filters[$expression] = $exclude;
  }

  /*
   * SetFilterAuxillary: define a filter based on the bit corresponding to the provided $rowid - $docfieldvalue combination
   */
  public function SetFilterAuxillary($indexname, $rowid, $exclude=false) {
    assert(is_string($indexname));
    assert(is_string($rowid));
    $this->auxfilters[$indexname] = array($rowid, $exclude);
  }

  /**
   * BuildQueryFilter: builds a query part that will filter only those documents that contain (at least one / all)
   *                   of the provided values in the provided field. query filters can be applied as excluding
   *                   or including.
   * @param string $field name of the field to search in
   * @param array $values the distinct values that should match in the provided field for a document to
   *                                  qualify as a match with respect to this query subpart.
   * @param constant $valuecombinator OP_OR implies that a document qualifies as a match for this
   *                                  filter if it contains at least one of the specified values in the
   *                                  specified field.
   *                                  OP_AND implies that a document qualifies as a match for this
   *                                  filter if it contains all of the specified values in the
   *                                  specified field.
   * @param boolean $exclude whether to exclude documents that match this filter or to include them in the
   *                          resultset.
   */
  public function BuildQueryFilter($field, $values, $valuecombinator = OP_OR, $exclude=false ) {
    assert(is_array($values));
    assert(is_string($field) && strlen($field)>0);

    $query = "";
    foreach ($values as $value) {
      $query = $this->SafeAppend($query, $value, $valuecombinator);
    }
    $query = $field.FIELD.GROUPSTART.$query.GROUPEND;
    return $query;
  }

  /**
   * BuildQueryRange: builds a query part that will filter only those documents that contain a value within
   *                  the specified min and max range, both limits inclusive in the provided field. query filters
   *                  can be applied as excluding or including.
   * @param string $field name of the field to search in
   * @param string $min the lexically minimum value that a document must contain in the specified field to match
   *                    this filter
   * @param string $max the lexically maximum value that a document must contain in the specified field to match
   *                    this filter
   * @param boolean $exclude whether to exclude documents that match this filter or to include them in the
   *                         resultset.
   */
  public function BuildQueryRange($field, $min, $max, $exclude=false) {
    assert(is_string($field) && strlen($field)>0);
    $subquery = $field.FIELD.RANGE_START.$min." ".RANGE_TO." ".$max.RANGE_END;
    if($exclude) $subquery = EXCLUDE.GROUPSTART.$subquery.GROUPEND;
    return $subquery;
  }

  /**
   * BuildQuery: builds a query part that will filter only those documents that match the query criteria
   * @param string $query a keyword query to be interpreted with respect to the modifiers specified
   * @param array $fields array of string field-names in which the specified keyword query is to be searched
   * @param array $modifiers associative array that contains one or more of the following entries:-
   *                         constant EXACT=>"": specifies that the query is to be cleaned up and interpreted as
   *                         an exact match criteria
   *                         constant PROXIMITY=><integer proximity value>: specifies that the query is to be
   *                         cleaned up and interpreted as an approximate match subject to a maximum error of the
   *                         <integer proximity value specified>
   *                         constant BOOST=><float boost factor>: specifies that documents matching the query part
   *                         have their score boosted by the specified float boost factor
   * @param boolean $exclude whether to exclude documents that match this filter or to include them in the
   *                         resultset.
   */
  public function BuildQuery($query, $fields, $modifiers=array(), $exclude = false) {
    if(in_array(EXACT,array_keys($modifiers))) {
      $query = str_replace(EXACT," ",$query);
      $query = EXACT.$query.EXACT;
    }
    if(in_array(PROXIMITY, array_keys($modifiers))) {
      $query = str_replace(EXACT," ",$query);
      $query = EXACT.$query.EXACT.PROXIMITY.$modifiers[PROXIMITY];
    }
    if(in_array(BOOST, array_keys($modifiers))) {
      $query = GROUPSTART.$query.GROUPEND.BOOST.$modifiers[BOOST];
    }
    if($exclude) {
      $query = EXCLUDE." ".GROUPSTART.$query.GROUPEND;
    }
    if(count($fields)>0) {
      $fieldquery = $this->AddFields($query,$fields);
      if(strlen($fieldquery)>0) {
        $query = $fieldquery;
      }
    }

    return $query;
  }

  /**
   * BuildQueryBoolean: builds a query part that will filter only those documents that match the boolean query criteria\
   * @param constant $combinator OP_AND/OP_OR: whether to combine the array of queries as a conjunction or a disjunction
   * @param array $queries an array of subqueries to be interpreted with respect to the modifiers specified
   * @param array $fields array of string field-names in which the specified keyword queries are to be searched
   *                      NOTE: this array should be non-empty ONLY if NONE of the subqueries have field(s) specified.
   * @param float $boost specifies that documents matching the query part
   *                     have their score boosted by the specified float boost factor
   * @param boolean $exclude whether to exclude documents that match this filter or to include them in the
   *                         resultset.
   */
  public function BuildQueryBoolean($combinator, $queries, $boost = 0, $fields = array(),  $exclude = false) {
    assert(is_array($queries));
    assert(is_numeric($boost));
    $query = "";
    foreach($queries as $subquery) {
      assert(is_string($subquery) && strlen($subquery)>0);
      $query = $this->SafeAppend($query, GROUPSTART.$subquery.GROUPEND, $combinator);
    }
    if($boost > 0) {
      $query = GROUPSTART.$query.GROUPEND.BOOST.$boost;
    }
    if($exclude) {
      $query = EXCLUDE." ".GROUPSTART.$query.GROUPEND;
    }

    if(count($fields)>0) {
      $fieldquery = $this->AddFields($query,$fields);
      if(strlen($fieldquery)>0) {
        $query = $fieldquery;
      }
    }
    return $query;
  }

  public function FlushQuery() {
    $this->query = "";
  }

  public function GetLastResult() {

  }

  /*
   * BuildSearch: return a serialized binary string representing the complete search request
   */
  public function BuildSearch($query, $indexes=array("*"), $comment="") {
    assert(isset($query));
    assert(is_string($query));
    assert(is_array($indexes));
    assert(is_string($comment));

    $this->query = $query;
    if($this->StoredFieldHighlighting) {
      $this->fieldWiseHighlightableWords = $this->getQueryWords($query);
      $this->fieldWiseHighlightablePhrases = $this->getQueryPhrases($query);
    }
    $this->indexes = $indexes;
    $this->comment = $comment;

    $req = pack("N", VERSION);
    $req .= pack("C", 2 );
    $req .= pack("N", 0 );
    if(VERSION > 0) {
      $req .= pack("NNN", $this->offset, $this->limit, $this->maxmatches);
      $req .= pack("N", $this->timeout );
      $req .= pack("N", strlen($this->luceneVersion)).$this->luceneVersion;
      $req .= pack("N", strlen($this->query)).$this->query;
      $req .= pack("N", count($this->indexes));
      foreach ($this->indexes as $index) {
        $req .= pack ( "N", strlen($index)).$index;
      }
      $req .= pack("N", strlen($this->analyzername)).$this->analyzername;
      $req .= $this->payloadFilterParamEncoder->encode($this->payloadFilterCriteria);

      $req .= pack("N", strlen($this->mozillaScope)).$this->mozillaScope;
      $req .= pack("N", count($this->selectFields));
      foreach ($this->selectFields as $field=>$type) {
        $req .= pack("N", strlen($field)).$field;
        $req .= pack("C",$type);
      }

      $req .= pack("N", count($this->fieldweights));
      foreach($this->fieldweights as $field=>$weight) {
        $req .= pack("N", strlen($field)).$field;
        $req .= pack("N", $weight);
      }
      $req .= pack("N", count($this->defaultFields));
      foreach($this->defaultFields as $field) {
        $req .= pack("N", strlen($field)).$field;
      }
      $req .= pack("C", $this->scorer);
      $req .= pack("N", strlen($this->expscore)).$this->expscore;
      $req .= pack("N", count($this->fieldTypes));
      foreach($this->fieldTypes as $type=>$fields) {
        $req .= pack("CN", $type, count($fields));
        foreach($fields as $field) {
          $req .= pack("N", strlen($field)).$field;
        }
      }

      $req .= pack("N", count($this->namedExprs));
      foreach($this->namedExprs as $name=>$valArr) {
        $req .= pack("N", strlen($name)).$name;
        $req .= pack("N", strlen($valArr[0])).$valArr[0];
        $req .= pack("C", $valArr[1]);
      }

      $req .= pack("N", count($this->readerBoosts));
      foreach($this->readerBoosts as $dir=>$boost) {
        $req .= pack("N", strlen($dir)).$dir;
        $req .= pack("N", strlen($boost)).$boost;
      }

      $req .= pack("N", count($this->filters));
      foreach($this->filters as $expression=>$exclude) {
        $req .= pack("N", strlen($expression)).$expression;
        $req .= pack("C", $exclude?1:0);
      }

      $req .= pack("N", count($this->auxfilters));
      foreach($this->auxfilters as $indexname=>$arr) {
        $req .= pack("N", strlen($indexname)).$indexname;
        $rowid = $arr[0];
        $exclude = $arr[1];
        $req .= pack("N", strlen($rowid)).$rowid;
        $req .= pack("C", $exclude?1:0);
      }

      $req .= pack("C", $this->sortmode);
      if($this->sortmode == SORT_SQL) {
        $req .= pack("N", count($this->sortby));
        foreach($this->sortby as $sortfield=>$arr) {
          $order = $arr['order'];
          $type = $arr['type'];
          $req .= pack("N", strlen($sortfield)).$sortfield;
          $req .= pack("C", $type);
          $req .= pack("C", $order);
        }
      }
      else {
        $req .= pack("N", strlen($this->sortby)).$this->sortby;
      }

      $req .= $this->EncodeNewGroupers($this->newGroupers);

      $req .= pack("N", count($this->faceters));
      foreach($this->faceters as $fname=>$fspec) {
        $req .= pack("C",$fspec["sortby"]);
        $req .= pack("N",$fspec["facetmincount"]);
        $req .= pack("N",$fspec["facetlimit"]);
        $req .= pack("N", strlen($fname)).$fname;
      }

      if($this->getHighlightables) {
        $req .= pack("C", 1);
        $req .= pack("N", strlen($this->highlightQuery)).$this->highlightQuery;
      } else {
        $req .= pack("C", 0);
      }

      $req .= pack("N", strlen($this->appname)).$this->appname;
      $req .= pack("N", strlen($this->comment)).$this->comment;
    }
    return $req;
  }

  /*
   * set the analyzer that will be used to process queries henceforth. valid names are either "default" or
   * one of those specified in search.xml for the schema whose indexes are to be searched
   * */
  public function SetAnalyzer($analyzername, $payfilencoder=null, $payfilparams=null) {
    assert(is_string($analyzername));
    if($payfilencoder==null)
      $payfilencoder=new DefaultPayloadFilterParamEncoder();
    assert($payfilencoder instanceof PayloadFilterParamEncoder);

    $this->analyzername = $analyzername;
    $this->payloadFilterParamEncoder = $payfilencoder;
    $this->payloadFilterCriteria = $payfilparams;
  }

  /*
   * Search: execute a search request based on currently set criteria and return the results
   */
  public function Search ($query, $indexes=array("*"), $comment="") {
    $req = $this->BuildSearch($query, $indexes, $comment);

    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ( $fp );
  }

  /*
   * Search: execute the search request represented by the passed binary string
   */
  public function SearchBinary ($req) {
    assert(isset($req));
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ( $fp );
  }

  private function &DecodeGroupings(&$container, $fp) {
    list ($groupCount) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
    for($i=0;$i<$groupCount;$i++) {
      list($len) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
      $gname = $this->ReadBytes ($fp, $len);
      list($totalcount) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
      $container[$gname]["totalcount"] = $totalcount;

      $container[$gname] = array();
      $container[$gname]["schema"] = array();
      list($fieldcount) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
      for($j=0;$j<$fieldcount;$j++) {
        list($len) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
        $container[$gname]["schema"][] = $this->ReadBytes($fp, $len);
      }
      $container[$gname]["results"] = array();
      list($keyCount) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
      for($j=0;$j<$keyCount;$j++) {
        list($keyElemCount) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
        $key = "";
        for($k=0;$k<$keyElemCount;$k++) {
          list($len) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
          $keyElem = $this->ReadBytes($fp, $len);
          $key .= "$keyElem";
          if($k<$keyElemCount-1) {
            $key .= ",";
          }
        }
        list($valElemCount) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
        $valElems = array();
        for($k=0;$k<$valElemCount;$k++) {
          list($len) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
          $valElems[$container[$gname]["schema"][$k]] = $this->ReadBytes($fp, $len);
        }

        $container[$gname]["results"][$key] = $valElems;
      }
      $container[$gname]["groupings"] = array();
      unset($container[$gname]["schema"]);
      $this->DecodeGroupings($container[$gname]["groupings"], $fp);
    }
    return $container;
  }

  /*
   * EnableHighlighting: ask for an array of distinct highlightable terms (based on the analyzed query string) to be returned
   * @Deprecated
   */
  public function EnableHighlighting($query = "") {
    assert(is_string($query));
    $this->getHighlightables = true;
    $this->highlightQuery = $query;
  }

  /*
   * EnableInversionRetrieval: ask for an array of distinct inversions terms (based on the analyzed query string) to be returned
   */
  public function EnableInversionRetrieval($query = "") {
    assert(is_string($query));
    $this->getHighlightables = true;
    $this->highlightQuery = $query;
  }

  /*
   *
   */
  public function EnableStoredFieldHighlightingOn($field, $hlexpr) {
    assert(is_string($field));
    assert(is_string($hlexpr));

    if(preg_match("/@queryphrase/",$hlexpr))
      $this->highlightedStoredFields[$field]['phrase'][] = $hlexpr;
    else if(preg_match("/@queryword/",$hlexpr))
      $this->highlightedStoredFields[$field]['word'][] = $hlexpr;
    $this->StoredFieldHighlighting = true;
  }

  private function getQueryPhrases($query) {
    $split1arr = preg_split("/\s*:\s*/",$query);

    $keys = array();
    $vals = array();
    foreach($split1arr as $key=>$val) {
      $matches = array();
      if(0 == $key) {
        preg_match("/\s*(?:NOT)?\s*(\w+)\s*/", $val, $matches);
        $keys[] = $matches[1];
      } else if($key < count($split1arr)-1) {
        preg_match("/(.*)\s+(\w+)$/", $val, $matches);
        $keys[] = $matches[2];
      } else {
        preg_match("/(.*)/", $val, $matches);
      }
      if($key>0) {
        $temparr = preg_split("/\"|'/",$matches[1]);
        $arr = array();
        for($i=0;$i<count($temparr);$i++) {
          if($i%2==0) { //not a phrase
            if(""!=trim($temparr[$i])) {
              $ta = preg_split("/\s+/",trim(preg_replace("/\"|'|\b(?:AND|OR|NOT|TO)\b|(?:\^|~)[\d.]+|\(|\)|\[|\]/"," ",$temparr[$i])),PREG_SPLIT_NO_EMPTY );
              foreach($ta as $w) {
                $w = trim($w);
                if(""!=$w) {
                  $arr[] = $w;
                }
              }
            }
          } else { //phrase
            $var = trim(preg_replace("/\s+|\s+(?:AND|OR|NOT|TO)\s+|(?:\^|~)[\d.]+|\(|\)|\[|\]/","\\s+",trim($temparr[$i])));
            if(""!=$var) {
              $arr[] = $var;
            }
          }
        }
        $vals[$key-1] = $arr;
        #$vals[$key-1] = preg_split("/\s+/",trim(preg_replace("/\"|'|\s+(?:AND|OR|NOT|TO)\s+|(?:\^|~)[\d.]+|\(|\)|\[|\]/"," ",$matches[1])));
      }
    }
    $final = array();
    for($i = 0;$i<count($keys);$i++) {
      if(!isset($final[$keys[$i]])) {
        $final[$keys[$i]] = $vals[$i];
      } else {
        $final[$keys[$i]] = array_merge($final[$keys[$i]], $vals[$i]);
      }
    }
    return $final;
  }

  private function getQueryWords($query) {
    $split1arr = preg_split("/\s*:\s*/",$query);

    $keys = array();
    $vals = array();
    foreach($split1arr as $key=>$val) {
      $matches = array();
      if(0 == $key) {
        preg_match("/\s*(?:NOT)?\s*(\w+)\s*/", $val, $matches);
        $keys[] = $matches[1];
      } else if($key < count($split1arr)-1) {
        preg_match("/(.*)\s+(\w+)$/", $val, $matches);
        $keys[] = $matches[2];
      } else {
        preg_match("/(.*)/", $val, $matches);
      }
      if($key>0) {
        $vals[$key-1] = preg_split("/\s+/",trim(preg_replace("/\"|'|\b(?:AND|OR|NOT|TO)\b|(?:\^|~)[\d.]+|\(|\)|\[|\]/"," ",$matches[1])));
      }
    }
    $final = array();
    for($i = 0;$i<count($keys);$i++) {
      if(!isset($final[$keys[$i]])) {
        $final[$keys[$i]] = $vals[$i];
      } else {
        $final[$keys[$i]] = array_merge($final[$keys[$i]], $vals[$i]);
      }
    }
    return $final;
  }

  private function HighlightField($field, &$str) {
    if(isset($this->highlightedStoredFields[$field]) && isset($this->highlightedStoredFields[$field]['phrase'])) {
      $exprs = $this->highlightedStoredFields[$field]['phrase'];
      if($exprs && count($exprs)>0) {
        if(isset($this->fieldWiseHighlightablePhrases[$field]) && count($this->fieldWiseHighlightablePhrases[$field])>0) {
          $qwords = $this->fieldWiseHighlightablePhrases[$field]; // = array("aditya chadha", " ", "");
          $mexpr = "/\b(".implode("|",$qwords).")\b/i";
          foreach($exprs as $expr) {
            $str = preg_replace($mexpr, preg_replace("/@queryphrase/i", "\\\${1}", $expr), $str);
          }
        }
      }
    }

    if(isset($this->highlightedStoredFields[$field]) && isset($this->highlightedStoredFields[$field]['word'])) {
      $exprs = $this->highlightedStoredFields[$field]['word'];
      if($exprs && count($exprs)>0) {
        if(isset($this->fieldWiseHighlightableWords[$field]) && count($this->fieldWiseHighlightableWords[$field])>0) {
          $qwords = $this->fieldWiseHighlightableWords[$field]; // = array("aditya", "chadha", "");
          $mexpr = "/\b(".implode("|",$qwords).")\b/i";
          foreach($exprs as $expr) {
            $str = preg_replace($mexpr, preg_replace("/@queryword/i", "\\\${1}", $expr), $str);
          }
        }
      }
    }
    return $str;
  }

  /* TermStats: retrieve term statistics from the specified indexes
   *
   */
  public function TermStats($indexes, $offset = 0, $limit = 100) {
    assert(is_array($indexes));
    assert(count($indexes)>0);
    assert(is_int($offset));
    assert(is_int($limit));

    $req  = pack("N", VERSION);
    $req .= pack("C", TERMSTATS );
    $req .= pack("N", 0 );

    $len = count($indexes);
    $req .= pack("N", $len);
    foreach($indexes as $index) {
      assert(strlen($index)>0);
      $req.=pack("N", strlen($index)).$index;
    }
    $req.= pack("N", $offset);
    $req.= pack("N", $limit);
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ( $fp );
  }

  /*
   * AnalyzeQuery: analyze a query by the configured analyzer and return back field-wise lists of
   * tokens/terms returned by the analyzer
   */
  public function AnalyzeQuery($query, $indexes=array("*")) {
    assert(is_string($query) && strlen($query)>0);
    assert(is_array($indexes) && count($indexes)>0);
    $req  = pack("N", VERSION);
    $req .= pack("C", ANALYZEQUERY );
    $req .= pack("N", 0 );
    $req .= pack("N", strlen($query)).$query;
    $req .= pack("N", count($indexes));
    foreach($indexes as $index) {
      assert(is_string($index) && strlen($index)>0);
      $req .= pack("N",strlen($index)).$index;
    }
    $req .= pack("N", strlen($this->analyzername)).$this->analyzername;
    $req .= pack("N", strlen($this->luceneVersion)).$this->luceneVersion;
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ( $fp );
  }

  /*
   * LoadIndexes: reload indexes
   */
  public function LoadIndexes($indexesToLoad, $ram=false) {
    assert(is_array($indexesToLoad));
    assert(is_bool($ram));
    $req  = pack("N", VERSION);
    $req .= pack("C", 4 );
    $req .= pack("N", 0 );
    $req .= pack("N", count($indexesToLoad));
    foreach($indexesToLoad as $indexName=>$indexPath) {
      $req .= pack("N", strlen($indexName)).$indexName;
      $req .= pack("N", strlen($indexPath)).$indexPath;
    }
    $req .= pack("C", $ram?1:0);
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ( $fp );
  }

  public function UnloadIndexes($indexesToUnload){
    assert(is_array($indexesToUnload));
    $req  = pack("N", VERSION);
    $req .= pack("C", 4 );
    $req .= pack("N", 0 );
    $req .= pack("N", count($indexesToUnload));
    foreach($indexesToUnload as $indexName=>$indexPath) {
      $req .= pack("N", strlen($indexName)).$indexName;
      $req .= pack("N", strlen($indexPath)).$indexPath;
    }
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ( $fp );
  }



  /*
   * OpenIndexWriter: open an index for writing. replaceDocuments call should
   * succeed if this is called, succeeds and CloseIndexWriter has not been called
   */
  public function OpenIndexWriter($indexname) {
    assert(is_string($indexname));
    assert(strlen($indexname)>0);
    $req = pack("N", VERSION);
    $req .= pack("C", OPENINDEXWRITER);
    $req .= pack("N", 0);
    $req .= pack("N",strlen($indexname)).$indexname;
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ($fp, $req, strlen($req));
    }
    return $this->GetResponse ($fp);
  }

  /*
   * CloseIndexWriter: close an index writer. replaceDocuments will fail if this call is in progress
   * from any thread. If this call has succeeded, reloading an index for reading (LoadIndexes) will
   * reflect the changes to the index at least till this point.
   */
  public function CloseIndexWriter($indexname, $optimize = false) {
    assert(is_string($indexname));
    assert(strlen($indexname)>0);
    assert(is_bool($optimize));
    $req = pack("N", VERSION);
    $req .= pack("C", CLOSEINDEXWRITER);
    $req .= pack("N", 0);
    $req .= pack("N",strlen($indexname)).$indexname;
    $req .= pack("C", $optimize?1:0);
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ($fp, $req, strlen($req));
    }
    return $this->GetResponse ($fp);
  }


  /*
   * ReplaceDocuments: write documents concurrently to an index for which a writer configuration has been specified
   * and permissions have been granted
   */

  public function ReplaceDocuments($indexname, $docs, $commit=false) {
    assert(is_string($indexname));
    assert(strlen($indexname)>0);
    assert(is_array($docs));
    assert(count($docs)>0);
    assert(is_bool($commit));
    $req = pack("N", VERSION);
    $req .= pack("C", REPLACEDOCUMENTS);
    $req .= pack("N", 0);
    $req .= pack("N", strlen($indexname)).$indexname;
    $req .= pack("N", count($docs));
    foreach($docs as $doc) {
      $req .= pack("N", count($doc));
      foreach($doc as $field=>$value) {
        $req .= pack("N", strlen($field)).$field;
        $req .= pack("N", strlen($value)).$value;
      }
    }
    $req .= pack("C", $commit?1:0);
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ($fp, $req, strlen($req));
    }
    return $this->GetResponse ($fp);
  }

  /*
   * ReplaceDocuments: delete documents concurrently from an index for which a writer configuration has been specified
   * and permissions have been granted
   */

  public function DeleteDocuments($indexname, $query, $commit=false) {
    assert(is_string($indexname));
    assert(strlen($indexname)>0);
    assert(is_string($query));
    assert(strlen($query)>0);

    $req = pack("N", VERSION);
    $req .= pack("C", DELETEDOCUMENTS);
    $req .= pack("N", 0);
    $req .= pack("N", strlen($indexname)).$indexname;
    $req .= pack("N", strlen($query)).$query;
    $req .= pack("N", strlen($this->luceneVersion)).$this->luceneVersion;
    $req .= pack("C", $commit?1:0);
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ($fp, $req, strlen($req));
    }
    return $this->GetResponse ($fp);
  }

  /*
   * Ping: should return a PONG if daemon is responsive, permission is granted and network link is up and
   * SetServer was called with correct parameters.
   */
  public function Ping () {
    $req = pack("N", VERSION);
    $req .= pack ( "C", 0 );
    $req .= pack ( "N", 0 );
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ( $fp );
  }

  public function Status () {
    $req = pack("N", VERSION);
    $req .= pack ( "C", 1 );
    $req .= pack ( "N", 0 );
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ( $fp );
  }

  public function RegisterWorker ($port) {
    assert(is_int($port));
    assert($port > 0);

    $req = pack( "N", VERSION );
    $req .= pack ( "C", 3 );
    $req .= pack ( "N", 0 );
    $req .= pack( "N",$port );
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    $res = $this->GetResponse ($fp);
    if($res == 1) {
      return "REGISTERED";
    }
    else {
      return "NOTREGISTERED";
    }
  }

  /*
   * CollectGarbage: calls System.gc()
   */
  public function CollectGarbage () {
    $req = pack( "N", VERSION );
    $req .= pack( "C", 6 );
    $req .= pack( "N", 0 );
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }

  /*
   * CorrectSpellings: use the specified corpus index to correct retrieve spelling corrections for the query
   */
  public function CorrectSpellings ( $indexName, $query ) {
    assert (is_string($indexName));
    assert (is_string($query));

    $req = pack( "N", VERSION );
    $req .= pack( "C", 7 );
    $req .= pack( "N", 0 );
    $req .= pack( "N", strlen($indexName)).$indexName;
    $req .= pack( "N", strlen($query)).$query;
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }

  public function InvertStems ( $indexName, $query ) {
    assert (is_string($indexName));
    assert (is_string($query));

    $req = pack( "N", VERSION );
    $req .= pack( "C", 8 );
    $req .= pack( "N", 0 );
    $req .= pack( "N", strlen($indexName)).$indexName;
    $req .= pack( "N", strlen($query)).$query;
    $req .= pack("N", strlen($this->luceneVersion)).$this->luceneVersion;
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }


  public function AddStringsToAuxIndex($row,$arrCells,$indexName,$boolByPassDictionary = false)
  {
    assert (is_string($row));
    assert(is_array($arrCells)) ;
    $req = pack( "N", VERSION );
    $req .= pack( "C", COMMANDADDTOAUX );
    $req .= pack( "N", 0 );
    $req .= pack( "C", $boolByPassDictionary?1:0); ///Passing String cell Ids
    $req .= pack( "N", strlen($indexName) ).$indexName;
    $req .= pack( "N", strlen($row) ).$row;
    $req .= pack( "N", count($arrCells));
    foreach($arrCells as $cell)
    {
      $req .= pack( "N", strlen($cell) ).$cell;
    }
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }


  public function AddIntsToAuxIndex($row,$arrCells,$indexName,$boolByPassDictionary = false)
  {
    assert (is_string($row));
    assert(is_array($arrCells)) ;
    $req = pack( "N", VERSION );
    $req .= pack( "C", COMMANDADDTOAUX );
    $req .= pack( "N", 0 );
    $req .= pack( "C", $boolByPassDictionary?1:0); ///Passing String cell Ids
    $req .= pack( "N", strlen($indexName) ).$indexName;
    $req .= pack( "N", strlen($row) ).$row;
    $req .= pack( "N", count($arrCells));
    foreach($arrCells as $cell)
    {
      $req .= pack( "N",$cell );
    }
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }

  public function CellWiseUpdateIntsToAux($arrCells,$indexName)
  {
    assert(is_array($arrCells)) ;

    foreach($arrCells as $cellId => $arrRows) {
      assert(is_int($cellId)) ;
      assert(is_array($arrRows)) ;
      assert(isset($arrRows['add']) ? is_array($arrRows['add']):true) ;
      assert(isset($arrRows['delete']) ? is_array($arrRows['delete']) : true) ;
    }
    $req = pack( "N", VERSION );
    $req .= pack( "C", COMMANDUPDATEAUX_CELLWISE );
    $req .= pack( "N", 0 );
    $req .= pack( "N", strlen($indexName) ).$indexName;
    $req .= pack( "N", count($arrCells));
    foreach($arrCells as $cellId => $arrRows) {
      $req .= pack( "N",$cellId );
      if(is_array($arrRows['add']))
      {
        $count = count($arrRows['add']) ;
        $req .= pack( "N", $count);
        foreach($arrRows['add'] as $row)
        {
          $req .= pack( "N", strlen($row) ).$row;
        }
      }
      else
        $req .= pack( "N",0 );

      if(is_array($arrRows['delete']))
      {
        $count = count($arrRows['delete']) ;
        $req .= pack( "N", $count);
        foreach($arrRows['delete'] as $row)
        {
          $req .= pack( "N", strlen($row) ).$row;
        }
      }
      else
        $req .= pack( "N",0 );
    }
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }

  public function DeleteStringsFromAuxIndex($row,$arrCells,$indexName,$boolByPassDictionary)
  {
    assert (is_string($row));
    assert(is_array($arrCells)) ;
    $req = pack( "N", VERSION );
    $req .= pack( "C", COMMANDDELETEFROMAUX );
    $req .= pack( "N", 0 );
    $req .= pack( "C", $boolByPassDictionary?1:0); ///Passing String cell Ids
    $req .= pack( "N", strlen($indexName) ).$indexName;
    $req .= pack( "N", strlen($row) ).$row;
    $req .= pack( "N", count($arrCells));
    foreach($arrCells as $cell)
    {
      $req .= pack( "N", strlen($cell) ).$cell;
    }
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }

  public function DeleteIntsFromAuxIndex($row,$arrCells,$indexName,$boolByPassDictionary) {
    assert (is_string($row));
    assert(is_array($arrCells)) ;
    $req = pack( "N", VERSION );
    $req .= pack( "C", COMMANDDELETEFROMAUX );
    $req .= pack( "N", 0 );
    $req .= pack( "C", $boolByPassDictionary?1:0); ///Passing String cell Ids
    $req .= pack( "N", strlen($indexName) ).$indexName;
    $req .= pack( "N", strlen($row) ).$row;
    $req .= pack( "N", count($arrCells));
    foreach($arrCells as $cell)
    {
      $req .= pack( "N", $cell );
    }
    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }

  public function OptimizeAuxIndex($row,$indexName,$fullOptimize=false) {
    assert(is_string($row)) ;
    $req = pack( "N", VERSION );
    $req .= pack( "C", COMMANDOPTIMIZEAUX );
    $req .= pack( "N", 0 );
    $req .= pack( "N", strlen($indexName) ).$indexName;
    $req .= pack( "N", strlen($row) ).$row;
    $req .= pack( "C", $fullOptimize?1:0);

    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }

  public function ReloadConfig() {
    $req = pack( "N", VERSION);
    $req .= pack("C",RELOADCONF);
    $req .= pack( "N", 0 );

    $fp = $this->Connect ();
    if(!is_null($fp)) {
      $this->Send ( $fp, $req, strlen ($req) );
    }
    return $this->GetResponse ($fp);
  }

  protected function AddFields($subquery, $fields) {
    assert(is_array($fields));
    $fieldquery = "";
    foreach($fields as $field) {
      if(is_string($field) && strlen($field)>0) {
        if(strlen($fieldquery)>0) {
          $fieldquery .= " ".OP_OR." ";
        }
        $fieldquery .= $field.FIELD.GROUPSTART.$subquery.GROUPEND;
      }
    }
    if(strlen($fieldquery)) {
      return $fieldquery;
    } else {
      return $subquery;
    }
  }

  protected function SafeAppend($query, $subquery, $combinator) {
    if(strlen($query)>0) {
      $query = $query." ".$combinator." ";
    }
    $query.=$subquery;
    return $query;
  }

  protected function Connect () {
    $errno=0;
    $errstr="";

    $fp = @fsockopen ( $this->host, $this->port, $errno, $errstr, 5 );
    if($errno != 0) {
      $this->errortype = CONNECTIONERROR;
      $this->error = $errstr;
      $this->connerror = true;
      return null;
    }
    return $fp;
  }

  protected function Send ( $handle, $data, $length ) {
    if ( is_null($handle) || feof($handle) || fwrite ( $handle, $data, $length ) !== $length )
    {
      $this->errortype = CONNECTIONERROR;
      $this->error = 'connection unexpectedly closed (timed out?)';
      $this->connerror = true;
      return false;
    }
    return true;
  }

  protected function ReadBytes ( &$fp, $count ) {
    $res = "";
    $remcount = $count;
    while ( !is_null($fp) && $remcount > 0 && !feof($fp) ) {
      $read = fread ($fp, $remcount);
      $info = stream_get_meta_data($fp);
      if ($info['timed_out']) {         //Did fread time-out?
        fclose($fp);
        $this->errortype = CONNECTIONERROR;
        $this->error = 'connection timed out on read';
        $this->connerror = true;
        $fp = null;
        break;
      }
      $res .= $read;
      $remcount -= strlen($read);
    } ;

    return $res;
  }

  protected function GetResponse ( $fp ) {
    if(!is_null($fp)) {
      stream_set_blocking($fp, 1);
      stream_set_timeout($fp,$this->sockTimeout);

      $var = $this->ReadBytes ( $fp, 1 );
      if($var != "") {
        list ($res) = array_values(unpack("C", $var));
        list ($id) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
      }
      else {
        $res = CONNERRORRESULT;
      }

      if ($res === PINGRESULT) { //ping
        list ($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
        $response = $this->ReadBytes ($fp, $len);
      }
      else if ($res === STATUSRESULT) { //status
        list ($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
        $response = $this->ReadBytes ($fp, $len);
      }
      else if ($res === SEARCHRESULT) { //search
        list ($displayCount) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
        $this->searchResults["totalcount"]=$displayCount;

        $this->searchResults["results"] = array();
        list ($resCount) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
        for($i=0;$i<$resCount;$i++) {
          $this->searchResults["results"][$i] = array();
          list($fieldCount) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
          for($j=0;$j<$fieldCount;$j++) {
            list($len) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
            $fieldName = $this->ReadBytes ($fp, $len);
            list($len) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
            $fieldValue = $this->ReadBytes ($fp, $len);
            $this->searchResults["results"][$i][$fieldName] = $this->StoredFieldHighlighting?$this->HighlightField($fieldName, $fieldValue):$fieldValue;
          }
        }

        $this->searchResults["groupings"] = array();
        $this->DecodeGroupings($this->searchResults["groupings"], $fp);

        list ($highlightPluStems) = array_values(unpack("C", $this->ReadBytes($fp, 1)));
        if($highlightPluStems) {
          list($count) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
          $this->steminversions = array();
          for($i=0; $i<$count; $i++) {
            list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
            $field = $this->ReadBytes($fp, $len);
            list($invsize) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
            $this->steminversions[$field] = array();
            for($j=0;$j<$invsize;$j++) {
              list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
              $orig = $this->ReadBytes($fp, $len);
              list($invcount) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
              $inv = array();
              for($k=0;$k<$invcount;$k++) {
                list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
                $inv[] = $this->ReadBytes($fp, $len);
              }
              $this->steminversions[$field][$orig] = $inv;
            }
          }
          $this->searchResults["highlight"] = $this->steminversions;
        }
        $response = $this->searchResults;
      } else if ($res === REGISTERWORKERRESULT) { //register
        list ($response) = array_values(unpack("C" ,$this->ReadBytes ($fp, 1) ) );
      } else if ($res === LOADINDEXES) { //load indexes
        list ($response) = array_values(unpack("C", $this->ReadBytes ($fp, 1) ) );
      } else if ($res === UNLOADINDEXES) {
        list($this->errortype) =  array_values(unpack("C" ,$this->ReadBytes ($fp, 1) ) );
        list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
        $this->error = $this->ReadBytes ($fp, $len);
        $response = array("errortype"=>$this->errortype, "message"=>$this->error);
      } else if($res === ERRORRESULT) { //error
        list($this->errortype) =  array_values(unpack("C" ,$this->ReadBytes ($fp, 1) ) );
        list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
        $this->error = $this->ReadBytes ($fp, $len);
        $response = array("errortype"=>$this->errortype, "message"=>$this->error);
      } else if($res === CONNERRORRESULT) {
        $response = array("errortype"=>$this->errortype, "message"=>$this->error);
      } else if($res === GCRESULT) {
        $response = "GCSUCCESS";
      } else if($res === SPELLINGCORRECTIONRESULT) {
        list($count) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
        $this->corrections = array();
        for($i=0; $i<$count; $i++) {
          list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
          $srcw = $this->ReadBytes($fp, $len);
          list($pos) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
          $this->corrections[$pos] = array("orig"=>$srcw, "corr"=>array());
          list($corrcount) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
          for($j=0;$j<$corrcount;$j++) {
            list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
            $corr = $this->ReadBytes($fp, $len);
            list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4)));
            $this->corrections[$pos]["corr"][$corr] = $this->ReadBytes($fp, $len);
          }

        }
        ksort($this->corrections, true);
        $response = $this->corrections;
      } else if($res === STEMINVERSIONRESULT) {
        list($count) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
        $this->steminversions = array();
        for($i=0; $i<$count; $i++) {
          list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
          $field = $this->ReadBytes($fp, $len);
          list($invsize) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
          $this->steminversions[$field] = array();
          for($j=0;$j<$invsize;$j++) {
            list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
            $orig = $this->ReadBytes($fp, $len);
            list($invcount) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
            $inv = array();
            for($k=0;$k<$invcount;$k++) {
              list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
              $inv[] = $this->ReadBytes($fp, $len);
            }
            $this->steminversions[$field][$orig] = $inv;
          }
        }
        $response = $this->steminversions;
      } else if($res === OPENINDEXWRITER) {
        $response = "OPENWRITERSUCCESS";
      } else if($res === CLOSEINDEXWRITER) {
        $response = "CLOSEWRITERSUCCESS";
      } else if($res === REPLACEDOCUMENTS) {
        $response = "REPLACEDOCSSUCCESS";
      } else if($res === DELETEDOCUMENTS) {
        $response = "DELETEDOCSSUCCESS";
      } else if($res === COMMANDADDTOAUX){
        list($status) = array_values(unpack("C", $this->ReadBytes ($fp, 1) ) );
        $response = $status?"ADDTOAUXSUCCESS":
          array("errortype"=>COMMANDADDTOAUX, "message"=>"ADDTOAUXFAIL");
      } else if($res === COMMANDDELETEFROMAUX) {
        list($status) = array_values(unpack("C", $this->ReadBytes ($fp, 1) ) );
        $response = $status?"DELETEFROMAUXSUCCESS":
          array("errortype"=>COMMANDDELETEFROMAUX, "message"=>"DELETEFROMAUXFAIL");
      } else if($res === COMMANDOPTIMIZEAUX){
        list($status) = array_values(unpack("C", $this->ReadBytes ($fp, 1) ) );
        $response = $status?"OPTIMIZEAUXSUCCESS":
          array("errortype"=>COMMANDOPTIMIZEAUX, "message"=>"OPTIMIZEAUXFAIL");
      } else if($res === COMMANDUPDATEAUX_CELLWISE){
        list($count) = array_values(unpack("C", $this->ReadBytes ($fp, 1) ) );
        if($count) {
          $failCells = array() ;
          for($i=0; $i < $count ; $i++) {
            list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
            $failCells[] = $this->ReadBytes($fp, $len);
          }
          $response = array("errortype"=>COMMANDUPDATEAUX_CELLWISE , "failIds"=>$failCells) ;
        }
        else
          $response = "UPDATEAUX_CELLWISE_SUCCESS" ;

      } else if($res === ANALYZEQUERY) {
        list($count) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
        $response = array();
        for($i=0; $i<$count; $i++) {
          list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
          $field = $this->ReadBytes($fp, $len);
          list($termcount) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
          $response[$field] = array();
          for($j=0;$j<$termcount;$j++) {
            list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
            $term = $this->ReadBytes($fp, $len);
            $response[$field][] = $term;
          }
        }
      } else if($res === TERMSTATS) {
        list($count) = array_values(unpack("N", $this->ReadBytes($fp, 4)));
        $response = array();
        for($i=0; $i<$count; $i++) {
          list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
          $field = $this->ReadBytes($fp, $len);
          $response[$field] = array();

          list($c) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
          for($j = 0; $j < $c; $j++) {
            list($len) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
            $text = $this->ReadBytes($fp, $len);
            list($dfreq) = array_values(unpack("N", $this->ReadBytes ($fp, 4) ) );
            $response[$field][$text] = $dfreq;
          }
        }
      } else if($res === RELOADCONF) {
        $response = "RELOADCONFSUCCESS";
      }
      if(!is_null($fp)) {
        fclose($fp);
      }
      return $response;
    }
    else return array("errortype"=>$this->errortype, "message"=>$this->error);
  }
}

