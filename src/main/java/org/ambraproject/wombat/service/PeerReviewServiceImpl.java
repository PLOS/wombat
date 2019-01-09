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
    String xml = toXml(itemTable);
    return toHtml(xml);
  }

  /**
   * Convert peer review XML to HTML
   *
   * @param peerReviewContent
   * @return an HTML representation of peer review content
   */
  private String toHtml(String peerReviewContent) {
    if (peerReviewContent == null) return null;

    // TODO: Why is xlink: namespace not recognized by transform?
    peerReviewContent = peerReviewContent.replaceAll("xlink:", "");

    // TODO: this is a hack to remove html escape sequences from my sample non-typeset ingested content
    peerReviewContent = peerReviewContent.replaceAll("&", "BLAH");

    XMLReader xmlReader = null;
    try {
      SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
      xmlReader = sp.getXMLReader();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
    SAXSource xmlSource = new SAXSource(xmlReader, new InputSource(new StringReader(peerReviewContent)));

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
   * @return XML content
   * @throws IOException
   * @param itemTable
   */
  private String toXml(Map<String, ?> itemTable) throws IOException {
    List<Map<String, ?>> peerReviewItems = new ArrayList<>();
    for (Object itemObj : new TreeMap(itemTable).values()) {
      if (((Map<String, ?>) itemObj).get("itemType").equals("reviewLetter")) {
        peerReviewItems.add((Map<String, ?>) itemObj);
      }
    }

    List<String> peerReviewFileContents = new ArrayList<>();
    for (Map<String, ?> itemMetadata : peerReviewItems) {
      String content = peerReviewFileContent(itemMetadata);
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
   * Fetch the content of a peer review asset from the content repository
   *
   * @param itemMetadata
   * @return the content of the peer review asset
   * @throws IOException
   */
  private String peerReviewFileContent(Map<String, ?> itemMetadata) throws IOException {
    Map<String, ?> files = (Map<String, ?>) itemMetadata.get("files");
    Map<String, ?> fileMetadata = (Map<String, ?>) files.get("letter");

    String crepoKey = (String) fileMetadata.get("crepoKey");
    UUID uuid = UUID.fromString((String) fileMetadata.get("crepoUuid"));
    ContentKey contentKey = ContentKey.createForUuid(crepoKey, uuid);

    CloseableHttpResponse response = corpusContentApi.request(contentKey, ImmutableList.of());
    String responseBody = EntityUtils.toString(response.getEntity());
    return responseBody;
  }

}
