/**
 * 
 */
package com.adichad.lucense.analysis.component.tokenizer;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class TokenizingCharsetTokenizerSourceTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.adichad.lucense.analysis.component.tokenizer.TokenizingCharsetTokenizerSource#getTokenStream(java.io.Reader)}
	 * .
	 */
	@Test
	public void testGetTokenStream() {
		TokenizingCharsetTokenizerSource tokenizingCharsetTokenizerSource = new TokenizingCharsetTokenizerSource(
				Version.LUCENE_33, "");

		String file = "/tmp/data/a.txt";
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}

		TokenStream ts = null;
		ts = tokenizingCharsetTokenizerSource.getTokenStream(reader);
		assertTrue(ts instanceof CharTokenizer);
	}

}
