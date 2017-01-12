/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap.internal;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import io.nuun.kernel.api.plugin.InitState;
import io.nuun.kernel.api.plugin.context.InitContext;
import org.seedstack.ldap.LdapConfig;
import org.seedstack.seed.SeedException;
import org.seedstack.seed.core.internal.AbstractSeedPlugin;
import org.seedstack.seed.security.SecurityConfig;

public class LdapPlugin extends AbstractSeedPlugin {
    private LDAPConnectionPool ldapConnectionPool;
    private boolean startPlugin;

    @Override
    public String name() {
        return "ldap";
    }

    @Override
    public InitState initialize(InitContext initContext) {

        startPlugin = getConfiguration(SecurityConfig.class).getRealm(LdapRealm.class.getSimpleName()).isPresent();
        if (startPlugin) {
            LdapConfig ldapConfig = getConfiguration(LdapConfig.class);

            LDAPConnection connection;
            try {
                connection = new LDAPConnection(
                        ldapConfig.getHost(),
                        ldapConfig.getPort(),
                        ldapConfig.getBindDN(),
                        ldapConfig.getBindPassword()
                );
                ldapConnectionPool = new LDAPConnectionPool(connection, ldapConfig.getConnectionNumber());
            } catch (LDAPException e) {
                SeedException seedException;
                switch (e.getResultCode().intValue()) {
                    case ResultCode.NO_SUCH_OBJECT_INT_VALUE:
                        seedException = SeedException.wrap(e, LdapErrorCode.UNKNOWN_BIND_DN);
                        break;
                    case ResultCode.INVALID_CREDENTIALS_INT_VALUE:
                        seedException = SeedException.wrap(e, LdapErrorCode.INVALID_CREDENTIALS);
                        break;
                    case ResultCode.CONNECT_ERROR_INT_VALUE:
                        seedException = SeedException.wrap(e, LdapErrorCode.CONNECT_ERROR);
                        break;
                    default:
                        seedException = SeedException.wrap(e, LdapErrorCode.LDAP_ERROR);
                        break;
                }
                seedException.put("host", ldapConfig.getHost())
                        .put("port", ldapConfig.getPort())
                        .put("dn", ldapConfig.getBindDN());
                throw seedException;
            }
        }
        return InitState.INITIALIZED;
    }

    @Override
    public Object nativeUnitModule() {
        if (startPlugin) {
            return new LdapModule(ldapConnectionPool);
        }
        return null;
    }

    @Override
    public void stop() {
        if (ldapConnectionPool != null) {
            ldapConnectionPool.close();
        }
    }
}
