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

import static junit.framework.TestCase.assertEquals;
import static org.ambraproject.wombat.service.PeerReviewServiceImpl.DEFAULT_PEER_REVIEW_XSL;
import static org.ambraproject.wombat.util.FileUtils.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

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
            "doi", "10.1371/journal.pone.0207232.r001",
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
              "<body>InitialDecisionLetterSampleBody</body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r002.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article article-type=\"author-comment\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>1</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body>FirstRoundAuthorResponseSampleBody</body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r003.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article specific-use=\"decision-letter\">" +
              "<front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>1</meta-value></custom-meta></custom-meta-group></front-stub>" +
              "<body>FirstRoundDecisionLetterSampleBody</body></sub-article>";
        }
        return letterContent;
      }
    };

    String html = serviceWithMockedContent.asHtml(itemTable);
    assertThat(html, containsString("Original Submission"));
    assertThat(html, containsString("InitialDecisionLetterSampleBody"));
    assertThat(html, containsString("Revision 1"));
//    assertThat(html, containsString("FirstRoundAuthorResponseSampleBody"));
    assertThat(html, containsString("FirstRoundDecisionLetterSampleBody"));

  }

  @Test
  public void testXslt() {
    String xml = read("xsl/peer-review/pone.0207232.xml");

    PeerReviewServiceImpl svc = new PeerReviewServiceImpl();
    String html = svc.xmlToHtml(xml, DEFAULT_PEER_REVIEW_XSL);

    assertThat(html.replaceAll("\\s+",""),  // strip all whitespace
      containsString("<h2>PeerReviewHistory</h2>"));
  }

  @Test
  public void testGetRevisionNumber() {
    Integer revisionNumber = new PeerReviewServiceImpl().getRevisionNumber("<sub-article><front-stub><custom-meta-group><custom-meta><meta-name>Submission Version</meta-name><meta-value>5</meta-value></custom-meta></custom-meta-group></front-stub><body></body></sub-article>");
    assertEquals(5, revisionNumber.intValue());

  }
}
