package org.silknow.converter.converters;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class GarinRecord {
  private static final String CONJUNCTION_ES = "(, ?| y )";

  private final Map<String, String> map;

  private GarinRecord() {
    this.map = new HashMap<>();
  }

  public String get(String key) {
    return this.map.get(key);
  }

  public Stream<String> getMulti(String key) {
    String res = this.get(key);
    if (res == null) return Stream.empty();
    else return Arrays.stream(res.split(CONJUNCTION_ES));
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
      if (key.isBlank()) continue;

      List<String> values = new ArrayList<>();

      while (cellIterator.hasNext()) {
        cell = cellIterator.next();
        values.add(cell.toString());
      }

      String value = values.stream()
              .filter(x -> !x.isBlank())
              .findFirst().orElse(null);

      if (value == null) continue; // Main section

      record.add(key, value);
    }

    workbook.close();
    fis.close();
    return record;
  }

}
