package org.ambraproject.wombat.service;

import static org.ambraproject.wombat.util.FileUtils.getFile;
import static org.ambraproject.wombat.util.FileUtils.getInputStream;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.ambraproject.wombat.config.TestSpringConfiguration;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.Theme;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.util.MockSiteUtil;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(classes = TestSpringConfiguration.class)
public class ArticleTransformServiceImplTest extends AbstractJUnit4SpringContextTests {
  private ArticleTransformServiceImpl service = new ArticleTransformServiceImpl();

  @Autowired
  private SiteSet siteSet;

  private Site site;
  private Theme theme;

  /* It would probably be better to do a directory listing, but you
   * can't really do that with resources in Java. */
  private String[] jatsFiles = new String[] {
    "jats/journal.pbio.1001613",
    "jats/journal.pbio.1001826",
    "jats/journal.pbio.1001835",
    "jats/journal.pcbi.1000628",
    "jats/journal.pcbi.1002430",
    "jats/journal.pgen.1000879",
    "jats/journal.pgen.1006304",
    "jats/journal.pmed.0020087",
    "jats/journal.pmed.1001681",
    "jats/journal.pntd.0000279",
    "jats/journal.pntd.0004970",
    "jats/journal.pntd.0005192",
    "jats/journal.pone.0005929",
    "jats/journal.pone.0009556",
    "jats/journal.pone.0013453",
    "jats/journal.pone.0016259",
    "jats/journal.pone.0016747",
    "jats/journal.pone.0019978",
    "jats/journal.pone.0023287",
    "jats/journal.pone.0024714",
    "jats/journal.pone.0027182",
    "jats/journal.pone.0032400",
    "jats/journal.pone.0033393",
    "jats/journal.pone.0036088",
    "jats/journal.pone.0038247",
    "jats/journal.pone.0038943",
    "jats/journal.pone.0041874",
    "jats/journal.pone.0042076",
    "jats/journal.pone.0045987",
    "jats/journal.pone.0050883",
    "jats/journal.pone.0052095",
    "jats/journal.pone.0052141",
    "jats/journal.pone.0053979",
    "jats/journal.pone.0059808",
    "jats/journal.pone.0059932",
    "jats/journal.pone.0064284",
    "jats/journal.pone.0066775",
    "jats/journal.pone.0067669",
    "jats/journal.pone.0071424",
    "jats/journal.pone.0073974",
    "jats/journal.pone.0078473",
    "jats/journal.pone.0087665",
    "jats/journal.pone.0088456",
    "jats/journal.pone.0089660",
    "jats/journal.pone.0091646",
    "jats/journal.pone.0093674",
    "jats/journal.pone.0094697",
    "jats/journal.pone.0097132",
    "jats/journal.pone.0099384",
    "jats/journal.pone.0099589",
    "jats/journal.pone.0102870",
    "jats/journal.pone.0105624",
    "jats/journal.pone.0107100",
    "jats/journal.pone.0111123",
    "jats/journal.pone.0111313",
    "jats/journal.pone.0115564",
    "jats/journal.pone.0119435",
    "jats/journal.pone.0124190",
    "jats/journal.pone.0124316",
    "jats/journal.pone.0132149",
    "jats/journal.pone.0136799",
    "jats/journal.pone.0141317",
    "jats/journal.pone.0142354",
    "jats/journal.pone.0142440",
    "jats/journal.pone.0142811",
    "jats/journal.pone.0142934",
    "jats/journal.pone.0143347",
    "jats/journal.pone.0144015",
    "jats/journal.pone.0147183",
    "jats/journal.pone.0153506",
    "jats/journal.pone.0155736",
    "jats/journal.pone.0157464",
    "jats/journal.pone.0158490",
    "jats/journal.pone.0159292",
    "jats/journal.pone.0164056",
    "jats/journal.pone.0171120",
    "jats/journal.pone.0174761",
    "jats/journal.pone.0175185",
    "jats/journal.pone.0177414",
    "jats/journal.pone.0181183",
    "jats/journal.pone.0182377",
    "jats/journal.pone.0183243",
    "jats/journal.pone.0189032",
    "jats/journal.pone.0189503",
    "jats/journal.pone.0190755",
    "jats/journal.pone.0192138",
    "jats/journal.pone.0195790",
    "jats/journal.pone.0197214",
    "jats/journal.pone.0201498",
    "jats/journal.pone.0203023",
    "jats/journal.pone.0204351",
    "jats/journal.pone.0208185",
    "jats/journal.pone.0213146",
    "jats/journal.pone.0219838",
    "jats/journal.pone.0220561",
    "jats/journal.ppat.0030086",
    "jats/journal.ppat.1000256",
    "jats/journal.ppat.1004143",
    "jats/journal.ppat.1005343",
    "jats/journal.ppat.1007815",
  };

  @Before
  public void setup() throws IOException {
    Site realSite = MockSiteUtil.getByUniqueJournalKey(siteSet, "journal1Key");
    site = spy(realSite);
    theme = mock(Theme.class);
    when(site.getTheme()).thenReturn(theme);
    Map<String, Object> configMap = new HashMap<String, Object>();
    configMap.put("showsCitedArticles", true);
    when(theme.getConfigMap("article")).thenReturn(configMap);
    when(theme.getStaticResource("xform/article-transform.xsl"))
        .thenReturn(getInputStream("article-transform-desktop.xsl"));
    when(theme.getStaticResource("xform/jpub3-html.xsl")).thenReturn(getInputStream("jpub3-html.xsl"));
  }

  @Test
  public void baselineSmokeTest() throws IOException {
    for (String jats : jatsFiles) {
      FileInputStream jatsInput = new FileInputStream(getFile(jats + ".xml"));
      String expectedHtml = IOUtils.toString(new FileInputStream(getFile(jats + ".html")));
      ArticlePointer articlePointer = mock(ArticlePointer.class);
      ByteArrayOutputStream actualHtml = new ByteArrayOutputStream();

      service.transformArticle(site, articlePointer, Arrays.asList(), jatsInput, actualHtml);
      assertEquals(String.format("Error comparing %s", jats), expectedHtml, actualHtml.toString("UTF-8"));
    }
  }

  @Test
  public void testTransform() throws IOException {
    FileInputStream jatsInput = new FileInputStream(getFile("jats/attrib.xml"));

    ArticlePointer articlePointer = mock(ArticlePointer.class);
    ByteArrayOutputStream actualHtml = new ByteArrayOutputStream();
    service.transformArticle(site, articlePointer, Arrays.asList(), jatsInput, actualHtml);
    
    Document doc = Jsoup.parse(actualHtml.toString("UTF-8"));
    assertEquals("Hofstadter's Law", doc.select("html body div.section.toc-section.body-section blockquote p.attrib").first().text());
  }
}
