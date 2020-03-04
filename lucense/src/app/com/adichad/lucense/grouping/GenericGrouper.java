package com.adichad.lucense.grouping;

import com.adichad.lucense.expression.fieldSource.ValueSource;

public abstract class GenericGrouper extends Grouper {
  protected ValueSource[] keypartsource;

  public GenericGrouper(GroupingCriteria grouper) {
    this.keypartsource = new ValueSource[grouper.groupfields.length];
    for (int i = 0; i < grouper.groupfields.length; ++i) {
      switch (grouper.gftypes[i]) {
      case TYPE_INT:
        this.keypartsource[i] = grouper.intValueSources.get(grouper.groupfields[i]);
        break;
      case TYPE_FLOAT:
        this.keypartsource[i] = grouper.floatValueSources.get(grouper.groupfields[i]);
        break;
      case TYPE_DOUBLE:
        this.keypartsource[i] = grouper.doubleValueSources.get(grouper.groupfields[i]);
        break;
      case TYPE_BOOLEAN:
        this.keypartsource[i] = grouper.booleanValueSources.get(grouper.groupfields[i]);
        break;
      case TYPE_STRING:
        this.keypartsource[i] = grouper.stringValueSources.get(grouper.groupfields[i]);
        break;
      }

    }
  }
}
