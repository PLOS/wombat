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

package org.ambraproject.wombat.config;

import com.google.common.collect.ImmutableSet;
import org.ambraproject.wombat.config.site.RequestMappingContext;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteResolver;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.util.ClientEndpoint;
import org.apache.commons.io.Charsets;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.cas.web.CasAuthenticationEntryPoint;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.cas.web.authentication.ServiceAuthenticationDetails;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private static final Logger log = LoggerFactory.getLogger(SpringSecurityConfiguration.class);

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private SiteResolver siteResolver;

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
  private static final String CAS_VALIDATION_URI = "/j_spring_cas_security_check";
  private static final String CAS_LOGOUT_URI = "/j_spring_cas_security_logout";
  private static final String CAS_AUTH_KEY = "casAuthProviderKey";
  private static final String LOGOUT_HANDLER_NAME = "userLogout"; // corresponds to @RequestHandler annotation name attribute
  private static final String USER_AUTH_INTERCEPT_PATTERN = "/**/user/secure/**";
  private static final String NEW_COMMENT_AUTH_INTERCEPT_PATTERN = "/**/article/comments/new**";
  private static final String FLAG_COMMENT_AUTH_INTERCEPT_PATTERN = "/**/article/comments/flag**";
  private static final ImmutableSet<String> CACHED_RESOURCE_HANDLERS = new ImmutableSet.Builder<String>()
      .add("staticResource")
      .add("repoObject")
      .add("versionedRepoObject")
      .add("repoObjectUsingPublicUrl")
      .add("figureImage")
      .build();

  private ServiceProperties serviceProperties() {
    ServiceProperties serviceProperties = new ServiceProperties();
    serviceProperties.setService(CAS_VALIDATION_URI);
    serviceProperties.setSendRenew(false);
    serviceProperties.setAuthenticateAllArtifacts(true);
    return serviceProperties;
  }

  public static class CasConfigurationRequiredException extends RuntimeException {
    private CasConfigurationRequiredException() {
      super("A bean was used that requires CAS configuration");
    }
  }

  private Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
    return runtimeConfiguration.getCasConfiguration()
        .map(casConfiguration -> new Cas20ServiceTicketValidator(casConfiguration.getCasUrl()))
        .orElseThrow(CasConfigurationRequiredException::new);
  }

  private AuthenticationUserDetailsService authenticationUserDetailsService() {
    return new AbstractCasAssertionUserDetailsService() {
      @Override
      protected UserDetails loadUserDetails(Assertion assertion) {
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        return new User(assertion.getPrincipal().getName(), "NO_PASSWORD", true, true, true, true, grantedAuthorities);
      }
    };
  }

  private CasAuthenticationProvider casAuthenticationProvider() {
    CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
    casAuthenticationProvider.setAuthenticationUserDetailsService(authenticationUserDetailsService());
    casAuthenticationProvider.setServiceProperties(serviceProperties());
    casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
    casAuthenticationProvider.setKey(CAS_AUTH_KEY);
    return casAuthenticationProvider;
  }

  private CasAuthenticationFilter casAuthenticationFilter() throws Exception {
    CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
    casAuthenticationFilter.setAuthenticationManager(authenticationManager());
    casAuthenticationFilter.setServiceProperties(serviceProperties());
    casAuthenticationFilter.setAuthenticationDetailsSource(dynamicServiceResolver());
    casAuthenticationFilter.setFilterProcessesUrl(CAS_VALIDATION_URI);
    return casAuthenticationFilter;
  }

  private AuthenticationDetailsSource<HttpServletRequest, ServiceAuthenticationDetails> dynamicServiceResolver() {
    return (HttpServletRequest request) -> {
      String url = getCasValidationPath(request);
      return (ServiceAuthenticationDetails) () -> url;
    };
  }

  private SingleSignOutFilter singleSignOutFilter() {
    // This filter handles a Single Logout Request from the CAS Server
    return new SingleSignOutFilter();
  }

  private LogoutFilter requestLogoutFilter() {
    // This filter redirects to the CAS Server to signal Single Logout should be performed
    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    logoutHandler.setClearAuthentication(true);
    logoutHandler.setInvalidateHttpSession(true);
    LogoutFilter logoutFilter = new LogoutFilter(getLogoutSuccessHandler(), logoutHandler);
    logoutFilter.setFilterProcessesUrl(CAS_LOGOUT_URI);
    return logoutFilter;
  }

  private LogoutSuccessHandler getLogoutSuccessHandler() {
    return runtimeConfiguration.getCasConfiguration().map(casConfiguration ->
        (LogoutSuccessHandler) (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
          if (authentication != null && authentication.getDetails() != null) {
            try {
              request.getSession().invalidate();
            } catch (IllegalStateException e) {
              // session is already invalid, so nothing to do, but log as error since it may indicate a config issue
              log.error("Attempted to log out of an already logged out session");
            }
          }

          validateHostname(request);
          String logoutServiceUrl = Link.toSitelessHandler()
              .toPattern(requestMappingContextDictionary, LOGOUT_HANDLER_NAME).build()
              .get(request);

          response.setStatus(HttpServletResponse.SC_OK);
          response.sendRedirect(casConfiguration.getLogoutUrl()
              + "?service=" + URLEncoder.encode(logoutServiceUrl, Charsets.UTF_8.name()));
        }).orElseThrow(CasConfigurationRequiredException::new);
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    // Allow internal or external resource requests bypass spring security, and thereby avoid the acquisition
    // of default cache control headers which would prevent client-side caching.
    web.ignoring().requestMatchers((RequestMatcher) request ->
        CACHED_RESOURCE_HANDLERS.stream()
            .map(handlerName -> requestMappingContextDictionary.getPattern(handlerName, siteResolver.resolveSite(request)))
            .filter(Objects::nonNull)
            .map(RequestMappingContext::getPattern)
            .anyMatch(handlerPattern -> ANT_PATH_MATCHER.match(handlerPattern, request.getServletPath()))
    );
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if (runtimeConfiguration.getCasConfiguration().isPresent()) {
      http.addFilter(casAuthenticationFilter())
          .addFilterBefore(requestLogoutFilter(), LogoutFilter.class)
          .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
          .authorizeRequests().antMatchers(USER_AUTH_INTERCEPT_PATTERN).fullyAuthenticated();
      http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint());
      http.csrf().disable();
    }
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    if (runtimeConfiguration.getCasConfiguration().isPresent()) {
      auth.authenticationProvider(casAuthenticationProvider());
    }
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  private CasAuthenticationEntryPoint casAuthenticationEntryPoint() {
    return runtimeConfiguration.getCasConfiguration().map(casConfiguration -> {
      CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint() {
        @Override
        protected String createServiceUrl(final HttpServletRequest request, final HttpServletResponse response) {
          return getCasValidationPath(request);
        }
      };
      casAuthenticationEntryPoint.setLoginUrl(casConfiguration.getLoginUrl());
      casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
      return casAuthenticationEntryPoint;
    }).orElseThrow(CasConfigurationRequiredException::new);
  }

  private String getCasValidationPath(HttpServletRequest request) {
    validateHostname(request);
    return Link.toSitelessHandler().toPath(CAS_VALIDATION_URI).get(request);
  }

  private void validateHostname(HttpServletRequest request) {
    ClientEndpoint clientEndpoint = ClientEndpoint.get(request);
    Set<String> hostNames = siteSet.getSites().stream()
        .map((Site site) -> site.getRequestScheme().getHostName())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
    if (!hostNames.isEmpty() && !hostNames.contains(clientEndpoint.getHostname())) {
      throw new AccessDeniedException(String.format("Attempt to validate against foreign hostname %s. " +
          "Possible hijack attempt.", clientEndpoint.getHostname()));
    }
  }

}
