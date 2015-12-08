package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.ambraproject.wombat.service.CommentFormatting.CommentModelField;
import static org.testng.Assert.assertEquals;

public class CommentFormattingTest {

  private static class TestCase {
    private String body;
    private String title;
    private String highlightedText;
    private String competingInterestBody;

    private Map<CommentModelField, String> expectedFields = new EnumMap<>(CommentModelField.class);

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

    public TestCase setExpectedValue(CommentModelField field, String value) {
      expectedFields.put(field, value);
      return this;
    }

    public ImmutableMap<String, Object> createView() {
      ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder();
      builder.put("replies", ImmutableList.of());
      if (body != null) builder.put("body", body);
      if (title != null) builder.put("title", title);
      if (highlightedText != null) builder.put("highlightedText", highlightedText);
      if (competingInterestBody != null) builder.put("competingInterestBody", competingInterestBody);
      return builder.build();
    }
  }

  @DataProvider
  public Object[][] getTestCases() {
    return TEST_CASES.stream()
        .map(testCase -> new Object[]{testCase})
        .collect(Collectors.toList()).toArray(new Object[0][]);
  }

  @Test(dataProvider = "getTestCases")
  public void testCommentFormatting(TestCase testCase) {
    Map<String, Object> modifiedView = CommentFormatting.addFormattingFields(testCase.createView());
    Map<String, Object> formatted = (Map<String, Object>) modifiedView.get("formatting");
    for (CommentModelField field : CommentModelField.values()) {
      String actual = (String) formatted.get(field.getKey());
      String expected = testCase.expectedFields.get(field);
      assertEquals(actual, expected, "Mismatched " + field.getKey());
    }
  }

  private static final ImmutableList<TestCase> TEST_CASES = ImmutableList.copyOf(new TestCase[]{

      // Empty case
      new TestCase()
          .setExpectedValue(CommentModelField.bodyHtml, "")
          .setExpectedValue(CommentModelField.truncatedBody, "")
          .setExpectedValue(CommentModelField.bodyWithUrlLinkingNoPTags, "")
          .setExpectedValue(CommentModelField.truncatedBodyWithUrlLinkingNoPTags, "")
          .setExpectedValue(CommentModelField.bodyWithHighlightedText, "")
          .setExpectedValue(CommentModelField.competingInterestStatement, "")
          .setExpectedValue(CommentModelField.truncatedCompetingInterestStatement, ""),

      // Basic non-empty case
      new TestCase()
          .setBody("Body")
          .setTitle("Title")
          .setHighlightedText("HighlightedText")
          .setCompetingInterestBody("CompetingInterestBody")
          .setExpectedValue(CommentModelField.bodyHtml, "<p>Body</p>")
          .setExpectedValue(CommentModelField.truncatedBody, "<p>Body</p>")
          .setExpectedValue(CommentModelField.bodyWithUrlLinkingNoPTags, "Body")
          .setExpectedValue(CommentModelField.truncatedBodyWithUrlLinkingNoPTags, "Body")
          .setExpectedValue(CommentModelField.bodyWithHighlightedText, "<p>HighlightedText<br/><br/>Body</p>")
          .setExpectedValue(CommentModelField.competingInterestStatement, "CompetingInterestBody")
          .setExpectedValue(CommentModelField.truncatedCompetingInterestStatement, "CompetingInterestBody"),

      // Basic markup
      new TestCase()
          .setBody("Supported markup tags: ''italic'' '''bold''' '''''bold italic''''' ^^superscript^^ ~~subscript~~")
          .setExpectedValue(CommentModelField.bodyHtml, "<p>Supported markup tags: <em>italic</em> <strong>bold</strong> <strong><em>bold italic</em></strong> <sup>superscript</sup> <sub>subscript</sub></p>")
          .setExpectedValue(CommentModelField.truncatedBody, "<p>Supported markup tags: <em>italic</em> <strong>bold</strong> <strong><em>bold italic</em></strong> <sup>superscript</sup> <sub>subscript</sub></p>")
          .setExpectedValue(CommentModelField.bodyWithUrlLinkingNoPTags, "Supported markup tags: <em>italic</em> <strong>bold</strong> <strong><em>bold italic</em></strong> <sup>superscript</sup> <sub>subscript</sub>")
          .setExpectedValue(CommentModelField.truncatedBodyWithUrlLinkingNoPTags, "Supported markup tags: <em>italic</em> <strong>bold</strong> <strong><em>bold italic</em></strong> <sup>superscript</sup> <sub>subscript</sub>")
          .setExpectedValue(CommentModelField.bodyWithHighlightedText, "<p>Supported markup tags: <em>italic</em> <strong>bold</strong> <strong><em>bold italic</em></strong> <sup>superscript</sup> <sub>subscript</sub></p>")
          .setExpectedValue(CommentModelField.competingInterestStatement, "")
          .setExpectedValue(CommentModelField.truncatedCompetingInterestStatement, ""),

      // Markup with HTML escaping
      new TestCase()
          .setBody("<p>Supported markup tags: ''<em>italic</em>'' '''<strong>bold</strong>''' '''''<strong><em>bold italic</em></strong>''''' ^^<sup>superscript</sup>^^ ~~<sub>subscript</sub>~~</p>")
          .setExpectedValue(CommentModelField.bodyHtml, "<p>&lt;p&gt;Supported markup tags: <em>&lt;em&gt;italic&lt;/em&gt;</em> <strong>&lt;strong&gt;bold&lt;/strong&gt;</strong> <strong><em>&lt;strong&gt;&lt;em&gt;bold italic&lt;/em&gt;&lt;/strong&gt;</em></strong> <sup>&lt;sup&gt;superscript&lt;/sup&gt;</sup> <sub>&lt;sub&gt;subscript&lt;/sub&gt;</sub>&lt;/p&gt;</p>")
          .setExpectedValue(CommentModelField.truncatedBody, "<p>&lt;p&gt;Supported markup tags: <em>&lt;em&gt;italic&lt;/em&gt;</em> <strong>&lt;strong&gt;bold&lt;/strong&gt;</strong> <strong><em>&lt;strong&gt;&lt;em&gt;bold italic&lt;/em&gt;&lt;/strong&gt;</em></strong>...</p>")
          .setExpectedValue(CommentModelField.bodyWithUrlLinkingNoPTags, "&lt;p&gt;Supported markup tags: <em>&lt;em&gt;italic&lt;/em&gt;</em> <strong>&lt;strong&gt;bold&lt;/strong&gt;</strong> <strong><em>&lt;strong&gt;&lt;em&gt;bold italic&lt;/em&gt;&lt;/strong&gt;</em></strong> <sup>&lt;sup&gt;superscript&lt;/sup&gt;</sup> <sub>&lt;sub&gt;subscript&lt;/sub&gt;</sub>&lt;/p&gt;")
          .setExpectedValue(CommentModelField.truncatedBodyWithUrlLinkingNoPTags, "&lt;p&gt;Supported markup tags: <em>&lt;em&gt;italic&lt;/em&gt;</em> <strong>&lt;strong&gt;bold&lt;/strong&gt;</strong> <strong><em>&lt;strong&gt;&lt;em&gt;bold italic&lt;/em&gt;&lt;/strong&gt;</em></strong>...")
          .setExpectedValue(CommentModelField.bodyWithHighlightedText, "<p>&lt;p&gt;Supported markup tags: <em>&lt;em&gt;italic&lt;/em&gt;</em> <strong>&lt;strong&gt;bold&lt;/strong&gt;</strong> <strong><em>&lt;strong&gt;&lt;em&gt;bold italic&lt;/em&gt;&lt;/strong&gt;</em></strong> <sup>&lt;sup&gt;superscript&lt;/sup&gt;</sup> <sub>&lt;sub&gt;subscript&lt;/sub&gt;</sub>&lt;/p&gt;</p>")
          .setExpectedValue(CommentModelField.competingInterestStatement, "")
          .setExpectedValue(CommentModelField.truncatedCompetingInterestStatement, ""),

      // URL detection
      new TestCase()
          .setBody("Visit example.com")
          .setExpectedValue(CommentModelField.bodyHtml, "<p>Visit example.com</p>")
          .setExpectedValue(CommentModelField.truncatedBody, "<p>Visit example.com</p>")
          .setExpectedValue(CommentModelField.bodyWithUrlLinkingNoPTags, "Visit example.com")
          .setExpectedValue(CommentModelField.truncatedBodyWithUrlLinkingNoPTags, "Visit example.com")
          .setExpectedValue(CommentModelField.bodyWithHighlightedText, "<p>Visit example.com</p>")
          .setExpectedValue(CommentModelField.competingInterestStatement, "")
          .setExpectedValue(CommentModelField.truncatedCompetingInterestStatement, ""),
      new TestCase()
          .setBody("Visit www.example.com")
          .setExpectedValue(CommentModelField.bodyHtml, "<p>Visit <a href=\"http://www.example.com\">www.example.com</a></p>")
          .setExpectedValue(CommentModelField.truncatedBody, "<p>Visit <a href=\"http://www.example.com\">www.example.com</a></p>")
          .setExpectedValue(CommentModelField.bodyWithUrlLinkingNoPTags, "Visit <a href=\"http://www.example.com\">www.example.com</a>")
          .setExpectedValue(CommentModelField.truncatedBodyWithUrlLinkingNoPTags, "Visit <a href=\"http://www.example.com\">www.example.com</a>")
          .setExpectedValue(CommentModelField.bodyWithHighlightedText, "<p>Visit <a href=\"http://www.example.com\">www.example.com</a></p>")
          .setExpectedValue(CommentModelField.competingInterestStatement, "")
          .setExpectedValue(CommentModelField.truncatedCompetingInterestStatement, ""),
      new TestCase()
          .setBody("Visit http://example.com")
          .setExpectedValue(CommentModelField.bodyHtml, "<p>Visit <a href=\"http://example.com\">http://example.com</a></p>")
          .setExpectedValue(CommentModelField.truncatedBody, "<p>Visit <a href=\"http://example.com\">http://example.com</a></p>")
          .setExpectedValue(CommentModelField.bodyWithUrlLinkingNoPTags, "Visit <a href=\"http://example.com\">http://example.com</a>")
          .setExpectedValue(CommentModelField.truncatedBodyWithUrlLinkingNoPTags, "Visit <a href=\"http://example.com\">http://example.com</a>")
          .setExpectedValue(CommentModelField.bodyWithHighlightedText, "<p>Visit <a href=\"http://example.com\">http://example.com</a></p>")
          .setExpectedValue(CommentModelField.competingInterestStatement, "")
          .setExpectedValue(CommentModelField.truncatedCompetingInterestStatement, ""),
      new TestCase()
          .setBody("Visit http://www.example.com")
          .setExpectedValue(CommentModelField.bodyHtml, "<p>Visit <a href=\"http://www.example.com\">http://www.example.com</a></p>")
          .setExpectedValue(CommentModelField.truncatedBody, "<p>Visit <a href=\"http://www.example.com\">http://www.example.com</a></p>")
          .setExpectedValue(CommentModelField.bodyWithUrlLinkingNoPTags, "Visit <a href=\"http://www.example.com\">http://www.example.com</a>")
          .setExpectedValue(CommentModelField.truncatedBodyWithUrlLinkingNoPTags, "Visit <a href=\"http://www.example.com\">http://www.example.com</a>")
          .setExpectedValue(CommentModelField.bodyWithHighlightedText, "<p>Visit <a href=\"http://www.example.com\">http://www.example.com</a></p>")
          .setExpectedValue(CommentModelField.competingInterestStatement, "")
          .setExpectedValue(CommentModelField.truncatedCompetingInterestStatement, ""),

  });

}
