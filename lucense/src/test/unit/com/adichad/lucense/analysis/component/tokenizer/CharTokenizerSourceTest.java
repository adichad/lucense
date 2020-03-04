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

import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource.CharNormalizerType;

public class CharTokenizerSourceTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource#getTokenStream(java.io.Reader)}
	 * .
	 */
	@Test
	public void testGetTokenStream() {
		CharTokenizerSource charTokenizerSource = new CharTokenizerSource(
				Version.LUCENE_33, "", CharNormalizerType.NONE);
		String file = "/tmp/data/a.txt";
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			// e1.printStackTrace();
		}

		TokenStream ts = charTokenizerSource.getTokenStream(reader);
		assertTrue(ts instanceof CharTokenizer);
	}

}
