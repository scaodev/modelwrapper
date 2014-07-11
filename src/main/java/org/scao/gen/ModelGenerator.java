package org.scao.gen;

import freemarker.template.*;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Date: 7/10/14
 * Time: 7:41 PM
 */
public class ModelGenerator {
  private final File targetPath;
  private final Properties packageMapping;
  private final String jarFilePath;
  private final String templatePath;
  private final Map<String, ClassDefinition> classNames = new HashMap<>();
  private final Predicate<? super Optional<Class>> regularClassOnly =
    (opt) -> opt.isPresent() ? !opt.get().isEnum() && opt.get().getEnclosingClass() == null : true;


  public void writeToFile(ClassDefinition classDefinition,
    Function<Writer, Optional<Exception>> cloure)
    throws IOException {
    File outFile = createTargetClassFile(classDefinition.getTargetPackage(),
      classDefinition.getTargetClassName());
    FileWriter writer = new FileWriter(outFile);
    cloure.apply(writer).ifPresent(e -> e.printStackTrace());
    writer.flush();
    writer.close();
  }

  public File createTargetClassFile(String packagePath, String className) {
    File targetFolder = new File(targetPath, packagePath.replace(".", "/"));
    if (!targetFolder.exists()) {
      targetFolder.mkdirs();
    }
    return new File(targetFolder, className + ".java");
  }

  public Function<Writer, Optional<Exception>> createWriter(final ClassDefinition clazz,
    String templateName)
    throws IOException {
    return (final Writer out) -> {
      try {
        Configuration cfg = new Configuration();
        // Specify the data source where the template files come from. Here I set a
        // plain directory for it, but non-file-system are possible too:
        cfg.setDirectoryForTemplateLoading(new File(templatePath));

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

        Template template = cfg.getTemplate(templateName);
        template.process(clazz, out);
      } catch (TemplateException | IOException e) {
        e.printStackTrace();
        return Optional.of(e);
      }
      return Optional.empty();
    };
  }

  public ModelGenerator(File targetPath, Properties packageMapping, String jarFilePath,
    String templatePath) {
    this.targetPath = targetPath;
    this.packageMapping = packageMapping;
    this.jarFilePath = jarFilePath;
    this.templatePath = templatePath;
  }

  public void generate() throws Exception {
    emptyTree();
    URLClassLoader classLoader =
      new URLClassLoader(new URL[] {new URL("file://" + jarFilePath)},
        this.getClass().getClassLoader());
    JarFile jarFile = new JarFile(jarFilePath);
    List<ClassDefinition> definitions = jarFile.stream()
      .filter((final JarEntry entry) -> entry.getName().endsWith(".class"))
      .map(this::jarEntry2Class)
      .filter(clazz -> !clazz.isEnum())
      .filter(clazz -> clazz.getEnclosingClass() == null)
      .map(clazz -> {
        ClassDefinition cd = new ClassDefinition(packageMapping);
        cd.setClass(clazz);
        return cd;
      }).collect(Collectors.toList());
    definitions.forEach(definition -> {
      try {
        writeToFile(definition, createWriter(definition, "pojov8.ftl"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }



  public static void main(String args[]) throws Exception {
    Builder b = new Builder();
    ModelGenerator generator =
      b.setPackageMapping("/Users/scao/projects/model-wrapper/src/pojo.properties")
        .setTargetPath("/Users/scao/projects/verify/src")
        .setJarFile(
          "/Users/scao/projects/royal/isobar/schema/target/services-schema-definitions-0.0.1-SNAPSHOT.jar")
        .setTemplatePath("/Users/scao/projects/model-wrapper/src/resources")
        .build();

    generator.generate();
  }



  private Consumer<Class> getEnclosingClass =
    (final Class clazz) -> System.out.println(clazz.getEnclosingClass());

  private Consumer<Class> printIfIsEnum = (final Class clazz) -> {
    if (clazz.isEnum()) {
      System.out.println(clazz.getName());
    }
  };

  private final Function<JarEntry, Optional<ClassDefinition>> loadClass() {
    return (final JarEntry entry) -> {
      try {
        ClassDefinition cd = new ClassDefinition(packageMapping);
        cd.setClass(Class.forName(jarEntry2SourceClassName(entry)));
        return Optional.of(cd);
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
        return Optional.empty();
      }
    };
  }


  private String jarEntry2SourceClassName(JarEntry entry) {
    String name = entry.getName();
    return name.substring(0, name.lastIndexOf(".")).replace("/", ".");
  }

  private Class jarEntry2Class(JarEntry entry) {
    try {
      return Class.forName(jarEntry2SourceClassName(entry));
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private void emptyTree() throws IOException {
    Path directory = Paths.get(targetPath.getAbsolutePath());
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
        throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc)
        throws IOException {
        if (!dir.toString().equals(targetPath.getAbsolutePath())) {
          Files.delete(dir);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }



  public static class Builder {
    private File targetPath;
    private Properties packageMapping;
    private String jarFile;
    private String templatePath;

    public Builder setTargetPath(File targetPath) {
      this.targetPath = targetPath;
      return this;
    }

    public Builder setTargetPath(String fullPath) {
      targetPath = new File(fullPath);
      return this;
    }


    public Builder setTemplatePath(String templatePath) {
      this.templatePath = templatePath;
      return this;
    }

    public Builder setJarFile(String jarFilePath) throws IOException {
      this.jarFile = jarFilePath;
      return this;
    }

    public Builder setPackageMapping(String mappingFilePath) throws IOException {
      Properties p = new Properties();
      p.load(new FileInputStream(new File(mappingFilePath)));
      this.packageMapping = p;
      return this;
    }

    public ModelGenerator build() {
      return new ModelGenerator(targetPath, packageMapping, jarFile, templatePath);
    }
  }
}
