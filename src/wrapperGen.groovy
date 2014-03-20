#!/usr/bin/env groovy
import freemarker.template.Configuration
import freemarker.template.DefaultObjectWrapper
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import freemarker.template.Version

//in intellij type alt+enter to import grab lib
@Grab(group = 'org.freemarker', module = 'freemarker', version = '2.3.20')

//indicate this is groovy script. groovy should be reachable from command line
def cli = new CliBuilder(
    usage: 'wrapperGen.groovy <-j pathToJar> <-c packagePath,classPath>'
)

import org.apache.commons.cli.Option

import java.beans.BeanInfo
import java.beans.Introspector
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.nio.file.FileSystems

cli.with {
  h(longOpt: 'help', 'Usage information', required: false)
  j(longOpt: 'jar', 'source jar file full path', args: 1, required: true)
  b(longOpt: 'base', 'target class package base', args: 1, required: false)
  t(longOpt: 'targetPath', 'target root folder write generated files to', args: 1, required: true)
  c(longOpt: 'classes', 'package path or full path class name to generate', args: Option.UNLIMITED_VALUES, valueSeparator: ",", required: true)
  //this parameter accept multi value separate by comma
}

def opt = cli.parse(args)

if (!opt) return

//opt.c will read first parameter only. to read all parameters as array, use opt.cs format. 's' indicate multi values
def classes = opt.cs

def jarPath = opt.j


this.class.classLoader.rootLoader.addURL(new URL("file://${jarPath}"))

assert classes.size() > 0, "At least one full path class name or package name should be specified."

Class clazz = Class.forName(classes[0]);


File basePath = makeTargetPath(opt.t, opt.b)

def root = processClass(clazz, opt.b)

File outFile = new File(basePath, "${clazz.simpleName}.java")

if (outFile.exists()) {
  //TODO promote if overwrite exist file
} else {
  outFile.createNewFile()
}
writeToOutput(root, new OutputStreamWriter(new FileOutputStream(outFile)))


def processClass(Class clazz, p) {
  def getters = []
  def imports = [] as HashSet
  def currentFieldName = "refVal"
  Map retVal = [package          : p, className: genTargetClassName(clazz), sourceClass: clazz.name, getters: getters,
                privateWrapFields: [[type: clazz.name, name: currentFieldName]], imports: imports]

  BeanInfo beanInfo = Introspector.getBeanInfo(clazz)
  beanInfo.methodDescriptors.each { md ->
    Method method = md.method




    if ((method.name.startsWith("get") && method.name != 'getClass') || method.name.startsWith("is")) {
      Class retType = method.returnType;
      Map methodAnalystResult = genMethodRetTypeInStr(clazz, method.name, p)
      imports.addAll(methodAnalystResult.imports)

      def getter = [retType: methodAnalystResult.retType, methodName: method.name]
      if (!retType.name.startsWith(clazz.package.name)) {
        getter.put('retInstance', currentFieldName)
        getter.put('retMethod', method.name)
      }
      getters.push(getter)
    }
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
  if (isListType(field)) {
    ParameterizedType type = (ParameterizedType) field.getGenericType()
    //actualTypeArguments list all generic type parameters
    return [retType: "${field.type.simpleName}<${getTypeSimpleName(type.actualTypeArguments[0])}>", imports: [fieldTypeLong]]
  } else if (fieldTypeLong.startsWith("java.lang") || fieldTypeLong.startsWith(currentPackage)) {
    return [retType: field.type.simpleName, imports: []]
  }

}

def getTypeSimpleName(Type type) {
  //the type.toString suppose return 'class java.lang.String'
  String s = type.toString().split(' ')[1]
  s.substring(s.lastIndexOf(".") + 1)
}

def isListType(Field field) {
  //check if is subClass of
  return List.class.isAssignableFrom(field.getType())
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
