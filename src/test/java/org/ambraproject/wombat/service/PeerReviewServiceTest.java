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

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

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


    PeerReviewService service = new PeerReviewServiceImpl() {
      @Override
      String getAssetContent(Map<String, ?> itemMetadata) throws IOException {
        Map<String, ?> files = (Map<String, ?>) itemMetadata.get("files");
        Map<String, ?> letter = (Map<String, ?>) files.get("letter");
        String crepoKey = (String) letter.get("crepoKey");

        String letterContent = null;
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r001.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article specific-use=\"decision-letter\"><body>InitialDecisionLetterSampleBody</body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r002.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article article-type=\"author-comment\"><body>FirstRoundAuthorResponseSampleBody</body></sub-article>";
        }
        if (crepoKey == "info:doi/10.1371/journal.pone.0207232.r003.xml") {
          letterContent = "<?xml version=\"1.0\" encoding=\"utf-8\"?><sub-article specific-use=\"decision-letter\"><body>FirstRoundDecisionLetterSampleBody</body></sub-article>";
        }
        return letterContent;
      }
    };

    String html = service.asHtml(itemTable);
    assertThat(html, containsString("Original Submission"));
    assertThat(html, containsString("InitialDecisionLetterSampleBody"));
    assertThat(html, containsString("Revision 1"));
//    assertThat(html, containsString("FirstRoundAuthorResponseSampleBody"));
    assertThat(html, containsString("FirstRoundDecisionLetterSampleBody"));

  }
}
