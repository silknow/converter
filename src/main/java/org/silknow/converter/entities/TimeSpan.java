package org.silknow.converter.entities;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.NotNull;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeSpan extends Entity {
  private static final TimeZone UCT = TimeZone.getTimeZone("UTC");
  private static final String UCT_DATE_REGEX = "\\d{4}(?:-(?:0[1-9]|1[0-2])(?:-(?:0[1-9]|[1-2]\\d|3[0-1]))?)?(?:T" +
          "(?:[0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\dZ?)?";

  public static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  static final DateFormat SLASH_LITTLE_ENDIAN = new SimpleDateFormat("dd/MM/yyyy");

  public TimeSpan() {
    super();

    createResource();
    this.setClass(CIDOC.E52_Time_Span);
  }

  public TimeSpan(Date date) {
    this();
    ISO_DATE_FORMAT.setTimeZone(UCT);


    String text = ISO_DATE_FORMAT.format(date).substring(0, 10);
    Literal literal = model.createTypedLiteral(text, XSDDatatype.XSDdate);
    Resource instant = model.createResource().addProperty(RDF.type, Time.Instant)
            .addProperty(Time.inXSDDate, literal);

    this.addProperty(RDFS.label, text);
    this.addProperty(Time.hasBeginning, instant)
            .addProperty(Time.hasEnd, instant);
  }

  public void addAppellation(String timeAppellation) {
    this.addProperty(RDFS.label, timeAppellation)
            .addProperty(CIDOC.P78_is_identified_by, timeAppellation);
  }

  static Date dateFromString(String value, @NotNull DateFormat format) throws ParseException {
    format.setTimeZone(UCT);
    return format.parse(value);
  }

}
