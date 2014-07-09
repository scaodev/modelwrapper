#!/usr/bin/env groovy
import freemarker.template.*

//in intellij type alt+enter to import grab lib
@Grab(group = 'org.freemarker', module = 'freemarker', version = '2.3.20')

//indicate this is groovy script. groovy should be reachable from command line
def cli = new CliBuilder(
    usage: 'wrapperGen.groovy <-j pathToJar> <-c packagePath,classPath>'
)

import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.file.FileSystems
import java.util.jar.JarFile

cli.with {
  h(longOpt: 'help', 'Usage information', required: false)
  j(longOpt: 'jar', 'source jar file full path', args: 1, required: true)
  b(longOpt: 'base', 'target class package base', args: 1, required: false)
  t(longOpt: 'targetPath', 'target root folder write generated files to', args: 1, required: true)
  //this parameter accept multi value separate by comma
  c(longOpt: 'config', 'package mapping config file', args: 1, required: true)
  f(longOpt: 'force', 'force overwrite exist file', required: false)
}

def opt = cli.parse(args)

if (!opt) return


def jarPath = opt.j

def packageMapping = new Properties()

packageMapping.load(new FileInputStream(new File(opt.c)))


this.class.classLoader.rootLoader.addURL(new URL("file://${jarPath}"))

File basePath = makeTargetPath(opt.t, opt.b)

if (opt.f) {
  basePath.listFiles().each { file -> file.delete() }
}

JarFile jarFile = new JarFile(opt.j)

jarFile.entries().each { entry ->
  if (entry.name.endsWith(".class")) {
    Class clazz = Class.forName(toClassName(entry.name))
    generate(clazz, opt, basePath)
  }
}

def toClassName(String name){
  name.substring(0, name.length() - 6).replace('/', '.')
}


private void generate(Class<?> clazz, OptionAccessor opt, File basePath) {
  def root = analystClass(clazz, opt.b, opt, basePath)
  //todo refactor this, no necessary passing parameters which might not be used

  File outFile = new File(basePath, "${clazz.simpleName}.java")

  if (outFile.exists()) {
    println("${outFile.getName()} already exist. generate ignored")
    return
    //TODO promote if overwrite exist file
  } else {
    outFile.createNewFile()
  }
  writeToOutput(root, new OutputStreamWriter(new FileOutputStream(outFile)))
}

def preparePackage(clazz){

}


//this method generate getter and setter, import statement...
def analystClass(Class clazz, p, opt, basePath) {
  def getters = []
  def imports = [] as HashSet
  def currentFieldName = "refVal"
  def initFields = []
  Map retVal = [package          : p, className: genTargetClassName(clazz), sourceClass: clazz.name, getters: getters,
                 imports: imports, initFields: initFields]

  //bean info return all methods including inherited from parent. but we don't need them here
  //BeanInfo beanInfo = Introspector.getBeanInfo(clazz)
  //beanInfo.methodDescriptors.each { md ->
  clazz.declaredMethods.each { method ->
    if (isGetter(method)) {
      Map methodAnalystResult = analystGetter(clazz, method.name, p)
      methodAnalystResult += [methodName: method.name, retMethod: method.name, sourceClass: methodAnalystResult.newFieldClass?.name,
      setterName: toSetter(method.name)]

      imports.addAll(methodAnalystResult.imports)

      if (methodAnalystResult.genericTypeIsGenerated) {
        generate(methodAnalystResult.newFieldClass, opt, basePath)
        initFields.add([name  : methodAnalystResult.retField, typeName: methodAnalystResult.newFieldClass.simpleName,
                        getter: method.name])
      }
      getters.push(methodAnalystResult)
    }
  }

  Class superClass = clazz.superclass
  if (superClass.simpleName != 'Object') {
    retVal.put('super', superClass.simpleName)
  }
  return retVal
}

def toSetter(String name) {
  String suffiFix = name.startsWith('is') ? name.substring(2) : name.substring(3)
  return "set${suffiFix}"
}

private boolean isGetter(Method method) {
  (method.name.startsWith("get") && method.name != 'getClass') || method.name.startsWith("is")
}

def analystGetter(Class clazz, String methodName, String currentPackage) {
  String fieldName;
  if (methodName.startsWith('get')) {
    fieldName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4, methodName.length())
  } else if (methodName.startsWith('is')) {
    fieldName = methodName.substring(2, 3).toLowerCase() + methodName.substring(3, methodName.length())
  } else {
    return [:]
  }
  Field field = clazz.getDeclaredField(fieldName)
  String fieldTypeLong = field.type.name

  def retVal = [isCollection        : false, fundamentalType: false, genericTypeIsGenerated: false, imports: [], newField: 'ref',
                internalRefFieldName: '_' + fieldName, mapValueTypeIsGenerated: false, fieldName: fieldName]

  if (isOneGenericTypeCollection(field)) {
    retVal.isCollection = true;

    ParameterizedType type = (ParameterizedType) field.getGenericType()
    //actualTypeArguments list all generic type parameters
    retVal.retType = "${field.type.simpleName}<${getTypeSimpleName(type.actualTypeArguments[0])}>"
    retVal.imports = [fieldTypeLong];

    if (field.type.simpleName.indexOf("List") != -1) {
      retVal.imports.add('java.util.ArrayList')
      retVal.collectionImplType = 'ArrayList'
    } else {
      retVal.imports.add('java.util.HashSet')
      retVal.collectionImplType = 'HashSet'
    }

    retVal.newFieldClass = Class.forName(getFullTypeClass(type.actualTypeArguments[0]))
    retVal.genericTypes = ["${getTypeSimpleName(type.actualTypeArguments[0])}"]
    retVal.targetClass = getTypeSimpleName(type.actualTypeArguments[0])
    retVal.genericTypeIsGenerated = genericTypeIsGenerated(clazz, type)
  } else if (fieldTypeIsMap(field)) {
    retVal.isCollection = true;
    retVal.collectionImplType = 'HashMap'
    ParameterizedType type = (ParameterizedType) field.getGenericType()

    retVal += [retType                   : "${field.type.simpleName}<${getTypeSimpleName(type.actualTypeArguments[0])}, ${getTypeSimpleName(type.actualTypeArguments[1])}>",
               imports                   : [fieldTypeLong, 'java.util.HashMap'], fundamentalType: false,
               mapKeyType                : type.actualTypeArguments[0].simpleName,
               mapKeyTypeIsGenerated     : isTypeGenerated(clazz, type.actualTypeArguments[0]),
               mapKeyTypeSourceFullName  : type.actualTypeArguments[0].name,
               mapValueType              : type.actualTypeArguments[1].simpleName,
               mapValueTypeIsGenerated   : isTypeGenerated(clazz, type.actualTypeArguments[1]),
               mapValueTypeSourceFullName: type.actualTypeArguments[1].name]

  } else if (isTypeGenerated(clazz, field.type)) {
    retVal += [retType: field.type.simpleName, newFieldClass: field.type, genericTypeIsGenerated: isTypeGenerated(clazz, field.type)]
  } else {
    retVal += [retType: field.type.simpleName, imports: findImports(fieldTypeLong, currentPackage), fundamentalType: true]
  }
  return retVal

}

private boolean genericTypeIsGenerated(Class clazz, ParameterizedType type) {
  isTypeGenerated(clazz, type.actualTypeArguments[0])
}

private boolean isTypeGenerated(Class clazz, Type type) {
  clazz.package.name.startsWith(getTypePackageName(type))
}

def findImports(String fieldTypeLong, String currentPackage) {
  //Array start with [ character
  if (fieldTypeLong.startsWith("java.lang") || fieldTypeLong.startsWith(currentPackage) || fieldTypeLong.startsWith('[')) {
    []
  } else {
    [fieldTypeLong]
  }
}

def getTypeSimpleName(Type type) {
  //the type.toString suppose return 'class java.lang.String'
  String s = type.toString().split(' ')[1]
  s.substring(s.lastIndexOf(".") + 1)
}

def getTypePackageName(Type type) {
  if (type.toString().startsWith('class')) {
    String s = type.toString().split(' ')[1]
    s.substring(0, s.lastIndexOf('.'))
  }else {
    type.toString()
  }

}

def isOneGenericTypeCollection(Field field) {
  //check if is subClass of
  return List.class.isAssignableFrom(field.getType()) || Set.class.isAssignableFrom(field.getType())
}

def fieldTypeIsMap(Field field) {
  return Map.class.isAssignableFrom(field.getType())
}


def genTargetClassName(Class clazz) {
  String[] names = clazz.name.split('\\.')
  names[names.length - 1]
}

def makeTargetPath(path, basePackage) {
  def targetPath = path ? path : '.'
  String fullPath = targetPath + FileSystems.default.separator + basePackage.replace('.', FileSystems.default.separator)

  new File(fullPath).mkdirs()

  new File(fullPath)
}

def writeToOutput(content, out) {
  Configuration cfg = new Configuration();
  // Specify the data source where the template files come from. Here I set a
// plain directory for it, but non-file-system are possible too:
  cfg.setDirectoryForTemplateLoading(new File("./"));

// Specify how templates will see the data-model. This is an advanced topic...
// for now just use this:
  cfg.setObjectWrapper(new DefaultObjectWrapper());

// Set your preferred charset template files are stored in. UTF-8 is
// a good choice in most applications:
  cfg.setDefaultEncoding("UTF-8");

// Sets how errors will appear. Here I assume we are developing HTML pages.
// For production systems TemplateExceptionHandler.RETHROW_HANDLER is better.
  cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

// At least in new projects, specify that you want the fixes that aren't
// 100% backward compatible too (these are very low-risk changes as far as the
// 1st and 2nd version number remains):
  cfg.setIncompatibleImprovements(new Version(2, 3, 20));  // FreeMarker 2.3.20

  Template template = cfg.getTemplate("pojo.ftl")

  template.process(content, out);
}

String getFullTypeClass(Type type) {
  type.toString().split(' ')[1]
}