package org.silknow.converter;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.NotNull;
import org.silknow.converter.commons.Converter;
import org.silknow.converter.imatex.ImatexConverter;
import org.silknow.converter.ontologies.CIDOC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Main implements Runnable {

  private Logger logger;

  enum Type {imatex}

  //  @Parameters(index = "0", paramLabel = "TYPE", description = "Type of source data: ${COMPLETION-CANDIDATES}")
  // private Type type;
  private final Type type = Type.imatex;


  //  @Parameters(index = "1", paramLabel = "FOLDER", description = "Source folder to process")
//  private File folder;
  private final File folder = new File("../crawler/data/imatex/records/3008_en.json");

  @Option(names = {"--log"}, description = "The log level. Default: ${DEFAULT-VALUE}", completionCandidates =
          LogLevels.class, defaultValue = "DEBUG")
  private String logLevel;

  @Option(names = {"-o", "--output"}, description = "Output folder. Default: an `out` folder siblings to the input directory")
  private File outputFolder;

  public static void main(String[] args) {
    CommandLine.run(new Main(), args);
  }

  @Override
  public void run() {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, this.logLevel);
    this.logger = LoggerFactory.getLogger(getClass());

    if (!folder.exists())
      throw new IllegalArgumentException("The FOLDER specified in parameters does not exists.");

    if (this.outputFolder == null)
      outputFolder = Paths.get(folder.getParentFile().getAbsolutePath(), "out").toFile();

    //noinspection ResultOfMethodCallIgnored
    outputFolder.mkdirs();
    try {
      logger.info("Output folder: " + outputFolder.getCanonicalPath());
    } catch (IOException e) {
      e.printStackTrace();
    }

    Converter converter = null;
    switch (type) {
      case imatex:
        converter = new ImatexConverter();
    }

    if (folder.isDirectory()) convertFolder(folder, converter);
    else if (folder.isFile()) convertFile(folder, converter);
  }

  private void convertFile(File file, @NotNull Converter converter) {
    Model m = converter.convert(file);
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
  }


  private void writeTtl(@NotNull Model m, String filename) throws IOException {
    m.setNsPrefix("ecrm", CIDOC.getURI());
    m.setNsPrefix("dc", DC.getURI());
    m.setNsPrefix("rdfs", RDFS.getURI());
//    m.setNsPrefix("xsd", XSD.getURI());
//    m.setNsPrefix("dcterms", DCTerms.getURI());
//    m.setNsPrefix("owl", OWL.getURI());
//    m.setNsPrefix("foaf", FOAF.getURI());
//    m.setNsPrefix("prov", PROV.getURI());
//    m.setNsPrefix("time", Time.getURI());
//    m.setNsPrefix("schema", Schema.getURI());


    File out = Paths.get(outputFolder.getAbsolutePath(), filename).toFile();
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
  public static String changeExtension(@NotNull String f, String newExtension) {
    int i = f.lastIndexOf('.');
    String name = f.substring(0, i);
    return name + newExtension;
  }


}
