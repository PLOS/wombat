package org.ambraproject.wombat.freemarker;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Encapsulates one element to be inserted into a document, replacing an element with a particular ID.
 */
public class HtmlElementSubstitution {
  private final Element replacementElement;

  public HtmlElementSubstitution(Element replacementElement) {
    this.replacementElement = Preconditions.checkNotNull(replacementElement);
  }

  public static ImmutableList<HtmlElementSubstitution> buildList(TemplateDirectiveBody body, String substAttrName)
          throws IOException, TemplateException {
    if (body == null) return ImmutableList.of();
    StringWriter bodyHtml = new StringWriter();
    body.render(bodyHtml);
    Document bodyDoc = Jsoup.parseBodyFragment(bodyHtml.toString());

    Elements substElements = bodyDoc.getElementsByAttribute(substAttrName);
    Collection<HtmlElementSubstitution> substitutionObjs = new ArrayList<>(substElements.size());
    for (Element substElement : substElements) {
      String substTarget = substElement.attr(substAttrName);
      substElement.removeAttr(substAttrName).attr("id", substTarget);
      substitutionObjs.add(new HtmlElementSubstitution(substElement));
    }
    return ImmutableList.copyOf(substitutionObjs);
  }

  /**
   * Modify a document by replacing an element with a particular ID with a new element.
   *
   * @param document the document to modify
   */
  public void substitute(Document document) {
    Element elementToReplace = document.getElementById(replacementElement.id());
    if (elementToReplace != null) {
      elementToReplace.replaceWith(replacementElement);
    }
  }
}