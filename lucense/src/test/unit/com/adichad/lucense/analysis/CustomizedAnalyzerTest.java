package com.adichad.lucense.analysis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.analysis.component.AnalyzerComponentFactory;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;

public class CustomizedAnalyzerTest {

	private TokenFilterSource tfs;
	private TokenStreamSource tss;
	private List<TokenFilterSource> filterSources = new ArrayList<TokenFilterSource>();
	private CustomizedAnalyzer ca1;
	private CustomizedAnalyzer ca2;

	@Before
	public void setup() {
		this.tfs = AnalyzerComponentFactory.getTokenFilterSource(
				Version.LUCENE_33, "stopword", "", true, "/tmp/data/a.txt", 0,
				0, 0, 0.5, 0.1, 5, true);
		this.tss = AnalyzerComponentFactory.getTokenStreamSource(
				Version.LUCENE_33, "whitespace", "");
		this.filterSources.add(this.tfs);

		this.ca1 = new CustomizedAnalyzer(this.tss, this.filterSources);
		this.ca2 = new CustomizedAnalyzer(null, null);
	}

	@Test
	public void testTokenStream() {
		TokenStream ts = ca1.tokenStream("a", null);
		assertNotNull(ts);

		ts = null;

		try {
			ts = ca2.tokenStream("a", null);
			assertNull(ts);
		} catch (Exception e) {
			assertNull(ts);
		}

	}
}
