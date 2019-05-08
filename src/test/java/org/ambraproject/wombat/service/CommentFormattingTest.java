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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.service.CommentFormatting.FormattedComment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class CommentFormattingTest {

  private static class TestCase {
    private String body;
    private String title;
    private String highlightedText;
    private String competingInterestBody;

    private String expectedBodyWithHighlightedText;
    private String expectedCompetingInterestStatement;

    public TestCase setBody(String body) {
      this.body = body;
      return this;
    }

    public TestCase setTitle(String title) {
      this.title = title;
      return this;
    }

    public TestCase setHighlightedText(String highlightedText) {
      this.highlightedText = highlightedText;
      return this;
    }

    public TestCase setCompetingInterestBody(String competingInterestBody) {
      this.competingInterestBody = competingInterestBody;
      return this;
    }

    public TestCase setExpectedBodyWithHighlightedText(String expectedBodyWithHighlightedText) {
      this.expectedBodyWithHighlightedText = expectedBodyWithHighlightedText;
      return this;
    }

    public TestCase setExpectedCompetingInterestStatement(String expectedCompetingInterestStatement) {
      this.expectedCompetingInterestStatement = expectedCompetingInterestStatement;
      return this;
    }

    /**
     * Represent this test case as a mutable metadata map.
     */
    public Map<String, Object> createView() {
      Map<String, Object> view = new HashMap<>();
      view.put("replies", ImmutableList.of());
      if (body != null) view.put("body", body);
      if (title != null) view.put("title", title);
      if (highlightedText != null) view.put("highlightedText", highlightedText);
      view.put("competingInterestStatement", (competingInterestBody != null)
          ? ImmutableMap.of("body", competingInterestBody) : ImmutableMap.of());
      return view;
    }
  }

  @Parameters
  public static List<Object[]> getTestCases() {
    return TEST_CASES.stream()
      .map(testCase -> new Object[]{testCase})
      .collect(Collectors.toList());
  }

  @Parameter(0)
  public TestCase testCase;

  @Test
  public void testCommentFormatting() {
    Map<String, Object> view = testCase.createView();
    CommentFormatting.addFormattingFields(view);
    FormattedComment formatted = (FormattedComment) view.get("formatting");
    assertEquals(testCase.expectedBodyWithHighlightedText, formatted.getBodyWithHighlightedText());
    assertEquals(testCase.expectedCompetingInterestStatement, formatted.getCompetingInterestStatement());
  }

  private static final ImmutableList<TestCase> TEST_CASES = ImmutableList.copyOf(new TestCase[]{

      // Empty case
      new TestCase()
          .setExpectedBodyWithHighlightedText("")
          .setExpectedCompetingInterestStatement(""),

      // Basic non-empty case
      new TestCase()
          .setBody("Body")
          .setTitle("Title")
          .setHighlightedText("HighlightedText")
          .setCompetingInterestBody("CompetingInterestBody")
          .setExpectedBodyWithHighlightedText("<p>HighlightedText<br/><br/>Body</p>")
          .setExpectedCompetingInterestStatement("CompetingInterestBody"),

      // Basic markup
      new TestCase()
          .setBody("Supported markup tags: ''italic'' '''bold''' '''''bold italic''''' ^^superscript^^ ~~subscript~~")
          .setExpectedBodyWithHighlightedText("<p>Supported markup tags: <em>italic</em> <strong>bold</strong> <strong><em>bold italic</em></strong> <sup>superscript</sup> <sub>subscript</sub></p>")
          .setExpectedCompetingInterestStatement(""),

      // Markup with HTML escaping
      new TestCase()
          .setBody("<p>Supported markup tags: ''<em>italic</em>'' '''<strong>bold</strong>''' '''''<strong><em>bold italic</em></strong>''''' ^^<sup>superscript</sup>^^ ~~<sub>subscript</sub>~~</p>")
          .setExpectedBodyWithHighlightedText("<p>&lt;p&gt;Supported markup tags: <em>&lt;em&gt;italic&lt;/em&gt;</em> <strong>&lt;strong&gt;bold&lt;/strong&gt;</strong> <strong><em>&lt;strong&gt;&lt;em&gt;bold italic&lt;/em&gt;&lt;/strong&gt;</em></strong> <sup>&lt;sup&gt;superscript&lt;/sup&gt;</sup> <sub>&lt;sub&gt;subscript&lt;/sub&gt;</sub>&lt;/p&gt;</p>")
          .setExpectedCompetingInterestStatement(""),

      // URL detection
      new TestCase()
          .setBody("Visit example.com")
          .setExpectedBodyWithHighlightedText("<p>Visit example.com</p>")
          .setExpectedCompetingInterestStatement(""),
      new TestCase()
          .setBody("Visit www.example.com")
          .setExpectedBodyWithHighlightedText("<p>Visit <a rel=\"nofollow\" href=\"http://www.example.com\">www.example.com</a></p>")
          .setExpectedCompetingInterestStatement(""),
      new TestCase()
          .setBody("Visit http://example.com")
          .setExpectedBodyWithHighlightedText("<p>Visit <a rel=\"nofollow\" href=\"http://example.com\">http://example.com</a></p>")
          .setExpectedCompetingInterestStatement(""),
      new TestCase()
          .setBody("Visit http://www.example.com")
          .setExpectedBodyWithHighlightedText("<p>Visit <a rel=\"nofollow\" href=\"http://www.example.com\">http://www.example.com</a></p>")
          .setExpectedCompetingInterestStatement(""),

  });

}
