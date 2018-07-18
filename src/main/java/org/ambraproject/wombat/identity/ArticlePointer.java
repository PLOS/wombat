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

package org.ambraproject.wombat.identity;

import com.google.common.collect.ImmutableMap;
import org.ambraproject.wombat.controller.DoiVersionArgumentResolver;
import org.ambraproject.wombat.service.remote.ApiAddress;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * A pointer to a particular article ingestion as provided by {@link org.ambraproject.wombat.service.remote.ArticleApi}.
 * To ensure that the DOI is in its canonical form (with regard to URI prefixes and case-sensitivity), the object must
 * be constructed from a string provided by the API, not an end user.
 */
public class ArticlePointer {

  /**
   * An object describing the original request from the client.
   */
  private final RequestedDoiVersion originalRequest;

  /**
   * The DOI in its canonical form. It should be a structurally equal DOI to {@code originalRequest.getDoi()}, but is
   * not necessarily equal character-for-character, due to case insensitivity and URI prefixes.
   */
  private final String doi;

  /**
   * The back-end identifier for the version to serve. Always present, regardless of whether the user requested it or
   * not.
   */
  private final int ingestionNumber;

  /**
   * The revision number that was used to find the {@link #ingestionNumber}. This should be empty <em>only</em> if the
   * user requested an ingestion number directly (probably because they were QC'ing it).
   * <p>
   * If the user requested the latest revision by default, this must be populated with that number (as looked up from
   * our back end). Contrast to {@link RequestedDoiVersion}, where the revision number would be empty if the user
   * defaulted to the latest revision.
   */
  private final OptionalInt revisionNumber;

  public ArticlePointer(RequestedDoiVersion originalRequest, String doi,
                        int ingestionNumber, OptionalInt revisionNumber) {
    this.originalRequest = Objects.requireNonNull(originalRequest);
    this.doi = Objects.requireNonNull(doi);
    this.ingestionNumber = ingestionNumber;
    this.revisionNumber = Objects.requireNonNull(revisionNumber);
  }

  public String getDoi() {
    return doi;
  }

  public int getIngestionNumber() {
    return ingestionNumber;
  }

  public OptionalInt getRevisionNumber() {
    return revisionNumber;
  }

  public boolean isOriginalRequestVersioned() {
    return originalRequest.getRevisionNumber().isPresent() || originalRequest.getIngestionNumber().isPresent();
  }

  /**
   * @return the URL parameter that should be used in outgoing links to DOIs of the same version as this article
   */
  public ImmutableMap<String, String> getVersionParameter() {
    return !isOriginalRequestVersioned() ? ImmutableMap.of()
        : revisionNumber.isPresent() ? ImmutableMap.of(DoiVersionArgumentResolver.REVISION_PARAMETER, Integer.toString(revisionNumber.getAsInt()))
        : ImmutableMap.of(DoiVersionArgumentResolver.INGESTION_PARAMETER, Integer.toString(ingestionNumber));
  }

  /**
   * @return the URL parameters that should be used in outgoing links to the same version and DOI of this article
   */
  public ImmutableMap<String, String> asParameterMap() {
    return ImmutableMap.<String, String>builder()
        .put(DoiVersionArgumentResolver.ID_PARAMETER, doi)
        .putAll(getVersionParameter())
        .build();
  }

  public ApiAddress.Builder asApiAddress() {
    return ApiAddress.builder("articles").embedDoi(doi)
        .addToken("ingestions").addToken(Integer.toString(ingestionNumber));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ArticlePointer that = (ArticlePointer) o;
    if (ingestionNumber != that.ingestionNumber) return false;
    if (!originalRequest.equals(that.originalRequest)) return false;
    if (!doi.equals(that.doi)) return false;
    return revisionNumber.equals(that.revisionNumber);
  }

  @Override
  public int hashCode() {
    int result = originalRequest.hashCode();
    result = 31 * result + doi.hashCode();
    result = 31 * result + ingestionNumber;
    result = 31 * result + revisionNumber.hashCode();
    return result;
  }
}
