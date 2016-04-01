package org.ambraproject.wombat.service.remote;

public interface UserApi extends RestfulJsonApi {

  public static class UserApiException extends RuntimeException {
    UserApiException(Exception cause) {
      super(cause);
    }
  }

}
