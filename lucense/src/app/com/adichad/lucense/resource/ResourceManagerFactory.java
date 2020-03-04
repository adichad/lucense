package com.adichad.lucense.resource;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.configuration.ConfigurationException;
import org.mozilla.javascript.Context;

import com.adichad.lucense.exception.NoValidIndexesFoundException;

public class ResourceManagerFactory {
  public static SearchResourceManager createSearchResourceManager(String configPath, int portNumber,
      StringWriter prehistoricLog, Context cx) throws Exception {
    SearchConfigParser parser = new SearchConfigParser(portNumber, prehistoricLog, cx);

    return parser.parse(configPath);
  }
  
  public static SearchResourceManager createMergedSearchResourceManager(SearchResourceManager old, Context cx) throws Exception {
    SearchConfigParser parser = new SearchConfigParser(old.getPortNumber(), new StringWriter(), cx);
    SearchResourceManager newRes = parser.parse(old);
    
    return newRes;
  }

  public static IndexerResourceManager createIndexerResourceManager(String configPath, StringWriter prehistoricLog,
      Context cx) throws ConfigurationException {
    IndexerConfigParser parser = new IndexerConfigParser(prehistoricLog, cx);

    return parser.parse(configPath);
  }

}
