package org.ambraproject.wombat.service;

import com.google.common.collect.Maps;
import com.opensymphony.util.UrlUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import sun.misc.BASE64Encoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommentFormatting {
  private CommentFormatting() {
    throw new AssertionError();
  }

  private static final String REPLIES_KEY = "replies";

  /**
   * Add fields defined by "CommentModelField" to the comment and all nested replies. A new, deep copy of the map is
   * returned. The map passed as an argument is not modified.
   */
  public static Map<String, Object> addFormattingFields(Map<String, ?> commentMetadata) {
    Map<String, Object> addedFields = EnumSet.allOf(CommentModelField.class).stream().collect(Collectors.toMap(
        field -> field.key,
        field -> field.generateFieldValue(commentMetadata)));

    Map<String, Object> modifiedMetadata = Maps.newHashMapWithExpectedSize(commentMetadata.size() + addedFields.size());
    modifiedMetadata.putAll(commentMetadata);
    Collection<Map<String, ?>> replies = (Collection<Map<String, ?>>) modifiedMetadata.remove(REPLIES_KEY);
    List<Map<String, Object>> modifiedReplies = replies.stream()
        .map(CommentFormatting::addFormattingFields)
        .collect(Collectors.toList());
    modifiedMetadata.put(REPLIES_KEY, modifiedReplies);

    modifiedMetadata.putAll(addedFields);
    return modifiedMetadata;
  }

  /**
   * Model fields defined by old Ambra's "AnnotationView".
   * <p>
   * This is a ridiculous amount of display logic that is reproduced here for the sake of expediency. Please delete
   * anything that is not needed.
   */
  private static enum CommentModelField {
    bodyHtml("bodyHtml") {
      @Override
      protected Object generateFieldValue(Map<String, ?> comment) {
        return hyperlinkEnclosedWithPTags(escapeHtml(get(comment, "body")), 25);
      }
    },
    truncatedBody("truncatedBody") {
      @Override
      protected Object generateFieldValue(Map<String, ?> comment) {
        return hyperlinkEnclosedWithPTags(truncateText(escapeHtml(get(comment, "body")), TRUNCATED_COMMENT_LENGTH), 25);
      }
    },
    bodyWithUrlLinkingNoPTags("bodyWithUrlLinkingNoPTags") {
      @Override
      protected Object generateFieldValue(Map<String, ?> comment) {
        return hyperlink(escapeHtml(get(comment, "body")), 25);
      }
    },
    truncatedBodyWithUrlLinkingNoPTags("truncatedBodyWithUrlLinkingNoPTags") {
      @Override
      protected Object generateFieldValue(Map<String, ?> comment) {
        return hyperlink(truncateText(escapeHtml(get(comment, "body")), TRUNCATED_COMMENT_LENGTH), 25);
      }
    },
    bodyWithHighlightedText("bodyWithHighlightedText") {
      @Override
      protected Object generateFieldValue(Map<String, ?> comment) {
        String body = get(comment, "body");
        String highlightedText = get(comment, "highlightedText");
        if (highlightedText.isEmpty()) return body;
        String bodyWithHt = highlightedText + "\n\n" + body;
        return hyperlinkEnclosedWithPTags(escapeHtml(bodyWithHt), 150);
      }
    },
    competingInterestStatement("competingInterestStatement") {
      @Override
      protected Object generateFieldValue(Map<String, ?> comment) {
        return escapeHtml(get(comment, "competingInterestBody"));
      }
    },
    truncatedCompetingInterestStatement("truncatedCompetingInterestStatement") {
      @Override
      protected Object generateFieldValue(Map<String, ?> comment) {
        return truncateText(escapeHtml(get(comment, "competingInterestBody")), TRUNCATED_COMMENT_LENGTH);
      }
    };

    private final String key;

    private CommentModelField(String key) {
      this.key = key;
    }

    private static String get(Map<String, ?> commentMetadata, String key) {
      Object value = commentMetadata.get(key);
      return (value == null) ? "" : (String) value;
    }

    protected abstract Object generateFieldValue(Map<String, ?> commentMetadata);
  }


  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // Code forklifted from old Ambra is below
  // Please refactor liberally
  // Please avoid introducing new dependencies on this

  private static final int TRUNCATED_COMMENT_LENGTH = 256;
  private static final String HTTP_PREFIX = "http://";
  private static final Pattern maliciousContentPattern = Pattern.compile("[<>\"\'%;()&+]");
  private static final Pattern lineBreakPattern = Pattern.compile("\\p{Zl}|\r\n|\n|\u0085|\\p{Zp}");
  private static final Pattern strongPattern = Pattern.compile("'''");
  private static final Pattern emphasizePattern = Pattern.compile("''");
  private static final Pattern strongEmphasizePattern = Pattern.compile("'''''");
  private static final Pattern superscriptPattern = Pattern.compile("\\^\\^");
  private static final Pattern subscriptPattern = Pattern.compile("~~");

  private static Logger log = LoggerFactory.getLogger(CommentFormatting.class);

  /**
   * Create a hash of a string
   *
   * @param string the string to make the hash
   * @return the hash of the string
   */
  private static String createHash(String string) {
    return createHash(string.getBytes());
  }

  /**
   * Create a hash of a byte array
   *
   * @param bytes
   * @return the hash of the byte array
   */
  private static String createHash(byte[] bytes) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      messageDigest.update(bytes);

      return encodeText(messageDigest.digest());
    } catch (NoSuchAlgorithmException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Produces a String value suitable for rendering in HTML for the given binary data.
   */
  private static String encodeText(byte[] data) {
    BASE64Encoder encoder = new BASE64Encoder();
    String base64 = encoder.encodeBuffer(data);

    // Make the returned value a little prettier by replacing slashes with underscores, and removing the trailing
    // "=".
    base64 = base64.replace('/', '_').trim();
    return base64.substring(0, base64.length() - 1);
  }

  /**
   * Convert a List of URIs to a List of Strings
   *
   * @param list a List of URIs
   * @return a list of strings
   */
  private static List<String> toStringList(List<URI> list) {
    List<String> simpleCollection = new ArrayList<String>();

    for (URI uri : list) {
      simpleCollection.add(uri.toString());
    }

    return simpleCollection;
  }

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
   * Linkify any possible web links excepting email addresses and enclosed with <p> tags
   *
   * @param text text
   * @return hyperlinked text
   */
  private static String hyperlinkEnclosedWithPTags(final String text) {
    return hyperlinkEnclosedWithPTags(text, 0);
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
   * Linkify any possible web links excepting email addresses
   *
   * @param text text
   * @return hyperlinked text
   */
  private static String hyperlink(final String text) {
    return hyperlink(text, 0);
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
   * @param bodyContent bodyContent
   * @return Return escaped and hyperlinked text
   */
  private static String escapeAndHyperlink(final String bodyContent) {
    return hyperlinkEnclosedWithPTags(escapeHtml(bodyContent), 0);
  }

  /**
   * Transforms an org.w3c.dom.Document into a String
   *
   * @param node Document to transform
   * @return String representation of node
   * @throws javax.xml.transform.TransformerException TransformerException
   */
  private static String getAsXMLString(final Node node) throws TransformerException {
    final Transformer tf = TransformerFactory.newInstance().newTransformer();
    final StringWriter stringWriter = new StringWriter();

    tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    tf.transform(new DOMSource(node), new StreamResult(stringWriter));

    return stringWriter.toString();
  }

  /**
   * @param url A URL
   * @return whether the url is a valid address
   */
  private static boolean verifyUrl(final String url) {
    try {
      URI u = new URI(url);

      // To see if we can get a valid url or if we get an exception
      u.toURL();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Make a valid url from the given input url or url fragment
   *
   * @param url url
   * @return valid url
   * @throws java.net.MalformedURLException MalformedURLException
   */
  private static String makeValidUrl(final String url) throws MalformedURLException {
    String finalUrl = url;
    if (!verifyUrl(finalUrl)) {
      finalUrl = HTTP_PREFIX + finalUrl;
      if (!verifyUrl(finalUrl)) {
        throw new MalformedURLException("Invalid url:" + url);
      }
    }
    return finalUrl;
  }

  /**
   * Check if the input text is potentially malicious. For more details read; http://www.dwheeler.com/secure-programs/Secure-Programs-HOWTO/cross-site-malicious-content.html
   *
   * @param text text
   * @return boolean
   */
  private static boolean isPotentiallyMalicious(final String text) {
    return maliciousContentPattern.matcher(text).find();
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

          if (UrlUtils.isValidURLChar(str.charAt(linkEndIndex))) {
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

        if (UrlUtils.verifyHierachicalURI(urlStr)) {
          //Construct the hyperlink for the url...
          String urlLink;

          if (maxDisplayLength > 0 && urlStr.length() > maxDisplayLength) {
            //urlLink = "<a href=\"" + urlStr + "\"" + targetString + ">" + urlToDisplay + "</a>";
            urlLink = "<a href=\"" + urlStr + "\"" + targetString + " title=\"" + htmlEncode(urlStr, true) + "\">" + urlToDisplay + "</a>";
          } else {
            urlLink = "<a href=\"" + urlStr + "\"" + targetString + ">" + urlToDisplay + "</a>";
          }

          //urlLink = "<a href=\"" + urlStr + '\"' + targetString + '>' + urlToDisplay + "</a>";

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
    int schemeIndex = str.indexOf(UrlUtils.SCHEME_URL, startIndex + 1);

    //if it was not found, or found at the start of the string, then return 'not found'
    if (schemeIndex <= 0) {
      return -1;
    }

    //walk backwards through the scheme until we find the first non valid character
    int schemeStart;

    for (schemeStart = schemeIndex - 1; schemeStart >= 0; schemeStart--) {
      char currentChar = str.charAt(schemeStart);

      if (!UrlUtils.isValidSchemeChar(currentChar)) {
        break;
      }
    }

    //reset the scheme to the starting character
    schemeStart++;

    /*
         we don't want to do this, otherwise an invalid scheme would ruin the linking for later schemes
                if (UrlUtils.isValidScheme(str.substring(schemeStart, schemeIndex)))
                    return schemeStart;
                else
                    return -1;
    */
    return schemeStart;
  }

  /**
   * Remove all of the XML and HTML tags from the <code>s</code> parameter. The RegEx in this method removes everything
   * between two "innermost" brackets (e.g., <code>&lt;...&gt;</code>) so it may accidentally remove sections of text
   * that are not tags, just because both the "greater than" and "less than" symbols exist and there is no tag bewteen
   * them.
   * <p>
   * For instance, the title: "Yak mass &lt; whale mass, but yak mass &gt; weasel mass" would be reduced to: "Yak mass
   * weasel mass" which is very much not the desired result. That is why this method is prefaced with the lable
   * "simple".
   * <p>
   * Note that the above example only fails because there is no tag between the &lt; and &gt; for this method to remove.
   * If the title was, instead, "Yak mass &lt; whale mass, &lt;p&gt;but yak mass &gt; weasel mass", then the &lt;p&gt;
   * tag would be removed and the rest of the title would be left alone.
   * <p>
   * TODO: Augment the RegEx to fix the above corner case.  This can be accomplished by ensuring todo: all openning tags
   * have matching closing tags, then handling valid singleton tags (e.g., todo: &lt;p/&gt;) as special cases.
   *
   * @param s The String which will have all of its tags removed
   * @return The <code>s</code> parameter with all tags removed
   */
  private static String simpleStripAllTags(String s) {
    return s.replaceAll("<[^<>]*?>", "");
  }

  /**
   * Transform a xml string to html text
   *
   * @param xmlContent xml
   * @return html html text
   */
  private static String transformXMLtoHtmlText(String xmlContent) {
    if (xmlContent != null) {
      String htmlContent = "";

      try {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        // surround the xml content with temporary root element to make sure that it can be parsed.
        InputSource source = new InputSource(new StringReader("<temprootelement>" + xmlContent + "</temprootelement>"));
        Document doc = db.parse(source);

        // remove all the elements from the xml content
        StringWriter stw = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "text");
        transformer.transform(new DOMSource(doc), new StreamResult(stw));

        htmlContent = stw.toString();
        // make sure all the characters are escaped using html entities
        htmlContent = StringEscapeUtils.escapeHtml(htmlContent);

      } catch (Exception e) {
        log.info("Failed to transform " + xmlContent + " to html text", e);
      }

      return htmlContent;
    } else {
      return "";
    }
  }

  /**
   * truncate text
   *
   * @param text            text to truncate
   * @param truncatedLength truncate length
   * @return truncated text
   */
  private static String truncateText(String text, int truncatedLength) {
    if (StringUtils.isBlank(text)) {
      return text;
    }

    if (text.length() > truncatedLength) {
      final String abrsfx = "...";
      final int abrsfxlen = 3;
      // attempt to truncate on a word boundary
      int index = truncatedLength - 1;

      while (!Character.isWhitespace(text.charAt(index)) ||
          index > (truncatedLength - abrsfxlen - 1)) {
        if (--index == 0) {
          break;
        }
      }

      if (index == 0) {
        index = truncatedLength - abrsfxlen - 1;
      }

      text = text.substring(0, index) + abrsfx;
      assert text.length() <= truncatedLength;
    }

    return text;
  }

  /**
   * truncate text and close open tags
   *
   * @param text            text to truncate
   * @param truncatedLength truncate length
   * @return truncated text
   */
  private static String truncateTextCloseOpenTag(String text, final int truncatedLength) {
    String shortenedText = truncateText(text, truncatedLength);
    int openIndex = shortenedText.lastIndexOf("<i>");
    if (openIndex != -1) {
      int closeIndex = shortenedText.indexOf("</i>", openIndex);
      if (closeIndex == -1) {
        shortenedText = shortenedText + "</i>";
      }
    }
    return shortenedText;
  }

  /**
   * Create a list of first, second and last authors
   *
   * @param authors the list of authors
   * @return a combined string of first, second and last authors
   */
  private static String makeAuthorString(String[] authors) {
    if (authors.length <= 3) {
      return StringUtils.join(authors, ", ");
    } else {
      //use first two and last.
      return authors[0].trim() + ", " + authors[1].trim() + ", [...], " + authors[authors.length - 1].trim();
    }
  }

}
