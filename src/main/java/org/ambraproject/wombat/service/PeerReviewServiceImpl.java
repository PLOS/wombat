package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class PeerReviewServiceImpl implements PeerReviewService {

  public static final String DEFAULT_PEER_REVIEW_XSL = "peer-review-transform.xsl";

  @Autowired
  private CorpusContentApi corpusContentApi;

  /**
   * Given an article's items, generates an HTML snippet representing the Peer Review tab of an article page.
   * @param itemTable a list of article items as per ArticleService.getItemTable
   * @return an HTML snippet
   * @throws IOException
   */
  public String asHtml(Map<String, ?> itemTable) throws IOException {
    List<Map<String, ?>> reviewLetterItems = getReviewItems(itemTable);
    if (reviewLetterItems.isEmpty()) return null;

    String xml = getAllReviewsAsXml(reviewLetterItems);
    String html = transformXmlToHtml(xml, DEFAULT_PEER_REVIEW_XSL);
    return html;
  }

  /**
   * Convert peer review XML to HTML
   *
   * @param allReviewsXml
   * @param xsl
   * @return an HTML representation of peer review content
   */
  String transformXmlToHtml(String allReviewsXml, String xsl) {
    XMLReader xmlReader = null;
    try {
      SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
      xmlReader = sp.getXMLReader();
    } catch (ParserConfigurationException | SAXException e) {
      throw new RuntimeException(e);
    }
    SAXSource xmlSource = new SAXSource(xmlReader, new InputSource(new StringReader(allReviewsXml)));

    ClassLoader classLoader = getClass().getClassLoader();
    InputStream stream = classLoader.getResourceAsStream(xsl);
    StreamSource xslSource = new StreamSource(stream);

    StringWriter htmlWriter = new StringWriter();
    try {
      Transformer transformer = SiteTransformerFactory.newTransformerFactory().newTransformer(xslSource);
      transformer.transform(xmlSource, new StreamResult(htmlWriter));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }

    return htmlWriter.toString();
  }

  /**
   * Aggregate the XML for all rounds of Peer Review
   *
   * @param reviewLetterItems metadata about review letters
   * @return entirety of peer review XML content
   * @throws IOException
   */
  String getAllReviewsAsXml(List<Map<String, ?>> reviewLetterItems) throws IOException {
    List<String> reviewLetters = new ArrayList<>();
    String acceptanceLetter = "";
    for (Map<String, ?> reviewLetterItem : reviewLetterItems) {
      String xml = getReviewXml(reviewLetterItem);

      // strip the XML declaration, which is not allowed when these are aggregated
      xml = xml.replaceAll("<\\?xml(.+?)\\?>", "");

      Document parsed = Jsoup.parse(xml, "", Parser.xmlParser());
      Elements subArticle = parsed.select("sub-article");
      if(!subArticle.isEmpty() && subArticle.attr("specific-use").equals("acceptance-letter")){
        acceptanceLetter = xml;
        continue;
      }

      reviewLetters.add(xml);
    }

    // group letters by revision
    Map<Integer, List<String>> lettersByRevision = new TreeMap<>();
    for (String letter : reviewLetters) {
      String revisionAsString = XmlUtil.extractText(XmlUtil.extractElement(letter, "meta-value"));
      Integer revisionNumber = new Integer(revisionAsString);

      if (lettersByRevision.get(revisionNumber) == null) {
        lettersByRevision.put(revisionNumber, new ArrayList<>());
      }
      lettersByRevision.get(revisionNumber).add(letter);
    }

    String peerReviewContent = "";
    for (List<String> revisionLetters : lettersByRevision.values()) {
      peerReviewContent += "<revision>" + String.join("", revisionLetters) + "</revision>";
    }

    // append the acceptance letter
    peerReviewContent += acceptanceLetter;

    // wrap it in a root node
    peerReviewContent = "<peer-review>" + peerReviewContent + "</peer-review>";
    return peerReviewContent;
  }

  /**
   * Get the sublist of those items which contain peer review content
   * @param itemTable
   * @return a list of review item
   */
  List<Map<String, ?>> getReviewItems(Map<String, ?> itemTable) {
    List<Map<String, ?>> reviewLetterItems = new ArrayList<>();
    for (Object itemObj : new TreeMap(itemTable).values()) {
      if (((Map<String, ?>) itemObj).get("itemType").equals("reviewLetter")) {
        reviewLetterItems.add((Map<String, ?>) itemObj);
      }
    }
    return reviewLetterItems;
  }

  /**
   * Fetch the content of an individual peer review asset from the content repository
   *
   * @param metadata
   * @return the content of the peer review asset
   * @throws IOException
   */
  String getReviewXml(Map<String, ?> metadata) throws IOException {
    Map<String, ?> files = (Map<String, ?>) metadata.get("files");
    Map<String, ?> contentRepoMetadata = (Map<String, ?>) files.get("letter");

    String crepoKey = (String) contentRepoMetadata.get("crepoKey");
    UUID uuid = UUID.fromString((String) contentRepoMetadata.get("crepoUuid"));
    ContentKey contentKey = ContentKey.createForUuid(crepoKey, uuid);

    return getContent(contentKey);
  }

  String getContent(ContentKey contentKey) throws IOException {
    CloseableHttpResponse response = corpusContentApi.request(contentKey, ImmutableList.of());
    String letterContent = EntityUtils.toString(response.getEntity(), "UTF-8");
    return letterContent;
  }
}
