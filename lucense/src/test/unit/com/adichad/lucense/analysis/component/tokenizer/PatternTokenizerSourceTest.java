/**
 * 
 */
package com.adichad.lucense.analysis.component.tokenizer;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.analysis.tokenizer.PatternTokenizer;

public class PatternTokenizerSourceTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.adichad.lucense.analysis.component.tokenizer.PatternTokenizerSource#getTokenStream(java.io.Reader)}
	 * .
	 */
	@Test
	public void testGetTokenStream() {
		PatternTokenizerSource patternTokenizerSource = new PatternTokenizerSource(
				"");
		String file = "/tmp/data/a.txt";
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			// e1.printStackTrace();
		}

		TokenStream ts = null;
		try {
			ts = patternTokenizerSource.getTokenStream(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}

		assertTrue(ts instanceof PatternTokenizer);
	}
}
