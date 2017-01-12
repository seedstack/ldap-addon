/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.ldap;

import org.hibernate.validator.constraints.NotBlank;
import org.seedstack.coffig.Config;
import org.seedstack.coffig.SingleValue;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Config("security.ldap")
public class LdapConfig {
    private static final int DEFAULT_PORT = 389;
    private static final int DEFAULT_CONNECTION_NUMBER = 8;
    @NotBlank
    private String host;
    @NotNull
    @Min(0)
    @Max(65535)
    private int port = DEFAULT_PORT;
    @Min(1)
    @NotNull
    private int connectionNumber = DEFAULT_CONNECTION_NUMBER;
    private String bindDN;
    private String bindPassword;
    @NotNull
    private UserConfig user = new UserConfig();
    @NotNull
    private GroupConfig group = new GroupConfig();

    public String getHost() {
        return host;
    }

    public LdapConfig setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public LdapConfig setPort(int port) {
        this.port = port;
        return this;
    }

    public int getConnectionNumber() {
        return connectionNumber;
    }

    public LdapConfig setConnectionNumber(int connectionNumber) {
        this.connectionNumber = connectionNumber;
        return this;
    }

    public String getBindDN() {
        return bindDN;
    }

    public LdapConfig setBindDN(String bindDN) {
        this.bindDN = bindDN;
        return this;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public LdapConfig setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
        return this;
    }

    public UserConfig user() {
        return user;
    }

    public GroupConfig group() {
        return group;
    }

    @Config("user")
    public static class UserConfig {
        private static final String USER_ID_DEFAULT_ATTRIBUTE = "uid";
        @NotBlank
        @SingleValue
        private String baseDN;
        @NotBlank
        private String idAttribute = USER_ID_DEFAULT_ATTRIBUTE;
        @NotNull
        private Set<String> additionalAttributes = new HashSet<>();
        private String objectClass;


        public String getBaseDN() {
            return baseDN;
        }

        public UserConfig setBaseDN(String baseDN) {
            this.baseDN = baseDN;
            return this;
        }

        public String getIdAttribute() {
            return idAttribute;
        }

        public UserConfig setIdAttribute(String idAttribute) {
            this.idAttribute = idAttribute;
            return this;
        }

        public Set<String> getAdditionalAttributes() {
            return Collections.unmodifiableSet(additionalAttributes);
        }

        public UserConfig addAdditionalAttribute(String additionalAttribute) {
            this.additionalAttributes.add(additionalAttribute);
            return this;
        }

        public String getObjectClass() {
            return objectClass;
        }

        public UserConfig setObjectClass(String objectClass) {
            this.objectClass = objectClass;
            return this;
        }
    }

    @Config("group")
    public static class GroupConfig {
        private static final String GROUP_MEMBER_DEFAULT_ATTRIBUTE = "member";
        @NotBlank
        @SingleValue
        private String baseDN;
        @NotBlank
        private String memberAttribute = GROUP_MEMBER_DEFAULT_ATTRIBUTE;
        private String objectClass;

        public String getBaseDN() {
            return baseDN;
        }

        public GroupConfig setBaseDN(String baseDN) {
            this.baseDN = baseDN;
            return this;
        }

        public String getMemberAttribute() {
            return memberAttribute;
        }

        public GroupConfig setMemberAttribute(String memberAttribute) {
            this.memberAttribute = memberAttribute;
            return this;
        }

        public String getObjectClass() {
            return objectClass;
        }

        public GroupConfig setObjectClass(String objectClass) {
            this.objectClass = objectClass;
            return this;
        }
    }
}
