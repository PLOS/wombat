package org.ambraproject.wombat.util;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.util.EnumSet;
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
    UNDERLINE("span");

    private final Pattern pattern;
    private final String replacement;

    /**
     * @param htmlTag the HTML tag type to replace XML tags of this type
     */
    private SimpleElementType(String htmlTag) {
      String nlmXmlTag = name().toLowerCase();

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
    ),
    SEC_TITLE(
        "<sec id=\\\"st1\\\">[\n\t ]*<title ?/>", // What is special about id="st1"?
        "" // Just delete it
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
      .addAll(EnumSet.allOf(LegacyKludge.class))
      .build();

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
    return text;
  }

}
