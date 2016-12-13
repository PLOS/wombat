package org.ambraproject.wombat.service;

import com.google.common.collect.ImmutableList;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.theme.StubTheme;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;

@Test
public class CommentCensorServiceImplTest {

  private Site testSite;
  private CommentCensorServiceImpl commentCensorService;

  @BeforeMethod
  public void setUpCommentCensorService() {
    StubTheme theme = new StubTheme.Builder("testTheme", "testJournal", "The Test Journal")
        .addConfigValue("comment", "censoredWords", ImmutableList.of("bar"))
        .build();
    testSite = theme.wrapInStubSite("testSite");

    commentCensorService = new CommentCensorServiceImpl();
    commentCensorService.siteSet = new SiteSet(ImmutableList.of(testSite));
  }

  @Test
  public void testFindCensoredWords() throws Exception {
    Collection<String> censoredWords = commentCensorService.findCensoredWords(testSite, "foo bar baz");
    Assert.assertNotNull(censoredWords);
  }

}