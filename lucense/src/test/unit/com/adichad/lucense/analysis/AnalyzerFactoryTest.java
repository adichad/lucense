package com.adichad.lucense.analysis;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.analysis.component.AnalyzerComponentFactory;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;

public class AnalyzerFactoryTest {

	private TokenFilterSource tfs;
	private TokenStreamSource tss;
	private List<TokenFilterSource> filterSources = new ArrayList<TokenFilterSource>();

	@Before
	public void setup() {
		new AnalyzerFactory();
		this.tfs = AnalyzerComponentFactory.getTokenFilterSource(
				Version.LUCENE_33, "stopword", "", true, "/tmp/data/a.txt", 0, 0, 0,
				0.5, 0.1, 5, true);
		this.tss = AnalyzerComponentFactory.getTokenStreamSource(
				Version.LUCENE_33, "whitespace", "");
	}

	@Test
	public void testCreateAnalyzer() {
		Analyzer al;
		this.filterSources.add(this.tfs);
		// assertTrue(tfs instanceof StopFilterSource);
		al = AnalyzerFactory.createAnalyzer(this.tss, this.filterSources);
		assertTrue(al instanceof CustomizedAnalyzer);
	}

	@Test
	public void testCreateNestedAnalyzer() {
		Analyzer al;
		Map<String, Analyzer> fieldAnalyzerMap = new HashMap<String, Analyzer>();
		al = AnalyzerFactory.createNestedAnalyzer(Version.LUCENE_33, this.tss,
				this.filterSources, fieldAnalyzerMap);
		assertTrue(al instanceof CustomizedAnalyzer);

		fieldAnalyzerMap.put("a", null);
		al = AnalyzerFactory.createNestedAnalyzer(Version.LUCENE_33, null,
				this.filterSources, fieldAnalyzerMap);
		assertTrue(al instanceof PerFieldAnalyzerWrapper);
	}
}
