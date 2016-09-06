---
title: "Basics"
name: "LDAP"
repo: "https://github.com/seedstack/ldap-addon"
date: 2016-02-09
author: Yves DAUTREMAY
description: "Provides a security realm which authenticates and authorizes subjects with an LDAP directory."
backend: true
weight: -1
tags:
    - "security"
    - "ldap"
    - "directory"
    - "realm"
    - "shiro"
zones:
    - Addons
menu:
    AddonLdap:
        weight: 10
---

Seed ldap addon enables your application to connect to a LDAP to identify, autentify and authorize users.

{{< dependency g="org.seedstack.addons.ldap" a="ldap" >}}

# Configuration

In your properties file, you can add the following properties to configure the connection to the LDAP

```ini
[org.seedstack.ldap]
server-host = ldaphost
server-port = 53800
account-dn = cn=admin, ou=people, dc=example,dc=com
account-password = admin
user-base = ou=people, dc=example,dc=com
user-identity-attribute = sn
group-base = ou=groups, dc=example,dc=com
```

* server-host: the hostname where the ldap is
* server-port: the port to use
* account-dn: the dn of the account that will make the requests. If none, requests will be anonymous
* account-password: password of the above account
* user-base: base dn where the users can be found in the LDAP
* user-identity-attribute: name of the attribute that is used to identify the user
* group-base: base dn where groups can be found in the LDAP

Then you can configure SEED security so it uses the LDAP Realm

```ini
[org.seedstack.seed.security]
realms = LdapRealm
```

You can map the LDAP groups to your application functional roles
```ini
[org.seedstack.seed.security.roles]
jedi = SEED.JEDI
```

And associate permissions to those roles
```ini
jedi = lightSaber:*, academy:*
```

With this configuration, the LDAP realm will be able to authenticate your user with their id and password, and retrieve the groups through the LDAP.

# Retrieving attributes

## From the current user

When authenticating the user, the LDAP Realm also puts in the user principals an entry point to the user LDAP attributes: _LdapUserContext_. You can then call the LdapService to retrieve attributes.

```java
    @Inject
    private SecuritySupport securitySupport;
    @Inject
    private LdapService ldapService;
    ...........
    
        LdapUserContext userContext = securitySupport.getPrincipalsByType(LdapUserContext.class).iterator().next().getPrincipal();
        String cn = ldapService.getAttributeValue(userContext, "cn")
```

## For any user

You can also use the LdapService and LdapUserContext to retrieve user attributes from any user that you know the id

```java
    @Inject
    private LdapService ldapService;
    ................
    
    LdapUserContext userContext = ldapService.findUser(userId);
    String cn = userContext.getAttributeValue(userContext, "cn");
```

# List a user groups

Once you have the user context you can also retrieve the list of the user groups

```java
    @Inject
    private LdapService ldapService;
    ................
    
    LdapUserContext userContext = ldapService.findUser(userId);
    Set<String> groups  = userContext.retrieveUserGroups(userContext);
```

# Going further

Seed uses [unboundid](https://www.unboundid.com/) library to connect to the ldap. You can inject its core component into your class to use it. Note that the connections you take from the pool are already configured and ready to be used.

```java
    @Inject
    private LDAPConnectionPool ldapConnectionPool;

    public void someMethod() {
        ...
        ldapConnectionPool.search(...);
        ...
    }
```
