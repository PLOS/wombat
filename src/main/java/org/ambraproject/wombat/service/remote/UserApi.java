package org.ambraproject.wombat.service.remote;

import java.io.IOException;

public interface UserApi extends RestfulJsonApi {

  /**
   * Given the authentication ID, get the user ID using the individuals/CAS/{authId} API.
   *
   * @param authId The authentication API.
   * @return the user ID
   * @throws IOException
   */
  public abstract String getUserIdFromAuthId(String authId) throws IOException;

  public static class UserApiException extends RuntimeException {
    UserApiException(String message) {
      super(message);
    }

    UserApiException(String message, Throwable cause) {
      super(message, cause);
    }

    UserApiException(Throwable cause) {
      super(cause);
    }
  }

}
