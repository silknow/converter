package org.silknow.converter;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.doremus.string2vocabulary.VocabularyManager;
import org.jetbrains.annotations.NotNull;
import org.silknow.converter.commons.GeoNames;
import org.silknow.converter.converters.Converter;
import org.silknow.converter.converters.GarinConverter;
import org.silknow.converter.converters.ImatexConverter;
import org.silknow.converter.converters.JocondeConverter;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.CRMdig;
import org.silknow.converter.ontologies.Schema;
import org.silknow.converter.ontologies.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@Command(mixinStandardHelpOptions = true)
public class Main implements Runnable {
  public static String source;

  enum Type {imatex, garin, joconde}

  @Parameters(index = "0", paramLabel = "TYPE", description = "Type of source data: ${COMPLETION-CANDIDATES}")
  private Type type;

  @Parameters(index = "1", paramLabel = "FOLDER", description = "Source folder to process")
  private File folder;

  @Option(names = {"--log"}, description = "The log level. Default: ${DEFAULT-VALUE}", completionCandidates =
          LogLevels.class, defaultValue = "WARN")
  private String logLevel;

  @Option(names = {"-o", "--output"}, description = "Output folder. Default: an `out` folder siblings to the input directory")
  public static File outputFolder;

  @Option(names = {"-g", "--geonames"}, required = true,
          description = "Username for accessing Geonames. See http://www.geonames.org/login")
  public static String geonamesUser;

  public static void main(String[] args) {
    CommandLine.run(new Main(), args);
  }

  @Override
  public void run() {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, this.logLevel);
    Logger logger = LoggerFactory.getLogger(getClass());

    if (!folder.exists())
      throw new IllegalArgumentException("The FOLDER specified in parameters does not exists.");

    ClassLoader classLoader = Converter.class.getClassLoader();
    URL vocabularyFolder = classLoader.getResource("vocabulary");
    URL p2fTable = classLoader.getResource("property2family.csv");
    assert vocabularyFolder != null && p2fTable != null;
    VocabularyManager.setVocabularyFolder(vocabularyFolder.getPath());
    try {
      VocabularyManager.init(p2fTable);
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (outputFolder == null)
      outputFolder = Paths.get(folder.getParentFile().getAbsolutePath(), "out").toFile();

    GeoNames.setUser(geonamesUser);
    GeoNames.setDestFolder(Paths.get(outputFolder.getPath(), "geonames").toFile());
    GeoNames.loadCache();

    //noinspection ResultOfMethodCallIgnored
    outputFolder.mkdirs();
    Paths.get(Main.outputFolder + "/img/").toFile().mkdirs();

    try {
      logger.info("Output folder: " + outputFolder.getCanonicalPath());
    } catch (IOException e) {
      e.printStackTrace();
    }

    Converter converter = null;
    switch (type) {
      case imatex:
        converter = new ImatexConverter();
        break;
      case joconde:
        converter = new JocondeConverter();
        break;
      case garin:
        converter = new GarinConverter();
    }

    source = type.toString();

    if (folder.isDirectory()) convertFolder(folder, converter);
    else if (folder.isFile()) convertFile(folder, converter);
  }

  private void convertFile(@NotNull File file, @NotNull Converter converter) {
    System.out.println(file.getName());
    Model m = converter.convert(file);
    VocabularyManager.string2uri(m);
    if (m == null) return;
    String outName = changeExtension(file.getName(), ".ttl");

    try {
      writeTtl(m, outName);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void convertFolder(@NotNull File folder, @NotNull Converter converter) {
    File[] files = Objects.requireNonNull(folder.listFiles());
    Arrays.stream(files)
            .filter(converter::canConvert)
            .sorted()
            .forEach(x -> convertFile(x, converter));

    Arrays.stream(files)
            .filter(File::isDirectory)
            .sorted()
            .peek(x -> System.out.println("--- " + x.getName()))
            .forEach(x -> convertFolder(x, converter));
  }

  private void writeTtl(@NotNull Model m, String filename) throws IOException {
    m.setNsPrefix("ecrm", CIDOC.getURI());
    m.setNsPrefix("crmdig", CRMdig.getURI());
    m.setNsPrefix("dc", DC.getURI());
    m.setNsPrefix("rdfs", RDFS.getURI());
    m.setNsPrefix("xsd", XSD.getURI());
    m.setNsPrefix("time", Time.getURI());
    m.setNsPrefix("schema", Schema.getURI());
//    m.setNsPrefix("dcterms", DCTerms.getURI());
//    m.setNsPrefix("owl", OWL.getURI());
//    m.setNsPrefix("foaf", FOAF.getURI());
//    m.setNsPrefix("prov", PROV.getURI());


    File out = Paths.get(outputFolder.getAbsolutePath(), filename).toFile();
    if (out.exists()) {
      System.out.println("DUPLICATE ID: " + filename.replace(".ttl", ""));
      return;
    }
    FileWriter fw = new FileWriter(out);

    // Write the output file
    // m.write(System.out, "TURTLE");
    m.write(fw, "TURTLE");
    fw.close();
  }

  static class LogLevels extends ArrayList<String> {
    LogLevels() {
      super(Arrays.asList("ERROR", "WARN", "INFO", "DEBUG", "TRACE"));
    }
  }

  @NotNull
  private static String changeExtension(@NotNull String f, @SuppressWarnings("SameParameterValue") String newExtension) {
    int i = f.lastIndexOf('.');
    String name = f.substring(0, i);
    return name + newExtension;
  }


}
