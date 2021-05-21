package org.silknow.converter;


import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;
import org.doremus.string2vocabulary.VocabularyManager;
import org.jetbrains.annotations.NotNull;
import org.silknow.converter.commons.GeoNames;
import org.silknow.converter.commons.ISNIWrapper;
import org.silknow.converter.converters.*;
import org.silknow.converter.entities.TimeSpan;
import org.silknow.converter.ontologies.*;
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

  enum Type {imatex, garin, joconde, europeana, MET, MFA, mobil, RISD, gallica, MAD, CER, MTMAD, PM, smiths, versailles, VAM, venezia, artic, UNIPA, ElTesoro}

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

  @Option(names = {"--replace"},
    description = "Replace the content of the destination folder")
  public static boolean replace;

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

    outputFolder.mkdirs();

    GeoNames.setUser(geonamesUser);
    GeoNames.setDestFolder(Paths.get(outputFolder.getPath(), "geonames").toFile());
    GeoNames.loadCache();

    File isniFolder = Paths.get(outputFolder.getParentFile().getAbsolutePath(), "isni").toFile();
    isniFolder.mkdirs();
    ISNIWrapper.init(isniFolder.getAbsolutePath());

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
        break;
      case MET:
        converter = new METConverter();
        break;
      case MFA:
        converter = new MFAConverter();
        break;
      case ElTesoro:
        converter = new ElTesoroConverter();
        break;
      case europeana:
        converter = new EuropeanaConverter();
        break;
      case RISD:
        converter = new RISDConverter();
        break;
      case MAD:
        converter = new MADConverter();
        break;
      case CER:
        converter = new CERConverter();
        break;
      case MTMAD:
        converter = new MTMADConverter();
        break;
      case gallica:
        converter = new GallicaConverter();
        break;
      case VAM:
        converter = new VAMConverter();
        break;
      case venezia:
        converter = new VeneziaConverter();
        break;
      case smiths:
        converter = new SmithsConverter();
        break;
      case PM:
        converter = new PMConverter();
        break;
      case mobil:
        converter = new MobilierConverter();
        break;
      case versailles:
        converter = new VersaillesConverter();
        break;
      case artic:
        converter = new ArticConverter();
        break;
      case UNIPA:
        converter = new UNIPAConverter();
    }

    source = type.toString();

    if (folder.isDirectory()) convertFolder(folder, converter);
    else if (folder.isFile()) convertFile(folder, converter);

    Paths.get(outputFolder.getAbsolutePath() + "/timespan/").toFile().mkdirs();
    File timespanOut = Paths.get(outputFolder.getAbsolutePath(), "/timespan/" + source + "timespans.ttl").toFile();
    try {
      writeTtl(TimeSpan.centralModel, timespanOut);
    } catch (IOException e) {
      e.printStackTrace();
    }

    File activitiesOut = Paths.get(outputFolder.getAbsolutePath(), "activities_"+source+".txt").toFile();
    try {
      FileWriter writer = new FileWriter(activitiesOut);
      for (String act : TimeSpan.activityList) {
        writer.write(act + System.lineSeparator());
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void convertFile(@NotNull File file, @NotNull Converter converter) {
    String outName = changeExtension(file.getName(), ".ttl");
    File out = Paths.get(outputFolder.getAbsolutePath(), outName).toFile();
    if (out.exists() && !replace) {
      System.out.println("DUPLICATE ID: " + outName.replace(".ttl", ""));
      return;
    }

    //System.out.println(file.getName());
    converter.resetModel();
    Model m = converter.convert(file);
    if (m == null) {
      System.out.println("Conversion failed:" + file.getName());
      return;
    }

    try {
      writeTtl(m, out);
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
      //.peek(x -> System.out.println("--- " + x.getName()))
      .forEach(x -> convertFolder(x, converter));
  }

  private static void writeTtl(@NotNull Model m, File out) throws IOException {
    m.setNsPrefix("ecrm", CIDOC.getURI());
    m.setNsPrefix("silk", Silknow.getURI());
    m.setNsPrefix("crmdig", CRMdig.getURI());
    m.setNsPrefix("crmsci", CRMsci.getURI());
    m.setNsPrefix("dc", DC.getURI());
    m.setNsPrefix("rdfs", RDFS.getURI());
    m.setNsPrefix("xsd", XSD.getURI());
    m.setNsPrefix("time", Time.getURI());
    m.setNsPrefix("schema", Schema.getURI());
    m.setNsPrefix("skos", SKOS.getURI());
//    m.setNsPrefix("dcterms", DCTerms.getURI());
//    m.setNsPrefix("owl", OWL.getURI());
//    m.setNsPrefix("foaf", FOAF.getURI());
//    m.setNsPrefix("prov", PROV.getURI());


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
