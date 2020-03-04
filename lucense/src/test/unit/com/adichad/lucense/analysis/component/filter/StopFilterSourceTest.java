/**
 * 
 */
package com.adichad.lucense.analysis.component.filter;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.util.Version;
import org.junit.Before;
import org.junit.Test;

import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource;
import com.adichad.lucense.analysis.component.tokenizer.CharTokenizerSource.CharNormalizerType;
import com.adichad.lucense.analysis.component.tokenizer.TokenStreamSource;
import com.adichad.lucense.analysis.stop.StopMarkingFilter;

public class StopFilterSourceTest {

	@Before
	public void setup() {
	}

	@Test
	public void testGetTokenStream() {

		String file = "/tmp/data/a.txt";
		TokenStreamSource tss = new CharTokenizerSource(Version.LUCENE_33,
				"somecharshere", CharNormalizerType.NONE);
		StopFilterSource sfs = null;

		// Creating file reader
		FileReader reader = null;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		TokenFilter tf = null;

		file = "/tmp/data/b.txt";
		try {
			sfs = new StopFilterSource(Version.LUCENE_33, file, true);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			tf = sfs.getTokenFilter(tss.getTokenStream(reader));
		} catch (Exception e) {
			// e.printStackTrace();
			fail("This code should not be reached");
		}
		assertTrue(tf instanceof StopMarkingFilter);

		file = "/tmp/data/b.txt";
		try {
			sfs = new StopFilterSource(Version.LUCENE_33, file, false);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			tf = sfs.getTokenFilter(tss.getTokenStream(reader));
		} catch (Exception e) {
			// e.printStackTrace();
			fail("This code should not be reached");
		}
		assertTrue(tf instanceof StopFilter);
	}
}
