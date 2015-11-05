package org.ambraproject.wombat.config;

import com.google.common.base.Optional;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
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

  private static String CAS_VALIDATION_URI = "/j_spring_cas_security_check";
  private static String CAS_LOGOUT_URI = "/j_spring_cas_security_logout";
  private static String CAS_AUTH_KEY = "casAuthProviderKey";
  private static String LOGOUT_HANDLER_NAME = "userLogout"; // corresponds to @RequestHandler annotation name attribute
  private static String AUTH_INTERCEPT_PATTERN = "/**/user/secure/**";

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
      final String url = createUrlFromRequest(request, CAS_VALIDATION_URI);
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

      // logout from any CAS server sessions using Single Logout and then return to our logout handler
      String logoutPath;
      try {
        logoutPath = requestMappingContextDictionary.getPattern(LOGOUT_HANDLER_NAME, null).getPattern();
      } catch (NullPointerException npe) {
        log.error("Expected to find a siteless logout controller with the name \"{}\". ", LOGOUT_HANDLER_NAME);
        httpServletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
        return;
      }
      httpServletResponse.setStatus(HttpServletResponse.SC_OK);
      String logoutServiceUrl = createUrlFromRequest(httpServletRequest, logoutPath);
      httpServletResponse.sendRedirect(runtimeConfiguration.getCasConfiguration().getLoginUrl().
              concat("?service=").concat(URLEncoder.encode(logoutServiceUrl, Charsets.UTF_8.name())));
    };
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.addFilter(casAuthenticationFilter())
            .addFilterBefore(requestLogoutFilter(), LogoutFilter.class)
            .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class)
            .authorizeRequests().antMatchers(AUTH_INTERCEPT_PATTERN).fullyAuthenticated();
    http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint());
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
        return createUrlFromRequest(request, CAS_VALIDATION_URI);
      }
    };
    casAuthenticationEntryPoint.setLoginUrl(runtimeConfiguration.getCasConfiguration().getLoginUrl());
    casAuthenticationEntryPoint.setServiceProperties(serviceProperties());
    return casAuthenticationEntryPoint;
  }

  private String createUrlFromRequest(HttpServletRequest request, String path){
    ClientEndpoint clientEndpoint = ClientEndpoint.get(request);
    if (!hasValidHostname(clientEndpoint)) {
      throw new AccessDeniedException(String.format("Attempt to validate against foreign hostname %s. " +
              "Possible hijack attempt.", clientEndpoint.getHostname()));
    }
    StringBuilder sb = new StringBuilder(request.getScheme()).append("://");
    sb.append(clientEndpoint.getHostname());
    sb.append((clientEndpoint.getPort().isPresent() ? (":" + clientEndpoint.getPort().get()) : ""));
    sb.append(request.getContextPath());
    sb.append(path.startsWith("/") ? path : sb.append("/").append(path));
    return sb.toString();
  }

  private boolean hasValidHostname(ClientEndpoint ce) {
    return siteSet.getSites().stream()
            .map((Site site) -> site.getRequestScheme().getHostName())
            .filter(Optional::isPresent)
            .anyMatch((Optional<String> hostName) -> hostName.get().equals(ce.getHostname()));
  }

}
