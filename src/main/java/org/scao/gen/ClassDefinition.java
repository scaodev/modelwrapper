package org.scao.gen;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Date: 7/10/14
 * Time: 8:12 PM
 */
public class ClassDefinition {
  private boolean enumm;
  private boolean inner;
  private boolean inherited;
  private boolean concrete;
  private String modifier;
  private String targetPackage;
  private List<String> imports = new ArrayList<>();
  private List<Field> fields = new ArrayList<>();
  private List<ClassDefinition> inners = new ArrayList<>();
  private String superClass;
  private String targetClassName;
  private final Properties packageMapping;

  public ClassDefinition(Properties packageMapping) {
    this.packageMapping = packageMapping;
  }

  public void setClass(Class clazz) {
    this.enumm = clazz.isEnum();

    targetPackage = genFullPackage(clazz);

    targetClassName = clazz.getSimpleName();

    superClass = genSuperClass(clazz);

    concrete = determineIsAbstract(clazz);

    modifier = getModifier(clazz);
  }

  private String getModifier(Class clazz) {
    if (Modifier.isProtected(clazz.getModifiers())) {
      return "protected";
    }else {
      return "public";
    }
  }

  private boolean determineIsAbstract(Class clazz) {
    return !Modifier.isAbstract(clazz.getModifiers());
  }



  private String genSuperClass(Class clazz) {
    Class sup = clazz.getSuperclass();
    if (sup == null || sup.getSimpleName().equals("Object")) {
      return null;
    }
    getImport(sup).ifPresent(this::addImport);
    return sup.getSimpleName();
  }

  private Optional<String> getImport(Class clazz) {
    String p = packageNameOfClass(clazz);
    String map = (String) packageMapping.get(p);
    if (map != null && !map.equals(targetPackage)) {
      System.out.println(map);
      return Optional.of(map + "." + clazz.getSimpleName());
    }
    if (!p.startsWith("java.lang")) {
      return Optional.of(p + "." + clazz.getSimpleName());
    }
    return Optional.empty();
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
  public boolean isEnumm() {
    return enumm;
  }

  public boolean isInner() {
    return inner;
  }

  public String getTargetPackage() {
    return targetPackage;
  }

  public List<String> getImports() {
    return new ArrayList<>(imports);
  }

  public List<Field> getFields() {
    return new ArrayList<>(fields);
  }

  public void addImport(String i) {
    if (!imports.contains(i)) {
      imports.add(i);
    }
  }

  public boolean isInherited() {
    return inherited;
  }

  public boolean isConcrete() {
    return concrete;
  }

  public String getSuperClass() {
    return superClass;
  }

  public void setSuperClass(String superClass) {
    this.superClass = superClass;
  }

  public void setInherited(boolean inherited) {
    this.inherited = inherited;
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

  public void setTargetPackage(String targetPackage) {
    this.targetPackage = targetPackage;
  }

  public String getTargetClassName() {
    return targetClassName;
  }

  public String getModifier() {
    return modifier;
  }

}
