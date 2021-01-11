package org.silknow.converter.entities;

import io.github.pasqlisena.RomanConverter;
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
import org.apache.jena.vocabulary.SKOS;
import org.doremus.string2vocabulary.VocabularyManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.converters.Converter;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Time;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class TimeSpan extends Entity {

  private static final TimeZone UCT = TimeZone.getTimeZone("UTC");
  private static final String UCT_DATE_REGEX = "\\d{4}(?:-(?:0[1-9]|1[0-2])(?:-(?:0[1-9]|[1-2]\\d|3[0-1]))?)?(?:T" +
    "(?:[0-1]\\d|2[0-3]):[0-5]\\d:[0-5]\\dZ?)?";
  private static final String SINGLE_YEAR = "\\d{3,4}s?";
  private static final String SEPARATORS = "(?:[-–=/]| to | a | or | y | and )";
  private static final String YEAR_SPAN = "(?i)(?:(?:entre|between)\\s+)?(\\d{3,4}s?)\\s*(?:[-–=/]|to|a|or|y|and)\\s*(\\d{2,4}s?)";
  private static final Pattern SPAN_PATTERN = Pattern.compile(YEAR_SPAN);
  private static final String CENTURY_SPAN = "(\\d{1,2}th|[XVI]+)(?: century| secolo)?\\s*(?:[-–=/]|to|or)\\s*(\\d{1,2}th|[XVI]+) (?:century|secolo)";
  private static final Pattern CENTURY_SPAN_PATTERN = Pattern.compile(CENTURY_SPAN);

  private static final String CENTURY_PART_EN = "(?i)((?:fir|1)st|(?:2|seco)nd|(?:3|thi)rd|fourth|last) (quarter|half|third),?(?: of(?: the)?)?";
  private static final String CENTURY_PART_IT = "(?i)(?:(prim|second|terz|ultim|I+)[oa]?) (ventennio|quarto|metà)(?: del)?";
  private static final String CENTURY_PART_ES = "(?i)([1234][ºª]|pr?i[mn]era?|segund[oa]|segon|tercer|último?) (cuarto|quart|mitad|meitat|tercio|1/3)(?: del)?";
  private static final String CENTURY_PART_FR = "(?i)(1[èe]re?|[234]d?e) (quart|moitié)(?: du)?";
  private static final Pattern CENTURY_PART_EN_PATTERN = Pattern.compile(CENTURY_PART_EN + " (.+)");
  private static final Pattern CENTURY_PART_IT_PATTERN = Pattern.compile(CENTURY_PART_IT + " (.+)");
  private static final Pattern CENTURY_PART_ES_PATTERN = Pattern.compile(CENTURY_PART_ES + " (.+)");
  private static final Pattern CENTURY_PART_FR_PATTERN = Pattern.compile(CENTURY_PART_FR + " (.+)");
  private static final Pattern[] CENTURY_PART_PATTERNS = {CENTURY_PART_EN_PATTERN, CENTURY_PART_ES_PATTERN, CENTURY_PART_IT_PATTERN, CENTURY_PART_FR_PATTERN};
  private static final String EARLY_REGEX = "(?i)(inizio?|début|early|(?:p[ri]+n?cipi|inici?)o(?:s)?)(?: del?| du)?";
  private static final String LATE_REGEX = "(?i)(?:very )?(late|fin(?:e|ale?s)?)(?: del?| du)?";
  private static final String MID_REGEX = "(?i)(mid(-| |dle)|milieu|^metà|second or third quarter of|^mitad|to mid-twentieth century|(?:a )?m+ediados|a mitjan)(?: del?| du)?";
  private static final Pattern EARLY_PATTERN = Pattern.compile(EARLY_REGEX);
  private static final Pattern LATE_PATTERN = Pattern.compile(LATE_REGEX);
  private static final Pattern MID_PATTERN = Pattern.compile(MID_REGEX);
  private static final Pattern[] MODIFIER_PATTERNS = {EARLY_PATTERN, LATE_PATTERN, MID_PATTERN};

  public static final String[] CENTURY_PART_REGEXES = {CENTURY_PART_EN, CENTURY_PART_ES, CENTURY_PART_IT, CENTURY_PART_FR, EARLY_REGEX, LATE_REGEX, MID_REGEX};

  private static final String DATE_ES_REGEX = "(?i)(?<!\\d)(\\d{1,2})?\\s?(?:de |-)?(ene(?:ro)?|feb(?:r|re+ro?)?|mar(?:[zc]o)?|abr(?:il)?|may(?:[o0])?|jun(?:io)?|jul(?:iol?)?|ago(?:sto)?|se[pt](?:tiembre)?|oct(?:ubre)?|nov(?:i?embre)?|dic(?:iembre)?)\\.?\\s?(?:de |-)?(\\d{2,4})";
  private static final Pattern DATE_ES_PATTERN = Pattern.compile(DATE_ES_REGEX);
  private static final String[] MONTHS_ES = {"enero", "febrero", "marzo", "abril", "mayo",
    "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
  };
  private static final String DATE_FR_REGEX = "(?i)(?<!\\d)(\\d{1,2})?\\s?(?:de |-)?(janvier|f[ée][vb]rier|mars|avril|mai|juin|juillet|août|septembre|octobre|november|décembre)\\.?\\s?(\\d{4})";
  private static final Pattern DATE_FR_PATTERN = Pattern.compile(DATE_FR_REGEX);
  private static final String[] MONTHS_FR = {"janvier", "fevrier", "mars", "avril", "mai",
    "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre"
  };

  private static final String DATE_EN_REGEX = "(?i)(january|february|march|april|may|june|july|august|september|october|november|december|spring|fall|winter|summer)(?: (\\d{1,2}))? (\\d{4})";
  private static final Pattern DATE_EN_PATTERN = Pattern.compile(DATE_EN_REGEX);
  private static final String[] MONTHS_EN = {"january", "february", "march", "april", "may",
    "june", "july", "august", "september", "october", "november", "december",
    "spring", "summer", "fall", "winter"
  };


  private static final String DECADES_ES_REGEX = "(?i)(?:década de lo|año)(s)? (\\d+|diez)(?:-(\\d+))?(?: del|,)?(?: sig(?:lo|\\.) ?([XIV]+))?( \\d+$)?";
  private static final Pattern DECADES_ES_PATTERN = Pattern.compile(DECADES_ES_REGEX);

  private static final String FULL_DATE_MULTI = "(3[01]|[012]?[0-9]|\\d{3,4})[-/ ·.](1[012]|0?\\d)[-/ ·.](\\d{2,4})";
  private static final Pattern FULL_DATE_PATTERN = Pattern.compile(FULL_DATE_MULTI);
  private static final String MONTH_DATE_REGEX = "(?:(\\d{2})/(\\d{4}))(?:-(\\d{2})/(\\d{4}))?";
  private static final Pattern MONTH_DATE_PATTERN = Pattern.compile(MONTH_DATE_REGEX);

  public static final DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  static final DateFormat SLASH_LITTLE_ENDIAN = new SimpleDateFormat("dd/MM/yyyy");

  public static final String BEFORE_CHRIST = "(BC|aC|a)";

  private static final BidiMap<Integer, Resource> CENTURY_URI_MAP;

  static {
    BidiMap<Integer, Resource> map = new DualHashBidiMap<>();

    map.put(8, ResourceFactory.createResource("http://vocab.getty.edu/aat/300404500"));
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
  private boolean fromVocabulary;
  private boolean splitted;

  public TimeSpan() {
    super();

    this.model = centralModel;
    createResource();
    this.setClass(CIDOC.E52_Time_Span);
    this.fromVocabulary = false;
    this.splitted = false;
  }

  public TimeSpan(String date) {
    super();
    if (StringUtils.isBlank(date)) return;
    this.splitted = false;

    this.fromVocabulary = false;
    this.century = null;
    this.model = centralModel;

    // Parsing the date
    parseDate(date);
    if (this.fromVocabulary)
      return;
    if (this.startDate == null) {
      this.splitted = true;
      // try again separating the parts
      for (String p : date.split(SEPARATORS, 2)) {
        parseDate(p);
      }
    }

    // Creating the RDF resource
    createResource();
    this.setClass(CIDOC.E52_Time_Span);

    this.addProperty(CIDOC.P78_is_identified_by, date);

    String seed = date;
    String label = date;
    if (this.startDate != null) {
      seed = this.startDate + "_" + this.endDate;
      label = startDate + " - " + endDate;
      if (startDate.equals(endDate)) label = seed = startDate;
    }

    this.addProperty(SKOS.prefLabel, label);
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

  public TimeSpan(int start, int end) {
    super();

    this.model = centralModel;
    this.fromVocabulary = false;
    this.splitted = false;

    startYear = startDate = padYear(start);
    endYear = endDate = padYear(end);
    startType = XSDDateType.XSDgYear;
    endType = XSDDateType.XSDgYear;


    String seed = this.startDate + "_" + this.endDate;
    String label = startDate + " - " + endDate;
    if (startDate.equals(endDate)) label = seed = startDate;

    this.setUri(ConstructURI.transparent(this.className, seed));
    this.setClass(CIDOC.E52_Time_Span);
    this.addProperty(SKOS.prefLabel, label);

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
  }

  private static String padYear(int year) {
    return padYear(Integer.toString(year));
  }

  private static String padYear(String year) {
    String y = year;
    String prefix = "";
    if (year.startsWith("-")) {
      prefix = "-";
      y = y.replace("-", "");
    }
    y = prefix + StringUtils.leftPad(y, 4, "0");
    return y;
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
    //System.out.println(date);

    // preliminary parsing
    date = date.replaceAll(" A.?D.?", "");
    date = date.replaceAll(" CE$", "");
    date = date.replaceAll(" CE-", "-");
    date = date.replaceAll("dC\\.?$", "");
    date = date.replaceAll(" d$", "");
    date = date.replaceAll("^by ", "");
    date = date.replace("soglo", "siglo");
    date = date.replace("sec.", "secolo");
    date = date.replaceAll("(?i)s(ig)?\\. ?", "siglo ");
    date = date.replaceAll("(?i)se? ([XVI]+)", "siglo $1");
    date = date.replaceAll(" ca$", " ");
    date = date.replaceAll("^ca ", " ");
    date = date.replaceAll("^dated ", " ");
    date = date.replaceAll("vers ", " ");

    date = date.replace("'s", "s");
    date = date.replace("centuries", "century");
    date = date.replaceAll("or (earli|lat)er", ""); // TODO represent this better
    date = date.replaceAll("[.,]$", ""); // trailing punctuation

    date = date.trim();

    if (date.matches(("\\d+ " + BEFORE_CHRIST))) {
      date = "-" + padYear(date.replaceAll(BEFORE_CHRIST, "").trim());
      if (startYear == null) {
        startYear = startDate = date;
        startType = XSDDatatype.XSDgYear;
      }
      endYear = endDate = date;
      endType = XSDDatatype.XSDgYear;
    }

    int modifier = 0; // 0 = NONE, 1 = EARLY, 2 = LATE, 3 = MID
    for (int i = 0; i < MODIFIER_PATTERNS.length; i++) {
      Matcher matcher = MODIFIER_PATTERNS[i].matcher(date);
      if (matcher.find()) {
        date = date.replace(matcher.group(), "");
        modifier = i + 1;
        break;
      }
    }
    date = date.trim();

    // cases: XX
    if (RomanConverter.isRoman(date)) date += " secolo";

    // cases: 18th century, secolo XVI
    century = VocabularyManager.searchInCategory(date, null, "dates", false);
    if (century != null && modifier == 0 && !splitted) {
      this.setUri(century.getURI());
      this.fromVocabulary = true;
      return;
    }

    // case 'early 18th century'
    double[] itSpan = getItSpanFromModifier(modifier);

    // case '1st half of the 18th century'
    for (Pattern pat : CENTURY_PART_PATTERNS) {
      Matcher matcher = pat.matcher(date);
      if (!matcher.find()) continue;
      String itString = matcher.group(1);
      String partString = matcher.group(2);
      String centuryString = matcher.group(3);
      itSpan = getItSpanFromCentParts(itString, partString);
      if (centuryString.matches("\\d{2}00s")) {
        century = getCenturyURI(centuryString.substring(0, 2) + "01");
      } else {
        if (RomanConverter.isRoman(centuryString)) centuryString += " secolo";
        century = VocabularyManager.searchInCategory(centuryString, null, "dates", false);
        if (century == null) { // this is a part, but of what?
          System.out.println("Century not found: " + centuryString);
          return;
        }
      }
    }
    double it = itSpan[0];
    double span = itSpan[1];

    if (century != null) {
      int cent = CENTURY_URI_MAP.inverseBidiMap().get(century) - 1;

      int end = (int) Math.round(cent * 100 + span * it);

      if (startYear == null) {
        it--;
        int start = (int) Math.round(cent * 100 + span * it + 1);
        startDate = startYear = padYear(start);
        startType = XSDDateType.XSDgYear;
      }

      endDate = endYear = padYear(end);
      endType = XSDDateType.XSDgYear;

      return;
    }

    // cases: 1871, 1920s
    if (date.matches(SINGLE_YEAR)) {
      if (startYear == null) {
        startYear = startDate = decade2year(date, false, modifier);
        startType = XSDDateType.XSDgYear;
      }
      endYear = endDate = decade2year(date, true, modifier);
      endType = XSDDateType.XSDgYear;
      return;
    }

    // cases: 1741–1754,  1960s to 1970s, ...
    if (date.matches(YEAR_SPAN)) {
      Matcher matcher = SPAN_PATTERN.matcher(date);
      if (matcher.find()) {
        String sy = decade2year(matcher.group(1), false, modifier);
        if (startYear == null) {
          startDate = startYear = sy;
          startType = XSDDateType.XSDgYear;
        }
        this.endYear = matcher.group(2);

        if (endYear.length() < sy.length()) {
          this.endYear = sy.substring(0, sy.length() - endYear.length()) + endYear;
        }
        endDate = endYear = decade2year(this.endYear, true, modifier);
        endType = XSDDateType.XSDgYear;
      }
      return;
    }

    // case: 19th–20th century
    if (date.matches(CENTURY_SPAN)) {
      Matcher matcher = CENTURY_SPAN_PATTERN.matcher(date);
      if (matcher.find()) {
        String startCentury = matcher.group(1).replace("th", "");
        String endCentury = matcher.group(2).replace("th", "");
        int startCent, endCent;
        if (RomanConverter.isRoman(startCentury))
          startCent = RomanConverter.toNumerical(startCentury);
        else startCent = parseInt(startCentury);
        if (RomanConverter.isRoman(endCentury))
          endCent = RomanConverter.toNumerical(endCentury);
        else endCent = parseInt(endCentury);

        // maybe add a note that this is a century?
        startYear = padYear((startCent - 1) + "01");
        endYear = padYear(endCent + "00");

        startType = XSDDateType.XSDgYear;
        endType = XSDDateType.XSDgYear;
        startDate = startYear;
        endDate = endYear;
      }
      return;
    }

    // case "22 abril 1985"
    Matcher matcherx = DATE_ES_PATTERN.matcher(date);
    while (matcherx.find()) {
      String dd = matcherx.group(1);
      String mm = matcherx.group(2);
      String yy = matcherx.group(3);
      int m;
      for (m = 0; m < MONTHS_ES.length; m++) {
        if (mm.substring(0, 3).equalsIgnoreCase(MONTHS_ES[m].substring(0, 3)))
          break;
      }
      if (m > 12) continue;
      m++;
      // workaround SET
      if (mm.equalsIgnoreCase("set")) m = 9;

      setDate(dd, m, yy);
    }
    if (startDate != null) return;

    // case "23 Fevrier 1934"
    matcherx = DATE_FR_PATTERN.matcher(date);
    while (matcherx.find()) {
      String dd = matcherx.group(1);
      String mm = matcherx.group(2);
      String yy = matcherx.group(3);
      int m;
      for (m = 0; m < MONTHS_FR.length; m++) {
        if (mm.substring(0, 3).equalsIgnoreCase(MONTHS_FR[m].substring(0, 3)))
          break;
      }
      if (m > 12) continue;
      m++;
      // workaround febrier
      if (mm.equalsIgnoreCase("febrier")) m = 2;

      setDate(dd, m, yy);
    }
    if (startDate != null) return;

    // case "April 30 1856", "December 2004", "Fall 1919"
    matcherx = DATE_EN_PATTERN.matcher(date);
    while (matcherx.find()) {
      String dd = matcherx.group(2);
      String mm = matcherx.group(1);
      String yy = matcherx.group(3);
      int m;
      for (m = 0; m < MONTHS_EN.length; m++) {
        if (mm.equalsIgnoreCase(MONTHS_EN[m])) {
          break;
        }
      }
      m++;
      //System.out.println(m);

      if (m <= 12) {
        setDate(dd, m, yy);
      } else {
        switch (m) {
          case 13: // Spring
            setDate("21", 3, yy);
            setDate("21", 6, yy);
            break;
          case 14: // Summer
            setDate("22", 6, yy);
            setDate("22", 9, yy);
            break;
          case 15: // Fall
            setDate("23", 9, yy);
            setDate("22", 12, yy);
            break;
          case 16: // Winter
            setDate("23", 12, yy);
            setDate("20", 3, Integer.toString(Integer.parseInt(yy) + 1));
        }
      }
    }
    if (startDate != null) return;


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
      return;
    } catch (ParseException e) {
      // nothing to do
    }

    // case 31/7/1816 , 13-9-67
    matcherx = FULL_DATE_PATTERN.matcher(date);
    while (matcherx.find()) {
      String dd = matcherx.group(1);
      int mm = parseInt(matcherx.group(2));
      String yy = matcherx.group(3);

      if (dd.length() > 2) {
        if (yy.length() > 2)
          continue;
        else { // swap
          String temp = dd;
          dd = yy;
          yy = temp;
        }
      }

      if (yy.length() == 3) yy = "1" + yy;
      else if (yy.length() == 2) yy = "19" + yy;

      setDate(dd, mm, yy);
    }
    if (startDate != null) return;

    // case 02/1877, 03/1881-04/1881
    matcherx = MONTH_DATE_PATTERN.matcher(date);
    while (matcherx.find()) {
      int mm = parseInt(matcherx.group(1));
      String yy = matcherx.group(2);

      setDate(null, mm, yy);

      yy = matcherx.group(4);
      if (yy != null) {
        mm = parseInt(matcherx.group(3));
        setDate(null, mm, yy);
      }
    }
    if (startDate != null) return;


    // case "Años 20 siglo XX"
    matcherx = DECADES_ES_PATTERN.matcher(date);
    if (matcherx.find()) {
      this.startType = this.endType = XSDDatatype.XSDgYear;

      String centuryString = matcherx.group(4);
      int century = 19;
      if (!StringUtils.isBlank(centuryString))
        century = RomanConverter.toNumerical(matcherx.group(4)) - 1;


      String preciseYear = matcherx.group(5);
      if (!StringUtils.isBlank(preciseYear)) {
        preciseYear = preciseYear.trim();
        if (preciseYear.length() == 3)
          preciseYear = "1" + preciseYear;
        else if (preciseYear.length() == 2) {
          preciseYear = century + preciseYear;
        }

        this.startDate = this.startYear = padYear(preciseYear);
        this.endDate = this.endYear = padYear(preciseYear);
        return;
      }

      boolean isSingular = StringUtils.isBlank(matcherx.group(1));
      String decade = matcherx.group(2);
      if ("diez".equalsIgnoreCase(decade)) decade = "10";
      String endDecade = matcherx.group(3);
      if (StringUtils.isBlank(endDecade))
        endDecade = decade;
      if (!isSingular)
        endDecade = endDecade.charAt(0) + "9";


      startYear = startDate = padYear(century + decade);
      endYear = endDate = padYear(century + endDecade);
    }

  }

  private static double[] getItSpanFromCentParts(String itString, String partString) {
    double it = ordinalToInt(itString);
    if (it == 0) {
      return new double[]{-1, -1};
    }
    double span = 50; // half century
    if (partString.matches("(third|tercio|1/3)")) {
      span = 33.3;
      if (it == -1) it = 3;
    }
    if (partString.matches("[qc]uart(o|er)?")) {
      span = 25;
      if (it == -1) it = 4;
    }
    if (partString.matches("ventennio")) {
      span = 20;
      if (it == -1) it = 5;
    }
    if (it == -1) it = 2;
    return new double[]{it, span};
  }

  private static double[] getItSpanFromModifier(int modifier) {
    double it = 1;
    double span = 100;

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
    return new double[]{it, span};
  }

  private void setDate(String dd, int m, String yy) {
    if (yy.length() == 2) yy = "19" + yy;

    XSDDatatype type = XSDDatatype.XSDgMonth;
    String formatted = yy + "-" + String.format("%02d", m);
    if (!StringUtils.isBlank(dd)) {
      type = XSDDatatype.XSDdate;
      formatted += "-" + String.format("%02d", parseInt(dd));
    }
    if (startDate == null) {
      startDate = formatted;
      startYear = yy;
      startType = type;
    }
    endDate = formatted;
    endYear = yy;
    endType = type;
  }

  @NotNull
  private static String decade2year(@NotNull String decade, boolean end, int modifier) {
    // modifier: 0 = NONE, 1 = EARLY, 2 = LATE, 3 = MID
    if (decade.endsWith("s")) {
      decade = StringUtils.leftPad(decade, 5, "0");
      if (end) { // end decade
        String endDigit = "9";
        if (modifier == 1) endDigit = "4";
        if (modifier == 3) endDigit = "8";
        return decade.replace("0s", endDigit);
      }
      // start decade
      if (modifier == 2)
        return decade.replace("0s", "5");
      if (modifier == 3)
        return decade.replace("0s", "2");
      return decade.replace("s", "");
    }

    // choice: mid-years (e.g. mid-1983) are not handle with month-precision
    return padYear(decade);
  }

  /**
   * Convert ordinal literals (e.g. first, second, ...) in related int.
   * Four langs: EN, ES, IT, FR
   *
   * @param ordinal
   * @return related integer, -1 if ordinal is "last", 0 if not recognised
   */
  private static int ordinalToInt(@NotNull String ordinal) {
    String ordinalMin = ordinal.replaceAll("[oa]", "").toUpperCase();
    ordinal = ordinal.toLowerCase();

    if (RomanConverter.isRoman(ordinalMin)) {
      return RomanConverter.toNumerical(ordinalMin);
    }
    if (ordinal.equals("first") || ordinal.startsWith("pri") || ordinal.startsWith("pimer"))
      return 1;
    if (ordinal.matches("(se(co|gu)nd[oa]?|segon)"))
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

  public static TimeSpan parseVenezia(String start, String end, String fraction, String century) {
    String[] labelParts = {century, fraction, start, end};
    String label = Arrays.stream(labelParts)
      .filter(s -> (s != null && s.length() > 0))
      .collect(Collectors.joining(", "));

    TimeSpan ts;

    if (start == null) {
      if (century.contains("/")) {
        String[] p = century.split("/");
        int startCentury = RomanConverter.toNumerical(p[0].trim());
        int endCentury = RomanConverter.toNumerical(p[1].trim());

        double itStart = -1;
        double spanStart = -1;
        double itEnd = -1;
        double spanEnd = -1;

        if (fraction != null && fraction.contains("/")) {
          String[] f = fraction.split("/");

          int modifier = 0; // 0 = NONE, 1 = EARLY, 2 = LATE, 3 = MID
          for (int i = 0; i < MODIFIER_PATTERNS.length; i++) {
            Matcher matcher = MODIFIER_PATTERNS[i].matcher(f[0]);
            if (matcher.find()) {
              modifier = i + 1;
              break;
            }
          }
          double[] itSpan = getItSpanFromModifier(modifier);
          itStart = itSpan[0];
          spanStart = itSpan[1];

          for (int i = 0; i < MODIFIER_PATTERNS.length; i++) {
            Matcher matcher = MODIFIER_PATTERNS[i].matcher(f[1]);
            if (matcher.find()) {
              modifier = i + 1;
              break;
            }
          }
          itSpan = getItSpanFromModifier(modifier);
          itEnd = itSpan[0];
          spanEnd = itSpan[1];
        }

        int startY, endY;
        if (itStart > 0 && spanStart > 0 && itEnd > 0 && spanEnd > 0) {
          endY = (int) Math.round(endCentury * 100 + spanEnd * itEnd);
          startY = (int) Math.round(startCentury * 100 + spanStart * (itStart - 1) + 1);
        } else {
          endY = Math.round(endCentury * 100);
          startY = Math.round((startCentury - 1) * 100 + 1);
        }
        ts = new TimeSpan(startY, endY);
        ts.addAppellation(label);
        return ts;
      }
      if (fraction != null) century = fraction + " " + century;
      ts = new TimeSpan(century);
      ts.addAppellation(label);
      return ts;
    }

    start = start.replaceAll(" ca\\.?$", "");
    for (String x : new String[]{CENTURY_PART_IT, EARLY_REGEX, MID_REGEX, LATE_REGEX}) {
      if (start.matches(x)) {
        start += " " + century;
        if (!start.contains("sec")) start += " secolo";
        ts = new TimeSpan(start);
        ts.addAppellation(label);
        return ts;
      }
    }

    if (end != null) {
      end = end.replaceAll(" ca\\.?$", "");
      ts = new TimeSpan(start + " - " + end);
    } else ts = new TimeSpan(start);

    ts.addAppellation(label);
    return ts;
  }


}
