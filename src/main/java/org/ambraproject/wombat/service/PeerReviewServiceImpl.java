package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.ambraproject.wombat.service.remote.ContentKey;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;


public class PeerReviewServiceImpl implements PeerReviewService {

  @Autowired
  private CorpusContentApi corpusContentApi;

  public String asHtml(Map<String, ?> itemTable) throws IOException {
    String xml = getXml(itemTable);
    return xmlToHtml(xml);
  }

  /**
   * Convert peer review XML to HTML
   *
   * @param peerReviewXml
   * @return an HTML representation of peer review content
   */
  private String xmlToHtml(String peerReviewXml) {
    if (peerReviewXml == null) return null;

    XMLReader xmlReader = null;
    try {
      SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
      xmlReader = sp.getXMLReader();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
    SAXSource xmlSource = new SAXSource(xmlReader, new InputSource(new StringReader(peerReviewXml)));

    StreamSource xslSource = new StreamSource(new File("src/main/webapp/WEB-INF/themes/desktop/xform/peer-review-transform.xsl"));
    StringWriter htmlWriter = new StringWriter();
    try {
      Transformer transformer = TransformerFactory.newInstance().newTransformer(xslSource);
      transformer.transform(xmlSource, new StreamResult(htmlWriter));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }

    return htmlWriter.toString();
  }

  /**
   * Aggregate the XML for all rounds of Peer Review
   *
   * @return entirety of peer review XML content
   * @throws IOException
   * @param itemTable an Article's itemTable
   */
  private String getXml(Map<String, ?> itemTable) throws IOException {
    List<Map<String, ?>> peerReviewItems = new ArrayList<>();
    for (Object itemObj : new TreeMap(itemTable).values()) {
      if (((Map<String, ?>) itemObj).get("itemType").equals("reviewLetter")) {
        peerReviewItems.add((Map<String, ?>) itemObj);
      }
    }

    List<String> peerReviewFileContents = new ArrayList<>();
    for (Map<String, ?> itemMetadata : peerReviewItems) {
      String content = getAssetContent(itemMetadata);

      // TODO: include formal accept letter (specific-use="acceptance-letter"), though for now we haven't figured out how to display it.
      if (content.contains("specific-use=\"acceptance-letter\"")) {
        continue;
      }

      // strip the XML declaration, which is not allowed when these are aggregated
      content = content.replaceAll("<\\?xml(.+?)\\?>", "");
      peerReviewFileContents.add(content);
    }

    if (peerReviewFileContents.isEmpty()) {
      return null;
    }

    // Group into revisions, all but first include an author response and a decision letter
    List<String> partitionableResponses = peerReviewFileContents.subList(1, peerReviewFileContents.size());
    List<List<String>> revisions = new ArrayList<>(Lists.partition(partitionableResponses, 2));
    List<String> firstSubmission = new ArrayList<>();
    firstSubmission.add(peerReviewFileContents.get(0));
    revisions.add(0, firstSubmission);

    String peerReviewContent  = "";
    for ( List<String> responses : revisions) {
      peerReviewContent  += "<revision>" + String.join("", responses) + "</revision>";
    }

    // wrap it in a root node
    peerReviewContent = "<peer-review>" + peerReviewContent  + "</peer-review>";
    return peerReviewContent;
  }

  /**
   * Fetch the content of an individual peer review asset from the content repository
   *
   * @param itemMetadata
   * @return the content of the peer review asset
   * @throws IOException
   */
  String getAssetContent(Map<String, ?> itemMetadata) throws IOException {
    Map<String, ?> files = (Map<String, ?>) itemMetadata.get("files");
    Map<String, ?> letter = (Map<String, ?>) files.get("letter");

    String crepoKey = (String) letter.get("crepoKey");
    UUID uuid = UUID.fromString((String) letter.get("crepoUuid"));
    ContentKey contentKey = ContentKey.createForUuid(crepoKey, uuid);

    CloseableHttpResponse response = corpusContentApi.request(contentKey, ImmutableList.of());
    String responseBody = EntityUtils.toString(response.getEntity());
    return responseBody;
  }
}
