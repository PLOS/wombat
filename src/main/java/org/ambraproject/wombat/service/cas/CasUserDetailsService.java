package org.ambraproject.wombat.service.cas;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

public class CasUserDetailsService extends AbstractCasAssertionUserDetailsService {

  private static final String NON_EXISTENT_PASSWORD_VALUE = "NO_PASSWORD";

  @Override
  protected UserDetails loadUserDetails(Assertion assertion) {
    final List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
    return new User(assertion.getPrincipal().getName(), NON_EXISTENT_PASSWORD_VALUE, true, true, true, true, grantedAuthorities);
  }
}
