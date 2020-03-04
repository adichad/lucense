package com.cleartrip.sw.search.searchj;

import java.util.List;

import org.codehaus.jackson.io.JsonStringEncoder;

import com.cleartrip.sw.search.context.ResourceManagerConfig.ReturnFormatConfig;

public abstract class ReturnFieldFormat {

  public abstract void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder);

  public static ReturnFieldFormat newFormatter(ReturnFormatConfig rfc) {
    switch (rfc.getType()) {
    case "float": {
      switch (rfc.getCardinality()) {
      case "one":
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            try {
              sb.append(Float.parseFloat(val.get(0)));
            } catch (Exception e) {
              sb.append(0f);
            }
          }

        };

      case "many":
      default: {
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            sb.append("[");
            for (String v : val) {
              try {
                sb.append(Float.parseFloat(v)).append(",");
              } catch (Exception e) {
                sb.append(0f);
              }
            }
            sb.deleteCharAt(sb.length() - 1).append("]");
          }

        };

      }
      }

    }
    case "double": {
      switch (rfc.getCardinality()) {
      case "one":
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            try {
              sb.append(Double.parseDouble(val.get(0)));
            } catch (Exception e) {
              sb.append(0d);
            }
          }

        };

      case "many":
      default: {
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            sb.append("[");
            for (String v : val) {
              try {
                sb.append(Double.parseDouble(v)).append(",");
              } catch (Exception e) {
                sb.append(0d);
              }
            }
            sb.deleteCharAt(sb.length() - 1).append("]");
          }

        };

      }
      }

    }
    case "int": {
      switch (rfc.getCardinality()) {
      case "one":
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            try {
              sb.append(Integer.parseInt(val.get(0)));
            } catch (Exception e) {
              sb.append(0);
            }
          }

        };

      case "many":
      default: {
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            sb.append("[");
            for (String v : val) {
              try {
                sb.append(Integer.parseInt(v)).append(",");
              } catch (Exception e) {
                sb.append(0);
              }
            }
            sb.deleteCharAt(sb.length() - 1).append("]");
          }

        };

      }
      }

    }
    case "boolean": {
      switch (rfc.getCardinality()) {
      case "one":
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            try {
              sb.append(Boolean.parseBoolean(val.get(0)));
            } catch (Exception e) {
              sb.append(false);
            }
          }

        };

      case "many":
      default: {
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            sb.append("[");
            for (String v : val) {
              try {
                sb.append(Boolean.parseBoolean(v)).append(",");
              } catch (Exception e) {
                sb.append(false);
              }
            }
            sb.deleteCharAt(sb.length() - 1).append("]");
          }

        };

      }
      }

    }
    case "string":
    default: {
      switch (rfc.getCardinality()) {
      case "one":
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            sb.append("\"").append(new String(encoder.quoteAsUTF8(val.get(0)==null?"":val.get(0))))
                .append("\"");
          }

        };

      case "many":
      default: {
        return new ReturnFieldFormat() {

          @Override
          public void formatAsJSON(StringBuilder sb, List<String> val, JsonStringEncoder encoder) {
            sb.append("[");
            for (String v : val)
              sb.append("\"").append(new String(encoder.quoteAsUTF8(v==null?"":v)))
                  .append("\",");
            sb.deleteCharAt(sb.length() - 1).append("]");
          }

        };

      }
      }
    }

    }

  }

}
