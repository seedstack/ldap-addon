/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap.internal;

import com.google.common.base.Strings;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;
import com.unboundid.ldap.sdk.SearchScope;
import org.seedstack.ldap.LdapConfig;
import org.seedstack.ldap.LdapService;
import org.seedstack.ldap.LdapUserContext;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.SeedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class DefaultLdapService implements LdapService {
    private final static Logger LOGGER = LoggerFactory.getLogger(DefaultLdapService.class);
    @Configuration
    private LdapConfig.UserConfig userConfig;
    @Configuration
    private LdapConfig.GroupConfig groupConfig;
    @Inject
    private LDAPConnectionPool ldapConnectionPool;

    @Override
    public LdapUserContext createUserContext(String dn) {
        return internalCreateUser(dn);
    }

    private DefaultLdapUserContext internalCreateUser(String dn) {
        return new DefaultLdapUserContext(dn);
    }

    @Override
    public LdapUserContext findUser(String identityAttributeValue) {
        try {
            Filter userClassFilter;
            if (Strings.isNullOrEmpty(userConfig.getObjectClass())) {
                userClassFilter = Filter.createPresenceFilter("objectclass");
            } else {
                userClassFilter = Filter.createEqualityFilter("objectclass", userConfig.getObjectClass());
            }

            Filter filter = Filter.createANDFilter(userClassFilter, Filter.createEqualityFilter(userConfig.getIdAttribute(), identityAttributeValue));
            LOGGER.debug(filter.toString());

            List<String> attributesToRetrieve = new ArrayList<>(userConfig.getAdditionalAttributes());
            if (!attributesToRetrieve.contains("cn") || !attributesToRetrieve.contains("CN")) {
                attributesToRetrieve.add("cn");
            }

            SearchResult searchResult = ldapConnectionPool.search(userConfig.getBaseDN(), SearchScope.SUB, filter, attributesToRetrieve.toArray(new String[attributesToRetrieve.size()]));
            if (searchResult.getEntryCount() != 1) {
                throw SeedException.createNew(LdapErrorCode.UNKNOWN_USER)
                        .put("user", identityAttributeValue);
            }
            SearchResultEntry searchResultEntry = searchResult.getSearchEntries().get(0);
            String dn = searchResultEntry.getDN();

            DefaultLdapUserContext ldapUserContext = internalCreateUser(dn);
            ldapUserContext.getKnownAttributes().put("cn", searchResultEntry.getAttributeValue("cn"));
            return ldapUserContext;
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw SeedException.wrap(e, LdapErrorCode.LDAP_ERROR);
        }
    }

    @Override
    public void authenticate(LdapUserContext userContext, String password) {
        try {
            ldapConnectionPool.bindAndRevertAuthentication(userContext.getDn(), password);
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw SeedException.wrap(e, LdapErrorCode.LDAP_ERROR);
        }
    }

    @Override
    public String getAttributeValue(LdapUserContext userContext, String attribute) {
        if (((DefaultLdapUserContext) userContext).getKnownAttributes().get(attribute.toLowerCase()) != null) {
            return ((DefaultLdapUserContext) userContext).getKnownAttributes().get(attribute.toLowerCase());
        }
        return getAttributeValues(userContext, attribute).get(attribute);
    }

    @Override
    public Map<String, String> getAttributeValues(LdapUserContext userContext, String... attributes) {
        Map<String, String> result = new HashMap<>();
        List<String> retainedAttr = new ArrayList<>();
        Map<String, String> knownAttributes = ((DefaultLdapUserContext) userContext).getKnownAttributes();
        for (String attr : attributes) {
            if (knownAttributes.get(attr.toLowerCase()) == null) {
                retainedAttr.add(attr.toLowerCase());
            }
        }
        if (!retainedAttr.isEmpty()) {
            LOGGER.debug("Connecting to LDAP directory to retrieve attributes {}", retainedAttr);
            try {
                SearchResultEntry entry = ldapConnectionPool.getEntry(userContext.getDn(), retainedAttr.toArray(new String[retainedAttr.size()]));
                for (String attr : retainedAttr) {
                    knownAttributes.put(attr, entry.getAttributeValue(attr));
                }
            } catch (com.unboundid.ldap.sdk.LDAPException e) {
                throw SeedException.wrap(e, LdapErrorCode.LDAP_ERROR);
            }
        }
        for (String attr : attributes) {
            result.put(attr.toLowerCase(), knownAttributes.get(attr.toLowerCase()));
        }
        return result;
    }

    @Override
    public Set<String> retrieveUserGroups(LdapUserContext userContext) {
        Set<String> groups = new HashSet<>();
        try {
            Filter groupClassFilter;
            if (Strings.isNullOrEmpty(groupConfig.getObjectClass())) {
                groupClassFilter = Filter.createPresenceFilter("objectclass");
            } else {
                groupClassFilter = Filter.createEqualityFilter("objectclass", groupConfig.getObjectClass());
            }

            Filter filter = Filter.createANDFilter(groupClassFilter, Filter.createEqualityFilter(groupConfig.getMemberAttribute(), userContext.getDn()));
            LOGGER.debug(filter.toString());

            SearchResult searchResult = ldapConnectionPool.search(groupConfig.getBaseDN(), SearchScope.SUB, filter, "cn");
            for (SearchResultEntry entry : searchResult.getSearchEntries()) {
                groups.add(entry.getAttributeValue("cn"));
            }
            return groups;
        } catch (com.unboundid.ldap.sdk.LDAPException e) {
            throw SeedException.wrap(e, LdapErrorCode.LDAP_ERROR);
        }
    }
}
