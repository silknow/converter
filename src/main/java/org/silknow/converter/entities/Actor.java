package org.silknow.converter.entities;

import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.isnimatcher.ISNIRecord;
import org.silknow.converter.commons.ConstructURI;
import org.silknow.converter.commons.ISNIWrapper;
import org.silknow.converter.ontologies.CIDOC;

import java.io.IOException;

public class Actor extends Entity {
  public Actor(String name) {
    this(name, null, null);
  }

  public Actor(String name, String birthDate, String deathDate) {
    super();
    name = clean(name);

    this.setUri(ConstructURI.build(this.className, name));

    this.setClass(CIDOC.E39_Actor);
    this.addProperty(RDFS.label, name)
      .addProperty(CIDOC.P1_is_identified_by, name);

    try {
      ISNIRecord isni_record = ISNIWrapper.search(name, safeDate(birthDate));
      if (isni_record != null) isniEnrich(isni_record);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void isniEnrich(ISNIRecord isni) {
    this.addPropertyResource(OWL.sameAs, isni.uri);
    this.addPropertyResource(OWL.sameAs, isni.getViafURI());
    this.addPropertyResource(OWL.sameAs, isni.getMusicBrainzUri());
    this.addPropertyResource(OWL.sameAs, isni.getMuziekwebURI());
    this.addPropertyResource(OWL.sameAs, isni.getWikidataURI());

    String wp = isni.getWikipediaUri();
    String dp = isni.getDBpediaUri();

    if (wp == null) {
      wp = isni.getWikipediaUri("en");
      dp = isni.getDBpediaUri("en");
    }
    this.addPropertyResource(OWL.sameAs, dp);
    this.addPropertyResource(FOAF.isPrimaryTopicOf, wp);
  }


  private String clean(String name) {
    return name.replace("(opens in new window)", "")
      .replaceAll("\\s+", " ") // double space
      .replaceAll("\\.$", "") // trailing dots
      .replaceAll("&quot;", "\"") // quotation mark
      .replaceAll(", \\d{4}", "") // trailing date
      .trim();
  }

  protected String safeDate(String date) {
    if (date == null || date.isEmpty() || date.contains("[")) return null;
    return date;
  }

}
