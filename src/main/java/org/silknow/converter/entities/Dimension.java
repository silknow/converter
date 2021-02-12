package org.silknow.converter.entities;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.vocabulary.RDFS;
import org.silknow.converter.ontologies.CIDOC;

import java.util.HashMap;
import java.util.Map;


public class Dimension extends Entity {
  private static final Map<String, Double> CONVERSION_RATE;

  static {
    Map<String, Double> map = new HashMap<>();
    // length => cm
    map.put("m", 100.0);
    map.put("mm", 0.1);
    map.put("ft", 30.48);
    map.put("in", 2.54);
    // weight => kg
    map.put("g", 0.001);
    map.put("troy", 0.311034768);
    map.put("lb", 0.453592);

    CONVERSION_RATE = map;
  }

  private final String unit;

  public Dimension(String uri, String value, String unit) {
    super();
    this.setUri(uri);

    this.unit = getStandardUnit(unit);
    this.setClass(CIDOC.E54_Dimension);
    this.addProperty(CIDOC.P90_has_value, convertInStandard(value, unit), XSDDatatype.XSDfloat);
    this.addProperty(CIDOC.P91_has_unit, this.unit);
  }

  public Dimension(String uri, String value, String unit, String type, String label) {
    this(uri, value, unit);
    this.addProperty(CIDOC.P2_has_type, type);
    this.addProperty(RDFS.label, label);
  }


  private String convertInStandard(String value, String unit) {
    if (StringUtils.isBlank(value) || !CONVERSION_RATE.containsKey(unit)) return value;

    float v = Float.parseFloat(value);
    return String.valueOf(v * CONVERSION_RATE.get(unit));
  }

  private String getStandardUnit(String unit) {
    switch (unit) {
      case "ft":
      case "in":
      case "mm":
      case "cm":
      case "m":
        return "cm";
      case "troy":
      case "kg":
      case "lb":
      case "g":
        return "kg";
      case "cl":
      default:
        return unit;
    }
  }


  public String getUnit() {
    return this.unit;
  }
}
