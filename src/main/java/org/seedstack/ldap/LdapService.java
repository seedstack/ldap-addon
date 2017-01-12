/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap;

import java.util.Map;
import java.util.Set;

/**
 * Convenient interface to interact with an LDAP with user issues in mind. This interface makes use of the UserContext object. Use the
 * createUserContext or findUser methods to get a userContext instance.
 */
public interface LdapService {

    /**
     * Creates a new DefaultLDAPUserContext knowing the dn of the user. Note that this method does not make an LDAP call.
     *
     * @param dn the dn of the user
     * @return the corresponding DefaultLDAPUserContext
     */
    LdapUserContext createUserContext(String dn);

    /**
     * Creates a new DefaultLDAPUserContext based on the identifying attribute of the user. The identifying attribute is the one defined in the
     * configuration (uid by default).
     *
     * @param identityAttributeValue the value of the identifying attribute to search
     * @return the DefaultLDAPUserContext corresponding to the attribute value.
     */
    LdapUserContext findUser(String identityAttributeValue);

    /**
     * Authenticates a user with its context
     *
     * @param userContext the context of a user
     */
    void authenticate(LdapUserContext userContext, String password);

    /**
     * Gives the value of the attribute name passed as parameter. Note that this method will call the LDAP only if the attribute value has not yet
     * been retrieved. As a result, multiple calls of this method with the same attribute name will only make one call to the LDAP.
     *
     * @param userContext the DefaultLDAPUserContext used
     * @param attribute   the name of the attribute
     * @return the value of the attribute passed as a parameter, or null if the attribute does not exist.
     */
    String getAttributeValue(LdapUserContext userContext, String attribute);

    /**
     * Gives the value of the attribute names passed as parameters. Note that this method will call the LDAP only if the attribute values have not yet
     * been retrieved. As a result, multiple calls of this method with the same attribute name will only make one call to the LDAP. Also, a unique
     * call will be made to retrieve all the required attributes.
     *
     * @param userContext the DefaultLDAPUserContext used
     * @param attributes  the names of the attributes
     * @return the values of the attributes passed as a parameters as a map. Non existing attributes in the LDAP will give null values.
     */
    Map<String, String> getAttributeValues(LdapUserContext userContext, String... attributes);

    /**
     * Finds all the groups in which the user is defined as a member. Note that this method will effectively call the LDAP each time it is executed.
     *
     * @param userContext the userContext to use
     * @return the groups as a set of the groups CNs
     */
    Set<String> retrieveUserGroups(LdapUserContext userContext);
}
