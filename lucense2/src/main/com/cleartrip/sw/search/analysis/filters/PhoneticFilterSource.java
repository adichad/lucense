package com.cleartrip.sw.search.analysis.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.Encoder;
import org.apache.commons.codec.language.DoubleMetaphone;
import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.Soundex;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;


public class PhoneticFilterSource extends TokenFilterSource {

  private final boolean injectMorphology;

  private final Encoder encoder;

  public PhoneticFilterSource(Map<String, ?> params, Properties env) throws Exception {
    super(params, env);
    String morphology = (String)params.get("morphology");
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
    this.injectMorphology = (Boolean)params.get("injectMorphology");
  }

  @Override
  public TokenFilter getTokenStream(TokenStream tokenStream) throws Exception {
    return new PhoneticFilter(tokenStream, this.encoder, this.injectMorphology);
  }

}
