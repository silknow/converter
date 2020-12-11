package org.silknow.converter.entities;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
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
import org.doremus.string2vocabulary.VocabularyManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class TimeSpan extends Entity {

  private static final TimeZone UCT = TimeZone.getTimeZone("UTC");
  private static final String UCT_DATE_REGEX = "\\d{4}(?:-(?:0[1-9]|1[0-2])(?:-(?:0[1-9]|[1-2]\\d|3[0-1]))?)?(?:T" +
    "(?:[0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\dZ?)?";
  private static final String SINGLE_YEAR = "\\d{4}s?";
  private static final String YEAR_SPAN = "(\\d{4}s?)\\s*(?:[-–=]|to|a)\\s*(\\d{2,4}s?)";
  private static final Pattern SPAN_PATTERN = Pattern.compile(YEAR_SPAN);
  private static final String CENTURY_SPAN = "(\\d{1,2})th(?: century)?\\s*(?:[-–=/]|to|or)\\s*(\\d{1,2})th century";
  private static final Pattern CENTURY_SPAN_PATTERN = Pattern.compile(CENTURY_SPAN);

  private static final String CENTURY_PART_EN = "(?i)(first|second|third|fourth|last) (quarter|half),? (?:of the )?";
  private static final String CENTURY_PART_IT = "(?i)(?:(prim|second|terz|ultim)[oa]) (quarto|metà) (?:del )?";
  private static final String CENTURY_PART_ES = "(?i)(primera?|segund[oa]|tercer|último) (cuarto|mitad|tercio) (?:del )?";
  private static final String CENTURY_PART_FR = "(?i)(1er|[234]e) (quart|moitié) (.+)";
  private static final Pattern CENTURY_PART_EN_PATTERN = Pattern.compile(CENTURY_PART_EN+"(.+)");
  private static final Pattern CENTURY_PART_IT_PATTERN = Pattern.compile(CENTURY_PART_IT+"(.+)");
  private static final Pattern CENTURY_PART_ES_PATTERN = Pattern.compile(CENTURY_PART_ES+"(.+)");
  private static final Pattern CENTURY_PART_FR_PATTERN = Pattern.compile(CENTURY_PART_FR+"(.+)");
  private static final Pattern[] CENTURY_PART_PATTERNS = {CENTURY_PART_EN_PATTERN, CENTURY_PART_ES_PATTERN, CENTURY_PART_IT_PATTERN, CENTURY_PART_FR_PATTERN};
  private static final String EARLY_REGEX = "(?i)(early|(?:p[ri]+ncip|inic)io(?:s| del))";
  private static final String LATE_REGEX = "(?i)(late|fin(?:e|ales))";
  private static final String MID_REGEX = "(?i)(mid-|metà del|second or third quarter of|to mid-twentieth century|(?:a )?m+ediados|a mitjan)";
  private static final Pattern EARLY_PATTERN = Pattern.compile(EARLY_REGEX);
  private static final Pattern LATE_PATTERN = Pattern.compile(LATE_REGEX);
  private static final Pattern MID_PATTERN = Pattern.compile(MID_REGEX);
  private static final Pattern[] MODIFIER_PATTERNS = {EARLY_PATTERN, LATE_PATTERN, MID_PATTERN};

  public static final String[] CENTURY_PART_REGEXES = {CENTURY_PART_EN, CENTURY_PART_ES, CENTURY_PART_IT, CENTURY_PART_FR, EARLY_REGEX, LATE_REGEX, MID_REGEX};


  public static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  static final DateFormat SLASH_LITTLE_ENDIAN = new SimpleDateFormat("dd/MM/yyyy");

  private static final BidiMap<Integer, Resource> CENTURY_URI_MAP;

  static {
    BidiMap<Integer, Resource> map = new DualHashBidiMap<>();

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
    CENTURY_URI_MAP = map;
  }

  public static final Model centralModel = ModelFactory.createDefaultModel();

  private Resource century;
  private String startYear, startMonth, startDay;
  private String endYear, endMonth, endDay;
  private String startDate, endDate;
  private String startTime;
  private XSDDatatype startType, endType;
  private String label;
  private boolean fromVocabulary;

  public TimeSpan() {
    super();

    this.model = centralModel;
    createResource();
    this.setClass(CIDOC.E52_Time_Span);
    this.fromVocabulary = false;
  }

  public TimeSpan(String date) {
    super();
    if (StringUtils.isBlank(date)) return;

    this.fromVocabulary = false;
    this.century = null;
    this.model = centralModel;

    // Parsing the date
    parseDate(date);
    if (this.fromVocabulary)
      return;

    // Creating the RDF resource
    createResource();
    this.setClass(CIDOC.E52_Time_Span);

    this.addProperty(RDFS.label, date)
      .addProperty(CIDOC.P78_is_identified_by, date);

    String seed = date;
    if (this.startDate != null)
      seed = this.startDate + "_" + this.endDate;
    this.setUri(ConstructURI.transparent(this.className, seed));

    if (this.startYear == null) return;

    Resource startInstant = makeInstant(startDate, startType);
    Resource endInstant = makeInstant(endDate, endType);

    if (startInstant != null) {
      startInstant = ResourceUtils.renameResource(startInstant, this.getUri() + "/start");
      this.addProperty(Time.hasBeginning, startInstant);
      this.addProperty(CIDOC.P86_falls_within, getCenturyURI(startYear));
    }
    if (endInstant != null) {
      endInstant = ResourceUtils.renameResource(endInstant, this.getUri() + "/end");
      this.addProperty(Time.hasEnd, endInstant);
      this.addProperty(CIDOC.P86_falls_within, getCenturyURI(endYear));
    }
    // WARNING: in cases like 1691-1721, the TS is linked both to 17th and 18th century
    // (even if formally not 100% correct)
  }



  public TimeSpan(Date date) {
    this();
    this.model = centralModel;
    this.fromVocabulary = false;

    ISO_DATE_FORMAT.setTimeZone(UCT);

    String text = ISO_DATE_FORMAT.format(date).substring(0, 10);
    Resource instant = makeInstant(text, XSDDatatype.XSDdate);

    this.addProperty(RDFS.label, text)
      .addProperty(CIDOC.P78_is_identified_by, text);
    this.addProperty(Time.hasBeginning, instant)
      .addProperty(Time.hasEnd, instant);
  }

  private void parseDate(@NotNull String date) {
    // preliminary parsing
    int modifier = 0; // 0 = NONE, 1 = EARLY, 2 = LATE, 3 = MID
    for (int i = 0; i < MODIFIER_PATTERNS.length; i++) {
      Matcher matcher = MODIFIER_PATTERNS[i].matcher(date);
      if (matcher.find()) {
        date = date.replace(matcher.group(), "").trim();
        modifier = i + 1;
        break;
      }
    }

    // cases: 18th century, secolo XVI
    century = VocabularyManager.searchInCategory(date, null, "dates", false);
    if (century != null && modifier == 0) {
      this.setUri(century.getURI());
      this.fromVocabulary = true;
      return;
    }

    // cases: 1871, 1920s
    if (date.matches(SINGLE_YEAR)) {
      startYear = decade2year(date, false, modifier);
      endYear = decade2year(date, true, modifier);
      startType = XSDDateType.XSDgYear;
      endType = XSDDateType.XSDgYear;
      startDate = startYear;
      endDate = endYear;
      return;
    }
    // cases: 1741–1754,  1960s to 1970s, ...
    if (date.matches(YEAR_SPAN)) {
      Matcher matcher = SPAN_PATTERN.matcher(date);
      if (matcher.find()) {
        this.startYear = decade2year(matcher.group(1), false, modifier);
        this.endYear = matcher.group(2);

        if (this.endYear.length() < 2) {
          this.endYear = this.startYear.substring(0, 2) + this.endYear;
        }
        this.endYear = decade2year(this.endYear, true, modifier);

        startType = XSDDateType.XSDgYear;
        endType = XSDDateType.XSDgYear;
        startDate = startYear;
        endDate = endYear;
      }
      return;
    }

    // case: 19th–20th century
    if (date.matches(CENTURY_SPAN)) {
      Matcher matcher = CENTURY_SPAN_PATTERN.matcher(date);
      if (matcher.find()) {
        String startCentury = matcher.group(1);
        String endCentury = matcher.group(2);
        int startCent = parseInt(startCentury);
        int endCent = parseInt(endCentury);

        // maybe add a note that this is a century?
        startYear = (startCent - 1) + "01";
        endYear = endCent + "00";

        startType = XSDDateType.XSDgYear;
        endType = XSDDateType.XSDgYear;
        startDate = startYear;
        endDate = endYear;
      }
      return;
    }

    // case 'early 18th century'
    double it = -1;
    double span = -1;
    // modifier: 0 = NONE, 1 = EARLY, 2 = LATE, 3 = MID
    switch (modifier) {
      case 1:
        it = 1;
        span = 25;
        break;
      case 2:
        it = 4;
        span = 25;
        break;
      case 3:
        it = 1.5;
        span = 50;
    }

    // case '1st half of the 18th century'
    for (Pattern pat : CENTURY_PART_PATTERNS) {
      Matcher matcher = pat.matcher(date);
      if (!matcher.find()) continue;
      String itString = matcher.group(1);
      String partString = matcher.group(2);
      String centuryString = matcher.group(3);

      century = VocabularyManager.searchInCategory(centuryString, null, "dates", false);
      if (century == null) { // this is a part, but of what?
        System.out.println("Century not found: " + centuryString);
        return;
      }

      it = ordinalToInt(itString);
      if (it == 0) {
        System.out.println("Error in parsing: " + date);
        return;
      }
      span = 50; // half century
      if (partString.equals("tercio")) {
        span = 33.3;
        if (it == -1) it = 3;
      }
      if (partString.matches("[qc]uart(o|er)?")) {
        span = 25;
        if (it == -1) it = 4;
      }
      if (it == -1) it = 2;
    }

    if (century != null && span > 0 && it > 0) {
      int cent = CENTURY_URI_MAP.inverseBidiMap().get(century) - 1;

      int end = (int) Math.round(cent * 100 + span * it);
      it--;
      int start = (int) Math.round(cent * 100 + span * it + 1);

      startYear = Integer.toString(start);
      endYear = Integer.toString(end);

      startType = XSDDateType.XSDgYear;
      endType = XSDDateType.XSDgYear;
      startDate = startYear;
      endDate = endYear;

      return;
    }

    // case 31/07/1816
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

  @NotNull
  private static String decade2year(@NotNull String decade, boolean end, int modifier) {
    // modifier: 0 = NONE, 1 = EARLY, 2 = LATE, 3 = MID
    if (decade.endsWith("s")) {
      if (end) { // end decade
        String endDigit = "9";
        if (modifier == 1) endDigit = "4";
        if (modifier == 3) endDigit = "8";
        return decade.substring(0, 3) + endDigit;
      }
      // start decade
      if (modifier == 2)
        return decade.replace("0s", "5");
      if (modifier == 3)
        return decade.replace("0s", "2");
      return decade.replace("s", "");
    }

    // choice: mid-years (e.g. mid-1983) are not handle with month-precision
    return decade;
  }

  /**
   * Convert ordinal literals (e.g. first, second, ...) in related int.
   * Four langs: EN, ES, IT, FR
   *
   * @param ordinal
   * @return related integer, -1 if ordinal is "last", 0 if not recognised
   */
  private static int ordinalToInt(@NotNull String ordinal) {
    ordinal = ordinal.toLowerCase();
    if (ordinal.equals("first") || ordinal.startsWith("prim"))
      return 1;
    if (ordinal.matches("se(co|gu)nd[oa]?"))
      return 2;
    if (ordinal.matches("(third|terz|tercer)"))
      return 3;
    if (ordinal.equals("fourth"))
      return 4;
    if (ordinal.matches("(last|[úu]ltimo?)"))
      return -1;
    ordinal = ordinal.replaceAll("\\D+", ""); // replace all non-digits
    try {
      return parseInt(ordinal);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private static Resource getCenturyURI(String year) {
    int x = (parseInt(year) + 99) / 100;
    return CENTURY_URI_MAP.getOrDefault(x, null);
  }

  @Nullable
  private Resource makeInstant(@NotNull String date, XSDDatatype type) {
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

  //public String toLabel() {return this.resource.getProperty(RDFS.label).getObject().toString();}

}
