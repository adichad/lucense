package com.adichad.lucense.request.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.IndexBoostCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.LCSLengthCollector;
import org.apache.lucene.search.NumwordsCollector;
import org.apache.lucene.search.TopFieldDocs;

import com.adichad.lucense.expression.ExpressionCollector;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public abstract class SearchResultRowFiller {

  public static class QLenFiller extends SearchResultRowFiller {
    private SearchResultRowFiller inner;

    private Object2IntOpenHashMap<String> qlenMap;

    private ArrayList<String> fields;

    public QLenFiller(ArrayList<String> fields, Object2IntOpenHashMap<String> qlenMap, SearchResultRowFiller inner) {
      this.inner = inner;
      this.qlenMap = qlenMap;
      this.fields = fields;
    }

    @Override
    public Document fill(Document doc, int i) throws IOException {
      if (doc == null)
        doc = new Document();
      for (String field : this.fields) {
        Integer qlen = this.qlenMap.get(field);
        doc.add(new Field("@qlen(" + field + ")", Integer.toString(qlen == null ? 0 : qlen), Field.Store.YES,
            Field.Index.NO));
      }
      if (this.inner != null)
        this.inner.fill(doc, i);
      return doc;
    }

  }

  public abstract Document fill(Document doc, int i) throws IOException;

  public static final class IndexBoostFiller extends SearchResultRowFiller {
    IndexBoostCollector nc;

    SearchResultRowFiller inner;

    TopFieldDocs tfd;

    public IndexBoostFiller(IndexBoostCollector nc, TopFieldDocs tfd, SearchResultRowFiller inner) {
      this.nc = nc;
      this.inner = inner;
      this.tfd = tfd;
    }

    @Override
    public Document fill(Document doc, int i) throws IOException {
      if (doc == null)
        doc = new Document();

      float indexBoost = this.nc.getIndexBoost(this.tfd.scoreDocs[i].doc);
      doc.add(new Field("@indexboost", Float.toString(indexBoost), Field.Store.YES, Field.Index.NO));
      if (this.inner != null)
        this.inner.fill(doc, i);
      return doc;
    }
  }

  public static final class NumwordsFiller extends SearchResultRowFiller {
    NumwordsCollector nc;

    SearchResultRowFiller inner;

    TopFieldDocs tfd;

    public NumwordsFiller(NumwordsCollector nc, TopFieldDocs tfd, SearchResultRowFiller inner) {
      this.nc = nc;
      this.inner = inner;
      this.tfd = tfd;
    }

    @Override
    public Document fill(Document doc, int i) throws IOException {
      if (doc == null)
        doc = new Document();

      Map<String, Integer> numwordsMap = this.nc.getNumwords(this.tfd.scoreDocs[i].doc);
      for (String field : numwordsMap.keySet()) {

        doc.add(new Field("@numwords(" + field + ")", Integer.toString(numwordsMap.get(field) == null ? 0 : numwordsMap
            .get(field)), Field.Store.YES, Field.Index.NO));
      }
      if (this.inner != null)
        this.inner.fill(doc, i);
      return doc;
    }
  }

  public static final class LCSLenFiller extends SearchResultRowFiller {
    LCSLengthCollector lcs;

    SearchResultRowFiller inner;

    TopFieldDocs tfd;

    public LCSLenFiller(LCSLengthCollector lcs, TopFieldDocs tfd, SearchResultRowFiller inner) {
      this.lcs = lcs;
      this.inner = inner;
      this.tfd = tfd;
    }

    @Override
    public Document fill(Document doc, int i) throws IOException {
      if (doc == null)
        doc = new Document();

      Map<String, Integer> lcsMap = this.lcs.getLCSLength(this.tfd.scoreDocs[i].doc);

      for (String field : lcsMap.keySet()) {
        Integer lcs = lcsMap.get(field);
        doc.add(new Field("@lcslen(" + field + ")", Integer.toString(lcs == null ? 0 : lcs), Field.Store.YES,
            Field.Index.NO));

      }
      if (this.inner != null)
        this.inner.fill(doc, i);
      return doc;
    }
  }

  public static final class ScoreFiller extends SearchResultRowFiller {
    TopFieldDocs tfd;

    SearchResultRowFiller inner;

    public ScoreFiller(TopFieldDocs tfd, SearchResultRowFiller inner) {
      this.tfd = tfd;
      this.inner = inner;
    }

    @Override
    public Document fill(Document doc, int i) throws IOException {
      if (doc == null)
        doc = new Document();

      doc.add(new Field("@score", Float.toString(this.tfd.scoreDocs[i].score), Field.Store.YES, Field.Index.NO));

      if (this.inner != null)
        this.inner.fill(doc, i);
      return doc;
    }
  }

  public static final class DocIDFiller extends SearchResultRowFiller {
    TopFieldDocs tfd;

    SearchResultRowFiller inner;

    public DocIDFiller(TopFieldDocs tfd, SearchResultRowFiller inner) {
      this.tfd = tfd;
      this.inner = inner;
    }

    @Override
    public Document fill(Document doc, int i) throws IOException {
      if (doc == null)
        doc = new Document();

      doc.add(new Field("@docid", Float.toString(this.tfd.scoreDocs[i].doc), Field.Store.YES, Field.Index.NO));

      if (this.inner != null)
        this.inner.fill(doc, i);
      return doc;
    }
  }

  public static final class ExpressionFiller extends SearchResultRowFiller {
    ExpressionCollector ec;

    String exprname;

    SearchResultRowFiller inner;

    private TopFieldDocs tfd;

    public ExpressionFiller(String exprname, ExpressionCollector ec, TopFieldDocs tfd, SearchResultRowFiller inner) {
      this.exprname = exprname;
      this.ec = ec;
      this.inner = inner;
      this.tfd = tfd;
    }

    @Override
    public Document fill(Document doc, int i) throws IOException {
      if (doc == null)
        doc = new Document();

      doc.add(new Field(this.exprname, this.ec.getVal(this.tfd.scoreDocs[i].doc).toString(), Field.Store.YES,
          Field.Index.NO));

      if (this.inner != null)
        this.inner.fill(doc, i);
      return doc;
    }
  }

  public static final class StoredFieldFiller extends SearchResultRowFiller {
    SearchResultRowFiller inner;

    IndexSearcher searcher;

    FieldSelector fieldSelector;

    private TopFieldDocs tfd;

    public class FilteringFieldSelector implements FieldSelector {
      private static final long serialVersionUID = 1L;

      private Set<String> fetchFields;

      public FilteringFieldSelector(Set<String> fetchFields) {
        this.fetchFields = fetchFields;
      }

      @Override
      public FieldSelectorResult accept(String fieldName) {
        if (this.fetchFields.contains(fieldName)) {
          return FieldSelectorResult.LOAD;
        }
        return FieldSelectorResult.NO_LOAD;
      }
    }

    public StoredFieldFiller(TopFieldDocs tfd, IndexSearcher searcher, Set<String> fetchFields,
        SearchResultRowFiller inner) {
      this.inner = inner;
      this.tfd = tfd;
      this.fieldSelector = new FilteringFieldSelector(fetchFields);
      this.searcher = searcher;
    }

    @Override
    public Document fill(Document doc, int i) throws IOException {
      if (doc == null)
        doc = new Document();

      Document sdoc = this.searcher.doc(this.tfd.scoreDocs[i].doc, this.fieldSelector);
      for (Fieldable sfield : sdoc.getFields()) {
        doc.add(sfield);
      }

      if (this.inner != null)
        this.inner.fill(doc, i);
      return doc;
    }
  }
}
