package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import org.junit.Test;

import org.ambraproject.wombat.service.remote.ContentKey;

import static java.lang.String.format;
import static junit.framework.TestCase.assertNull;
import static org.ambraproject.wombat.service.PeerReviewServiceImpl.DEFAULT_PEER_REVIEW_XSL;
import static org.ambraproject.wombat.util.FileUtils.deserialize;
import static org.ambraproject.wombat.util.FileUtils.getFile;
import static org.ambraproject.wombat.util.FileUtils.read;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

@ContextConfiguration(classes = {PeerReviewServiceImplTest.class})
public class PeerReviewServiceImplTest extends AbstractJUnit4SpringContextTests {

  private PeerReviewServiceImpl service = new PeerReviewServiceImpl();

  @Test
  public void testAsHtml() throws IOException {
    ImmutableMap<String, ? extends Map<String, ?>> itemTable = new ImmutableMap.Builder<String, Map<String, ?>>()
        .put("10.1371/journal.pone.0207232.r001", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r001",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r001.xml",
                "crepoUuid", UUID.randomUUID().toString()
            ))
        ))
        .put("10.1371/journal.pone.0207232.r002", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r002",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r002.xml",
                "crepoUuid", UUID.randomUUID().toString()
            ))
        ))
        .put("10.1371/journal.pone.0207232.r003", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r003",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r003.xml",
                "crepoUuid", UUID.randomUUID().toString()
            ))
        ))
        .put("10.1371/journal.pone.0207232.r004", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r004",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r004.xml",
                "crepoUuid", UUID.randomUUID().toString()
            ))
        ))
        .put("10.1371/journal.pone.0207232.r005", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r005",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r005.xml",
                "crepoUuid", UUID.randomUUID().toString()
            ))
        ))
        .put("10.1371/journal.pone.0207232.r006", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r006",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r006.xml",
                "crepoUuid", UUID.randomUUID().toString()
            ))
        )).build();


    PeerReviewService serviceWithMockedContent = getServiceWithMockedContent();

    String html = serviceWithMockedContent.asHtml(itemTable);
    Document d = Jsoup.parse(html);

    assertThat(d.select(".review-history .revision").get(0).text(), containsString("Original Submission"));
    assertThat(d.select(".review-history .revision").get(0).text(), containsString("June 1, 2018"));
    assertThat(d.select(".review-history .decision-letter").get(0).text(), containsString("InitialDecisionLetterSampleBody"));
    assertThat(d.select(".review-history .revision").get(1).text(), containsString("Revision 1"));
    assertThat(d.select(".review-history .author-response").get(0).text(), containsString("FirstRoundAuthorResponseSampleBody"));
    assertThat(d.select(".review-history .decision-letter").get(1).text(), containsString("FirstRoundDecisionLetterSampleBody"));
    assertThat(d.select(".review-history .revision").get(2).text(), containsString("Revision 2"));
    assertThat(d.select(".review-history .author-response").get(1).text(), containsString("SecondRoundAuthorResponseSampleBody"));
    assertThat(d.select(".review-history .decision-letter").get(2).text(), containsString("SecondRoundDecisionLetterSampleBody"));
    assertThat(d.select(".review-history .revision").get(3).text(), containsString("Formally Accepted"));
    assertThat(d.select(".review-history .acceptance-letter").get(0).text(), containsString("AcceptanceLetterSampleBody"));
  }

  @Test
  public void testSubarticleDoi() throws IOException {
    ImmutableMap<String, ? extends Map<String, ?>> itemTable = new ImmutableMap.Builder<String, Map<String, ?>>()
        .put("10.1371/journal.pone.0207232.r001", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r001",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r001.xml",
                "crepoUuid", UUID.randomUUID().toString()
            ))
        )).build();
    
    PeerReviewService serviceWithMockedContent = getServiceWithMockedContent();
    String html = serviceWithMockedContent.asHtml(itemTable);
    Document d = Jsoup.parse(html);

    assertThat(d.select(".review-history .review__doi").get(0).text(), containsString("https://doi.org/10.1371/journal.pone.0207232.r001"));
  }

  PeerReviewServiceImpl getServiceWithMockedContent() {
    return new PeerReviewServiceImpl() {

      @Override
      String getArticleReceivedDate(Map<String,?> itemTable) throws IOException {
        return "June 1, 2018";
      }

      @Override
      String getContent(ContentKey contentKey) throws IOException {
        String crepoKey = (String) contentKey.getKey();

        String letterContent = null;
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r001.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article specific-use=\"decision-letter\" id=\"pone.0207232.r001\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>0</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body><p>InitialDecisionLetterSampleBody</p></body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r002.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article article-type=\"author-comment\" id=\"pone.0207232.r002\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>1</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body><p>FirstRoundAuthorResponseSampleBody</p></body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r003.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article specific-use=\"decision-letter\" id=\"pone.0207232.r003\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>1</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body><p>FirstRoundDecisionLetterSampleBody</p></body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r004.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article article-type=\"author-comment\" id=\"pone.0207232.r004\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>2</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body><p>SecondRoundAuthorResponseSampleBody</p></body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r005.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article specific-use=\"decision-letter\" id=\"pone.0207232.r005\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>2</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body><p>SecondRoundDecisionLetterSampleBody</p></body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r006.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article specific-use=\"acceptance-letter\" id=\"pone.0207232.r006\">" +
              "<front-stub></front-stub>" +
              "<body><p>AcceptanceLetterSampleBody</p></body></sub-article>";
        }
        return letterContent;
      }
    };
  }

  @Test
  public void testAsHtmlHandlesNoPeerReviewItems() throws IOException {
    ImmutableMap<String, ? extends Map<String, ?>> itemTable = ImmutableMap.of(
        "10.1371/journal.pone.0207232.t001", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.t001",
            "itemType", "table"
        )
    );
    String html = service.asHtml(itemTable);
    assertNull(html);
  }

  @Test
  public void testAttachmentLink() throws IOException {

    PeerReviewServiceImpl spy = spy(service);

    doAnswer(invocation -> read(prefix(getFilename(invocation.getArgument(0).toString()).toLowerCase())))
      .when(spy).getContent(any(ContentKey.class));

    Map<String,?> itemTable = (Map<String,?>) deserialize(getFile(prefix("item-table.pone.0207232.ser")));

    String html = spy.asHtml(itemTable);

    Document doc = Jsoup.parse(html);

    Element firstAttachment = doc.select(".review-history .review-files .supplementary-material").first();
    Element anchorElement = firstAttachment.select("a").first();

    assertThat(anchorElement.attr("href"), containsString("file?id=10.1371/journal.pone.0207232.s001&type=supplementary"));
    assertThat(anchorElement.attr("title"), containsString("Download .pdf file"));
  }

  @Test
  public void testAttachmentAnchorTitles() throws IOException {

    String xml = read(prefix("peer-review-attachment-filenames.pone.0207232.xml"));
 
    String html = service.transformXmlToHtml(xml, DEFAULT_PEER_REVIEW_XSL);

    Document doc = Jsoup.parse(html);

    String[] expectedAnchorTitles = { "docwithnodotspdf", ".docx", ".docx" };

    for (int i=0; i<3; ++i) {
      Element attachment = doc.select(".review-history .review-files .supplementary-material").get(i);
      Element anchor = attachment.select("a").first();
      assertThat(anchor.attr("href"), containsString(format("file?id=10.1371/journal.pone.0207232.s00%d&type=supplementary",(i+1))));
      assertThat(anchor.attr("title"), containsString(format("Download %s file", expectedAnchorTitles[i])));
    }
  }

  @Test
  public void testAuthorResponse() {
    String xml = read(prefix("peer-review.pone.0207232.xml"));
 
    String html = service.transformXmlToHtml(xml, DEFAULT_PEER_REVIEW_XSL);

    Document doc = Jsoup.parse(html);

    // ORIGINAL SUBMISSION 

    assertThat(doc.select(".review-history .revision .letter__title").get(0).text(), containsString("Original Submission"));
    assertThat(doc.select(".review-history .revision .letter__date").get(0).text(), containsString("June 1, 2018"));

    assertThat(doc.select(".review-history .decision-letter .letter__date").get(0).text(), containsString("September 12, 2018"));
    assertThat(doc.select(".review-history .decision-letter span[itemprop=name]").get(0).text(), containsString("Qinghui Zhang, Editor"));

    // REVISION 1

    assertThat(doc.select(".review-history .revision .letter__title").get(1).text(), containsString("Revision 1"));

    Element authorResponseDiv = doc.select(".author-response").first();
    assertThat(authorResponseDiv.text(), containsString("Author Response"));

    Element attachmentElem = authorResponseDiv.select(".review-files .supplementary-material").first();
    assertThat(attachmentElem.text(), containsString("Response to Reviewers.docx"));

    assertThat(doc.select(".review-history .decision-letter .letter__date").get(1).text(), containsString("October 9, 2018"));
    assertThat(doc.select(".review-history .decision-letter span[itemprop=name]").get(1).text(), containsString("Qinghui Zhang, Editor"));
  }

  @Test
  public void testGetReceivedDate() throws IOException {
    String expectedDates[] = {
      "January 1, 2018",
      "January 10, 2018",
      "October 1, 2018",
      "October 10, 2018"
    };

    for (int i=0; i < expectedDates.length; ++i) {
      String receivedDate = read(prefix("article-received-date/" + format("received-date.%d.xml",i)));
      assertThat(service.parseArticleReceivedDate(receivedDate), is(expectedDates[i]));
    }
  }

  @Test
  public void testFormatDate() {
    String acceptedDates[] = {
      "9 Oct 2018",
      "15 Nov 2018",
      "1 1 2000",
      "11 15 2010",
      "   10\r\n Feb\t\t 1999 "
    };

    String expectedDates[] = {
      "October 9, 2018",
      "November 15, 2018",
      "January 1, 2000",
      "November 15, 2010",
      "February 10, 1999"
    };

    for (int i=0; i < acceptedDates.length; ++i) {
      assertThat(service.formatDate(acceptedDates[i]), is(expectedDates[i]));
    }

    String rejectedDates[] = {
      "Jan 1, 2010",
      "02/10/2003",
      "2018-12-10",
      "not-a-date",
      ""
    };

    for (int i=0; i < rejectedDates.length; ++i) {
      assertThat(service.formatDate(rejectedDates[i]), is(""));
    }
  }

  private String getFilename(String uuidKey) {
    // [key: info:doi/10.1371/journal.pone.0207232.r001.xml, uuid: cbcdde53-66f4-4885-85b0-50966be2ba28]
    return uuidKey.substring(uuidKey.indexOf("10.1371/journal.")+16, uuidKey.lastIndexOf(", uuid:"));
  }

  private String prefix(String file) {
    return "peer-review/" + file;
  }
}
