package org.scao.gen;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by scao on 7/11/14.
 */
public class Getter {
  private String modifier;
  private boolean concrect;
  private String returnClass;
  private String getterName;
  private String setterName;
  private String fieldName;

  public String getModifier() {
    return modifier;
  }

  public void setModifier(String modifier) {
    this.modifier = modifier;
  }

  public boolean isConcrect() {
    return concrect;
  }

  public void setConcrect(boolean concrect) {
    this.concrect = concrect;
  }

  public String getReturnClass() {
    return returnClass;
  }

  public void setReturnClass(String returnClass) {
    this.returnClass = returnClass;
  }

  public String getGetterName() {
    return getterName;
  }

  public void setGetterName(String getterName) {
    this.getterName = getterName;
  }

  public String getSetterName() {
    return setterName;
  }

  public void setSetterName(String setterName) {
    this.setterName = setterName;
  }


  public void setFieldName(String fieldName) {
    if (fieldName.equals("package")) {
      this.fieldName = "_" + fieldName;
    }else{
      this.fieldName = fieldName;
    }
  }

  public String getFieldName() {
    return fieldName;
  }
}
