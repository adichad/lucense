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
import com.adichad.lucense.analysis.stem.ControlledPluralsInvertingStemFilter;

public class ControlledPluralsInvertingStemFilterSourceTest {

	@Before
	public void setup() {
	}

	@Test
	public void testGetTokenStream() {

		String file = "/tmp/data/b.txt";
		TokenStreamSource tss = new CharTokenizerSource(Version.LUCENE_33,
				"somecharshere", CharNormalizerType.NONE);
		ControlledPluralsInvertingStemFilterSource cpisfs1 = new ControlledPluralsInvertingStemFilterSource(
				Version.LUCENE_33, file, 0, true);
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		TokenFilter tf = null;
		try {
			tf = cpisfs1.getTokenFilter(tss.getTokenStream(reader));
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertTrue(tf instanceof ControlledPluralsInvertingStemFilter);

		file = "/tmp/data/c.txt";
		new ControlledPluralsInvertingStemFilterSource(Version.LUCENE_33, file,
				0, true);
		reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
		}
		assertNull(reader);

	}

}
