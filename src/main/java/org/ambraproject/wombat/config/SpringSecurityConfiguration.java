package org.ambraproject.wombat.config;

import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private static final Logger log = LoggerFactory.getLogger(SpringSecurityConfiguration.class);

  @Autowired
  private SiteSet siteSet;

  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;

  @Autowired
  private RuntimeConfiguration runtimeConfiguration;

  private static final String CAS_VALIDATION_URI = "/j_spring_cas_security_check";
  private static final String CAS_LOGOUT_URI = "/j_spring_cas_security_logout";
  private static final String CAS_AUTH_KEY = "casAuthProviderKey";
  private static final String LOGOUT_HANDLER_NAME = "userLogout"; // corresponds to @RequestHandler annotation name attribute
  private static final String AUTH_INTERCEPT_PATTERN = "/**/user/secure/**";

  @Bean
  public ServiceProperties serviceProperties() {
    ServiceProperties serviceProperties = new ServiceProperties();
    serviceProperties.setService(CAS_VALIDATION_URI);
    serviceProperties.setSendRenew(false);
    serviceProperties.setAuthenticateAllArtifacts(true);
    return serviceProperties;
  }

  @Bean
  public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
    return new Cas20ServiceTicketValidator(runtimeConfiguration.getCasConfiguration().getCasUrl());
  }

  @Bean
  public AuthenticationUserDetailsService authenticationUserDetailsService() {
    return new AbstractCasAssertionUserDetailsService(){
      @Override
      protected UserDetails loadUserDetails(Assertion assertion) {
        final List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        return new User(assertion.getPrincipal().getName(), "NO_PASSWORD", true, true, true, true, grantedAuthorities);
      }
    };
  }

  @Bean
  public CasAuthenticationProvider casAuthenticationProvider() {
    CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
    casAuthenticationProvider.setAuthenticationUserDetailsService(authenticationUserDetailsService());
    casAuthenticationProvider.setServiceProperties(serviceProperties());
    casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
    casAuthenticationProvider.setKey(CAS_AUTH_KEY);
    return casAuthenticationProvider;
  }

  @Bean
  public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
    CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
    casAuthenticationFilter.setAuthenticationManager(authenticationManager());
    casAuthenticationFilter.setServiceProperties(serviceProperties());
    casAuthenticationFilter.setAuthenticationDetailsSource(dynamicServiceResolver());
    casAuthenticationFilter.setFilterProcessesUrl(CAS_VALIDATION_URI);
    return casAuthenticationFilter;
  }

  @Bean
  AuthenticationDetailsSource<HttpServletRequest,
          ServiceAuthenticationDetails> dynamicServiceResolver() {
    return request -> {
      String url = getCasValidationPath(request);
      return (ServiceAuthenticationDetails) () -> url;
    };
  }

  @Bean
  public SingleSignOutFilter singleSignOutFilter() {
    // This filter handles a Single Logout Request from the CAS Server
    return new SingleSignOutFilter();
  }

  @Bean
  public LogoutFilter requestLogoutFilter() {
    // This filter redirects to the CAS Server to signal Single Logout should be performed
    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    logoutHandler.setClearAuthentication(true);
    logoutHandler.setInvalidateHttpSession(true);
    LogoutFilter logoutFilter = new LogoutFilter(getLogoutSuccessHandler(), logoutHandler);
    logoutFilter.setFilterProcessesUrl(CAS_LOGOUT_URI);
    return logoutFilter;
  }

  @Bean
  public LogoutSuccessHandler getLogoutSuccessHandler() {
    return (httpServletRequest, httpServletResponse, authentication) -> {
      if (authentication != null && authentication.getDetails() != null){
        try {
          httpServletRequest.getSession().invalidate();
        } catch (IllegalStateException e) {
          // session is already invalid, so nothing to do, but log as error since it may indicate a config issue
          log.error("Attempted to log out of an already logged out session");
        }
      }

      validateHostname(httpServletRequest);
      String logoutServiceUrl = Link.toSitelessHandler()
          .toPattern(requestMappingContextDictionary, LOGOUT_HANDLER_NAME).build()
          .get(httpServletRequest);

      httpServletResponse.setStatus(HttpServletResponse.SC_OK);
      httpServletResponse.sendRedirect(runtimeConfiguration.getCasConfiguration().getLogoutUrl()
          + "?service=" + URLEncoder.encode(logoutServiceUrl, Charsets.UTF_8.name()));
    };
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.addFilter(casAuthenticationFilter())
            .addFilterBefore(requestLogoutFilter(), LogoutFilter.class)
            .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
            .authorizeRequests().antMatchers(AUTH_INTERCEPT_PATTERN).fullyAuthenticated();
    http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint());
    http.headers().cacheControl().disable();
    http.csrf().disable();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(casAuthenticationProvider());
  }

  @Bean @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Bean
  public CasAuthenticationEntryPoint casAuthenticationEntryPoint() {

    CasAuthenticationEntryPoint casAuthenticationEntryPoint = new CasAuthenticationEntryPoint() {
      @Override
      protected String createServiceUrl(final HttpServletRequest request, final HttpServletResponse response) {
        return getCasValidationPath(request);
      }
    };
    casAuthenticationEntryPoint.setLoginUrl(runtimeConfiguration.getCasConfiguration().getLoginUrl());
    casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
    return casAuthenticationEntryPoint;
  }

  private String getCasValidationPath(HttpServletRequest request) {
    validateHostname(request);
    return Link.toSitelessHandler().toPath(CAS_VALIDATION_URI).get(request);
  }

  private void validateHostname(HttpServletRequest request) {
    ClientEndpoint clientEndpoint = ClientEndpoint.get(request);
    boolean hasValidHostname = siteSet.getSites().stream()
        .map((Site site) -> site.getRequestScheme().getHostName())
        .filter(Optional::isPresent)
        .anyMatch((Optional<String> hostName) -> hostName.get().equals(clientEndpoint.getHostname()));
    if (!hasValidHostname) {
      throw new AccessDeniedException(String.format("Attempt to validate against foreign hostname %s. " +
              "Possible hijack attempt.", clientEndpoint.getHostname()));
    }
  }

}
