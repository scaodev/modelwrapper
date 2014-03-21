#!/usr/bin/env groovy
import freemarker.template.*
import org.apache.commons.cli.Option

//in intellij type alt+enter to import grab lib
@Grab(group = 'org.freemarker', module = 'freemarker', version = '2.3.20')

//indicate this is groovy script. groovy should be reachable from command line
def cli = new CliBuilder(
    usage: 'wrapperGen.groovy <-j pathToJar> <-c packagePath,classPath>'
)

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.file.FileSystems

cli.with {
  h(longOpt: 'help', 'Usage information', required: false)
  j(longOpt: 'jar', 'source jar file full path', args: 1, required: true)
  b(longOpt: 'base', 'target class package base', args: 1, required: false)
  t(longOpt: 'targetPath', 'target root folder write generated files to', args: 1, required: true)
  //this parameter accept multi value separate by comma
  c(longOpt: 'classes', 'package path or full path class name to generate', args: Option.UNLIMITED_VALUES, valueSeparator: ",", required: true)
  f(longOpt: 'force', 'force overwrite exist file', required: false)
}

def opt = cli.parse(args)

if (!opt) return

//opt.c will read first parameter only. to read all parameters as array, use opt.cs format. 's' indicate multi values
def classes = opt.cs

def jarPath = opt.j


this.class.classLoader.rootLoader.addURL(new URL("file://${jarPath}"))

assert classes.size() > 0, "At least one full path class name or package name should be specified."

File basePath = makeTargetPath(opt.t, opt.b)

if (opt.f) {
  basePath.listFiles().each { file -> file.delete() }
}

classes.each { cn ->
  Class clazz = Class.forName(cn)
  generate(clazz, opt, basePath)
}

private void generate(Class<?> clazz, OptionAccessor opt, File basePath) {
  def root = processClass(clazz, opt.b, opt, basePath)
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


def processClass(Class clazz, p, opt, basePath) {
  def getters = []
  def imports = [] as HashSet
  def currentFieldName = "refVal"
  def privateWrapFields = [[type: clazz.name, name: currentFieldName]]
  def initFields = []
  Map retVal = [package          : p, className: genTargetClassName(clazz), sourceClass: clazz.name, getters: getters,
                privateWrapFields: privateWrapFields, imports: imports, initFields: initFields]

  //bean info return all methods including inherited from parent. but we don't need them here
  //BeanInfo beanInfo = Introspector.getBeanInfo(clazz)
  //beanInfo.methodDescriptors.each { md ->
  clazz.declaredMethods.each { method ->
    if ((method.name.startsWith("get") && method.name != 'getClass') || method.name.startsWith("is")) {
      Map methodAnalystResult = genMethodRetTypeInStr(clazz, method.name, p)
      imports.addAll(methodAnalystResult.imports)

      def getter = [retType: methodAnalystResult.retType, methodName: method.name,wrapperType:false]
      if (methodAnalystResult.newField) {
        getter.put('wrapperType', true)
        getter.put('retMethod', method.name)
        generate(methodAnalystResult.newFieldClass, opt, basePath)
        initFields.add([name  : methodAnalystResult.retField, typeName: methodAnalystResult.newFieldClass.simpleName,
                        getter: method.name])
      } else {
        getter.put('retMethod', method.name)
      }
      getters.push(getter)
    }
  }

  Class superClass = clazz.superclass
  if (superClass.simpleName != 'Object') {
    retVal.put('super', superClass.simpleName)
  }
  return retVal
}

def genMethodRetTypeInStr(Class clazz, String methodName, String currentPackage) {
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


  if (hasOneGenericType(field)) {
    ParameterizedType type = (ParameterizedType) field.getGenericType()
    //actualTypeArguments list all generic type parameters
    return [retType: "${field.type.simpleName}<${getTypeSimpleName(type.actualTypeArguments[0])}>", imports: [fieldTypeLong]]
  } else if (isMapType(field)) {
    ParameterizedType type = (ParameterizedType) field.getGenericType()
    return [retType: "${field.type.simpleName}<${getTypeSimpleName(type.actualTypeArguments[0])}, ${getTypeSimpleName(type.actualTypeArguments[1])}>",
            imports: [fieldTypeLong]]
  } else if (fieldTypeLong.startsWith(clazz.package.name)) {
    return [retType      : field.type.simpleName, imports: [], newField: true, newFieldClass:field.type]
  } else {
    return [retType: field.type.simpleName, imports: findImports(fieldTypeLong, currentPackage)]
  }

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

def hasOneGenericType(Field field) {
  //check if is subClass of
  return List.class.isAssignableFrom(field.getType()) || Set.class.isAssignableFrom(field.getType())
}

def isMapType(Field field) {
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

// Sets how errors will appear. Here we assume we are developing HTML pages.
// For production systems TemplateExceptionHandler.RETHROW_HANDLER is better.
  cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

// At least in new projects, specify that you want the fixes that aren't
// 100% backward compatible too (these are very low-risk changes as far as the
// 1st and 2nd version number remains):
  cfg.setIncompatibleImprovements(new Version(2, 3, 20));  // FreeMarker 2.3.20

  Template template = cfg.getTemplate("wrapper.ftl")

  template.process(content, out);
}
