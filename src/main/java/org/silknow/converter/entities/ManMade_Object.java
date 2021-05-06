package org.silknow.converter.entities;

import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.string2vocabulary.VocabularyManager;
import org.apache.jena.vocabulary.SKOS;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.CRMsci;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManMade_Object extends Entity {
  private static final String DIMENSION_REGEX = "(\\d+(?:[,.]\\d)?)(?: ?cm)? *x *(\\d+(?:[,.]\\d)?)(?: ?cm)?";
  private static final Pattern DIMENSION_PATTERN = Pattern.compile(DIMENSION_REGEX, Pattern.CASE_INSENSITIVE);
  private int imgCount;

  public ManMade_Object(String id) {
    super(id);
    this.setClass(CIDOC.E22_Man_Made_Object);

    this.addSimpleIdentifier(id);
    imgCount = 0;
  }




  public void addSubject(String visual_item, String lang) {
    Resource result = VocabularyManager.searchInCategory(visual_item, null, "thesaurus", false);
    if (result != null) {
      ResIterator resIterator = result.getModel().listResourcesWithProperty(SKOS.member, result);
      if (resIterator.hasNext()) {
        Resource collection_level2 = resIterator.next();
        ResIterator resIterator2 = result.getModel().listResourcesWithProperty(SKOS.member, collection_level2);
        String collection;
        if (resIterator2.hasNext()) {
          Resource collection_level1 = resIterator2.next();
          collection = collection_level1.getURI();  }
        else {
          collection = collection_level2.getURI(); }
        if (collection.contains("depiction") || collection.contains("300264087")) {
          this.addProperty(CIDOC.P65_shows_visual_item, result);
        }

        else {
          result = null;
        }
      }
    }
    if (result == null) {
      //System.out.println("Depiction not found in vocabularies: " + subject;
      Visual_Item depict = new Visual_Item(visual_item, lang);
      this.addProperty(CIDOC.P65_shows_visual_item, depict);
    }
  }









  public void addIntention(String intention) {
    intention = intention.replaceAll("^\\..{3}", "").trim();
    if (intention.equalsIgnoreCase("unconfirmed")) return;
    this.addProperty(CIDOC.P103_was_intended_for, intention);
  }

  public ManMade_Object add(Image img) {
    if (img.hasNullUri()) img.generateUri(this.id, ++imgCount);
    this.addProperty(CIDOC.P138i_has_representation, img);

    return this;
  }



  public ManMade_Object add(Inscription ins) {
    this.addProperty(CIDOC.P128_carries, ins);
    return this;
  }

  public ManMade_Object add(Right right) {
    this.addProperty(CIDOC.P104_is_subject_to, right);
    return this;
  }

  public Resource addSinglePattern(String pattern) {
    String dimUri = this.getUri() + "/dimension/";
    pattern = pattern.replace(",", ".");

    Dimension p = new Dimension(dimUri + "1", pattern, "cm", "pattern", pattern);
    String patUri = this.getUri() + "/pattern/1";
    String pURI2 = "http://data.silknow.org/vocabulary/444";

    this.addProperty(CIDOC.P43_has_dimension, p);
    Pattern_Unit patt = new Pattern_Unit(patUri, p);
    this.addProperty(CIDOC.P58_has_section_definition, patt);
    this.addProperty(CIDOC.P58_has_section_definition, model.createResource(pURI2));

    this.model.add(p.model).add(patt.model);


    return null;
  }




  public Resource addMeasure(String value) throws RuntimeException {
    if (value == null || value.equals("cm")) return null;
    Matcher m = DIMENSION_PATTERN.matcher(value);
    try {
      if (!m.find()) {
        throw new RuntimeException("Dimension not parsed: " + value);
      }
    } catch (RuntimeException re) {
      //re.printStackTrace();
      return null;
    }
    return addMeasure(m.group(1), m.group(2));
  }

  public Resource addMeasure(String width, String height) {
    String dimUri = this.getUri() + "/dimension/";

    width = width.replace(",", ".");
    height = height.replace(",", ".");

    Dimension w = new Dimension(dimUri + "1", width, "cm", "width", width);
    this.addProperty(CIDOC.P43_has_dimension, w);
    Dimension h = new Dimension(dimUri + "2", height, "cm", "height", height);
    this.addProperty(CIDOC.P43_has_dimension, h);

    this.model.add(w.model).add(h.model);


    return null;
  }

  public void addTitle(String title, String lang) {
    this.addProperty(RDFS.label, title, lang)
            .addProperty(CIDOC.P102_has_title, title, lang);
  }

  public void addConstructedTitle(String title, String lang) {
    this.addProperty(RDFS.label, title, lang);
  }

  public Resource addInfo(String section, String text, String lang) {
    Resource sec = model.createResource(this.getUri() + "/section/" + section)
            .addProperty(RDF.type, CIDOC.E53_Place)
            .addProperty(RDFS.label, section)
            .addProperty(CIDOC.P87_is_identified_by, section);

    this.addProperty(CIDOC.P59_has_section, sec);

    return model.createResource(this.getUri() + "/section/" + section + "/info")
            .addProperty(RDF.type, CRMsci.S4_Observation)
            .addProperty(CRMsci.O8_observed, sec)
            .addProperty(CIDOC.P3_has_note, text, lang);
  }

}


