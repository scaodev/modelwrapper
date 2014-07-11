package org.scao.gen;

import java.util.ArrayList;
import java.util.List;

/**
 * Date: 7/10/14
 * Time: 8:12 PM
 */
public class ClassDefinition {
  private boolean enumm;
  private boolean inner;
  private String fullPackage;
  private List<String> imports = new ArrayList<>();
  private List<Field> fields = new ArrayList<>();
  private List<ClassDefinition> inners = new ArrayList<>();

  public boolean isEnumm() {
    return enumm;
  }

  public boolean isInner() {
    return inner;
  }

  public String getFullPackage() {
    return fullPackage;
  }

  public List<String> getImports() {
    return new ArrayList<>(imports);
  }

  public List<Field> getFields() {
    return new ArrayList<>(fields);
  }

  public void addImport(String i) {
    imports.add(i);
  }

  public void setEnumm(boolean enumm) {
    this.enumm = enumm;
  }

  public void addInner(ClassDefinition definition) {
    inners.add(definition);
  }

  public List<ClassDefinition> getInners() {
    return new ArrayList<>(inners);
  }

  public void setInner(boolean inner) {
    this.inner = inner;
  }

  public void setFullPackage(String fullPackage) {
    this.fullPackage = fullPackage;
  }
}
