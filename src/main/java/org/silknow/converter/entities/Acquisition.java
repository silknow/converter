package org.silknow.converter.entities;

import org.silknow.converter.ontologies.CIDOC;

import java.text.ParseException;
import java.util.Date;

public class Acquisition extends Entity {
  public Acquisition(String id) {
    super(id);
    this.setClass(CIDOC.E8_Acquisition);
  }

  public void transfer(String from, ManMade_Object of, LegalBody to) {
    this.addProperty(CIDOC.P23_transferred_title_from, from)
            .addProperty(CIDOC.P24_transferred_title_of, of)
            .addProperty(CIDOC.P22_transferred_title_to, to);
  }

  public void setType(String type) {
    this.addProperty(CIDOC.P2_has_type, type);
  }

  public void setDate(String date) {
    if (date == null || date.isBlank()) return;

    Date d;
    try {
      d = TimeSpan.dateFromString(date, TimeSpan.SLASH_LITTLE_ENDIAN);
    } catch (ParseException e) {
      e.printStackTrace();
      return;
    }

    TimeSpan ts = new TimeSpan(d);

  }
}
