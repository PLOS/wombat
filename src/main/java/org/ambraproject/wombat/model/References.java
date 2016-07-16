package org.ambraproject.wombat.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "references")
@XmlAccessorType(XmlAccessType.FIELD)
public class References {

  @XmlElement(name = "reference")
  private List<Reference> references;

  public List<Reference> getReferences() {
    return references;
  }

  public void setReferences(List<Reference> references) {
    this.references = references;
  }
}
