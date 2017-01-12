/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ldap;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.ldap.LdapService;
import org.seedstack.ldap.LdapUserContext;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.seed.it.WithPlugins;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.WithUser;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
@WithPlugins(LdapITPlugin.class)
public class LdapRealmIT {
    @Inject
    private SecurityManager securityManager;
    @Inject
    private SecuritySupport securitySupport;
    @Inject
    private LdapService ldapService;

    @Test
    @WithUser(id = "jdoe", password = "password")
    public void completeTest() {
        assertThat(securitySupport.hasRole("jedi")).isTrue();
        LdapUserContext userContext = securitySupport.getPrincipalsByType(LdapUserContext.class).iterator().next().getPrincipal();
        assertThat(ldapService.getAttributeValue(userContext, "sn")).isEqualTo("jdoe");
        assertThat(ldapService.getAttributeValue(userContext, "dummy")).isNull();
    }

    @Test(expected = IncorrectCredentialsException.class)
    public void wrongPasswordTest() {
        ThreadContext.bind(securityManager);
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("jdoe", "dummy");
        subject.login(token);
    }

    @Test(expected = AuthenticationException.class)
    public void unknownUserTest() {
        ThreadContext.bind(securityManager);
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("dummy", "password");
        subject.login(token);
    }
}
