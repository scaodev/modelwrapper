package org.scao.gen;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Date: 7/10/14
 * Time: 8:12 PM
 */
public class ClassDefinition implements AbstractClass{
  private final Set<String> primitives;
  private boolean enumm;
  private boolean inner;
  private boolean inherited;
  private boolean concrete;
  private String modifier;
  private String targetPackage;
  private List<String> imports = new ArrayList<>();
  private List<Getter> getters = new ArrayList<>();
  private List<ClassDefinition> inners = new ArrayList<>();
  private String superClass;
  private String targetClassName;
  private final Properties packageMapping;

  private final Pattern p1 = Pattern.compile("([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.\\$]+)");
  private final Pattern p2 =
    Pattern.compile("([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.]+)\\s*<([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.\\$]+)>");
  private final Pattern p3 = Pattern.compile(
    "([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.]+)\\s*<([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.\\$]+),\\s*([a-zA-Z0-9]+\\.[a-zA-Z0-9\\.\\$]+)>");

  private List<Pattern> typePatterns;



  public ClassDefinition(Properties packageMapping) {
    this.packageMapping = packageMapping;
    primitives = new HashSet<>();
    primitives.add("int");
    primitives.add("boolean");
    primitives.add("long");
    primitives.add("char");
    primitives.add("double");
    primitives.add("short");
    primitives.add("float");
    primitives.add("byte");

    typePatterns = new ArrayList<>();
    typePatterns.add(p1);
    typePatterns.add(p2);

    typePatterns.add(p3);
  }

  public void setClass(Class clazz) {
    this.enumm = clazz.isEnum();

    targetPackage = genFullPackage(clazz);

    targetClassName = clazz.getSimpleName();

    superClass = genSuperClass(clazz);

    concrete = determineIsAbstract(clazz);

    modifier = getModifier(clazz);

    analystOthers(clazz);

    List<ClassDefinition> definitions = new ArrayList<>();
    for (Class c : clazz.getClasses()) {
      ClassDefinition cd = new ClassDefinition(packageMapping);
      cd.setClass(c);
      definitions.add(cd);

      List<String> innerImport = cd.getImports();
      innerImport.forEach(this::addImport);
    }
    this.inners = definitions;
  }

  private void analystOthers(Class clazz) {
    this.getters = Arrays.asList(clazz.getDeclaredMethods())
      .stream()
      .filter(method -> method.getName().startsWith("is") || method.getName().startsWith("get"))
      .map(processGetterSetter())
      .collect(Collectors.toList());
  }

  private Function<Method, Getter> processGetterSetter() {
    return (final Method method) -> {
      Class returnClazz = method.getReturnType();
      getImport(returnClazz).ifPresent(this::addImport);

      Getter getter = new Getter();
      getter.setGetterName(genGetterName(method));
      getter.setSetterName(genSetterName(method));
      getter.setConcrect(!Modifier.isAbstract(method.getModifiers()));
      getter.setModifier(Modifier.isPublic(method.getModifiers()) ? "public" : "protected");
      getter.setReturnClass(genReturnClass(method.getGenericReturnType()));
      getter.setFieldName(genFieldName(method));
      return getter;
    };
  }

  private String genReturnClass(Type genericReturnType) {
    String type = genericReturnType.toString();
    if (type.startsWith("class")) {
      type = type.substring(type.indexOf(" ") + 1);
    }
    Matcher matcher;
    for (Pattern p : typePatterns) {
      matcher = p.matcher(type);
      if (matcher.matches()) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
          getImport(matcher.group(i));
        }
        if (matcher.groupCount() == 1) {
          return getClassName(matcher.group(1));
        } else if (matcher.groupCount() == 2) {
          return getClassName(matcher.group(1)) + "<"
            + getClassName(matcher.group(2)) + ">";
        } else {
          return getClassName(matcher.group(1)) + "<"
            + getClassName(
            matcher.group(2)) + "," + getClassName(
            matcher.group(3)) + ">";
        }
      }
    }
    return type;
  }

  private String genClassTypeFromStr(String fullPath) {
    String s = getClassName(fullPath);
    return s;
  }



  private final Function<String, String> stripPackage =
    input -> input.substring(input.lastIndexOf(".") + 1);

  private final Function<String, String> stripInnerClass = input -> {
    if (input.contains("$")) {
      return input.substring(0, input.indexOf("$"));
    } else {
      return input;
    }
  };

  private final Function<String, String> processClassFullPath =
    stripInnerClass.compose(stripPackage);

  private void getImport(String group) {
    String p = packageNameFromStr(group);
    String className = getClassName(group);
    if (packageMapping.getProperty(p) != null) {
      addImport(packageMapping.getProperty(p) + "." + className);
    } else if (!p.contains("java.lang")) {
      addImport(stripInnerClass.apply(group));
    }
  }

  private String packageNameFromStr(String input) {
    return input.substring(0, input.lastIndexOf("."));
  }

  private String genMappedClassName(String input) {
    if (packageMapping.containsKey(input)) {
      return packageMapping.getProperty(input);
    } else if (input.startsWith("java.lang")) {
      return "";
    } else {
      return input;
    }
  }

  private String getClassName(String input) {
    String inner = input.substring(input.lastIndexOf(".") + 1);
    return inner.replace('$', '.');
  }



  private String genFieldName(Method method) {
    String methodName = method.getName();
    if (methodName.startsWith("is")) {
      methodName = methodName.substring(2);
    } else {
      methodName = methodName.substring(3);
    }
    return methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
  }

  private String genGetterName(Method method) {
    return method.getName();
  }

  private String genSetterName(Method method) {
    String getterName = method.getName();
    if (getterName.startsWith("is")) {
      return "set" + getterName.substring(2);
    } else {
      return "set" + getterName.substring(3);
    }
  }



  private String getModifier(Class clazz) {
    if (Modifier.isProtected(clazz.getModifiers())) {
      return "protected";
    } else {
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
      return Optional.of(map + "." + clazz.getSimpleName());
    }
    if (map != null) {
      return Optional.empty();
    }
    if (p.length() == 0) {
      return Optional.empty();
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
    if (isPrimitiveType(clazz)) {
      return "";
    }
    String s = clazz.getPackage().getName();
    return s.substring(s.indexOf(" ") + 1);
  }

  private boolean isPrimitiveType(Class clazz) {
    return primitives.contains(clazz.getName());
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

  public List<Getter> getGetters() {
    return getters;
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
