package org.ambraproject.wombat.util;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CacheParamsTest {

  @Test
  public void testGetCacheKey(){
    String exampleUrl1 = "articles?journal=PLoSPathogens&min=3&since=Thu%2C+30+Oct+2014+00%3A55%3A34+GMT&type=" +
     "http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FResearch%2520Article&exclude=http%3A%2F%2Frdf.plos.org%2" +
     "FRDF%2FarticleType%2FCorrection&exclude=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FRetraction&exclu" +
     "de=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FExpression%2520of%2520Concern";
    String exampleUrl2 = "articles?journal=PLoSMedicine&min=3&since=Thu%2C+30+Oct+2014+00%3A46%3A37+GMT&type=h" +
     "ttp%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FEditorial&type=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleT" +
     "ype%2FResearch%2520Article&type=*&exclude=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FCorrection&exc" +
     "lude=http%3A%2F%2Frdf.plos.org%2FRDF%2FarticleType%2FRetraction&exclude=http%3A%2F%2Frdf.plos.org%2FRDF%" +
     "2FarticleType%2FExpression%2520of%2520Concern";

    Map<String, Integer> keyLenMap = new HashMap<>();
    keyLenMap.put("SHA-256", 64); // 256 bits = 64 hex chars
    keyLenMap.put("SHA-1", 40); // 160 bits = 40 hex chars
    keyLenMap.put("MD5", 32); // 128 bits = 32 hex chars;

    String hash1 = CacheParams.createKeyHash(exampleUrl1);
    String hash2 = CacheParams.createKeyHash(exampleUrl2);

    Assert.assertFalse(hash1.contentEquals(exampleUrl1), "Hash key generation failure");
    Assert.assertFalse(hash1.contentEquals(CacheParams.createKeyHash(exampleUrl1)), "Non-deterministic hash key");
    Assert.assertFalse(hash1.contentEquals(hash2), "Hash keys are not unique");
    Assert.assertTrue(keyLenMap.containsKey(CacheParams.HASH_ALGORITHM), "Invalid hashing algorithm");
    Assert.assertTrue(Pattern.matches("^[0-9a-f]+$", hash1), "Hash keys are not hex string format");
    Assert.assertTrue(hash1.length() == keyLenMap.get(CacheParams.HASH_ALGORITHM), "Incorrect hash length");
  }
}
