/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static junit.framework.TestCase.assertNull;
import static org.ambraproject.wombat.service.PeerReviewServiceImpl.DEFAULT_PEER_REVIEW_XSL;
import static org.ambraproject.wombat.util.FileUtils.read;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;


@ContextConfiguration(classes = {PeerReviewServiceTest.class})
public class PeerReviewServiceTest extends AbstractTestNGSpringContextTests {

  @Test
  public void testAsHtml() throws IOException {
    ImmutableMap<String, ? extends Map<String, ?>> itemTable = ImmutableMap.of(
        "10.1371/journal.pone.0207232.r001", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r001",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r001.xml"
            ))
        ),
        "10.1371/journal.pone.0207232.r002", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r002",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r002.xml"
            ))
        ),
        "10.1371/journal.pone.0207232.r003", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.r003",
            "itemType", "reviewLetter",
            "files", ImmutableMap.of("letter", ImmutableMap.of(
                "crepoKey", "info:doi/10.1371/journal.pone.0207232.r003.xml"
            ))
        )
    );

    PeerReviewService serviceWithMockedContent = new PeerReviewServiceImpl() {
      @Override
      String getReviewXml(Map<String, ?> metadata) throws IOException {
        Map<String, ?> files = (Map<String, ?>) metadata.get("files");
        Map<String, ?> letter = (Map<String, ?>) files.get("letter");
        String crepoKey = (String) letter.get("crepoKey");

        String letterContent = null;
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r001.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article specific-use=\"decision-letter\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>0</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body><p>InitialDecisionLetterSampleBody</p></body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r002.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article article-type=\"author-comment\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>1</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body><p>FirstRoundAuthorResponseSampleBody</p></body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r003.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article specific-use=\"decision-letter\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>1</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body><p>FirstRoundDecisionLetterSampleBody</p></body></sub-article>";
        }
        return letterContent;
      }
    };

    String html = serviceWithMockedContent.asHtml(itemTable);
    Document d = Jsoup.parse(html);

    assertThat(d.select(".review-history th").get(0).text(), containsString("Original Submission"));
    assertThat(d.select(".review-history th").get(1).text(), containsString("Revision 1"));
    assertThat(d.select(".review-history .decision-letter").get(0).text(), containsString("InitialDecisionLetterSampleBody"));
    assertThat(d.select(".review-history .decision-letter").get(1).text(), containsString("FirstRoundDecisionLetterSampleBody"));
    assertThat(d.select(".review-history .author-response").get(0).text(), containsString("FirstRoundAuthorResponseSampleBody"));
  }

  @Test
  public void testAsHtmlHandlesNoPeerReviewItems() throws IOException {
    ImmutableMap<String, ? extends Map<String, ?>> itemTable = ImmutableMap.of(
        "10.1371/journal.pone.0207232.t001", ImmutableMap.of(
            "doi", "10.1371/journal.pone.0207232.t001",
            "itemType", "table"
        )
    );
    String html = new PeerReviewServiceImpl().asHtml(itemTable);
    assertNull(html);
  }

  @Test
  public void testAuthorResponse() {
    String xml = read("xsl/peer-review/pone.0207232.xml");
 
    PeerReviewServiceImpl svc = new PeerReviewServiceImpl();
    String html = svc.transformXmlToHtml(xml, DEFAULT_PEER_REVIEW_XSL);

    Document doc = Jsoup.parse(html);

    assertThat(doc.select(".review-history .letter__date").get(0).text(), containsString("12 Sep 2018"));

    Element authorResponseDiv = doc.select(".author-response").first();
    assertThat(authorResponseDiv.text(), containsString("Author Response"));

    Element attachmentElem = authorResponseDiv.select(".review-files .supplementary-material").first();
    assertThat(attachmentElem.text(), containsString("Response to Reviewers.docx"));
  }
}
