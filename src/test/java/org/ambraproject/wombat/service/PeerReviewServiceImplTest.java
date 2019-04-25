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
import static org.ambraproject.wombat.util.FileUtils.deserialize;
import static org.ambraproject.wombat.util.FileUtils.getFile;
import static org.ambraproject.wombat.util.FileUtils.read;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

@ContextConfiguration(classes = {PeerReviewServiceImplTest.class})
public class PeerReviewServiceImplTest extends AbstractJUnit4SpringContextTests {

  private PeerReviewServiceImpl service = new PeerReviewServiceImpl();

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
 
    String html = service.transformXmlToHtml(xml);

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
  public void testTransformXmlToHtml() {
    String xml = read(prefix("peer-review.pone.0207232.xml"));
    String html = service.transformXmlToHtml(xml);
    Document doc = Jsoup.parse(html);

    // ORIGINAL SUBMISSION

    Element firstHeader = doc.select(".review-history .revision").get(0);
    assertThat(firstHeader.select(".letter__title").text(), containsString("Original Submission"));
    assertThat(firstHeader.select(".letter__date").text(), containsString("June 1, 2018"));

    Element firstDecision = doc.select(".review-history .decision-letter").get(0);
    assertThat(firstDecision.select(".letter__date").text(), containsString("September 12, 2018"));
    assertEquals("Qinghui Zhang, Editor, Surachai Supattapone, Editor", firstDecision.select(".letter_author").get(0).text());
    assertThat(firstDecision.select(".letter__body").text(), containsString("Thank you for submitting"));
    assertThat(firstDecision.select(".review__doi a[href]").text(), containsString("https://doi.org/10.1371/journal.pone.0207232.r001"));

    // REVISION 1

    Element secondHeader = doc.select(".review-history .revision").get(1);
    assertThat(secondHeader.select(".letter__title").text(), containsString("Revision 1"));

    Element authorResponse = doc.select(".author-response").get(0);
    assertThat(authorResponse.text(), containsString("Author Response"));
    assertThat(authorResponse.select(".letter__body").text(), containsString("[Response to Reviewers]"));
    assertThat(authorResponse.select(".supplementary-material").get(0).text(), containsString("Response to Reviewers.docx"));
    assertThat(authorResponse.select(".review__doi a[href]").text(), containsString("https://doi.org/10.1371/journal.pone.0207232.r002"));

    Element secondDecision = doc.select(".review-history .decision-letter").get(1);
    assertThat(secondDecision.select(".letter__date").text(), containsString("October 9, 2018"));
    assertEquals("Qinghui Zhang, Editor", secondDecision.select(".letter_author").text());
    assertThat(secondDecision.select(".letter__body").text(), containsString("We are pleased"));
    assertThat(secondDecision.select(".review__doi a[href]").text(), containsString("https://doi.org/10.1371/journal.pone.0207232.r003"));
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
