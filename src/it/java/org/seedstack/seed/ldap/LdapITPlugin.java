/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.seed.ldap;

import com.google.common.collect.Lists;
import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPException;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import io.nuun.kernel.core.AbstractPlugin;
import org.seedstack.ldap.internal.LdapPlugin;

import java.util.Collection;

public class LdapITPlugin extends AbstractPlugin {

    private InMemoryDirectoryServer ds;

    @Override
    public String name() {
        return "ldapIT";
    }

    @Override
    public Collection<Class<?>> dependentPlugins() {
        return Lists.<Class<?>>newArrayList(LdapPlugin.class);
    }

    @Override
    public InitState init(InitContext initContext) {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=example,dc=com");
            config.addAdditionalBindCredentials("cn=admin, ou=people, dc=example,dc=com", "admin");
            config.addAdditionalBindCredentials("cn=John Doe - 123456, ou=people, dc=example,dc=com", "password");
            InMemoryListenerConfig listenerConfig = new InMemoryListenerConfig("test", null, 53800, null, null, null);
            config.setListenerConfigs(listenerConfig);
            ds = new InMemoryDirectoryServer(config);
            ds.importFromLDIF(true, "src/it/resources/data.ldif");
            ds.startListening();
        } catch (LDAPException e) {
            throw new RuntimeException(e);
        }
        return InitState.INITIALIZED;
    }

    @Override
    public void stop() {
        if (ds != null)
            ds.shutDown(true);
    }

}
