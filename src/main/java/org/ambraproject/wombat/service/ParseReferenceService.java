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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.management.modelmbean.XMLParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ParseReferenceService {
  private static final Logger log = LoggerFactory.getLogger(ParseReferenceService.class);
  private static final Pattern YEAR_FALLBACK = Pattern.compile("\\d{4,}");
  private static final Pattern VOL_NUM_RE = Pattern.compile("(\\d{1,})");
  private static final String WESTERN_NAME_STYLE = "western";
  private static final String EASTERN_NAME_STYLE = "eastern";
  private static final Joiner NAME_JOINER = Joiner.on(' ').skipNulls();
  private static enum CitationElement {
    ELEMENT_CITATION("element-citation"),
    MIXED_CITATION("mixed-citation"),
    NLM_CITATION("nlm-citation"); //deprecated

    String value;

    CitationElement(String citationElement) {
      this.value = citationElement;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * Builds a list of Reference objects for each <ref></ref> element
   *
   * @param refElement a <ref></ref> node which can contain (label?, (element-citation | mixed-citation | nlm-citation)+)
   * @return list of Reference objects
   */
  public List<Reference> buildReferences(Element refElement) throws XMLParseException {
    List<Node> citationElements =
        !ParseXmlUtil.isNullOrEmpty(refElement.getElementsByTagName(CitationElement.ELEMENT_CITATION.getValue())) ?
        NodeListAdapter.wrap(refElement.getElementsByTagName(CitationElement.ELEMENT_CITATION.getValue())) :
        !ParseXmlUtil.isNullOrEmpty(refElement.getElementsByTagName(CitationElement.MIXED_CITATION.getValue())) ?
        NodeListAdapter.wrap(refElement.getElementsByTagName(CitationElement.MIXED_CITATION.getValue())) :
        !ParseXmlUtil.isNullOrEmpty(refElement.getElementsByTagName(CitationElement.NLM_CITATION.getValue())) ?
        NodeListAdapter.wrap(refElement.getElementsByTagName(CitationElement.NLM_CITATION.getValue())) : null;


    List<Reference> references = new ArrayList<>();
    // a <ref></ref> element can have one or more of citation elements
    for (Node citationNode : citationElements) {
      if (citationNode.getNodeType() != Node.ELEMENT_NODE) {
        throw new XMLParseException("<element-citation>, <mixed-citation>, <nlm-citation> is not an element" +
            ".");
      }

      Element element = (Element) citationNode;
      String unstructuredReference = null;
      String uri = null;
      String doi = null;
      String[] pages = new String[2];
      String year = ParseXmlUtil.getElementSingleValue(element, "year");
      String volume = ParseXmlUtil.getElementSingleValue(element, "volume");
      String title = buildTitle(element);

      if (Strings.isNullOrEmpty(title)) {
        unstructuredReference = getUnstructuredCitation(element);
      }

      NodeList extLinkList = element.getElementsByTagName("ext-link");
      if (!ParseXmlUtil.isNullOrEmpty(extLinkList)) {
        Element extLink = (Element) extLinkList.item(0);
        String linkType = ParseXmlUtil.getElementAttributeValue(extLink, "ext-link-type");
        if (linkType.equals("uri")) {
          uri = ParseXmlUtil.getElementAttributeValue(extLink, "xlink:href");
          // TODO: add a check for doi
          doi = extLink.getFirstChild().getNodeValue();
        }
      }
      buildPages(element, pages);

      Reference reference  = Reference.build()
          .setJournal(parseJournal(element))
          .setTitle(title)
          .setChapterTitle(buildChapterTitle(element))
          .setUnStructuredReference(unstructuredReference)
          .setAuthors(parseAuthors(element))
          .setCollabAuthors(parseCollabAuthors(element))
          .setYear(parseYear(year))
          .setVolume(volume)
          .setVolumeNumber(parseVolumeNumber(volume))
          .setIssue(ParseXmlUtil.getElementSingleValue(element, "issue"))
          .setPublisherName(ParseXmlUtil.getElementSingleValue(element, "publisher-name"))
          .setIsbn(ParseXmlUtil.getElementSingleValue(element, "isbn"))
          .setUri(uri)
          .setDoi(doi)
          .setfPage(pages[0])
          .setlPage(pages[1])
          .build();

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

  /**
   *
   * @return the text within <mixed-citation></mixed-citation> with no structure
   */
  private String getUnstructuredCitation(Element citationElement) {
    String unstructuredCitation = null;
    if (citationElement.getTagName().equals(CitationElement.MIXED_CITATION.getValue())
        && citationElement.getFirstChild().getNodeType() == Node.TEXT_NODE) {
      unstructuredCitation = ParseXmlUtil.getElementMixedContent(new StringBuilder(), citationElement).toString();

    }
    return (unstructuredCitation == null) ? null : ParseXmlUtil.standardizeWhitespace(unstructuredCitation);
  }

  /**
   * @return book chapter title
   */
  private String buildChapterTitle(Element citationElement) {
    NodeList chapterTitleNode = citationElement.getElementsByTagName("chapter-title");
    if (ParseXmlUtil.isNullOrEmpty(chapterTitleNode)) {
      return null;
    }

    String chapterTitle = ParseXmlUtil.getElementMixedContent(new StringBuilder(),
        chapterTitleNode.item(0)).toString();

    return (chapterTitle == null) ? null : ParseXmlUtil.standardizeWhitespace(chapterTitle);

  }

  private List<NlmPerson> parseAuthors(Element citationElement) {
    List<Node> personGroupElement = NodeListAdapter.wrap(citationElement.getElementsByTagName("person-group"));
    List<NlmPerson> personGroup = new ArrayList<>();

    if (!ParseXmlUtil.isNullOrEmpty(personGroupElement)) {
      String type;
      for (Node pgNode : personGroupElement) {
        Element pgElement = (Element) pgNode;
        List<Node> nameNodes = NodeListAdapter.wrap(pgElement.getElementsByTagName("name"));
        type = ParseXmlUtil.getElementAttributeValue(pgElement, "person-group-type");
        if (!Strings.isNullOrEmpty(type) && type.equals("author")) {
          personGroup = nameNodes.stream().map(node -> parsePersonNameWrapper((Element) node)).collect
              (Collectors.toList());
        }
      }
    } else {
      List<Node> nameNodes = NodeListAdapter.wrap(citationElement.getElementsByTagName("name"));
      int editorStartIndex = getEditorStartIndex(nameNodes);

      List<Node> authorNodes = editorStartIndex == -1 ? nameNodes: nameNodes.subList(0, editorStartIndex);
      personGroup = authorNodes.stream()
          .map(node -> parsePersonNameWrapper((Element) node))
          .collect(Collectors.toList());

    }
    return personGroup;
  }

  private NlmPerson parsePersonNameWrapper(Element nameElement) {
    try {
      return parsePersonName(nameElement);
    } catch (XmlContentException xmlCE) {
      throw new RuntimeException(xmlCE);
    }

  }
  private NlmPerson parsePersonName(Element nameElement) throws XmlContentException {
    String nameStyle = ParseXmlUtil.getElementAttributeValue(nameElement, "name-style");
    String surname = ParseXmlUtil.getElementSingleValue(nameElement, "surname");
    String givenNames = ParseXmlUtil.getElementSingleValue(nameElement, "given-names");
    String suffix = ParseXmlUtil.getElementSingleValue(nameElement, "suffix");

    if (surname == null) {
      throw new XmlContentException("Required surname is omitted from node: " + nameElement.getNodeName());
    }

    String[] fullNameParts;
    if (WESTERN_NAME_STYLE.equals(nameStyle)) {
      fullNameParts = new String[]{givenNames, surname, suffix};
    } else if (EASTERN_NAME_STYLE.equals(nameStyle)) {
      fullNameParts = new String[]{surname, givenNames, suffix};
    } else {
      throw new XmlContentException("Invalid name-style: " + nameStyle);
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
  private String[] buildPages(Element citationElement, String[] pages) {
    String fPage = ParseXmlUtil.getElementSingleValue(citationElement, "fpage");
    pages[0] = fPage;
    String lPage = ParseXmlUtil.getElementSingleValue(citationElement, "lpage");
    pages[1] = lPage;

    if (Strings.isNullOrEmpty(fPage) && Strings.isNullOrEmpty(lPage)) {
      String range = ParseXmlUtil.getElementSingleValue(citationElement, "page-range");
      if (!Strings.isNullOrEmpty(range)) {
        int index = range.indexOf("-");
        if (index > 0) {
          pages[0] = range.substring(0, index);
          pages[1] = range.substring(index + 1);
        }
      }
    }
    return pages;
  }

}


