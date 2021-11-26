package org.silknow.converter.entities;

import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.SKOS;
import org.doremus.string2vocabulary.VocabularyManager;
import org.jetbrains.annotations.NotNull;
import org.silknow.converter.commons.CrawledJSONExhibitions;
import org.silknow.converter.commons.CrawledJSONPublications;
import org.silknow.converter.ontologies.CIDOC;
import org.silknow.converter.ontologies.Schema;

public class InformationObject extends PropositionalObject {
    public InformationObject(String id) {
        super(id);
        this.setClass(CIDOC.E73_Information_Object);
    }
    public InformationObject() {
        super();
        this.setClass(CIDOC.E73_Information_Object);
    }

    public InformationObject isAbout(ManMade_Object obj) {
        this.addProperty(CIDOC.P129_is_about, obj);
        return this;
    }

    public void setContentUrl(String url) {
        this.addProperty(Schema.contentUrl, model.createResource(url));
    }

    public static InformationObject fromCrawledJSON(@NotNull CrawledJSONPublications pub) {
        InformationObject publication;
        if (pub.hasTitle()) publication = new InformationObject(pub.getTitle());

        else publication = new InformationObject();

        publication.setContentUrl(pub.getUrl());
        publication.setType("Publication");
        publication.addNote(pub.getTitle());
        publication.addNote(pub.getSubtitle());

        return publication;
    }

    public static InformationObject fromCrawledJSON(@NotNull CrawledJSONExhibitions exh) {
        InformationObject exhibition;
        if (exh.hasTitle()) exhibition = new InformationObject(exh.getTitle());

        else exhibition = new InformationObject();

        exhibition.setContentUrl(exh.getUrl());
        exhibition.setType("Exhibition");
        exhibition.addNote(exh.getTitle());
        exhibition.addNote(exh.getBlurb());
        exhibition.addNote(exh.getDate());
        return exhibition;
    }

  public void addInformationObjectType(String info_object, String lang) {
    Resource result = VocabularyManager.searchInCategory(info_object, null, "information", false);
    if (result != null) {

      this.addProperty(CIDOC.P2_has_type, result);
    }

        else {
      this.addProperty(CIDOC.P2_has_type, info_object, lang);
        }
      }




}
