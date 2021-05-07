package org.silknow.converter.entities;

import org.apache.jena.vocabulary.RDF;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Schema;

public class Person extends Actor {
  public Person(String name) {
    super(name);
    this.addProperty(RDF.type, CIDOC.E21_Person);
  }

  public Person(String name, String birthDate, String deathDate) {
    super(name, birthDate, deathDate);
    this.addProperty(RDF.type, CIDOC.E21_Person);

    addDate(safeDate(birthDate), false);
    addDate(safeDate(deathDate), true);
  }

  public void addDate(String date, boolean isDeath) {
    if(date==null) return;
    TimeSpan timeSpan = new TimeSpan(date);

    String url = this.getUri() + (isDeath ? "/death" : "/birth");
    timeSpan.setUri(url + "/interval");
    addProperty(isDeath ? CIDOC.P100i_died_in : CIDOC.P98i_was_born,
      model.createResource(url)
        .addProperty(RDF.type, isDeath ? CIDOC.E69_Death : CIDOC.E67_Birth)
        .addProperty(CIDOC.P4_has_time_span, timeSpan.asResource())
    );

    this.resource.addProperty(isDeath ? Schema.deathDate : Schema.birthDate, timeSpan.getStartDate());

    model.add(timeSpan.getModel());
  }

}
