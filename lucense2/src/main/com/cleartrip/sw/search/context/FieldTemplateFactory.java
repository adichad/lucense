package com.cleartrip.sw.search.context;

import java.io.IOException;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.FieldInfo.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

import com.cleartrip.sw.search.context.ResourceManagerConfig.FieldTemplateConfig;
import com.cleartrip.sw.search.schema.FieldTemplate;

public class FieldTemplateFactory {

  static abstract class MultiFieldTemplate<T> extends FieldTemplate {

    @Override
    public Fieldable[] setValue(String name, Fieldable[] input, Object value) {
      Fieldable[] fields = input;
      @SuppressWarnings("unchecked")
      T[] val = (T[]) value;

      if (val == null)
        return new Fieldable[0];
      if (input == null)
        input = new Fieldable[val.length];

      if (val.length != input.length) {
        fields = new Fieldable[val.length];
        int i = 0;
        if (input.length < val.length) {
          for (; i < input.length; i++) {
            fields[i] = input[i];
          }
          for (; i < val.length; i++) {
            fields[i] = createField(name);
          }
        } else {
          for (; i < val.length; i++) {
            fields[i] = input[i];
          }
        }
      }

      for (int i = 0; i < val.length; ++i) {
        setSingleValue(fields[i], val[i]);
      }
      return fields;
    }

    public abstract void setSingleValue(Fieldable field, T val);
  }

  static class MultiStringFieldTemplate extends MultiFieldTemplate<String> {

    private final float        defaultBoost;
    private final IndexOptions indexOptions;

    private final TermVector   termVector;
    private Store              store;
    private Index              index;
    private boolean            intern;

    public MultiStringFieldTemplate(FieldTemplateConfig ftc) {
      this.defaultBoost = ftc.getDefaultBoost();
      this.indexOptions = IndexOptions.valueOf(ftc.getIndexOptions());
      this.store = Store.valueOf(ftc.getStored());
      this.index = Index.valueOf(ftc.getIndexed());
      this.intern = ftc.getInternName();
      this.termVector = TermVector.valueOf(ftc.getTermVector());
    }

    @Override
    public Fieldable createField(String fieldName) {
      Fieldable f;

      f = new Field(fieldName, intern, "", store, index, termVector);
      if (!f.getOmitNorms())
        f.setBoost(defaultBoost);
      f.setIndexOptions(indexOptions);
      return f;
    }

    @Override
    public void setSingleValue(Fieldable field, String val) {
      ((Field) field).setValue(val);
    }

    @Override
    public void warmFieldCache(IndexReader reader, String name)
        throws IOException {
      FieldCache.DEFAULT.getStrings(reader, name);

    }

  }

  public static class StringFieldTemplate extends MultiStringFieldTemplate {

    public StringFieldTemplate(FieldTemplateConfig ftc) {
      super(ftc);
    }

    @Override
    public Fieldable[] setValue(String name, Fieldable[] input, Object value) {

      String val = (String) value;

      Fieldable[] fields;
      if (val == null)
        fields = new Fieldable[0];
      else if (input.length > 0) {
        setSingleValue(input[0], val);
        fields = input;
      } else {
        fields = new Fieldable[1];
        fields[0] = createField(name);
        setSingleValue(fields[0], val);
        return fields;
      }
      return fields;
    }
  }

  static abstract class MultiNumericFieldTemplate<T> extends
      MultiFieldTemplate<T> {
    private final float        defaultBoost;
    private final IndexOptions indexOptions;
    private final int          precisionStep;
    private final Store        stored;
    private final Index        indexed;

    public MultiNumericFieldTemplate(FieldTemplateConfig ftc) {
      this.defaultBoost = ftc.getDefaultBoost();
      this.indexOptions = IndexOptions.valueOf(ftc.getIndexOptions());

      this.precisionStep = ftc.getPrecisionStep();
      // this.termVector = TermVector.valueOf(ftc.getTermVector());
      this.stored = Store.valueOf(ftc.getStored());
      this.indexed = Index.valueOf(ftc.getIndexed());
    }

    @Override
    public Fieldable createField(String fieldName) {
      Fieldable f;
      // NumericTokenStream tokStream = new NumericTokenStream(precisionStep);
      f = new NumericField(fieldName, precisionStep, stored,
          Index.NO.equals(indexed) ? false : true);
      if (!f.getOmitNorms())
        f.setBoost(defaultBoost);
      f.setIndexOptions(indexOptions);
      return f;
    }
  }

  public static class MultiIntFieldTemplate extends
      MultiNumericFieldTemplate<Integer> {

    public MultiIntFieldTemplate(FieldTemplateConfig ftc) {
      super(ftc);
    }

    @Override
    public void setSingleValue(Fieldable field, Integer val) {
      ((NumericField) field).setIntValue(val);
    }

    @Override
    public void warmFieldCache(IndexReader reader, String name)
        throws IOException {
      FieldCache.DEFAULT.getInts(reader, name);

    }
  }

  public static class IntFieldTemplate extends MultiIntFieldTemplate {

    public IntFieldTemplate(FieldTemplateConfig ftc) {
      super(ftc);
    }

    @Override
    public Fieldable[] setValue(String name, Fieldable[] input, Object value) {
      Integer val;
      Fieldable[] fields = null;
      val = (Integer) value;

      if (val == null)
        fields = new Fieldable[0];
      else if (input.length > 0) {
        setSingleValue(input[0], val);
        fields = input;
      } else {
        fields = new Fieldable[1];
        fields[0] = createField(name);
        setSingleValue(fields[0], val);

      }

      return fields;
    }
  }

  public static class MultiFloatFieldTemplate extends
      MultiNumericFieldTemplate<Float> {

    public MultiFloatFieldTemplate(FieldTemplateConfig ftc) {
      super(ftc);
    }

    @Override
    public void setSingleValue(Fieldable field, Float val) {
      ((NumericField) field).setFloatValue(val);
    }

    @Override
    public void warmFieldCache(IndexReader reader, String name)
        throws IOException {
      FieldCache.DEFAULT.getFloats(reader, name);

    }
  }

  public static class FloatFieldTemplate extends MultiFloatFieldTemplate {

    public FloatFieldTemplate(FieldTemplateConfig ftc) {
      super(ftc);
    }

    @Override
    public Fieldable[] setValue(String name, Fieldable[] input, Object value) {
      Float val = null;
      Fieldable[] fields = null;
      try {
        if (value == null) {
          fields = new Fieldable[0];
        } else {
          val = ((Double) value).floatValue();
          if (input.length > 0) {
            setSingleValue(input[0], val);
            fields = input;
          } else {
            fields = new Fieldable[1];
            fields[0] = createField(name);
            setSingleValue(fields[0], val);
            return fields;
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("field: " + name, e);
      }
      return fields;
    }
  }

  public static class MultiDoubleFieldTemplate extends
      MultiNumericFieldTemplate<Double> {

    public MultiDoubleFieldTemplate(FieldTemplateConfig ftc) {
      super(ftc);
    }

    @Override
    public void setSingleValue(Fieldable field, Double val) {
      ((NumericField) field).setDoubleValue(val);
    }

    @Override
    public void warmFieldCache(IndexReader reader, String name)
        throws IOException {
      FieldCache.DEFAULT.getDoubles(reader, name);

    }
  }

  public static class DoubleFieldTemplate extends MultiDoubleFieldTemplate {

    public DoubleFieldTemplate(FieldTemplateConfig ftc) {
      super(ftc);
    }

    @Override
    public Fieldable[] setValue(String name, Fieldable[] input, Object value) {
      Double val = (Double) value;

      Fieldable[] fields;
      if (val == null)
        fields = new Fieldable[0];
      else if (input.length > 0) {
        setSingleValue(input[0], val);
        fields = input;
      } else {
        fields = new Fieldable[1];
        fields[0] = createField(name);
        setSingleValue(fields[0], val);
        return fields;
      }
      return fields;
    }
  }

  public static class MultiLongFieldTemplate extends
      MultiNumericFieldTemplate<Long> {

    public MultiLongFieldTemplate(FieldTemplateConfig ftc) {
      super(ftc);
    }

    @Override
    public void setSingleValue(Fieldable field, Long val) {
      ((NumericField) field).setLongValue(val);
    }

    @Override
    public void warmFieldCache(IndexReader reader, String name)
        throws IOException {
      FieldCache.DEFAULT.getLongs(reader, name);

    }
  }

  public static class LongFieldTemplate extends MultiLongFieldTemplate {

    public LongFieldTemplate(FieldTemplateConfig ftc) {
      super(ftc);
    }

    @Override
    public Fieldable[] setValue(String name, Fieldable[] input, Object value) {

      Long val = (Long) value;

      Fieldable[] fields;
      if (val == null)
        fields = new Fieldable[0];
      else if (input.length > 0) {
        setSingleValue(input[0], val);
        fields = input;
      } else {
        fields = new Fieldable[1];
        fields[0] = createField(name);
        setSingleValue(fields[0], val);
        return fields;
      }
      return fields;
    }
  }

  static FieldTemplate createTemplate(FieldTemplateConfig ftc) {
    String type = ftc.getType();
    String cardinality = ftc.getCardinality();
    switch (type) {
    case "int":
      if (cardinality != null && cardinality.equals("many"))
        return new MultiIntFieldTemplate(ftc);
      else
        return new IntFieldTemplate(ftc);
    case "long": {
      if (cardinality != null && cardinality.equals("many"))
        return new MultiLongFieldTemplate(ftc);
      else
        return new LongFieldTemplate(ftc);
    }
    case "float": {
      if (cardinality != null && cardinality.equals("many"))
        return new MultiFloatFieldTemplate(ftc);
      else
        return new FloatFieldTemplate(ftc);
    }
    case "double": {
      if (cardinality != null && cardinality.equals("many"))
        return new MultiDoubleFieldTemplate(ftc);
      else
        return new DoubleFieldTemplate(ftc);
    }
    case "string": {
      if (cardinality != null && cardinality.equals("many"))
        return new MultiStringFieldTemplate(ftc);
      else
        return new StringFieldTemplate(ftc);
    }

    default:
      throw new RuntimeException("unknown field type: " + type);
    }

  }
}
