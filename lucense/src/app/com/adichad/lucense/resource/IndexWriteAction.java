/*
 * @(#)com.adichad.lucense.resource.IndexWriteAction.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Query;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.StringLucenseExpression;
import com.adichad.lucense.indexer.FieldFactory;
import com.adichad.lucense.indexer.FieldTransformer;
import com.adichad.lucense.indexer.target.IndexingTarget;

public abstract class IndexWriteAction {
  public abstract void execute(IndexingTarget it) throws Throwable;

  public static class DeleteDocumentsAction extends IndexWriteAction {
    private Query query;

    public DeleteDocumentsAction(Query query) {
      this.query = query;
    }

    @Override
    public void execute(IndexingTarget it) throws CorruptIndexException, IOException {
      it.deleteDocuments(query);
    }
  }

  public static class ReplaceDocumentsAction extends IndexWriteAction {
    private Context cx;

    private List<Map<String, String>> docs;

    public ReplaceDocumentsAction(Context cx, List<Map<String, String>> docs) {
      this.cx = cx;
      this.docs = docs;
    }

    public void execute(IndexingTarget it) throws Exception {
      
      StringLucenseExpression filter = it.getFilter(cx);
      FieldFactory ff = it.getFieldFactory();
      //ff.addContext(cx);
      Map<String, StringLucenseExpression> transformExpressions = ff.getTransformExpressions(cx, null);
      List<FieldTransformer> customTransforms = ff.getCustomTransforms(cx, null);
      Document doc = new Document();
      List<Field> cfields = ff.initConcreteFields(doc);
      for (Map<String, String> mdoc : docs) {
        ff.scrapeConcreteFields(cfields, mdoc);
        List<Field> sfields = ff.scrapeSoftFields(mdoc);
        for(Field field:sfields) {
          doc.add(field);
        }
        
        it.addDocument(doc, filter, transformExpressions, customTransforms, cx);
        for(Field field:sfields) {
          doc.removeFields(field.name());
        }
      }
    }
  }
}
