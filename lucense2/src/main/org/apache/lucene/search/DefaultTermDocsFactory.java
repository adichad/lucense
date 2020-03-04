package org.apache.lucene.search;

import java.util.Map;

import org.apache.lucene.index.TermDocs;

public class DefaultTermDocsFactory extends TermDocsFactory {

  public DefaultTermDocsFactory(Map<String, ?> params) {
    super(params);
  }

  @Override
  public TermDocs wrapTermDocs(TermDocs termDocs) {
    return termDocs;
  }

}