package org.silknow.converter.entities;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.impl.XSDDateType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static org.silknow.converter.Main.outputFolder;

public class TimeSpan extends Entity {

  private static final TimeZone UCT = TimeZone.getTimeZone("UTC");
  private static final String UCT_DATE_REGEX = "\\d{4}(?:-(?:0[1-9]|1[0-2])(?:-(?:0[1-9]|[1-2]\\d|3[0-1]))?)?(?:T" +
    "(?:[0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\dZ?)?";
  private static final String SINGLE_YEAR = "\\d{4}s?";
  private static final String YEAR_SPAN = "(\\d{4}s?)\\s*(?:[-–=]|to)\\s*(\\d{2,4}s?)";
  private static final Pattern SPAN_PATTERN = Pattern.compile(YEAR_SPAN);
  private static final String CENTURY_SPAN = "(\\d{1,2})th(?: century)?\\s*(?:[-–=/]|to|or)\\s*(\\d{1,2})th century";
  private static final Pattern CENTURY_SPAN_PATTERN = Pattern.compile(CENTURY_SPAN);

  public static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  static final DateFormat SLASH_LITTLE_ENDIAN = new SimpleDateFormat("dd/MM/yyyy");

  private static final Map<Integer, Resource> CENTURY_URI_MAP;

  static {
    Map<Integer, Resource> map = new HashMap<>();

    map.put(9, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404501"));
    map.put(10, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404502"));
    map.put(11, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404503"));
    map.put(12, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404504"));
    map.put(13, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404505"));
    map.put(14, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404506"));
    map.put(15, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404465"));
    map.put(16, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404510"));
    map.put(17, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404511"));
    map.put(18, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404512"));
    map.put(19, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404513"));
    map.put(20, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404514"));
    CENTURY_URI_MAP = Collections.unmodifiableMap(map);
  }

  public static final Model centralModel = ModelFactory.createDefaultModel();

  private String startYear, startMonth, startDay;
  private String endYear, endMonth, endDay;
  private String startDate, endDate;
  private String startTime;
  private XSDDatatype startType, endType;
  private String label;

  public TimeSpan() {
    super();

    this.model = centralModel;
    createResource();
    this.setClass(CIDOC.E52_Time_Span);
  }

  public TimeSpan(String date) {
    super();

    this.model = centralModel;
    createResource();
    this.setClass(CIDOC.E52_Time_Span);

    if (StringUtils.isBlank(date)) return;

    // Parsing the date

    // cases: 1871, 1920s
    if (date.matches(SINGLE_YEAR)) {
      startYear = date;
      endYear = date;
      startType = XSDDateType.XSDgYear;
      endType = XSDDateType.XSDgYear;
      // cases: 1741–1754,  1960s to 1970s, ...
    } else if (date.matches(YEAR_SPAN)) {
      Matcher matcher = SPAN_PATTERN.matcher(date);
      if (matcher.find()) {
        this.startYear = matcher.group(1);
        this.endYear = matcher.group(2);

        if (this.endYear.length() < 2) {
          this.endYear = this.startYear.substring(0, 2) + this.endYear;
        }

        startType = XSDDateType.XSDgYear;
        endType = XSDDateType.XSDgYear;
        startDate = startYear;
        endDate = endYear;
      }
      // case: 19th–20th century
    } else if (date.matches(CENTURY_SPAN)) {
      Matcher matcher = CENTURY_SPAN_PATTERN.matcher(date);
      if (matcher.find()) {
        String startCentury = matcher.group(1);
        String endCentury = matcher.group(2);
        int startCent = parseInt(startCentury);
        int endCent = parseInt(endCentury);

        // maybe add a note that this is a century?
        this.startYear = (startCent - 1) + "01";
        this.endYear = endCent + "00";

        startType = XSDDateType.XSDgYear;
        endType = XSDDateType.XSDgYear;
        startDate = startYear;
        endDate = endYear;
      }
    } else { // case 31/07/1816
      try {
        Date d = TimeSpan.dateFromString(date, SLASH_LITTLE_ENDIAN);
        String text = ISO_DATE_FORMAT.format(d).substring(0, 10);
        startDate = text;
        endDate = text;
        startYear = text.substring(0, 4);
        endYear = startYear;

        startType = XSDDateType.XSDdate;
        endType = XSDDateType.XSDdate;
      } catch (ParseException e) {
        // nothing to do
      }
    }

    // Creating the RDF resource

    this.addProperty(RDFS.label, date)
      .addProperty(CIDOC.P78_is_identified_by, date);

    String seed = date;
    if (this.startDate != null)
      seed = this.startDate + "_" + this.endDate;
    this.setUri(ConstructURI.transparent(this.className, seed));

    if (this.startYear == null) return;

    // TODO possibly add some notes
    if (this.startYear.endsWith("s")) // start decade
      this.startYear = this.startYear.replace("s", "");
    if (this.endYear.endsWith("s"))  // end decade
      this.endYear = this.endYear.substring(0, 3) + "9";


    Resource startInstant = makeInstant(startDate, startType);
    Resource endInstant = makeInstant(endDate, endType);

    if (startInstant != null) {
      startInstant = ResourceUtils.renameResource(startInstant, this.getUri() + "/start");
      this.resource.addProperty(Time.hasBeginning, startInstant);
      this.resource.addProperty(CIDOC.P86_falls_within, getCenturyURI(startYear));
    }
    if (endInstant != null) {
      endInstant = ResourceUtils.renameResource(endInstant, this.getUri() + "/end");
      this.resource.addProperty(Time.hasEnd, endInstant);
      this.resource.addProperty(CIDOC.P86_falls_within, getCenturyURI(endYear));
    }
    // WARNING: in cases like 1691-1721, the TS is linked both to 17th and 18th century
    // (even if formally not 100% correct)
  }

  public TimeSpan(Date date) {
    this();
    ISO_DATE_FORMAT.setTimeZone(UCT);

    String text = ISO_DATE_FORMAT.format(date).substring(0, 10);
    Resource instant = makeInstant(text, XSDDatatype.XSDdate);

    this.addProperty(RDFS.label, text)
      .addProperty(CIDOC.P78_is_identified_by, text);
    this.addProperty(Time.hasBeginning, instant)
      .addProperty(Time.hasEnd, instant);
  }


  private static Resource getCenturyURI(String year) {
    int x = (parseInt(year) + 99) / 100;
    return CENTURY_URI_MAP.getOrDefault(x, null);
  }

  @Nullable
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
