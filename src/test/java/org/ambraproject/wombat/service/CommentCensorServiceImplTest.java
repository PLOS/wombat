package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.StubTheme;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Test
public class CommentCensorServiceImplTest {

  private static Site createStubSite(Collection<String> censoredWords) {
    return new StubTheme.Builder("testTheme", "testJournal", "The Test Journal")
        .addConfigValue("comment", "censoredWords", censoredWords)
        .build()
        .wrapInStubSite("testSite");
  }

  @DataProvider
  public Iterator<Object[]> getCensoredWordCases() {
    Object[][] cases = new Object[][]{
        {"", new String[]{}, new String[]{}},
        {"bar", new String[]{"foo"}, new String[]{}},
        {"foo", new String[]{"foo"}, new String[]{"foo"}},
        {"a b c d e", new String[]{"b", "d", "f"}, new String[]{"b", "d"}},

        // Should be caught despite extra whitespace in the middle of a censored phrase
        {"a foo bar b", new String[]{"foo bar"}, new String[]{"foo bar"}},
        {"a foo \t bar b", new String[]{"foo bar"}, new String[]{"foo bar"}},
    };
    return Stream.of(cases).map(c -> {
      String text = (String) c[0];
      Collection<String> censoredWords = ImmutableList.copyOf((String[]) c[1]);
      Collection<String> expectedHits = ImmutableList.copyOf((String[]) c[2]);
      return new Object[]{censoredWords, text, expectedHits};
    }).iterator();
  }

  @Test(dataProvider = "getCensoredWordCases")
  public void testFindCensoredWords(Collection<String> censoredWords, String text, Collection<String> expectedHits) {
    CommentCensorServiceImpl commentCensorService = new CommentCensorServiceImpl();
    Site testSite = createStubSite(censoredWords);
    commentCensorService.siteSet = new SiteSet(ImmutableList.of(testSite));
    Collection<String> actualHits = commentCensorService.findCensoredWords(testSite, text);
    Assert.assertEquals(ImmutableSet.copyOf(actualHits), ImmutableSet.copyOf(expectedHits));
  }


  @DataProvider
  public Iterator<Object[]> getMultiSiteCensoredWordCases() {
    Object[][] cases = new Object[][]{
        {
            // list of sites
            new Object[][]{
                // one site
                new Object[]{
                    "siteToTest" /*site key*/,
                    new String[]{"foo", "bar"}/*censored words*/
                },
                // one site
                new Object[]{
                    "otherSite" /*site key*/,
                    new String[]{"bar", "baz"}/*censored words*/
                },
            },
            "a bar b baz c" /*text to test*/,
            new String[]{"bar"}/*expected hits*/
        },
    };
    return Stream.of(cases).map(c -> {
      Object[][] siteSpecs = (Object[][]) c[0];
      List<Site> sites = Stream.of(siteSpecs).map((Object[] siteSpec) -> {
        String siteKey = (String) siteSpec[0];
        String[] censoredWords = (String[]) siteSpec[1];
        return new StubTheme.Builder("theme_for_" + siteKey, "testJournal", "The Test Journal")
            .addConfigValue("comment", "censoredWords", censoredWords)
            .build()
            .wrapInStubSite(siteKey);
      }).collect(Collectors.toList());

      String siteKeyUnderTest = sites.get(0).getKey();
      String text = (String) c[1];
      Collection<String> expectedHits = ImmutableList.copyOf((String[]) c[2]);
      return new Object[]{sites, siteKeyUnderTest, text, expectedHits};
    }).iterator();
  }

  @Test(dataProvider = "getMultiSiteCensoredWordCases")
  public void testFindCensoredWordsWithMultipleSites(Collection<Site> sites, String siteKeyUnderTest,
                                                     String text, Collection<String> expectedHits) {
    CommentCensorServiceImpl commentCensorService = new CommentCensorServiceImpl();
    commentCensorService.siteSet = new SiteSet(sites);
    Site siteUnderTest = commentCensorService.siteSet.getSite(siteKeyUnderTest);

    Collection<String> actualHits = commentCensorService.findCensoredWords(siteUnderTest, text);
    Assert.assertEquals(ImmutableSet.copyOf(actualHits), ImmutableSet.copyOf(expectedHits));
  }

}