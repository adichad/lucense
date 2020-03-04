/**
 * 
 */
package com.adichad.lucense.analysis.component.tokenizer;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class WhitespaceTokenizerSourceTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.adichad.lucense.analysis.component.tokenizer.WhitespaceTokenizerSource#getTokenStream(java.io.Reader)}
	 * .
	 */
	@Test
	public void testGetTokenStream() {
		WhitespaceTokenizerSource whitespaceTokenizerSource = new WhitespaceTokenizerSource(
				Version.LUCENE_33);

		String file = "/tmp/data/a.txt";
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}

		TokenStream ts = null;
		ts = whitespaceTokenizerSource.getTokenStream(reader);
		assertTrue(ts instanceof WhitespaceTokenizer);
	}

}
