package com.adichad.lucense.resource;

import com.adichad.lucense.request.Request.FieldType;

public class Substitution {

  private int pos;

  private String sourcename;

  private String vartype;

  private String varname;

  public Substitution(int pos, String sourcename, String vartype, String varname) {
    this.pos = pos;
    this.sourcename = sourcename;
    this.vartype = vartype;
    this.varname = varname;
  }

  @Override
  public String toString() {
    return new String("[" + this.pos + "," + this.sourcename + "," + this.vartype + "," + this.varname + "]");
  }

  public int getPos() {
    return this.pos;
  }

  public String getVarQueryName() {
    return this.sourcename;
  }

  public FieldType getVarType() {
    return FieldType.TYPE_INT;
  }

  public String getVarName() {
    return this.varname;
  }

}
