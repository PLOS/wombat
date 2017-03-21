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

package org.ambraproject.wombat.util;


import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class CacheKeyTest {

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

    int keyLen = CacheKey.HASH_ALGORITHM.bits() / Byte.SIZE;

    String hash1 = CacheKey.createKeyHash(ImmutableList.of(exampleUrl1));
    String hash2 = CacheKey.createKeyHash(ImmutableList.of(exampleUrl2));

    Assert.assertFalse(hash1.contentEquals(exampleUrl1), "Hash key generation failure");
    Assert.assertTrue(hash1.contentEquals(CacheKey.createKeyHash(ImmutableList.of(exampleUrl1))), "Non-deterministic hash key");
    Assert.assertFalse(hash1.contentEquals(hash2), "Hash keys are not unique");
    byte[] hash1Bytes;
    try {
      hash1Bytes = CacheKey.HASH_BASE.decode(hash1);
    } catch (IllegalArgumentException e) {
      Assert.fail("Hash keys are not in base-encoded format");
      throw e; // satisfy the compiler
    }
    Assert.assertTrue(hash1Bytes.length == keyLen, "Incorrect hash length");

    Map<Object, String> expectedHashes = new HashMap<>();
    expectedHashes.put(Hashing.md5(), "25FF3F5D3A44A1C19DD9F54773C4D32D");
    expectedHashes.put(Hashing.sha1(), "4E435D1AB8B9F08B36D1F4A76F0628644452A339");
    expectedHashes.put(Hashing.sha256(), "63B957877D9909518BAA5FB4F6DDC200AB494E0819887AD96CB6A98B2CADA3FA");
    expectedHashes.put(Hashing.sha512(), "B0D827FF43389ECC322022EBB2A447C045313804CECD324560CD9727285A3AAD87A822C23" +
            "F763BAB884DA75F91A0768DAD30D0E6F5E5AA2361A0B3F5AEF757B7");
    if (expectedHashes.keySet().contains(CacheKey.HASH_ALGORITHM)) {
      byte[] expectedHash = BaseEncoding.base16().decode(expectedHashes.get(CacheKey.HASH_ALGORITHM));
      Assert.assertEquals(hash1Bytes, expectedHash, "Incorrect hash");
    }
  }
}
