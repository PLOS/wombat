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

public class HtmlAttributeTransformationTest {

  @Test
  public void testApply(){

    SitePageContext sitePageContext = mock(SitePageContext.class);
    SiteSet siteset = mock(SiteSet.class);
    when(sitePageContext.buildLink(anyString())).thenReturn("path/to/an/internal/page");
    when(sitePageContext.buildLink(any(SiteSet.class), anyString(), anyString())).thenReturn("path/to/another/journal/page");

    String testHtml =  "<img data-lemur-key=\"content_1234\"/>" +
            "<a data-lemur-key=\"file_4321\"/>" +
            "<a data-lemur-link=\"s/lorum_ipsum#anchor_id\">Lorem Ipsum</a>" +
            "<a data-lemur-link=\"PLoSCompBiol|s/test-page\">test cross-journal link</a>" +
            "<a data-lemur-doi=\"10.1371/journal.pone.0008083\">article link</a>";

    Document document = Jsoup.parseBodyFragment(testHtml);

    HtmlAttributeTransformation.LINK.apply(sitePageContext, siteset, document);
    HtmlAttributeTransformation.ASSET.apply(sitePageContext, siteset, document);
    HtmlAttributeTransformation.IMAGE.apply(sitePageContext, siteset, document);
    HtmlAttributeTransformation.ARTICLE.apply(sitePageContext, siteset, document);

    ArgumentCaptor<String> pathArg = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> journalKeyArg = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<SiteSet> siteSetArg = ArgumentCaptor.forClass(SiteSet.class);

    verify(sitePageContext, times(4)).buildLink(pathArg.capture());
    verify(sitePageContext).buildLink(siteSetArg.capture(), journalKeyArg.capture(), pathArg.capture());

    List<String> pathArgs = pathArg.getAllValues();
    assertEquals("s/lorum_ipsum#anchor_id", pathArgs.get(0));
    assertEquals("indirect/file_4321", pathArgs.get(1));
    assertEquals("indirect/content_1234", pathArgs.get(2));
    assertEquals("article?id=10.1371/journal.pone.0008083", pathArgs.get(3));
    assertEquals("s/test-page", pathArgs.get(4));
    assertEquals("PLoSCompBiol", journalKeyArg.getValue());

    assertEquals(document.toString(),
            "<html>\n" +
            " <head></head>\n" +
            " <body>\n" +
            "  <img src=\"path/to/an/internal/page\" />\n" +
            "  <a href=\"path/to/an/internal/page\"></a>\n" +
            "  <a href=\"path/to/an/internal/page\">Lorem Ipsum</a>\n" +
            "  <a href=\"path/to/another/journal/page\">test cross-journal link</a>\n" +
            "  <a href=\"path/to/an/internal/page\">article link</a>\n" +
            " </body>\n" +
            "</html>");

  }
}
