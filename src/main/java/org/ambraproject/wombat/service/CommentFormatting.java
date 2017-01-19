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

import com.google.common.base.Strings;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.regex.Pattern;

public class CommentFormatting {
  private CommentFormatting() {
    throw new AssertionError();
  }

  /**
   * Add a new map, keyed {@code "formatting"} and containing a {@link FormattedComment} object, to the comment.
   */
  public static void addFormattingFields(Map<String, Object> commentMetadata) {
    commentMetadata.put("formatting", new FormattedComment(commentMetadata));
  }

  /*
   * Model fields defined by old Ambra's "AnnotationView".
   */
  public static class FormattedComment {
    private final String bodyWithHighlightedText;
    private final String competingInterestStatement;

    public FormattedComment(Map<String, ?> comment) {
      this.bodyWithHighlightedText = buildBodyWithHighlightedText(comment);
      this.competingInterestStatement = buildCompetingInterestStatement(comment);
    }

    public String getBodyWithHighlightedText() {
      return bodyWithHighlightedText;
    }

    public String getCompetingInterestStatement() {
      return competingInterestStatement;
    }
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // WARNING: Now entering some extremely hairy code that was forklifted from old Ambra.
  // The current maintainers disclaim all culpability for the code's quality.
  // PLEASE AVOID INTRODUCING ANY NEW DEPENDENCIES ON ANY VALUES OR METHODS DEFINED BELOW.
  // If the functionality needs to be touched for any reason, feel free to delete or refactor liberally.
  //
  //      _;~)                  (~;_
  //     (   |                  |   )
  //      ~', ',    ,''~'',   ,' ,'~
  //          ', ',' ^   ^ ',' ,'
  //            ',: {o} {o} :,'
  //              ;   /^\   ;
  //               ~\     /~
  //             ,' ,\===/, ',
  //           ,' ,' ;   ; ', ',
  //         ,' ,'    '''    ', ',
  //       (~  ;               ;  ~)
  //        -;_)               (_;-
  //


  private static String buildBodyWithHighlightedText(Map<String, ?> comment) {
    String highlightedText = (String) comment.get("highlightedText");
    if (Strings.isNullOrEmpty(highlightedText)) {
      String body = (String) comment.get("body");
      if (Strings.isNullOrEmpty(body)) return "";
      return hyperlinkEnclosedWithPTags(escapeHtml(body), 25);
    }
    String body = (String) comment.get("body");
    if (Strings.isNullOrEmpty(body)) return "";
    String bodyWithHt = highlightedText + "\n\n" + body;
    return hyperlinkEnclosedWithPTags(escapeHtml(bodyWithHt), 150);
  }

  private static String buildCompetingInterestStatement(Map<String, ?> comment) {
    String competingInterestBody = (String) ((Map<String, ?>) comment.get("competingInterestStatement")).get("body");
    if (Strings.isNullOrEmpty(competingInterestBody)) return "";
    return escapeHtml(competingInterestBody);
  }


  private static final Pattern lineBreakPattern = Pattern.compile("\\p{Zl}|\r\n|\n|\u0085|\\p{Zp}");
  private static final Pattern strongPattern = Pattern.compile("'''");
  private static final Pattern emphasizePattern = Pattern.compile("''");
  private static final Pattern strongEmphasizePattern = Pattern.compile("'''''");
  private static final Pattern superscriptPattern = Pattern.compile("\\^\\^");
  private static final Pattern subscriptPattern = Pattern.compile("~~");

  /**
   * Takes in a String and returns it with all line separators replaced by <br/> tags suitable for display as HTML.
   *
   * @param input HTML
   * @return String with line separators replaced with <br/>
   */
  private static String makeHtmlLineBreaks(final String input) {
    if (StringUtils.isBlank(input)) {
      return input;
    }
    return lineBreakPattern.matcher(input).replaceAll("<br/>");
  }

  /**
   * Takes in a String and returns it with all pairs of <code>'''</code> replaced by \<strong\>\</strong\> tags suitable
   * for display as HTML. For example: <code>foo '''bar''' baz</code> is transformed into <code>foo
   * \<strong\>bar\</strong\> baz</code>
   * <p>
   * The <code>strong</code> tag is used instead of the <code>b</code> tag because <code>strong</code> is preferred for
   * CSS styling.
   * <p>
   * There is no check for consistency of markup pairs (e.g., <code>foo ''bar''' baz</code> will become <code>foo
   * ''bar\<strong\> baz</code>) which will, rightfully, infuriate some users.
   *
   * @param input HTML
   * @return String with all pairs of <code>'''</code> replaced by \<strong\>\</strong\> tags
   */
  private static String makeHtmlStrong(final String input) {
    // If no Pattern in "input" parameter, then do nothing.
    if (StringUtils.isBlank(input)
        || (!strongPattern.matcher(input).find())) {
      return input;
    }

    String transformedInput = input; // This will be the String that gets returned.
    boolean isInsideATag = false; // Whether an open tag was the most recent substitution.

    // While there is Pattern in "input" parameter, replace each instance of Pattern with
    //   either an open or close tag.  Alternate the tag substituted to give tag pairs.
    while (strongPattern.matcher(transformedInput).find()) {
      if (!isInsideATag) {
        transformedInput = strongPattern.matcher(transformedInput).replaceFirst("<strong>");
        isInsideATag = true;
      } else {
        transformedInput = strongPattern.matcher(transformedInput).replaceFirst("</strong>");
        isInsideATag = false;
      }
    }

    return transformedInput;
  }

  /**
   * Takes in a String and returns it with all pairs of <code>''</code> replaced by \<em\>\</em\> tags suitable for
   * display as HTML.
   * <p>
   * For example: <code>foo ''bar'' baz</code> is transformed into <code>foo \<em\>bar\</em\> baz</code>
   * <p>
   * The <code>em</code> tag is used instead of the <code>i</code> tag because <code>em</code> is preferred for CSS
   * styling.
   * <p>
   * There is no check for consistency of markup pairs (e.g., <code>foo 'bar'' baz</code> will become <code>foo
   * 'bar\<em\> baz</code>) which will, rightfully, infuriate some users.
   *
   * @param input HTML
   * @return String with all pairs of <code>''</code> replaced by \<em\>\</em\> tags
   */
  private static String makeHtmlEmphasized(final String input) {
    // If no Pattern in "input" parameter, then do nothing.
    if (StringUtils.isBlank(input)
        || (!emphasizePattern.matcher(input).find())) {
      return input;
    }

    String transformedInput = input; // This will be the String that gets returned.
    boolean isInsideATag = false; // Whether an open tag was the most recent substitution.

    // While there is Pattern in "input" parameter, replace each instance of Pattern with
    //   either an open or close tag.  Alternate the tag substituted to give tag pairs.
    while (emphasizePattern.matcher(transformedInput).find()) {
      if (!isInsideATag) {
        transformedInput = emphasizePattern.matcher(transformedInput).replaceFirst("<em>");
        isInsideATag = true;
      } else {
        transformedInput = emphasizePattern.matcher(transformedInput).replaceFirst("</em>");
        isInsideATag = false;
      }
    }

    return transformedInput;
  }

  /**
   * Takes in a String and returns it with all pairs of <code>'''''</code> replaced by
   * \<strong\>\<em\>\</em\>\</strong\> tags suitable for display as HTML.
   * <p>
   * For example: <code>foo '''''bar''''' baz</code> is transformed into <code>foo \<strong\>\<em\>bar\</em\>\</strong\>
   * baz</code>
   * <p>
   * The <code>em</code> tag is used instead of the <code>i</code> tag because <code>em</code> is preferred for CSS
   * styling. The <code>strong</code> tag is used instead of the <code>b</code> tag because <code>strong</code> is
   * preferred for CSS styling.
   * <p>
   * There is no check for consistency of markup pairs (e.g., <code>foo 'bar''''' baz</code> will become <code>foo
   * 'bar\<strong\>\<em\> baz</code>) which will, rightfully, infuriate some users.
   *
   * @param input HTML
   * @return String with all pairs of <code>'''''</code> replaced by \<strong\>\<em\>\</em\>\</strong\> tags
   */
  private static String makeHtmlStrongEmphasized(final String input) {
    // If no Pattern in "input" parameter, then do nothing.
    if (StringUtils.isBlank(input)
        || (!strongEmphasizePattern.matcher(input).find())) {
      return input;
    }

    String transformedInput = input; // This will be the String that gets returned.
    boolean isInsideATag = false; // Whether an open tag was the most recent substitution.

    // While there is Pattern in "input" parameter, replace each instance of Pattern with
    //   either an open or close tag.  Alternate the tag substituted to give tag pairs.
    while (strongEmphasizePattern.matcher(transformedInput).find()) {
      if (!isInsideATag) {
        transformedInput = strongEmphasizePattern.matcher(transformedInput).replaceFirst("<strong><em>");
        isInsideATag = true;
      } else {
        transformedInput = strongEmphasizePattern.matcher(transformedInput).replaceFirst("</em></strong>");
        isInsideATag = false;
      }
    }

    return transformedInput;
  }

  /**
   * Takes in a String and returns it with all pairs of <code>^^</code> replaced by \<sup\>\</sup\> tags suitable for
   * display as HTML.
   * <p>
   * For example: <code>foo ^^bar^^ baz</code> is transformed into <code>foo \<sup\>bar\</sup\> baz</code>
   * <p>
   * There is no check for consistency of markup pairs (e.g., <code>foo ^bar^^ baz</code> will become <code>foo
   * ^bar\<sup\> baz</code>) which will, rightfully, infuriate some users.
   *
   * @param input HTML
   * @return String with all pairs of <code>^^</code> replaced by \<sup\>\</sup\> tags
   */
  private static String makeHtmlSuperscript(final String input) {
    // If no Pattern in "input" parameter, then do nothing.
    if (StringUtils.isBlank(input)
        || (!superscriptPattern.matcher(input).find())) {
      return input;
    }

    String transformedInput = input; // This will be the String that gets returned.
    boolean isInsideATag = false; // Whether an open tag was the most recent substitution.

    // While there is Pattern in "input" parameter, replace each instance of Pattern with
    //   either an open or close tag.  Alternate the tag substituted to give tag pairs.
    while (superscriptPattern.matcher(transformedInput).find()) {
      if (!isInsideATag) {
        transformedInput = superscriptPattern.matcher(transformedInput).replaceFirst("<sup>");
        isInsideATag = true;
      } else {
        transformedInput = superscriptPattern.matcher(transformedInput).replaceFirst("</sup>");
        isInsideATag = false;
      }
    }

    return transformedInput;
  }

  /**
   * Takes in a String and returns it with all pairs of <code>~~</code> replaced by \<sub\>\</sub\> tags suitable for
   * display as HTML.
   * <p>
   * For example: <code>foo ~~bar~~ baz</code> is transformed into <code>foo \<sub\>bar\</sub\> baz</code>
   * <p>
   * There is no check for consistency of markup pairs (e.g., <code>foo ~bar~~ baz</code> will become <code>foo
   * ~bar\<sub\> baz</code>) which will, rightfully, infuriate some users.
   *
   * @param input HTML
   * @return String with all pairs of <code>~~</code> replaced by \<sub\>\</sub\> tags
   */
  private static String makeHtmlSubscript(final String input) {
    // If no Pattern in "input" parameter, then do nothing.
    if (StringUtils.isBlank(input)
        || (!subscriptPattern.matcher(input).find())) {
      return input;
    }

    String transformedInput = input; // This will be the String that gets returned.
    boolean isInsideATag = false; // Whether an open tag was the most recent substitution.

    // While there is Pattern in "input" parameter, replace each instance of Pattern with
    //   either an open or close tag.  Alternate the tag substituted to give tag pairs.
    while (subscriptPattern.matcher(transformedInput).find()) {
      if (!isInsideATag) {
        transformedInput = subscriptPattern.matcher(transformedInput).replaceFirst("<sub>");
        isInsideATag = true;
      } else {
        transformedInput = subscriptPattern.matcher(transformedInput).replaceFirst("</sub>");
        isInsideATag = false;
      }
    }

    return transformedInput;
  }

  /**
   * Linkify any possible web links excepting email addresses and enclosed with <p> tags
   *
   * @param text      text
   * @param maxLength The max length (in displayed characters) of the text to be displayed inside the <a>tag</a>
   * @return hyperlinked text
   */
  private static String hyperlinkEnclosedWithPTags(final String text, int maxLength) {
    final StringBuilder retStr = new StringBuilder("<p>");
    retStr.append(hyperlink(text, maxLength));
    retStr.append("</p>");
    return (retStr.toString());
  }

  /**
   * Linkify any possible web links excepting email addresses
   *
   * @param text      text
   * @param maxLength The max length (in displayed characters) of the text to be displayed inside the <a>tag</a>
   * @return hyperlinked text
   */
  private static String hyperlink(final String text, int maxLength) {
    if (StringUtils.isBlank(text)) {
      return text;
    }
    /*
     * HACK: [issue - if the text ends with ')' this is included in the hyperlink] 
     * so to avoid this we explicitly guard against it here 
     * NOTE: com.opensymphony.util.TextUtils.linkURL guards against an atomically wrapped url: 
     * "(http://www.domain.com)" but NOT "(see http://www.domain.com)"
     */
    if (text.indexOf('}') >= 0 || text.indexOf('{') >= 0) {
      return linkURL(text, null, maxLength);
    }
    String s = text.replace('(', '{');
    s = s.replace(')', '}');
    s = linkURL(s, null, maxLength);
    s = StringUtils.replace(s, "{", "(");
    s = StringUtils.replace(s, "}", ")");
    return s;
    // END HACK
  }

  /**
   * Return the escaped html. Useful when you want to make any dangerous scripts safe to render.
   * <p>
   * Also transforms wiki-type markup into HTML tags and replaces line breaks with HTML "break" tags.
   *
   * @param bodyContent bodyContent
   * @return escaped html text
   */
  private static String escapeHtml(final String bodyContent) {
    String transformedBodyContent = makeHtmlLineBreaks(StringEscapeUtils.escapeHtml(bodyContent));

    // The order of these three methods is important; we have to transform all instances of
    //   ''''' before trying to match instances of ''' or ''
    transformedBodyContent = makeHtmlStrongEmphasized(transformedBodyContent); // matches '''''
    transformedBodyContent = makeHtmlStrong(transformedBodyContent); // matches '''
    transformedBodyContent = makeHtmlEmphasized(transformedBodyContent); // matches ''

    transformedBodyContent = makeHtmlSuperscript(transformedBodyContent); // matches ^^
    transformedBodyContent = makeHtmlSubscript(transformedBodyContent); // matches ~~

    return transformedBodyContent;
  }

  /**
   * Escape html entity characters and high characters (eg "curvy" Word quotes). Note this method can also be used to
   * encode XML.
   *
   * @param s                  the String to escape.
   * @param encodeSpecialChars if true high characters will be encode other wise not.
   * @return the escaped string
   */
  private static String htmlEncode(String s, boolean encodeSpecialChars) {
    s = noNull(s, "");

    StringBuilder str = new StringBuilder();

    for (int j = 0; j < s.length(); j++) {
      char c = s.charAt(j);

      // encode standard ASCII characters into HTML entities where needed
      if (c < '\200') {
        switch (c) {
          case '"':
            str.append("&quot;");

            break;

          case '&':
            str.append("&amp;");

            break;

          case '<':
            str.append("&lt;");

            break;

          case '>':
            str.append("&gt;");

            break;

          default:
            str.append(c);
        }
      }
      // encode 'ugly' characters (ie Word "curvy" quotes etc)
      else if (encodeSpecialChars && (c < '\377')) {
        String hexChars = "0123456789ABCDEF";
        int a = c % 16;
        int b = (c - a) / 16;
        str.append("&#x")
            .append(hexChars.charAt(b))
            .append(hexChars.charAt(a))
            .append(';');
      }
      //add other characters back in - to handle charactersets
      //other than ascii
      else {
        str.append(c);
      }
    }

    return str.toString();
  }

  /**
   * Wrap all urls ('abc://' and 'www.abc') in specified string with href tags. Any text after the length defined by the
   * maxDisplayLength parameter will be dropped and three periods will be added "..."
   *
   * @param str              The block of text to check.
   * @param target           The target to use for the href (optional).
   * @param maxDisplayLength The max length (in displayed characters) of the text to be displayed inside the <a>tag</a>
   * @return String The block of text with all url's placed in href tags.
   */
  //TODO: If openSymphony's implemntation of this method one day mactches this, we can remove this class
  private static String linkURL(String str, String target, int maxDisplayLength) {
    StringBuilder sb = new StringBuilder((int) (str.length() * 1.05));
    sb.append(str);
    linkURL(sb, target, maxDisplayLength);
    return sb.toString();
  }

  /**
   * Return <code>string</code>, or <code>defaultString</code> if <code>string</code> is <code>null</code> or
   * <code>""</code>. Never returns <code>null</code>. <p> <p>Examples:</p>
   * <pre>
   * // prints "hello"
   * String s=null;
   * System.out.println(TextUtils.noNull(s,"hello");
   *
   * // prints "hello"
   * s="";
   * System.out.println(TextUtils.noNull(s,"hello");
   *
   * // prints "world"
   * s="world";
   * System.out.println(TextUtils.noNull(s, "hello");
   * </pre>
   *
   * @param string        the String to check.
   * @param defaultString The default string to return if <code>string</code> is <code>null</code> or <code>""</code>
   * @return <code>string</code> if <code>string</code> is non-empty, and <code>defaultString</code> otherwise
   * @see #stringSet(String)
   */
  private static String noNull(String string, String defaultString) {
    return (stringSet(string)) ? string : defaultString;
  }

  /**
   * Check whether <code>string</code> has been set to something other than <code>""</code> or <code>null</code>.
   *
   * @param string the <code>String</code> to check
   * @return a boolean indicating whether the string was non-empty (and non-null)
   */
  private static boolean stringSet(String string) {
    return (string != null) && !"".equals(string);
  }

  /**
   * Get the starting index of a URL (either 'abc://' or 'www.')
   *
   * @param str        String builder
   * @param startIndex index
   * @return new index
   */
  private static int getStartUrl(StringBuilder str, int startIndex) {
    int schemeIndex = getSchemeIndex(str, startIndex);
    final int wwwIndex = str.indexOf("www.", startIndex + 1);

    if ((schemeIndex == -1) && (wwwIndex == -1)) {
      return -1;
    } else if (schemeIndex == -1) {
      return wwwIndex;
    } else if (wwwIndex == -1) {
      return schemeIndex;
    }

    return Math.min(schemeIndex, wwwIndex);
  }

  private static void linkURL(StringBuilder str, String target, int maxDisplayLength) {
    String urlToDisplay;

    int lastEndIndex = -1; //Stores the index position, within the whole string, of the ending char of the last URL found.

    String targetString = ((target == null) || (target.trim().length() == 0)) ? "" : (" target=\"" + target.trim() + '\"');

    while (true) {
      int linkStartIndex = getStartUrl(str, lastEndIndex);

      //if no more links found - then end the loop
      if (linkStartIndex == -1) {
        break;
      } else {
        //Get the whole URL...
        //We move forward and add each character to the URL string until we encounter
        //an invalid URL character (we assume that the URL ends there).
        int linkEndIndex = linkStartIndex;
        String urlStr = "";

        while (true) {
          // if char at linkEndIndex is '&' then we look at the next 4 chars
          // to see if they make up "&amp;" altogether. This is the html coded
          // '&' and will pretty much stuff up an otherwise valid link becos of the ';'.
          // We therefore have to remove it before proceeding...
          if (str.charAt(linkEndIndex) == '&') {
            if (((linkEndIndex + 6) <= str.length()) && "&quot;".equals(str.substring(linkEndIndex, linkEndIndex + 6))) {
              break;
            } else if (((linkEndIndex + 5) <= str.length()) && "&amp;".equals(str.substring(linkEndIndex, linkEndIndex + 5))) {
              str.replace(linkEndIndex, linkEndIndex + 5, "&");
            }
          }

          if (isValidURLChar(str.charAt(linkEndIndex))) {
            urlStr += str.charAt(linkEndIndex);
            linkEndIndex++;

            if (linkEndIndex == str.length()) { //Reached end of str...

              break;
            }
          } else {
            break;
          }
        }

        //if the characters before the linkStart equal 'href="' then don't link the url - CORE-44
        if (linkStartIndex >= 6) { //6 = "href\"".length()

          String prefix = str.substring(linkStartIndex - 6, linkStartIndex);

          if ("href=\"".equals(prefix)) {
            lastEndIndex = linkEndIndex;

            continue;
          }
        }

        //if the characters after the linkEnd are '</a>' then this url is probably already linked - CORE-44
        if (str.length() >= (linkEndIndex + 4)) { //4 = "</a>".length()

          String suffix = str.substring(linkEndIndex, linkEndIndex + 4);

          if ("</a>".equals(suffix)) {
            lastEndIndex = linkEndIndex + 4;

            continue;
          }
        }

        //Decrement linkEndIndex back by 1 to reflect the real ending index position of the URL...
        linkEndIndex--;

        // If the last char of urlStr is a '.' we exclude it. It is most likely a full stop and
        // we don't want that to be part of an url.
        while (true) {
          char lastChar = urlStr.charAt(urlStr.length() - 1);

          if (lastChar == '.') {
            urlStr = urlStr.substring(0, urlStr.length() - 1);
            linkEndIndex--;
          } else {
            break;
          }
        }

        //if the URL had a '(' before it, and has a ')' at the end, trim the last ')' from the url
        //ie '(www.opensymphony.com)' => '(<a href="http://www.openymphony.com/">www.opensymphony.com</a>)'
        char lastChar = urlStr.charAt(urlStr.length() - 1);

        if (lastChar == ')') {
          if ((linkStartIndex > 0) && ('(' == (str.charAt(linkStartIndex - 1)))) {
            urlStr = urlStr.substring(0, urlStr.length() - 1);
            linkEndIndex--;
          }
        } else if (lastChar == '\'') {
          if ((linkStartIndex > 0) && ('\'' == (str.charAt(linkStartIndex - 1)))) {
            urlStr = urlStr.substring(0, urlStr.length() - 1);
            linkEndIndex--;
          }
        }
        //perhaps we ended with '&gt;', '&lt;' or '&quot;'
        //We need to strip these
        //ie '&quot;www.opensymphony.com&quot;' => '&quot;<a href="http://www.openymphony.com/">www.opensymphony.com</a>&quot;'
        //ie '&lt;www.opensymphony.com&gt;' => '&lt;<a href="http://www.openymphony.com/">www.opensymphony.com</a>&gt;'
        else if (lastChar == ';') {
          // 6 = "&quot;".length()
          if ((urlStr.length() > 6) && "&quot;".equalsIgnoreCase(urlStr.substring(urlStr.length() - 6))) {
            urlStr = urlStr.substring(0, urlStr.length() - 6);
            linkEndIndex -= 6;
          }
          // 4 = "&lt;".length()  || "&gt;".length()
          else if (urlStr.length() > 4) {
            final String endingStr = urlStr.substring(urlStr.length() - 4);

            if ("&lt;".equalsIgnoreCase(endingStr) || "&gt;".equalsIgnoreCase(endingStr)) {
              urlStr = urlStr.substring(0, urlStr.length() - 4);
              linkEndIndex -= 4;
            }
          }
        }

        // we got the URL string, now we validate it and convert it into a hyperlink...

        if (maxDisplayLength > 0 && urlStr.length() > maxDisplayLength) {
          urlToDisplay = htmlEncode(urlStr.substring(0, maxDisplayLength), true) + "...";
        } else {
          urlToDisplay = htmlEncode(urlStr, true);
        }

        if (urlStr.toLowerCase().startsWith("www.")) {
          urlStr = "http://" + urlStr;
        }

        if (verifyHierarchicalURI(urlStr, null)) {
          //Construct the hyperlink for the url...
          String urlLink = "<a rel=\"nofollow\" href=\"" + urlStr + "\"" + targetString;

          if (maxDisplayLength > 0 && urlStr.length() > maxDisplayLength) {
            urlLink += " title=\"" + htmlEncode(urlStr, true) + "\">" + urlToDisplay + "</a>";
          } else {
            urlLink += ">" + urlToDisplay + "</a>";
          }

          //Remove the original urlStr from str and put urlLink there instead...
          str.replace(linkStartIndex, linkEndIndex + 1, urlLink);

          //Set lastEndIndex to reflect the position of the end of urlLink
          //within the whole string...
          lastEndIndex = (linkStartIndex - 1) + urlLink.length();
        } else {
          //lastEndIndex is different from the one above cos' there's no
          //<a href...> tags added...
          lastEndIndex = (linkStartIndex - 1) + urlStr.length();
        }
      }
    }
  }

  /**
   * Given a string, and the index to start looking at, find the index of the start of the scheme. Eg.
   * <pre>
   * getSchemeIndex("notes://abc", 0) -> 0
   * getSchemeIndex("abc notes://abc", 0) -> 4
   * </pre>
   *
   * @param str        The string to search for
   * @param startIndex Where to start looking at
   * @return The location the string was found, ot -1 if the string was not found.
   */
  private static int getSchemeIndex(StringBuilder str, int startIndex) {
    int schemeIndex = str.indexOf(SCHEME_URL, startIndex + 1);

    //if it was not found, or found at the start of the string, then return 'not found'
    if (schemeIndex <= 0) {
      return -1;
    }

    //walk backwards through the scheme until we find the first non valid character
    int schemeStart;

    for (schemeStart = schemeIndex - 1; schemeStart >= 0; schemeStart--) {
      char currentChar = str.charAt(schemeStart);

      if (!isValidSchemeChar(currentChar)) {
        break;
      }
    }

    //reset the scheme to the starting character
    schemeStart++;

    /*
         we don't want to do this, otherwise an invalid scheme would ruin the linking for later schemes
                if (isValidScheme(str.substring(schemeStart, schemeIndex)))
                    return schemeStart;
                else
                    return -1;
    */
    return schemeStart;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Forked from com.opensymphony.util.UrlUtils

  private static final String SCHEME_URL = "://";

  private static boolean isAcceptableReservedChar(char c) {
    return (c == ';') || (c == '/') || (c == '?') || (c == ':') || (c == '@') || (c == '&') || (c == '=') || (c == '+') || (c == '$') || (c == ',');
  }

  private static boolean isAlpha(char c) {
    return ((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z'));
  }

  private static boolean isDigit(char c) {
    return ((c >= '0') && (c <= '9'));
  }

  private static boolean isOtherChar(char c) {
    return (c == '#') || (c == '%');
  }

  private static boolean isUnreservedChar(char c) {
    return (c == '-') || (c == '_') || (c == '.') || (c == '!') || (c == '~') || (c == '*') || (c == '\'') || (c == '(') || (c == ')');
  }

  private static boolean isValidScheme(String scheme) {
    if ((scheme == null) || (scheme.length() == 0)) {
      return false;
    }
    char[] schemeChars = scheme.toCharArray();
    if (!isAlpha(schemeChars[0])) {
      return false;
    }
    for (int i = 1; i < schemeChars.length; i++) {
      char schemeChar = schemeChars[i];
      if (!(isValidSchemeChar(schemeChar))) {
        return false;
      }
    }
    return true;
  }

  private static boolean isValidSchemeChar(char c) {
    return isAlpha(c) || isDigit(c) || (c == '+') || (c == '-') || (c == '.');
  }

  private static boolean isValidURLChar(char c) {
    return isAlpha(c) || isDigit(c) || isAcceptableReservedChar(c) || isUnreservedChar(c) || isOtherChar(c);
  }

  private static boolean verifyHierarchicalURI(String uri, String[] schemesConsideredInvalid) {
    if ((uri == null) || (uri.length() < SCHEME_URL.length())) {
      return false;
    }
    int schemeUrlIndex = uri.indexOf(SCHEME_URL);
    if (schemeUrlIndex == -1) {
      return false;
    }
    final String scheme = uri.substring(0, schemeUrlIndex);
    if (!isValidScheme(scheme)) {
      return false;
    }
    if (schemesConsideredInvalid != null) {
      for (int i = 0; i < schemesConsideredInvalid.length; i++) {
        String invalidScheme = schemesConsideredInvalid[i];
        if (scheme.equalsIgnoreCase(invalidScheme)) {
          return false;
        }
      }
    }
    if (uri.length() < (schemeUrlIndex + SCHEME_URL.length() + 1)) {
      return false;
    }
    for (int i = schemeUrlIndex + SCHEME_URL.length(); i < uri.length();
         i++) {
      char c = uri.charAt(i);
      if (!isValidURLChar(c)) {
        return false;
      }
    }
    return true;
  }

}
