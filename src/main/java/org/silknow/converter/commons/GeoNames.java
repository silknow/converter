package org.silknow.converter.commons;

import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GeoNames {
  public static final String NAME = "http://www.geonames.org/ontology#name";
  static Map<String, Integer> cache; // cache idItema3 -> idGeoNames
  public static String destFolder;

  public static void setDestFolder(File folder) {
    folder.mkdirs();
    destFolder = folder.getPath();
  }

  public static void setUser(String user) {
    WebService.setUserName(user); // add your username here
  }

  public static String toURI(int id) {
    return "https://sws.geonames.org/" + id + "/";
  }

  public static void downloadRdf(int id) {
    String uri = toURI(id) + "about.rdf";
    String outPath = Paths.get(destFolder, id + ".rdf").toString();
    if (new File(outPath).exists()) return; // it is already there!
    try {
      URL website = new URL(uri);
      ReadableByteChannel rbc = Channels.newChannel(website.openStream());
      FileOutputStream fos = new FileOutputStream(outPath);
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Toponym query(String label) {
    return query(label, null, null);
  }

  public static Toponym query(String label, String featureCode, String continent) {
    label = label.trim();
    Toponym tp = null;

    if (cache.containsKey(label)) {
      int k = cache.get(label);
      if (k != -1) {
        tp = new Toponym();
        tp.setGeoNameId(k);
      }
      return tp;
    }

    ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
    searchCriteria.setName(label);
    searchCriteria.setMaxRows(1);
    if (featureCode != null) searchCriteria.setFeatureCode(featureCode);
    if (continent != null) searchCriteria.setContinentCode(continent);

    try {
      ToponymSearchResult searchResult = WebService.search(searchCriteria);
      if (searchResult.getToponyms().size() > 0)
        tp = searchResult.getToponyms().get(0);

      addToCache(label, tp != null ? tp.getGeoNameId() : -1);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return tp;
  }

  public static void loadCache() {
    cache = new HashMap<>();
    try {
      FileInputStream fis = new FileInputStream("places.properties");
      Properties properties = new Properties();
      properties.load(fis);

      for (String key : properties.stringPropertyNames()) {
        cache.put(key, Integer.parseInt(properties.get(key).toString()));
      }
    } catch (IOException e) {
      System.out.println("No 'places.properties' file found. I will create it.");
    }
  }

  private static void addToCache(String key, int value) {
    cache.put(key, value);
    saveCache();
  }

  static void removeFromCache(String key) {
    cache.remove(key);
    saveCache();
  }

  private static void saveCache() {
    Properties properties = new SortedProperties();

    cache.keySet().forEach(key -> properties.put(key, cache.get(key) + ""));

    try {
      properties.store(new FileOutputStream("places.properties"), null);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
