/**
 * 
 */
package com.adichad.lucense.analysis.component.filter;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource.CharNormalizerType;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;
import com.adichad.lucense.analysis.stem.ControlledPresentParticipleStemFilter;

public class ControlledPresentParticipleStemFilterSourceTest {

	@Before
	public void setup() {
	}

	@Test
	public void testGetTokenStream() {

		String file = "/tmp/data/b.txt";
		TokenStreamSource tss = new CharTokenizerSource(Version.LUCENE_33,
				"somecharshere", CharNormalizerType.NONE);
		ControlledPresentParticipleStemFilterSource cppsfs1 = new ControlledPresentParticipleStemFilterSource(
				Version.LUCENE_33, file, 0, true, true);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		TokenFilter tf = null;
		try {
			tf = cppsfs1.getTokenFilter(tss.getTokenStream(reader));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(tf instanceof ControlledPresentParticipleStemFilter);

		file = "/tmp/data/c.txt";
		new ControlledPresentParticipleStemFilterSource(Version.LUCENE_33,
				file, 0, true, true);
		reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
		}
		assertNull(reader);

	}
}
