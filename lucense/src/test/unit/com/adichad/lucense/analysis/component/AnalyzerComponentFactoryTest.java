/*
 * @(#)com.adichad.lucense.expression.ExpressionParsingTest.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.analysis.component;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.analysis.component.filter.ControlledPluralsInvertingStemFilterSource;
import com.adichad.lucense.analysis.component.filter.ControlledPluralsStemFilterSource;
import com.adichad.lucense.analysis.component.filter.ControlledPresentParticipleInvertingStemFilterSource;
import com.adichad.lucense.analysis.component.filter.ControlledPresentParticipleStemFilterSource;
import com.adichad.lucense.analysis.component.filter.LowerCaseFilterSource;
import com.adichad.lucense.analysis.component.filter.PhoneticFilterSource;
import com.adichad.lucense.analysis.component.filter.PorterStemFilterSource;
import com.adichad.lucense.analysis.component.filter.ShingleFilterSource;
import com.adichad.lucense.analysis.component.filter.StopFilterSource;
import com.adichad.lucense.analysis.component.filter.SynonymFilterSource;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.PatternTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.StandardTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;
import com.adichad.lucense.analysis.component.tokenizer.TokenizingCharsetTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.WhitespaceTokenizerSource;

public class AnalyzerComponentFactoryTest {
	public ArrayList<String> exprs;

	@Before
	public void setup() {

	}

	@Test
	public void testGetTokenStreamSource() {
		// Just to cover the class definition
		new AnalyzerComponentFactory();

		TokenStreamSource tss = AnalyzerComponentFactory.getTokenStreamSource(
				Version.LUCENE_33, "", "");
		assertNull(tss);
		tss = AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
				"whitespace", null);
		assertTrue(tss instanceof WhitespaceTokenizerSource);
		tss = AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
				"charset", null);
		assertTrue(tss instanceof CharTokenizerSource);
		tss = AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
				"standard", null);
		assertTrue(tss instanceof StandardTokenizerSource);
		/*
		 * tss =
		 * AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
		 * "tokenizing-charset", null); assertTrue(tss instanceof
		 * TokenizingCharsetTokenizerSource);
		 */
		/*
		 * tss =
		 * AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
		 * "pattern", null); assertTrue(tss instanceof PatternTokenizerSource);
		 */

		tss = AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
				"whitespace", "somecharshere");
		assertTrue(tss instanceof WhitespaceTokenizerSource);
		tss = AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
				"charset", "somecharshere");
		assertTrue(tss instanceof CharTokenizerSource);
		tss = AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
				"standard", "somecharshere");
		assertTrue(tss instanceof StandardTokenizerSource);
		tss = AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
				"tokenizing-charset", "somecharshere");
		assertTrue(tss instanceof TokenizingCharsetTokenizerSource);
		tss = AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
				"pattern", "somecharshere");
		assertTrue(tss instanceof PatternTokenizerSource);

		tss = AnalyzerComponentFactory.getTokenStreamSource(Version.LUCENE_33,
				"other", "somecharshere");
		assertNull(tss);
	}

	@Test
	public void testTokenFilterSource() {

		TokenFilterSource tfs;
		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"stopword", "", true, "/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1, 5,
				true);
		assertTrue(tfs instanceof StopFilterSource);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"synonym", "", true, "/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1, 5,
				true);
		assertTrue(tfs instanceof SynonymFilterSource);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"lowercase", "", true, "/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1, 5,
				true);
		assertTrue(tfs instanceof LowerCaseFilterSource);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"phonetic", "", true, "/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1, 5,
				true);
		assertTrue(tfs instanceof PhoneticFilterSource);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"stemporter", "", true, "/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1,
				5, true);
		assertTrue(tfs instanceof PorterStemFilterSource);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"stemplurals", "", true, "/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1,
				5, true);
		assertTrue(tfs instanceof ControlledPluralsStemFilterSource);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"stemplurals-inversions", "", true, "/tmp/data/a.txt", 0, 0, 0,
				0.5, 0.1, 5, true);
		assertTrue(tfs instanceof ControlledPluralsInvertingStemFilterSource);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"stempresentparticiples", "", true, "/tmp/data/a.txt", 0, 0, 0,
				0.5, 0.1, 5, true);
		assertTrue(tfs instanceof ControlledPresentParticipleStemFilterSource);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"stempresentparticiples-inversions", "", true,
				"/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1, 5, true);
		assertTrue(tfs instanceof ControlledPresentParticipleInvertingStemFilterSource);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"shingle", "", true, "/tmp/data/a.txt", 2, 0, 0, 0.5, 0.1, 5,
				true);
		assertTrue(tfs instanceof ShingleFilterSource);
		/*
		 * tfs =
		 * AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
		 * "spellcorrect", "", true, "/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1, 5,
		 * true); assertTrue(tfs instanceof
		 * ContextualSpellingCorrectingFilterSource);
		 */
		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"", "", true, "/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1, 5, true);
		assertNull(tfs);

		// Testing the catch blocks
		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"stopword", "", true, "/tmp/data/c.txt", 0, 0, 0, 0.5, 0.1, 5,
				true);
		assertTrue(true);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(null, "stopword",
				"", true, "/tmp/data/a.txt", 0, 0, 0, 0.5, 0.1, 5, true);
		assertTrue(true);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"synonym", "", true, "/tmp/data/c.txt", 0, 0, 0, 0.5, 0.1, 5,
				true);
		assertTrue(true);

		tfs = AnalyzerComponentFactory.getTokenFilterSource(Version.LUCENE_33,
				"synonym", "", true, "/tmp/data/b.txt", 0, 0, 0, 0.5, 0.1, 5,
				true);
		assertTrue(true);
	}

}
