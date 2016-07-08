package org.ambraproject.wombat.service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import org.ambraproject.wombat.model.Reference;
import org.ambraproject.wombat.model.ReferencePerson;
import org.ambraproject.wombat.util.NodeListAdapter;
import org.apache.commons.lang3.StringEscapeUtils;
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
  /**
   * Pattern describing what is considered a DOI (as opposed to another kind of URI) if it appears in the "ext-link"
   * element of a citation.
   */
  private static final Pattern EXT_LINK_DOI = Pattern.compile("^\\d+\\.\\d+\\/");
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
    references = refNodes.stream().map(ref -> buildReferences((Element) ref)).collect(Collectors.toList());

    return references;
  }

  private Reference buildReferences(Element refElement) {
    Reference reference = new Reference();
    setTypeAndJournal(refElement, reference);
    reference.setTitle(buildTitle(refElement));
    parsePersonGroup(refElement, reference);
    String year = getElementSingleValue(refElement, "year");
    reference.setYear(parseYear(year));
    reference.setMonth(getElementSingleValue(refElement, "month"));
    reference.setDay(getElementSingleValue(refElement, "day"));
    String volume = getElementSingleValue(refElement, "volume");
    reference.setVolume(volume);
    reference.setVolumeNumber(parseVolumeNumber(volume));
    reference.setIssue(getElementSingleValue(refElement, "issue"));
    reference.setPublisherName(getElementSingleValue(refElement, "publisher-name"));


    NodeList extLinkList = refElement.getElementsByTagName("ext-link");
    if (extLinkList != null && extLinkList.getLength() > 0) {
      Element extLink = (Element) extLinkList.item(0);
      String linkType = getElementAttributeValue(extLink, "ext-link-type");
      if (linkType.equals("uri")) {
        // TODO: add doi validation
        reference.setDoi(getElementAttributeValue(extLink, "xlink:href"));
      }
    }
    buildPages(refElement, reference);

    return reference;
  }

  private String getElementSingleValue(Element element, String name) {
    NodeList node = element.getElementsByTagName(name);
    if (node == null || node.getLength() == 0) {
      return null;
    }
    Node firstChild = node.item(0).getFirstChild();
    if (firstChild == null || firstChild.getNodeType() != Node.TEXT_NODE) {
      return null;
    }
    return firstChild.getNodeValue();
  }

  private String getElementAttributeValue(Element element, String attributeName) {
    return element == null ? null :element.getAttribute(attributeName);
  }

  /**
   * Sets the citationType and journal properties of a reference appropriately based on the XML.
   */
  private void setTypeAndJournal(Element element, Reference reference) {
    Element elementCitation = element.getElementsByTagName("element-citation") != null ?
        (Element) element.getElementsByTagName("element-citation").item(0) : null;
    Element mixedCitation=   element.getElementsByTagName("mixed-citation") != null ? (Element) element
        .getElementsByTagName("mixed-citation").item(0) : null;

    Element citationElement = elementCitation != null ? elementCitation : mixedCitation != null ?
        mixedCitation : null;

    String type = getElementAttributeValue(citationElement, "publication-type");
    if (Strings.isNullOrEmpty(type)) {
      return;
    }

    Reference.PublicationType pubType;
    // pmc2obj-v3.xslt lines 730-739
    if ("journal".equals(type)) {
      pubType = Reference.PublicationType.ARTICLE;
      reference.setJournal(getElementSingleValue(element, "source"));
    } else if ("book".equals(type)) {
      pubType = Reference.PublicationType.BOOK;
    } else {
      pubType = Reference.PublicationType.MISC;
    }
    reference.setPublicationType(pubType.toString());
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
      return null;
    }

    String title = getElementMixedContent(new StringBuilder(), titleNode.item(0)).toString();

    return (title == null) ? null : standardizeWhitespace(title);
  }

  /**
   * Build a text field by partially reconstructing the node's content as XML. The output is text content between the
   * node's two tags, including nested XML tags with attributes, but not this node's outer tags. Continuous substrings
   * of whitespace may be substituted with other whitespace. Markup characters are escaped.
   * <p/>
   * This method is used instead of an appropriate XML library in order to match the behavior of legacy code, for now.
   *
   * @param node the node containing the text we are retrieving
   * @return the marked-up node contents
   */
  private static StringBuilder getElementMixedContent(StringBuilder mixedContent, Node node) {
    List<Node> children = NodeListAdapter.wrap(node.getChildNodes());
    for (Node child : children) {
      switch (child.getNodeType()) {
        case Node.TEXT_NODE:
          appendTextNode(mixedContent, child);
          break;
        case Node.ELEMENT_NODE:
          appendElementNode(mixedContent, child);
          break;
        default:
          log.warn("Skipping node (name={}, type={})", child.getNodeName(), child.getNodeType());
      }
    }
    return mixedContent;
  }


  private static void appendTextNode(StringBuilder nodeContent, Node child) {
    String text = child.getNodeValue();
    text = StringEscapeUtils.escapeXml11(text);
    nodeContent.append(text);
  }

  private static void appendElementNode(StringBuilder mixedContent, Node child) {
    String nodeName = child.getNodeName();
    mixedContent.append('<').append(nodeName);
    List<Node> attributes = NodeListAdapter.wrap(child.getAttributes());

    // Search for xlink attributes and declare the xlink namespace if found
    // TODO Better way? This is probably a symptom of needing to use a proper XML library here in the first place.
    for (Node attribute : attributes) {
      if (attribute.getNodeName().startsWith("xlink:")) {
        mixedContent.append(" xmlns:xlink=\"http://www.w3.org/1999/xlink\"");
        break;
      }
    }

    for (Node attribute : attributes) {
      mixedContent.append(' ').append(attribute.toString());
    }

    mixedContent.append('>');
    getElementMixedContent(mixedContent, child);
    mixedContent.append("</").append(nodeName).append('>');
  }

  protected static String standardizeWhitespace(CharSequence text) {
    return (text == null) ? null : CharMatcher.WHITESPACE.trimAndCollapseFrom(text, ' ');
  }

  private void parsePersonGroup(Element element, Reference reference) {
    List<Node> personGroupElement = NodeListAdapter.wrap(element.getElementsByTagName("person-group"));
    List<ReferencePerson> personGroup = null;
    String type = "";
    for (Node pgNode : personGroupElement) {
      Element pgElement = (Element) pgNode;
      List<Node> personNodes = NodeListAdapter.wrap(pgElement.getElementsByTagName("name"));
      type = getElementAttributeValue(pgElement, "person-group-type");
      personGroup = new ArrayList<>();
      for (Node node : personNodes) {
        ReferencePerson referencePerson = parsePersonName((Element) node);
        personGroup.add(referencePerson);
      }
      if (type.equals("author")) {
        reference.setAuthors(personGroup);
      } else if (type.equals("editor")) {
        reference.setEditors(personGroup);
      }
    }
  }

  private ReferencePerson parsePersonName(Element nameElement) {
    String nameStyle = getElementAttributeValue(nameElement, "name-style");
    String surname = getElementSingleValue(nameElement, "surname");
    String givenNames = getElementSingleValue(nameElement, "given-names");
    String suffix = getElementSingleValue(nameElement, "suffix");

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
    return new ReferencePerson.Builder(fullName)
        .givenNames(givenNames)
        .surname(surname)
        .suffix(suffix)
        .build();
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
    String fPage = getElementSingleValue(refElement, "fpage");
    reference.setfPage(fPage);
    String lPage = getElementSingleValue(refElement, "lpage");
    reference.setlPage(lPage);

    if (Strings.isNullOrEmpty(fPage) && Strings.isNullOrEmpty(lPage)) {
      String range = getElementSingleValue(refElement, "page-range");
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
