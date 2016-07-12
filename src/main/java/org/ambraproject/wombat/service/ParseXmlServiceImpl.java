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
import java.util.stream.Collector;
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

    if (ParseXmlUtil.isNullOrEmpty(refListNodes)) {
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
        .map(ref -> buildReferences((Element) ref)) // Stream<List<Reference>>
        .flatMap(r -> r.stream()).collect(Collectors.toList());

    return references;
  }

  private List<Reference> buildReferences(Element refElement) {
    List<Node> citationElements =
        !ParseXmlUtil.isNullOrEmpty(refElement.getElementsByTagName("element-citation")) ?
        NodeListAdapter.wrap(refElement.getElementsByTagName("element-citation")) :
        !ParseXmlUtil.isNullOrEmpty(refElement.getElementsByTagName("mixed-citation")) ?
        NodeListAdapter.wrap(refElement.getElementsByTagName("mixed-citation")) : null;


    List<Reference> references = new ArrayList<>();
    // a <ref> element can have one or more of citation elements
    for (Node citationNode : citationElements) {
      if (citationNode.getNodeType() != Node.ELEMENT_NODE) {
        throw new RuntimeException("<element-citation> or <mixed-citation> is not an element.");
      }

      Element element = (Element) citationNode;
      Reference reference = new Reference();
      reference.setJournal(parseJournal(element));
      reference.setTitle(buildTitle(element));
      if (Strings.isNullOrEmpty(reference.getTitle())) {
        reference.setUnStructuredReference(getUnstructuredCitation(element));
      }
      reference.setAuthors(parseAuthors(element));
      reference.setCollabAuthors(parseCollabAuthors(element));
      String year = ParseXmlUtil.getElementSingleValue(element, "year");
      reference.setYear(parseYear(year));
      String volume = ParseXmlUtil.getElementSingleValue(element, "volume");
      reference.setVolume(volume);
      reference.setVolumeNumber(parseVolumeNumber(volume));
      reference.setIssue(ParseXmlUtil.getElementSingleValue(element, "issue"));
      reference.setPublisherName(ParseXmlUtil.getElementSingleValue(element, "publisher-name"));
      reference.setIsbn(ParseXmlUtil.getElementSingleValue(element, "isbn"));

      NodeList extLinkList = element.getElementsByTagName("ext-link");
      if (!ParseXmlUtil.isNullOrEmpty(extLinkList)) {
        Element extLink = (Element) extLinkList.item(0);
        String linkType = ParseXmlUtil.getElementAttributeValue(extLink, "ext-link-type");
        if (linkType.equals("uri")) {
          reference.setUri(ParseXmlUtil.getElementAttributeValue(extLink, "xlink:href"));
          // TODO: add a check for doi
          reference.setDoi(extLink.getFirstChild().getNodeValue());
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
  private String parseJournal(Element citationElement) {

    String type = ParseXmlUtil.getElementAttributeValue(citationElement, "publication-type");
    if (Strings.isNullOrEmpty(type)) {
      return null;
    }

    // pmc2obj-v3.xslt lines 730-739
    if (Reference.PublicationType.JOURNAL.getValue().equals(type)) {
      return ParseXmlUtil.getElementSingleValue(citationElement, "source");
    }
    return null;
  }

  /**
   * @return the value of the title property, retrieved from the XML
   */
  private String buildTitle(Element citationElement) {

    NodeList titleNode = citationElement.getElementsByTagName("article-title");

    if (ParseXmlUtil.isNullOrEmpty(titleNode)) {
      titleNode = citationElement.getElementsByTagName("source");
    }
    // unstructured citation
    if (ParseXmlUtil.isNullOrEmpty(titleNode)) {
      return null;
    }

    String title = ParseXmlUtil.getElementMixedContent(new StringBuilder(), titleNode.item(0)).toString();

    return (title == null) ? null : ParseXmlUtil.standardizeWhitespace(title);
  }

  private String getUnstructuredCitation(Element citationElement) {
    String unstructuredCitation = null;
    if (citationElement.getTagName().equals("mixed-citation")
        && citationElement.getFirstChild().getNodeType() == Node.TEXT_NODE) {
      unstructuredCitation = ParseXmlUtil.getElementMixedContent(new StringBuilder(), citationElement).toString();

      }
    return (unstructuredCitation == null) ? null : ParseXmlUtil.standardizeWhitespace(unstructuredCitation);
  }

  private List<NlmPerson> parseAuthors(Element citationElement) {
    List<Node> personGroupElement = NodeListAdapter.wrap(citationElement.getElementsByTagName("person-group"));
    List<NlmPerson> personGroup = null;

    if (!ParseXmlUtil.isNullOrEmpty(personGroupElement)) {
      String type;
      for (Node pgNode : personGroupElement) {
        Element pgElement = (Element) pgNode;
        List<Node> nameNodes = NodeListAdapter.wrap(pgElement.getElementsByTagName("name"));
        type = ParseXmlUtil.getElementAttributeValue(pgElement, "person-group-type");
        if (Strings.isNullOrEmpty(type) && type.equals("author")) {
          personGroup = nameNodes.stream().map(node -> parsePersonName((Element) node)).collect
              (Collectors.toList());
        }
      }
    } else {
      List<Node> nameNodes = NodeListAdapter.wrap(citationElement.getElementsByTagName("name"));
      int editorStartIndex = getEditorStartIndex(nameNodes);

      List<Node> authorNodes = editorStartIndex == -1 ? nameNodes: nameNodes.subList(0, editorStartIndex);
      personGroup = authorNodes.stream()
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

  private int getEditorStartIndex(List<Node> nameNodes) {
    // this is a very very very ugly hack for <mixed-citation> element with the following format
    // <name name-style="western">
    //   <surname>s1</surname>
    //   <given-names>g1</given-names>
    // </name> ...
    // <chapter-title>Some tite</chapter-title>. In:
    // <name name-style="western">
    //   <surname>s2</surname>
    //   <given-names>g2</given-names>
    // </name>, editors, ...
    for (int i = 0; i < nameNodes.size(); i++) {
      Node previousNode = nameNodes.get(i).getPreviousSibling();
      if (previousNode.getNodeType() == Node.TEXT_NODE && previousNode.getNodeValue().contains("In")) {
        return i;
      }

    }
    return -1;
  }

  private List<String> parseCollabAuthors(Element citationElement) {
    List<Node> collabNodes = NodeListAdapter.wrap(citationElement.getElementsByTagName("collab"));
    return collabNodes.stream()
        .map(collabNode -> collabNode.getFirstChild().getNodeValue())
        .collect(Collectors.toList());
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
  private void buildPages(Element citationElement, Reference reference) {
    String fPage = ParseXmlUtil.getElementSingleValue(citationElement, "fpage");
    reference.setfPage(fPage);
    String lPage = ParseXmlUtil.getElementSingleValue(citationElement, "lpage");
    reference.setlPage(lPage);

    if (Strings.isNullOrEmpty(fPage) && Strings.isNullOrEmpty(lPage)) {
      String range = ParseXmlUtil.getElementSingleValue(citationElement, "page-range");
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
