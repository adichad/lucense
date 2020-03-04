/**
 * 
 */
package com.adichad.lucense.analysis.component.filter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource.CharNormalizerType;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;

public class ShingleFilterSourceTest {

	@Before
	public void setup() {
	}

	@Test
	public void testGetTokenStream() {

		String file = "/tmp/data/a.txt";
		TokenStreamSource tss = new CharTokenizerSource(Version.LUCENE_33,
				"somecharshere", CharNormalizerType.NONE);
		ShingleFilterSource pfs = null;

		// Creating file reader
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		TokenFilter tf = null;

		pfs = new ShingleFilterSource(2);
		try {
			tf = pfs.getTokenFilter(tss.getTokenStream(reader));
		} catch (Exception e) {
			// e.printStackTrace();
			fail("getTokenFilter fails for maxShingleSize = 2");
		}
		assertTrue(tf instanceof ShingleFilter);

		pfs = new ShingleFilterSource(1);
		try {
			tf = pfs.getTokenFilter(tss.getTokenStream(reader));
		} catch (Exception e) {
			// e.printStackTrace();
			fail("getTokenFilter fails for maxShingleSize = 1");

		}
		assertTrue(tf instanceof ShingleFilter);

		try {
			pfs = new ShingleFilterSource(0);
			fail("ShingleFilterSource constructor fails for maxShingleSize = 0");
		} catch (IllegalArgumentException e) {
		}

	}
}
