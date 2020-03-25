package org.ambraproject.wombat.service;

import static java.lang.String.format;
import static org.ambraproject.wombat.util.FileUtils.read;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import com.google.gson.Gson;
import org.ambraproject.wombat.controller.ControllerTest;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class PeerReviewServiceImplTest extends ControllerTest {
  @Configuration
  static class ContextConfiguration {
    @Bean
    PeerReviewServiceImpl peerReviewServiceImpl() {
      return spy(new PeerReviewServiceImpl());
    }
  }

  @Autowired
  CorpusContentApi corpusContentApi;

  @Autowired
  PeerReviewServiceImpl peerReviewServiceImpl;

  @Autowired
  Gson gson;
  
  @Test
  public void testAttachmentLink() throws IOException {
    doAnswer(invocation -> read(prefix(getFilename(invocation).toLowerCase())))
        .when(corpusContentApi).requestContent(any(URI.class));

    Map<String, ?> itemTable =
        (Map<String, ?>) gson.fromJson(read(prefix("item-table.pone.0207232.json")), Map.class);
    String html = peerReviewServiceImpl.asHtml(itemTable);
    Document doc = Jsoup.parse(html);

    Element firstAttachment = doc.select(".review-history .review-files .supplementary-material").first();
    Element anchorElement = firstAttachment.select("a").first();

    assertThat(anchorElement.attr("href"), containsString("file?id=10.1371/journal.pone.0207232.s001&type=supplementary"));
    assertThat(anchorElement.attr("title"), containsString("Download .pdf file"));
  }

  @Test
  public void testAttachmentAnchorTitles() throws IOException {

    String xml = read(prefix("peer-review-attachment-filenames.pone.0207232.xml"));
 
    String html = peerReviewServiceImpl.transformXmlToHtml(xml);

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
    String html = peerReviewServiceImpl.transformXmlToHtml(xml);
    Document doc = Jsoup.parse(html);

    // SUBMISSION

    Element submissionHeader = doc.select(".review-history .revision").get(0);
    assertEquals("Original Submission", submissionHeader.select(".letter__title").text());
    assertEquals("June 1, 2018", submissionHeader.select(".letter__date").text());

    Element firstDecision = doc.select(".review-history .decision-letter").get(0);
    assertEquals("September 12, 2018", firstDecision.select(".letter__date").text());
    assertEquals("Qinghui Zhang, Editor, Surachai Supattapone, Editor", firstDecision.select(".letter__author").text());
    assertThat(firstDecision.select(".letter__body").text(), containsString("Thank you for submitting"));
    assertEquals("https://doi.org/10.1371/journal.pone.0207232.r001", firstDecision.select(".review__doi a[href]").text());

    // REVISION

    Element revisionHeader = doc.select(".review-history .revision").get(1);
    assertEquals("Revision 1", revisionHeader.select(".letter__title").text());

    Element authorResponse = doc.select(".author-response").get(0);
    assertThat(authorResponse.text(), containsString("Author Response"));
    assertThat(authorResponse.select(".letter__body").text(), containsString("[Response to Reviewers]"));
    assertThat(authorResponse.select(".supplementary-material").get(0).text(), containsString("Response to Reviewers.docx"));
    assertEquals("https://doi.org/10.1371/journal.pone.0207232.r002", authorResponse.select(".review__doi a[href]").text());

    Element secondDecision = doc.select(".review-history .decision-letter").get(1);
    assertEquals("October 9, 2018", secondDecision.select(".letter__date").text());
    assertEquals("Qinghui Zhang, Editor", secondDecision.select(".letter__author").text());
    assertThat(secondDecision.select(".letter__body").text(), containsString("We are pleased"));
    assertEquals("https://doi.org/10.1371/journal.pone.0207232.r003", secondDecision.select(".review__doi a[href]").text());

    // ACCEPTANCE

    Element acceptanceHeader = doc.select(".review-history .revision").get(2);
    assertEquals("Formally Accepted", acceptanceHeader.select(".letter__title").text());

    Element acceptanceLetter = doc.select(".review-history .acceptance-letter").get(0);
    assertEquals("November 1, 2018", acceptanceLetter.select(".letter__date").text());
    assertEquals("Agatha Scepter, Editor", acceptanceLetter.select(".letter__author").text());
    assertThat(acceptanceLetter.select(".letter__body").text(), containsString("I am pleased"));
    assertEquals("https://doi.org/10.1371/journal.pone.0207232.r004", acceptanceLetter.select(".review__doi a[href]").text());
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
      assertThat(peerReviewServiceImpl.parseArticleReceivedDate(receivedDate), is(expectedDates[i]));
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
      assertThat(peerReviewServiceImpl.formatDate(acceptedDates[i]), is(expectedDates[i]));
    }

    String rejectedDates[] = {
      "Jan 1, 2010",
      "02/10/2003",
      "2018-12-10",
      "not-a-date",
      ""
    };

    for (int i=0; i < rejectedDates.length; ++i) {
      assertThat(peerReviewServiceImpl.formatDate(rejectedDates[i]), is(""));
    }
  }

  private String getFilename(InvocationOnMock invocation) {
    URI uri = invocation.getArgument(0);
    String path = uri.getPath();
    return path.substring(path.lastIndexOf('/') + 9).toLowerCase();
  }

  private String prefix(String file) {
    return "peer-review/" + file;
  }
}
