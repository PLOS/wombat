package org.ambraproject.wombat.service;

import org.ambraproject.wombat.util.BuildInfo;

public interface BuildInfoService {

  /**
   * @return build properties describing this application, or {@code null} if not available
   */
  public abstract BuildInfo getLocalBuildInfo();

  /**
   * @return build properties describing the service component accessed by {@link org.ambraproject.wombat.service.SoaService},
   * or {@code null} if not available
   */
  public abstract BuildInfo getServiceBuildInfo();

}
