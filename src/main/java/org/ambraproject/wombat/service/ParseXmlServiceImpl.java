package org.ambraproject.wombat.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.ambraproject.wombat.model.NlmPerson;
import org.ambraproject.wombat.model.Reference;
import org.ambraproject.wombat.util.NodeListAdapter;
import org.ambraproject.wombat.util.ParseXmlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ParseXmlServiceImpl implements ParseXmlService {
  private static final Logger log = LoggerFactory.getLogger(ParseXmlServiceImpl.class);
  private static final Pattern YEAR_FALLBACK = Pattern.compile("\\d{4,}");
  private static final Pattern VOL_NUM_RE = Pattern.compile("(\\d{1,})");
  private static final String WESTERN_NAME_STYLE = "western";
  private static final String EASTERN_NAME_STYLE = "eastern";
  private static final Joiner NAME_JOINER = Joiner.on(' ').skipNulls();



  @Override
  public List<Reference> parseArticleReferences(InputStream xml) throws ParserConfigurationException, IOException, SAXException {
    Objects.requireNonNull(xml);

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document doc = builder.parse(xml);
    // to combine adjacent text nodes and remove empty ones in the dom
    doc.getDocumentElement().normalize();

    List<Reference> references = new ArrayList<>();
    List<Node> refListNodes = NodeListAdapter.wrap(doc.getElementsByTagName("ref-list"));

    if (refListNodes.size() == 0) {
      log.info("No <ref-list> element was found in the xml.");
      return references;
    }

    if (refListNodes.size() > 1) {
      throw new RuntimeException("More than one <ref-list> element was found in the xml.");
    }

    Node refListNode = refListNodes.get(0);
    if (refListNode.getNodeType() != Node.ELEMENT_NODE) {
      throw new RuntimeException("<ref-list> is not an element.");
    }

    Element refListElement = (Element) refListNode;

    List<Node> refNodes = NodeListAdapter.wrap(refListElement.getElementsByTagName("ref"));
    references = refNodes.stream()
        .map(ref -> buildReferences((Element) ref))
        .collect(Collectors.toList()).stream() // List<List<Reference>>
        .flatMap(r -> r.stream()).collect(Collectors.toList());

    return references;
  }

  private List<Reference> buildReferences(Element refElement) {
    NodeList citationElements = refElement.getElementsByTagName("element-citation") != null ?
        refElement.getElementsByTagName("element-citation") :
        refElement.getElementsByTagName("mixed-citation") != null ?
        refElement.getElementsByTagName("mixed-citation") : null;
    List<Reference> references = new ArrayList<>();
    // a <ref> element can have one or more of citation elements
    for (Node citationNode : NodeListAdapter.wrap(citationElements)) {
      if (citationNode.getNodeType() != Node.ELEMENT_NODE) {
        throw new RuntimeException("<element-citation> or <mixed-citation> is not an element.");
      }

      Element element = (Element) citationNode;
      Reference reference = new Reference();
      reference.setJournal(setJournal(element));
      reference.setTitle(buildTitle(element));
      reference.setAuthors(parseAuthors(element));
      String year = ParseXmlUtil.getElementSingleValue(element, "year");
      reference.setYear(parseYear(year));
      String volume = ParseXmlUtil.getElementSingleValue(element, "volume");
      reference.setVolume(volume);
      reference.setVolumeNumber(parseVolumeNumber(volume));
      reference.setIssue(ParseXmlUtil.getElementSingleValue(element, "issue"));
      reference.setPublisherName(ParseXmlUtil.getElementSingleValue(element, "publisher-name"));
      reference.setIsbn(ParseXmlUtil.getElementSingleValue(element, "isbn"));

      NodeList extLinkList = element.getElementsByTagName("ext-link");
      if (extLinkList != null && extLinkList.getLength() > 0) {
        Element extLink = (Element) extLinkList.item(0);
        String linkType = ParseXmlUtil.getElementAttributeValue(extLink, "ext-link-type");
        if (linkType.equals("uri")) {
          // TODO: add doi validation
          reference.setDoi(ParseXmlUtil.getElementAttributeValue(extLink, "xlink:href"));
        }
      }
      buildPages(element, reference);
      references.add(reference);
    }
    return references;
  }


  /**
   * Sets the journal title property of a reference appropriately based on the XML.
   */
  private String setJournal(Element element) {
    Element elementCitation = element.getElementsByTagName("element-citation") != null ?
        (Element) element.getElementsByTagName("element-citation").item(0) : null;
    Element mixedCitation=   element.getElementsByTagName("mixed-citation") != null ? (Element) element
        .getElementsByTagName("mixed-citation").item(0) : null;

    Element citationElement = elementCitation != null ? elementCitation : mixedCitation != null ?
        mixedCitation : null;

    String type = ParseXmlUtil.getElementAttributeValue(citationElement, "publication-type");
    if (Strings.isNullOrEmpty(type)) {
      return null;
    }

    // pmc2obj-v3.xslt lines 730-739
    if (Reference.PublicationType.JOURNAL.getValue().equals(type)) {
      return ParseXmlUtil.getElementSingleValue(element, "source");
    }
    return null;
  }

  /**
   * @return the value of the title property, retrieved from the XML
   */
  private String buildTitle(Element refElement) {

    NodeList titleNode = refElement.getElementsByTagName("article-title");

    if (titleNode == null || titleNode.getLength() == 0) {
      titleNode = refElement.getElementsByTagName("source");
    }

    if (titleNode == null || titleNode.getLength() == 0) {
      NodeList mixedCitation = refElement.getElementsByTagName("mixed-citation");
      if (mixedCitation.item(0).getNodeType() == Node.TEXT_NODE) {
       titleNode = mixedCitation;
      } else {
        return null;
      }
    }

    String title = ParseXmlUtil.getElementMixedContent(new StringBuilder(), titleNode.item(0)).toString();

    return (title == null) ? null : ParseXmlUtil.standardizeWhitespace(title);
  }


  private List<NlmPerson> parseAuthors(Element element) {
    List<Node> personGroupElement = NodeListAdapter.wrap(element.getElementsByTagName("person-group"));
    List<NlmPerson> personGroup = null;


    if (personGroupElement != null) {
      String type;
      for (Node pgNode : personGroupElement) {
        Element pgElement = (Element) pgNode;
        List<Node> personNodes = NodeListAdapter.wrap(pgElement.getElementsByTagName("name"));
        type = ParseXmlUtil.getElementAttributeValue(pgElement, "person-group-type");
        if (type != null && type.equals("author")) {
          personGroup = personNodes.stream().map(node -> parsePersonName((Element) node)).collect
              (Collectors.toList());
        }
      }
    } else {
      List<Node> personNodes = NodeListAdapter.wrap(element.getElementsByTagName("name"));

      personGroup = personNodes.stream().filter(node -> isEditorNode(node))
          .map(node -> parsePersonName((Element) node))
          .collect(Collectors.toList());
    }
    return personGroup;
  }

  private NlmPerson parsePersonName(Element nameElement) {
    String nameStyle = ParseXmlUtil.getElementAttributeValue(nameElement, "name-style");
    String surname = ParseXmlUtil.getElementSingleValue(nameElement, "surname");
    String givenNames = ParseXmlUtil.getElementSingleValue(nameElement, "given-names");
    String suffix = ParseXmlUtil.getElementSingleValue(nameElement, "suffix");

    if (surname == null) {
      throw new RuntimeException("Required surname is omitted from node: " + nameElement.getNodeName());
    }

    String[] fullNameParts;
    if (WESTERN_NAME_STYLE.equals(nameStyle)) {
      fullNameParts = new String[]{givenNames, surname, suffix};
    } else if (EASTERN_NAME_STYLE.equals(nameStyle)) {
      fullNameParts = new String[]{surname, givenNames, suffix};
    } else {
      throw new RuntimeException("Invalid name-style: " + nameStyle);
    }

    String fullName = NAME_JOINER.join(fullNameParts);
    givenNames = Strings.nullToEmpty(givenNames);
    suffix = Strings.nullToEmpty(suffix);
    return NlmPerson.builder()
        .setFullName(fullName)
        .setGivenNames(givenNames)
        .setSurname(surname)
        .setSuffix(suffix)
        .build();
  }

  private boolean isEditorNode(Node node) {
    return false;
  }

  private Integer parseYear(String displayYear) {
    if (displayYear == null) {
      return null;
    }
    try {
      return Integer.valueOf(displayYear);
    } catch (NumberFormatException e) {
      // Test data suggests this is normal input. TODO: Report a warning to the client?
      return parseYearFallback(displayYear);
    }
  }


  /**
   * As a fallback for parsing the display year into an integer, treat an uninterrupted sequence of four or more digits
   * as the year.
   *
   * @param displayYear the display year given as text in the article XML
   * @return the first sequence of four or more digits as a number, if possible; else, {@code null}
   */
  private Integer parseYearFallback(String displayYear) {
    Matcher matcher = YEAR_FALLBACK.matcher(displayYear);
    if (!matcher.find()) {
      return null; // displayYear contains no sequence of four digits
    }
    String displayYearSub = matcher.group();

    if (matcher.find()) {
      // Legacy behavior was to concatenate all such matches into one number.
      log.warn("Matched more than one year-like digit string: using {}; ignoring {}", displayYearSub, matcher.group());
    }

    try {
      return Integer.valueOf(displayYearSub);
    } catch (NumberFormatException e) {
      return null; // in case so many digits were matched that the number overflows
    }
  }

  @VisibleForTesting
  static Integer parseVolumeNumber(String volume) {
    if (Strings.isNullOrEmpty(volume)) {
      return null;
    }

    // pmc2obj-v3.xslt lines 742-753.  Note that there is currently a bug in this
    // function, however--it returns 801 for the string "80(1)", instead of 80!
    Matcher match = VOL_NUM_RE.matcher(volume);
    if (match.find()) {
      return Integer.parseInt(match.group());
    } else {
      return null;
    }
  }

  /**
   * @return the value of the first and last page of the reference
   */
  private void buildPages(Element refElement, Reference reference) {
    String fPage = ParseXmlUtil.getElementSingleValue(refElement, "fpage");
    reference.setfPage(fPage);
    String lPage = ParseXmlUtil.getElementSingleValue(refElement, "lpage");
    reference.setlPage(lPage);

    if (Strings.isNullOrEmpty(fPage) && Strings.isNullOrEmpty(lPage)) {
      String range = ParseXmlUtil.getElementSingleValue(refElement, "page-range");
      if (!Strings.isNullOrEmpty(range)) {
        int index = range.indexOf("-");
        if (index > 0) {
          reference.setfPage(range.substring(0, index));
          reference.setlPage(range.substring(index + 1));
        }
      }
    }
  }

}
