/**
 * 
 */
package com.adichad.lucense.analysis.component.filter;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource.CharNormalizerType;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;

public class LowerCaseFilterSourceTest {

	@Before
	public void setup() {
	}

	@Test
	public void testGetTokenStream() {

		String file = "/tmp/data/a.txt";
		TokenStreamSource tss = new CharTokenizerSource(Version.LUCENE_33,
				"somecharshere", CharNormalizerType.NONE);
		LowerCaseFilterSource lcfs1 = null;
		lcfs1 = new LowerCaseFilterSource(Version.LUCENE_33);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		TokenFilter tf = null;
		try {
			tf = lcfs1.getTokenFilter(tss.getTokenStream(reader));
		} catch (Exception e) {
			// e.printStackTrace();
		}
		assertTrue(tf instanceof LowerCaseFilter);
	}
}
