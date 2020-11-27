package org.silknow.converter.entities;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.string2vocabulary.VocabularyManager;
import org.jetbrains.annotations.NotNull;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static org.apache.jena.vocabulary.XSD.gYear;
import static org.apache.jena.vocabulary.XSD.time;
import static org.silknow.converter.ontologies.Time.hasBeginning;
import static org.silknow.converter.ontologies.Time.inXSDDate;

public class TimeSpan extends Entity {
  private static final TimeZone UCT = TimeZone.getTimeZone("UTC");
  private static final String UCT_DATE_REGEX = "\\d{4}(?:-(?:0[1-9]|1[0-2])(?:-(?:0[1-9]|[1-2]\\d|3[0-1]))?)?(?:T" +
          "(?:[0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\dZ?)?";
  private static final String SINGLE_YEAR = "\\d{4}";
  private static final String YEAR_SPAN = "(\\d{4})[-–=](\\d{4})";
  private static final Pattern SPAN_PATTERN = Pattern.compile(YEAR_SPAN);
  private int tsCount;



  public static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  static final DateFormat SLASH_LITTLE_ENDIAN = new SimpleDateFormat("dd/MM/yyyy");


  private String startYear, startMonth, startDay;
  private String endYear, endMonth, endDay;
  private String startDate, endDate;
  private String startTime;
  private XSDDatatype startType, endType;
  private String label;
  private boolean endRequired = true;
  private int startCentury;
  private int endCentury;



  public TimeSpan() {
    super();

    createResource();
    this.setClass(CIDOC.E52_Time_Span);
  }


  public TimeSpan(String date) {
    super();

    tsCount = 0;
    this.setUri(this.getUri() + ++tsCount);


    createResource();
    this.setClass(CIDOC.E52_Time_Span);

    /*
    date = date.replaceAll("\\s+", " ");
    date = date.replace("?", "");
    date = date.replace("ca.", "");
    date = date.replace("circa", "");
    date = date.replace("early", "");
    date = date.replace("late", "");
    date = date.replace("mid-", "");
    date = date.replace("Finales ", "");
    date = date.replace("Primer tercio del ", "");
    date = date.replace("Principios ", "");
    date = date.replace("Primer tercio ", "");
    date = date.replace("about ", "");
    date = date.replaceAll("\\((.+)\\)", "");
    date = date.trim();
    */


    if (date == null) return;

    //ISO_DATE_FORMAT.setTimeZone(UCT);
    //String text = ISO_DATE_FORMAT.format(date).substring(0, 10);
    //Literal literal = model.createTypedLiteral(text, XSDDatatype.XSDdate);
    //Resource instant = model.createResource().addProperty(RDF.type, Time.Instant)
    //.addProperty(Time.inXSDDate, literal);


    if (date.matches(SINGLE_YEAR)) {
      this.startYear = date;
      this.endYear = date;
    }


    if
    (date.matches(YEAR_SPAN)) {
      Matcher matcher = SPAN_PATTERN.matcher(date);
      if (matcher.find())
        this.startYear = matcher.group(1);
      this.endYear = matcher.group(2);
    }


    this.addProperty(RDFS.label, date)
            .addProperty(CIDOC.P78_is_identified_by, date);

    Resource result = VocabularyManager.searchInCategory(date, null, "dates", false);
    if (result != null) {
      this.addProperty(CIDOC.P78_is_identified_by, result);
    }

    if (this.startYear != null) {
    startType = XSDDateType.XSDgYear;
    startDate = startYear;
    startCentury = ((parseInt(startYear) + 99) / 100);

      endType = XSDDateType.XSDgYear;
      endDate = endYear;
      endCentury = ((parseInt(endYear) + 99) / 100);

      Map<String, String> map = new HashMap<>();
      map.put("15", "http://vocab.getty.edu/aat/300404465");
      map.put("16", "http://vocab.getty.edu/aat/300404510");
      map.put("17", "http://vocab.getty.edu/aat/300404511");
      map.put("18", "http://vocab.getty.edu/aat/300404512");
      map.put("19", "http://vocab.getty.edu/aat/300404513");
      map.put("20", "http://vocab.getty.edu/aat/300404514");

      map.forEach((k,v) -> {
        if (parseInt(k) == startCentury) {
          this.addProperty(CIDOC.P78_is_identified_by, v);
        }
        if (parseInt(k) == endCentury) {
          this.addProperty(CIDOC.P78_is_identified_by, v);
        }});

        ;



    Resource startInstant = makeInstant(startDate, startType);
    Resource endInstant = makeInstant(endDate, endType);


    if (startInstant != null) {
      startInstant = ResourceUtils.renameResource(startInstant, this.getUri() + "/start");
      this.resource.addProperty(Time.hasBeginning, startInstant);
    }
    if (endInstant != null) {
      endInstant = ResourceUtils.renameResource(endInstant, this.getUri() + "/end");
      this.resource.addProperty(Time.hasEnd, endInstant);
    }


      }
    }




  private Resource makeInstant(String date, XSDDatatype type) {
    if (!date.matches(UCT_DATE_REGEX)) return null;

    return this.model.createResource()
            .addProperty(RDF.type, Time.Instant)
            .addProperty(Time.inXSDDate, this.model.createTypedLiteral(date, type));
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
