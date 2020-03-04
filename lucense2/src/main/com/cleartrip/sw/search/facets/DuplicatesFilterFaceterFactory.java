package com.cleartrip.sw.search.facets;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.TopFieldDocs;

public class DuplicatesFilterFaceterFactory extends SearchFaceterFactory {
	private final String field;
	private final List<String> stringsToDuplicate;
	private Term term;

	public DuplicatesFilterFaceterFactory(Map<String, ?> params, Properties env) {
		super(params, env);
		this.field = (String) params.get("field");
		this.stringsToDuplicate = (List<String>) params
				.get("stringsToDuplicate");
		this.term = new Term(field);

	}

	private static class DuplicatesFilterFaceter extends SearchFaceter {

		// private int docBase;
		private final String field;
		private String[] vals;
		private Object2IntOpenHashMap<String> facets;
		private Object2IntOpenHashMap<String> firstDocRefInDupSet;
		private HashMap<String, String> nameMatchRefSet;
		private final FieldSelector fieldSelector;
		private final List<String> stringsToDuplicate;
		private TopFieldCollector tfc;
		private IndexReader indexReader;
		private int docBase;

		public DuplicatesFilterFaceter(final String field, Collector next,
				final List<String> stringsToDuplicate) {
			super(next);
			this.field = field;
			this.stringsToDuplicate = stringsToDuplicate;
			facets = new Object2IntOpenHashMap<>();
			firstDocRefInDupSet = new Object2IntOpenHashMap<>();
			nameMatchRefSet = new HashMap<>();
			fieldSelector = new FieldSelector() {

				@Override
				public FieldSelectorResult accept(String fieldName) {
					if (fieldName.equals(field))
						return FieldSelectorResult.LOAD;
					return FieldSelectorResult.LOAD;
				}

			};
		}

		@Override
		public SearchFaceter setTFC(TopFieldCollector tfc, IndexReader reader, int docid)
				throws CorruptIndexException, IOException {
			this.tfc = tfc;
			if (docid != -1) {
//				System.out.println("Name Doc ID : " + docid);
				this.indexReader = reader;
				String docField = indexReader.document(docid, fieldSelector)
						.get(field).toLowerCase();
				String docSwID = indexReader.document(docid, fieldSelector)
						.get("_id");
				
				if (stringsToDuplicate.contains(docField)){
					if (!firstDocRefInDupSet.containsKey(docField)) {
						firstDocRefInDupSet.put(docField, docid);
						nameMatchRefSet.put(docField, docSwID);
						facets.add(docField, 1);
					}
				}
	
			}
			return this;
		}

		@Override
		public void collect(int doc) throws IOException {
			boolean found = false;
			String docField = indexReader.document(doc, fieldSelector)
					.get(field).toLowerCase();			
			if (stringsToDuplicate.contains(docField)) {
//				System.out.println("Doc Field:" + docField);
				if (!firstDocRefInDupSet.containsKey(docField)) {
//					System.out.println("First doc ref : " + swId );
					firstDocRefInDupSet.put(docField, doc + docBase);
					found = true;
				}
				facets.add(docField, 1);
			} else
				found = true;
			
			if (next != null && found){
				next.collect(doc);
			}
		}

		@Override
		public void setNextReader(IndexReader reader, int docBase)
				throws IOException {
			// this.docBase = docBase;
			// this.vals = FieldCache.DEFAULT.getStrings(reader, this.field);
			this.indexReader = reader;
			this.docBase = docBase;
			super.setNextReader(reader, docBase);
		}

		@Override
		public String getFacetJson(IndexSearcher searcher, FacetsJsonTopDocs facetsJsonTopDocs)
				throws CorruptIndexException, IOException {

			StringBuilder sb = new StringBuilder();

			if (facetsJsonTopDocs.getFieldDocsList() != null) {
				ScoreDoc[] scoreDocs = facetsJsonTopDocs.getFieldDocsList().scoreDocs;
				List<Integer> tfcDocIds = new ArrayList<>();
//				System.out.println("Limit:" + limit +" offset:" + offset + " scoreDocsLength:" + scoreDocs.length);
				for(int i=0; i< scoreDocs.length; i++) {
					tfcDocIds.add(scoreDocs[i].doc);
				}
				
				sb.append("{ ");
				for (String val : facets.keySet()) {
					if (nameMatchRefSet.containsKey(val)) {
						sb.append("\"")
						.append(nameMatchRefSet.get(val))
						.append("\": ")
						.append("{")
						.append("\"name\":\"")
						.append(val).append("\",")
						.append("\"count\":")
						.append(facets.getInt(val)).append("}").append(",");

					}
					else if (tfcDocIds.contains(firstDocRefInDupSet.getInt(val))) {
						sb.append("\"")
						.append(searcher.doc(firstDocRefInDupSet.getInt(val))
								.get("_id"))
						.append("\": ")
						.append("{")
						.append("\"name\":\"")
						.append(val).append("\",")
						.append("\"count\":")
						.append(facets.getInt(val)).append("}").append(",");
					}
				}
				sb.deleteCharAt(sb.length() - 1).append("}");
			}
			else {
				sb.append("{}");
			}
			return sb.toString();

		}
	}

	@Override
	public SearchFaceter createFaceter(Collector c) {
		return new DuplicatesFilterFaceter(field, c, stringsToDuplicate);
	}

	@Override
	public Query createFilter(String[] vals) {
		BooleanQuery q = new BooleanQuery();
		for (String val : vals)
			q.add(new TermQuery(this.term.createTerm(val)), Occur.SHOULD);
		return q;
	}
}
