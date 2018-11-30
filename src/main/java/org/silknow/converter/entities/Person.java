package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

public class Person extends Actor {
  public Person(String name) {
    super(name);
    this.setClass(CIDOC.E21_Person);
  }
}
