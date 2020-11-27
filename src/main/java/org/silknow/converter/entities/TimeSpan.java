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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeSpan extends Entity {
  private static final TimeZone UCT = TimeZone.getTimeZone("UTC");
  private static final String UCT_DATE_REGEX = "\\d{4}(?:-(?:0[1-9]|1[0-2])(?:-(?:0[1-9]|[1-2]\\d|3[0-1]))?)?(?:T" +
          "(?:[0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\dZ?)?";
  private static final String SINGLE_YEAR = "\\d{4}";
  private static final String YEAR_SPAN = "(\\d{4})[-–=](\\d{4})";
  private static final Pattern SPAN_PATTERN = Pattern.compile(YEAR_SPAN);


  public static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  static final DateFormat SLASH_LITTLE_ENDIAN = new SimpleDateFormat("dd/MM/yyyy");

  private String start_year;
  private String end_year;


  public TimeSpan() {
    super();

    createResource();
    this.setClass(CIDOC.E52_Time_Span);
  }


  public TimeSpan(String date) {
    super();

    createResource();
    this.setClass(CIDOC.E52_Time_Span);


    if (date == null) return;

    //ISO_DATE_FORMAT.setTimeZone(UCT);
    //String text = ISO_DATE_FORMAT.format(date).substring(0, 10);
    //Literal literal = model.createTypedLiteral(text, XSDDatatype.XSDdate);
    //Resource instant = model.createResource().addProperty(RDF.type, Time.Instant)
            //.addProperty(Time.inXSDDate, literal);


    if (date.matches(SINGLE_YEAR)) {
      this.start_year = date;
      this.end_year = date;
    }


    if
      (date.matches(YEAR_SPAN)) {
      Matcher matcher = SPAN_PATTERN.matcher(date);
      if (matcher.find())
        this.start_year = matcher.group(1);
        this.end_year = matcher.group(2);
    }



    this.addProperty(RDFS.label, date)
        .addProperty(CIDOC.P78_is_identified_by, date);

    if (this.start_year != null) {
        this.addProperty(CIDOC.P79_beginning_is_qualified_by, this.start_year)
              .addProperty(CIDOC.P80_end_is_qualified_by, this.end_year);
    }
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
