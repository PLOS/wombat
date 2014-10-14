package org.ambraproject.wombat.util;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities to transforming excerpts of NLM XML article text.
 * <p/>
 * There are several places where we have such excerpts (usually the extracted content of a single XML element) and wish
 * to display it as HTML. Properly, this is handled by an XSL transformation, which is extensible and customizable
 * through the theme system. This class contains some legacy Java kludges that do the same thing. (Forked from Ambra 2's
 * {@code org.ambraproject.util.ArticleFormattingDirective}.)
 * <p/>
 * All markup output from here has classes of the form {@code "nlm-*"}, in order to associate styling (or JavaScript,
 * etc.) specifically with elements that originated as NLM XML elements transformed in this way. This is in lieu of
 * allowing themes to place their own classes and identifiers (as would be possible in a theme-defined transformation).
 * <p/>
 * The implementation looks for XML tags with regexes, which is inherently brittle. XML is <a
 * href="http://stackoverflow.com/a/1732454">is not a regular language,</a> which makes it impossible to find certain
 * kinds of nested structures. We assume the use case excludes such problems. Beyond that, there are a ton of gotchas
 * around whitespace, optional elements, etc. This whole class is ripe for replacement with a real XML parser or
 * transformer.
 */
public class XmlExcerptTransformation {
  private XmlExcerptTransformation() {
    throw new AssertionError("Not instantiable");
  }

  private static interface TextReplacement {
    String replace(String input);
  }

  /**
   * NLM element types with no attributes and a common pattern of replacement. Assumes that two elements of the same
   * type will never be recursively nested.
   */
  private static enum SimpleElementType implements TextReplacement {
    // Enum names are (privately) used reflectively
    ITALIC("i"),
    BOLD("b"),
    MONOSPACE("span"),
    OVERLINE("span"),
    SC("span"),
    STRIKE("del"),
    UNDERLINE("span"),
    LIST_ITEM("li"),
    P("p"); // leave the element type alone but add class="nlm-p"

    private final Pattern pattern;
    private final String replacement;

    /**
     * @param htmlTag the HTML tag type to replace XML tags of this type
     */
    private SimpleElementType(String htmlTag) {
      String nlmXmlTag = name().toLowerCase().replace('_', '-');

      this.pattern = Pattern.compile("" +
              // Not sure why we are checking for HTML-escaped "&lt;" and "&gt;" in what is supposed to be XML.
              // The legacy impl did it (only for <italic>), so surely there was a special case at some point.
              // The special case likely doesn't exist on this platform, so investigate removing it.
              "(?:<|&lt;)" + nlmXmlTag + "(?:>|&gt;)" +
              "(.*?)" +
              "(?:<|&lt;)/" + nlmXmlTag + "(?:>|&gt;)",
          Pattern.DOTALL);
      this.replacement = String.format("<%s class=\"nlm-%s\">$1</%s>",
          htmlTag, nlmXmlTag, htmlTag);
    }

    /**
     * Replace matched XML elements with HTML elements.
     *
     * @param input the XML text to search
     * @return the transformed text
     */
    @Override
    public String replace(String input) {
      return pattern.matcher(input).replaceAll(replacement);
    }
  }

  /**
   * Tags to delete, leaving the element content behind.
   */
  private static enum TagToDelete implements TextReplacement {
    BODY, SEC, TITLE;
    private final Pattern pattern;

    private TagToDelete() {
      String nlmXmlTag = name().toLowerCase();
      this.pattern = Pattern.compile("" +
              "<" + nlmXmlTag + ".*?>" + // match any attributes
              "|" +
              "</" + nlmXmlTag + "\\s*>",
          Pattern.DOTALL);
    }

    @Override
    public String replace(String input) {
      return pattern.matcher(input).replaceAll("");
    }
  }

  /**
   * Ad-hoc regexes from the legacy Ambra implementation.
   */
  private static enum LegacyKludge implements TextReplacement {
    NAMED_CONTENT(
        "<named-content(?:" +
            "(?:\\s+xmlns:xlink\\s*=\\s*\"http://www.w3.org/1999/xlink\"\\s*)|" +
            "(?:\\s+content-type\\s*=\\s*\"genus-species\"\\s*)|" +
            "(?:\\s+xlink:type\\s*=\\s*\"simple\"\\s*)" +
            ")*>(.*?)</named-content>",
        "<i class=\"nlm-named-content\">$1</i>"
    ),
    EMAIL(
        "<email(?:" +
            "(?:\\s+xmlns:xlink\\s*=\\s*\"http://www.w3.org/1999/xlink\"\\s*)|" +
            "(?:\\s+xlink:type\\s*=\\s*\"simple\"\\s*)" +
            ")*>(.*?)</email>",
        "<a class=\"nlm-email\" href=\"mailto:$1\">$1</a>"
    );

    private final Pattern pattern;
    private final String replacement;

    private LegacyKludge(String pattern, String replacement) {
      this.pattern = Pattern.compile(pattern);
      this.replacement = replacement;
    }

    @Override
    public String replace(String input) {
      return pattern.matcher(input).replaceAll(replacement);
    }
  }

  private static final ImmutableCollection<TextReplacement> REPLACEMENTS = ImmutableList.<TextReplacement>builder()
      .addAll(EnumSet.allOf(SimpleElementType.class))
      .addAll(EnumSet.allOf(TagToDelete.class))
      .addAll(EnumSet.allOf(LegacyKludge.class))
      .build();


  private static final Pattern LIST_PATTERN = Pattern.compile("" +
          "<list" +
          "(?:\\s+list-type\\s*=\\s*\"([-\\w_]*?)\")?" +
          "\\s*>" +
          "(.*?)" +
          "</list\\s*>",
      Pattern.DOTALL);

  /**
   * Replace 'list' XML tags with HTML 'ul' or 'ol'.
   * <p/>
   * This one is a bit more complicated than the above because we depend on an attribute of the input tag to know
   * whether to replace it with 'ul' or 'ol'. In our crude searching, we conveniently assume that the opening tag will
   * have only a "list-type" attribute or no attributes at all. So this is even more brittle than the above.
   *
   * @param text the XML text to search for lists
   * @return the text with lists transformed to HTML
   */
  private static String transformLists(String text) {
    StringBuffer transformed = new StringBuffer(text.length());
    Matcher matcher = LIST_PATTERN.matcher(text);
    while (matcher.find()) {
      String xmlListType = matcher.group(1); // may be null
      String htmlListType = "bullet".equals(xmlListType) ? "ul" : "ol";
      String replacement = String.format("<%s class=\"nlm-list\">$2</%s>", htmlListType, htmlListType);
      matcher.appendReplacement(transformed, replacement);
    }
    matcher.appendTail(transformed);
    return transformed.toString();
  }


  /**
   * Transform NLM XML text to generic HTML. Applies a simplified transformation that looks only for XML elements in
   * common excerpts.
   *
   * @param text the XML text to search
   * @return the transformed HTML
   */
  public static String transform(String text) {
    for (TextReplacement replacement : REPLACEMENTS) {
      text = replacement.replace(text);
    }
    text = transformLists(text);
    return text;
  }

}
