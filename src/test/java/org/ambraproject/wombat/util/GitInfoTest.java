package org.ambraproject.wombat.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class GitInfoTest {

  @Test
  public void testCheckGitValues() {
    GitInfo gitInfo = new GitInfo();

    Assert.assertEquals(gitInfo.getBranch(), "", "git branch value is incorrect");

    Assert.assertEquals(gitInfo.getDescribe(), "", "git describe value is incorrect");

    Assert.assertEquals(gitInfo.getBuildUserName(), "", "git build user name value is incorrect");
    Assert.assertEquals(gitInfo.getBuildUserEmail(), "", "git build user email value is incorrect");
    Assert.assertEquals(gitInfo.getBuildTime(), "05.06.2014 @ 14:42:36 PDT", "git build time value is incorrect");

    Assert.assertEquals(gitInfo.getCommitId(), "bb2bdabf1d38c720a7871befa2e98e03ade1a2c3", "git commit id value is incorrect");
    Assert.assertEquals(gitInfo.getCommitIdAbbrev(), "bb2bdab", "git commit id abbrev value is incorrect");
    Assert.assertEquals(gitInfo.getCommitUserName(), "", "git commit user name value is incorrect");
    Assert.assertEquals(gitInfo.getCommitUserEmail(), "", "git cinnut yser enauk value is incorrect");
    Assert.assertEquals(gitInfo.getCommitMessageFull(), "commit message FULL!!", "git commit message full value is incorrect");
    Assert.assertEquals(gitInfo.getCommitMessageShort(), "commit message short", "git commit message short value is incorrect");
    Assert.assertEquals(gitInfo.getCommitTime(), "05.06.2014 @ 14:37:41 PDT", "git commit time value is incorrect");
  }
}
