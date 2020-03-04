package com.adichad.lucense.analysis.component.filter;

import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.adichad.lucense.analysis.PhoneticFilter;

public class PhoneticFilterSource implements TokenFilterSource {

  private final boolean injectMorphology;

  private final Encoder encoder;

  public PhoneticFilterSource(String morphology, boolean injectMorphology) {
    if (morphology.equals("soundex")) {
      this.encoder = new Soundex();
    } else if (morphology.equals("soundex-refined")) {
      this.encoder = new RefinedSoundex();
    } else if (morphology.equals("metaphone")) {
      this.encoder = new Metaphone();
    } else if (morphology.equals("metaphone-double")) {
      this.encoder = new DoubleMetaphone();
    } else {
      this.encoder = new Soundex();
    }
    this.injectMorphology = injectMorphology;
  }

  @Override
  public TokenFilter getTokenFilter(TokenStream tokenStream) {
    return new PhoneticFilter(tokenStream, this.encoder, this.injectMorphology);
  }

}
