package org.scao.gen;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by scao on 7/13/14.
 */
public class EnumDifinition implements AbstractClass{
  private List<String> fields = new ArrayList<>();
  private String sourceEnum;
  private final Properties packageMapping;
  private String targetPackage;
  private String targetClassName;

  public void setTargetPackage(String targetPackage) {
    this.targetPackage = targetPackage;
  }

  public void setTargetClassName(String targetClassName) {
    this.targetClassName = targetClassName;
  }

  public EnumDifinition(Properties packageMapping) {
    this.packageMapping = packageMapping;
  }

  public List<String> getFields() {
    return fields;
  }

  public void setFields(List<String> fields) {
    this.fields = fields;
  }

  public String getSourceEnum() {
    return sourceEnum;
  }

  public void setSourceEnum(String sourceEnum) {
    this.sourceEnum = sourceEnum;
  }

  @Override public String getTargetPackage() {
    return this.targetPackage;
  }

  @Override public String getTargetClassName() {
    return this.targetClassName;
  }

  public void setClass(Class clazz) {
    targetPackage = genFullPackage(clazz);

    targetClassName = clazz.getSimpleName();

    sourceEnum = clazz.getName();

    Field[] fs = clazz.getFields();
    for (Field field : fs) {
      fields.add(field.getName());
    }


  }

  private String genFullPackage(Class clazz) {
    String sourcePackage = packageNameOfClass(clazz);
    if (packageMapping.containsKey(sourcePackage)) {
      return packageMapping.getProperty(sourcePackage);
    } else {
      throw new RuntimeException("Can't find mapping for package " + sourcePackage);
    }
  }

  private String packageNameOfClass(Class clazz) {
    String s = clazz.getPackage().getName();
    return s.substring(s.indexOf(" ") + 1);
  }
}
