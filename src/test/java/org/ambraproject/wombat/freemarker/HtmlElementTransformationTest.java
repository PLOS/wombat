package org.ambraproject.wombat.freemarker;

import org.ambraproject.wombat.config.site.SiteSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class HtmlElementTransformationTest {

  @Test
  public void testApply() {

    SitePageContext sitePageContext = mock(SitePageContext.class);
    SiteSet siteset = mock(SiteSet.class);
    when(sitePageContext.buildLink(anyString())).thenReturn("path/to/an/internal/page");
    when(sitePageContext.buildLink(any(SiteSet.class), anyString(), anyString())).thenReturn("path/to/another/journal/page");

    String testHtml = "<img data-lemur-key=\"content_1234\"/>" +
        "<a data-lemur-key=\"file_4321\"/>" +
        "<a data-lemur-link=\"s/lorum_ipsum\" data-lemur-link-suffix=\"#anchor_string\">Lorem Ipsum</a>" +
        "<a data-lemur-link=\"s/test-page\" data-lemur-link-journal=\"PLoSCompBiol\">test cross-journal link</a>" +
        "<a data-lemur-doi=\"10.1371/journal.pone.0008083\">article link</a>";

    Document document = Jsoup.parseBodyFragment(testHtml);

    HtmlElementTransformation.LINK.apply(sitePageContext, siteset, document);
    HtmlElementTransformation.ASSET.apply(sitePageContext, siteset, document);
    HtmlElementTransformation.IMAGE.apply(sitePageContext, siteset, document);
    HtmlElementTransformation.ARTICLE.apply(sitePageContext, siteset, document);

    ArgumentCaptor<String> pathArg = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> journalKeyArg = ArgumentCaptor.forClass(String.class);

    verify(sitePageContext, times(4)).buildLink(pathArg.capture());
    verify(sitePageContext).buildLink(any(SiteSet.class), journalKeyArg.capture(), pathArg.capture());

    List<String> pathArgs = pathArg.getAllValues();
    assertEquals(pathArgs.get(0), "s/lorum_ipsum");
    assertEquals(pathArgs.get(1), "s/file?id=file_4321");
    assertEquals(pathArgs.get(2), "indirect/content_1234");
    assertEquals(pathArgs.get(3), "article?id=10.1371/journal.pone.0008083");
    assertEquals(pathArgs.get(4), "s/test-page");
    assertEquals(journalKeyArg.getValue(), "PLoSCompBiol");

    assertEquals(document.toString(),
        "<html>\n" +
            " <head></head>\n" +
            " <body>\n" +
            "  <img src=\"path/to/an/internal/page\" />\n" +
            "  <a href=\"path/to/an/internal/page\"></a>\n" +
            "  <a href=\"path/to/an/internal/page#anchor_string\">Lorem Ipsum</a>\n" +
            "  <a href=\"path/to/another/journal/page\">test cross-journal link</a>\n" +
            "  <a href=\"path/to/an/internal/page\">article link</a>\n" +
            " </body>\n" +
            "</html>");

  }
}
