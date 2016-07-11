package org.ambraproject.wombat.util;


import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

public class KeyHashingTest {

  private static final String exampleUrl1 = "articles?journal=PLoSPathogens&min=3&since=Thu%2C+30+Oct+2014+00%3A55%3A34+GMT&type=" +
      "http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FResearch%2520Article&exclude=http%3A%2F%2Frdf.plos.org%2" +
      "FRDF%2FarticleType%2FCorrection&exclude=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FRetraction&exclu" +
      "de=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FExpression%2520of%2520Concern";
  private static final String exampleUrl2 = "articles?journal=PLoSMedicine&min=3&since=Thu%2C+30+Oct+2014+00%3A46%3A37+GMT&type=h" +
      "ttp%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FEditorial&type=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleT" +
      "ype%2FResearch%2520Article&type=*&exclude=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FCorrection&exc" +
      "lude=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FRetraction&exclude=http%3A%2F%2Frdf.plos.org%2FRDF%" +
      "2FarticleType%2FExpression%2520of%2520Concern";

  @Test
  public void testCreateKeyHash() {

    int keyLen = KeyHashing.HASH_ALGORITHM.bits() / Byte.SIZE;

    String hash1 = KeyHashing.createKeyHash(ImmutableList.of(exampleUrl1));
    String hash2 = KeyHashing.createKeyHash(ImmutableList.of(exampleUrl2));

    Assert.assertFalse(hash1.contentEquals(exampleUrl1), "Hash key generation failure");
    Assert.assertTrue(hash1.contentEquals(KeyHashing.createKeyHash(ImmutableList.of(exampleUrl1))), "Non-deterministic hash key");
    Assert.assertFalse(hash1.contentEquals(hash2), "Hash keys are not unique");
    byte[] hash1Bytes;
    try {
      hash1Bytes = KeyHashing.HASH_BASE.decode(hash1);
    } catch (IllegalArgumentException e) {
      Assert.fail("Hash keys are not in base-encoded format");
      throw e; // satisfy the compiler
    }
    Assert.assertTrue(hash1Bytes.length == keyLen, "Incorrect hash length");
  }


  @DataProvider
  public Object[][] distinctHashes() {
    return new String[][][]{
        {{}, {""}},
        {{""}, {}},
        {{""}, {"", ""}},
        {{"ab", "c"}, {"a", "bc"}},
        {{"ab", "c"}, {"a", "b", "c"}},
        {{"a", "b"}, {"a", "", "b"}},
        {{"", ""}, {"\0"}},
    };
  }

  @Test(dataProvider = "distinctHashes")
  public void testDistinctHashes(String[] key1, String[] key2) {
    String hash1 = KeyHashing.createKeyHash(Arrays.asList(key1));
    String hash2 = KeyHashing.createKeyHash(Arrays.asList(key2));
    Assert.assertFalse(hash1.equals(hash2));
  }


  @DataProvider
  public Object[][] hashValues() {
    return new Object[][]{
        {new String[]{}, "3I42H3S6NNFQ2MSVX7XZKYAYSCX5QBYJ"},
        {new String[]{""}, "SBU4U6HHIUFCQULTIMNT4UWFYJJJTZDT"},
        {new String[]{"", ""}, "AX7EAV2TCZXREVKZ47E2YVMGKTYQPR7J"},
        {new String[]{"_"}, "DESMC2JEA55IYZLX3JHLE6ZCJTBEG352"},
        {new String[]{"ab", "c"}, "4SX2ZGOPYBKMHU5QOJDUW2SJHOXA4FR5"},
        {new String[]{"a", "bc"}, "OI7M3EQICN5LRX2XJG6IPOJKS6I4SLXX"},
        {new String[]{"\0"}, "YWQKEUU5FXVWB7WAIG2PXVZCULV6GFYC"},
        {new String[]{"\u2603"}, "4W3A5DFF7FMURSJDEN3UEI7S3YYMHJU5"},
        {new String[]{exampleUrl1}, "Z4QEFQCKOS37FU3FT7N3XZ7CLSUBJDQV"},
        {new String[]{exampleUrl2}, "R64AZGNQIKK5OVSB73TQFOCT43R2YZ6I"},
        {new String[]{exampleUrl1, exampleUrl2}, "ME63UK4VELOGF3OUIK6I7T6H7GSDIS3M"},
        {new String[]{exampleUrl2, exampleUrl1}, "N4VMEJINUOLL44GTPS3RHSJHDESY5OCD"},
    };
  }

  @Test(dataProvider = "hashValues")
  public void testHashValues(String[] testValue, String expectedHash) {
    // If any of these assertions fail, update them together with the data provider
    Assert.assertEquals(KeyHashing.HASH_ALGORITHM, Hashing.sha1());
    Assert.assertEquals(KeyHashing.HASH_BASE, BaseEncoding.base32());
    Assert.assertEquals(KeyHashing.HASH_CHARSET, Charsets.UTF_8);

    String actualHash = KeyHashing.createKeyHash(Arrays.asList(testValue));
    Assert.assertEquals(actualHash, expectedHash);
  }

}
