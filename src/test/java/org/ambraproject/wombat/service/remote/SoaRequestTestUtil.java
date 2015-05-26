package org.ambraproject.wombat.service.remote;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.primitives.Booleans;
import org.apache.http.NameValuePair;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class SoaRequestTestUtil {
  private SoaRequestTestUtil() {
    throw new AssertionError();
  }

  /**
   * Builds a matcher for an {@link SoaRequest}.
   * <p/>
   * To ignore the path and check only parameters, pass {@link Predicates#alwaysTrue()} as the value of {@code
   * pathPredicate}.
   * <p/>
   * To ignore the parameters and check only the path, pass an empty array as the value of {@code parameterPredicates}.
   * (If {@code parameterPredicates} is non-empty, all requests with zero parameters will be rejected.)
   *
   * @param pathPredicate       matches if this is true for the path
   * @param parameterPredicates matches if each of these is true for at least one predicate
   * @return the matcher
   */
  public static Matcher<SoaRequest> buildMatcher(final Predicate<? super String> pathPredicate,
                                                 final Predicate<? super NameValuePair>... parameterPredicates) {
    Preconditions.checkNotNull(pathPredicate);
    for (Predicate<? super NameValuePair> parameterPredicate : parameterPredicates) {
      Preconditions.checkNotNull(parameterPredicate);
    }

    return new BaseMatcher<SoaRequest>() {
      @Override
      public boolean matches(Object item) {
        SoaRequest soaRequest = (SoaRequest) item;
        if (!pathPredicate.apply(soaRequest.getPath())) return false;

        boolean[] parameterResults = new boolean[parameterPredicates.length];
        for (NameValuePair param : soaRequest.getParams()) {
          for (int i = 0; i < parameterPredicates.length; i++) {
            if (parameterPredicates[i].apply(param)) {
              parameterResults[i] = true;
            }
          }
        }
        return !Booleans.asList(parameterResults).contains(false);
      }

      @Override
      public void describeTo(Description description) {
      }
    };
  }

}
