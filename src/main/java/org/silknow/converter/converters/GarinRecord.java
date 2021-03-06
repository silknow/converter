package org.silknow.converter.converters;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class GarinRecord {
  private static final String CONJUNCTION_ES = "(, ?| y )";

  private final Map<String, String> map;
  private final List<byte[]> images;

  private GarinRecord() {
    this.map = new HashMap<>();
    this.images = new ArrayList<>();
  }

  public String get(String key) {
    return this.map.get(key);
  }

  public Stream<String> getMulti(String key) {
    String res = this.get(key);
    if (res == null) return Stream.empty();
    else return Arrays.stream(res.split(CONJUNCTION_ES));
  }

  public List<byte[]> getImages() {
    return this.images;
  }


  private void add(String key, String value) {
    key = key.trim()
            .replaceFirst("^\\d\\.\\d+[ .]+", "")
            .replaceFirst(":$", "")
            .trim();
    value = value.trim();
    this.map.put(key, value);
  }


  public static GarinRecord from(File file) throws IOException {
    FileInputStream fis = new FileInputStream(file);
    Workbook workbook = new HSSFWorkbook(fis);

    GarinRecord record = new GarinRecord();

    // get the first sheet
    Sheet sheet = workbook.getSheetAt(0);

    // iterate on rows
    Iterator<Row> rowIt = sheet.iterator();

    while (rowIt.hasNext()) {
      Row row = rowIt.next();

      // iterate on cells for the current row
      Iterator<Cell> cellIterator = row.cellIterator();
      if (!cellIterator.hasNext()) continue;

      Cell cell = cellIterator.next();
      String key = cell.toString();
      if (StringUtils.isBlank(key)) continue;

      List<String> values = new ArrayList<>();

      while (cellIterator.hasNext()) {
        cell = cellIterator.next();
        values.add(cell.toString());
      }

      String value = values.stream()
              .filter(x -> !StringUtils.isBlank(x))
              .findFirst().orElse(null);

      if (value == null) continue; // Main section

      record.add(key, value);
    }

    // images
    List lst = workbook.getAllPictures();
    for (Iterator it = lst.iterator(); it.hasNext(); ) {
      PictureData pict = (PictureData) it.next();
      String ext = pict.suggestFileExtension();
      if (ext.equals("jpeg"))
        record.images.add(pict.getData());
    }

    workbook.close();
    fis.close();
    return record;
  }

}
