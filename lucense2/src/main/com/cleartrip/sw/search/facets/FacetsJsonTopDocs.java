package com.cleartrip.sw.search.facets;

import java.util.List;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopFieldDocs;

public class FacetsJsonTopDocs {

	private TopFieldDocs tfd;
	
	public FacetsJsonTopDocs() {
			
	}
	
	public FacetsJsonTopDocs(TopFieldDocs tfd) {
		this.tfd = tfd;
	}

	public TopFieldDocs getFieldDocsList() {
		return tfd;
	}

}
