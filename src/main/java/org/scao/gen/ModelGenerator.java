package org.scao.gen;

import freemarker.template.*;

import javax.swing.text.html.Option;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Date: 7/10/14
 * Time: 7:41 PM
 */
public class ModelGenerator {
  private final File targetPath;
  private final Properties packageMapping;
  private final String jarFilePath;
  private final String templatePath;
  private final Set<String> classNames = new HashSet<String>();


  public void writeToFile(String filePath, Function<Writer, Optional<Exception>> cloure)
    throws IOException {
    File outFile = new File(filePath);
    if (!outFile.exists()) {
      outFile.createNewFile();
    }
    FileWriter writer = new FileWriter(outFile);
    cloure.apply(writer).ifPresent(e -> e.printStackTrace());
    writer.close();
  }

  public Function<Writer, Optional<Exception>> createWriter(final ClassDefinition clazz, String templateName)
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
        return Optional.of(e);
      }
      return Optional.empty();
    };
  }

  public ModelGenerator(File targetPath, Properties packageMapping, String jarFile,
    String templatePath) {
    this.targetPath = targetPath;
    this.packageMapping = packageMapping;
    this.jarFilePath = jarFile;
    this.templatePath = templatePath;
  }

  public void generate() throws Exception{
    emptyTree();
    gatherClassesInfoFromJar();
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

  private List<ClassDefinition> gatherClassesInfoFromJar() throws Exception {
    URLClassLoader classLoader =
      new URLClassLoader(new URL[]{new URL("file://" + jarFilePath)}, this.getClass().getClassLoader());
    JarFile jarFile = new JarFile(jarFilePath);
    jarFile.stream().filter((final JarEntry entry) -> entry.getName().endsWith(".class"))
      .map(loadClass)
      .forEach((final Optional<Class> clazz) -> {
      clazz.ifPresent(c -> System.out.println(c.getPackage().getName()));
    });
    return null;
  }

  private Function<JarEntry, Optional<Class>> loadClass =
    (final JarEntry entry) -> {
      try {
        return Optional.of(Class.forName(jarEntry2ClassName(entry)));
      }catch (ClassNotFoundException e) {
        e.printStackTrace();
        return Optional.empty();
      }
    };

  private String jarEntry2ClassName(JarEntry entry) {
    String name = entry.getName();
    return name.substring(0, name.lastIndexOf(".")).replace("/", ".");
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
