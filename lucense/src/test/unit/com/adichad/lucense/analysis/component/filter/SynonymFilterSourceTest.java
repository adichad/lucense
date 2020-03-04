/**
 * 
 */
package com.adichad.lucense.analysis.component.filter;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource.CharNormalizerType;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;
import com.adichad.lucense.analysis.synonym.AugmentingSynonymFilter;
import com.adichad.lucense.analysis.synonym.ReplacingSynonymFilter;

public class SynonymFilterSourceTest {

	@Before
	public void setup() {
	}

	@Test
	public void testGetTokenStream() {

		String file = "/tmp/data/d.txt";
		TokenStreamSource tss = new CharTokenizerSource(Version.LUCENE_33,
				"somecharshere", CharNormalizerType.NONE);
		SynonymFilterSource sfs = null;
		
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		try {
			sfs = new SynonymFilterSource(file, true);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		TokenFilter tf = null;
		try {
			tf = sfs.getTokenFilter(tss.getTokenStream(reader));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(tf instanceof ReplacingSynonymFilter);
		
		try {
			sfs = new SynonymFilterSource(file, false);
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		
		try {
			tf = sfs.getTokenFilter(tss.getTokenStream(reader));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(tf instanceof AugmentingSynonymFilter);
	}
}
