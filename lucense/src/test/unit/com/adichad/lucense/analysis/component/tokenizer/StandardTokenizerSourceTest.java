/**
 * 
 */
package com.adichad.lucense.analysis.component.tokenizer;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

public class StandardTokenizerSourceTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.adichad.lucense.analysis.component.tokenizer.StandardTokenizerSource#getTokenStream(java.io.Reader)}
	 * .
	 */
	@Test
	public void testGetTokenStream() {
		StandardTokenizerSource standardTokenizerSource = new StandardTokenizerSource(
				Version.LUCENE_33);

		String file = "/tmp/data/a.txt";

		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e) {
			// e.printStackTrace();
		}

		TokenStream ts = null;

		ts = standardTokenizerSource.getTokenStream(reader);
		assertTrue(ts instanceof StandardTokenizer);
	}
}
