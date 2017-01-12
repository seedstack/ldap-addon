/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap.internal;

import org.seedstack.ldap.LdapUserContext;

import java.util.HashMap;
import java.util.Map;

class DefaultLdapUserContext implements LdapUserContext {
    private final String dn;
    private final Map<String, String> knownAttributes = new HashMap<>();

    DefaultLdapUserContext(String dn) {
        this.dn = dn;
    }

    @Override
    public String getDn() {
        return dn;
    }

    Map<String, String> getKnownAttributes() {
        return knownAttributes;
    }
}
