---
title: "LDAP"
repo: "https://github.com/seedstack/ldap-addon"
author: Yves DAUTREMAY
description: "Provides a security realm which authenticates and authorizes subjects with an LDAP directory."
tags:
    - security
zones:
    - Addons
menu:
    AddonLdap:
        weight: 10
---

SeedStack LDAP addon enables your application to connect to an LDAP directory to identify, authenticate and authorize 
users. <!--more-->

{{< dependency g="org.seedstack.addons.ldap" a="ldap" >}}

# Configuration

To use the LDAP add-on, its realm must be specified in security configuration:

```yaml
security:
    realms: LdapRealm
```

Configuration of the realm itself is done by defining the following properties:

{{% config p="ldap" %}}
```yaml
ldap:
  # The hostname of the LDAP directory server
  host: (String)
  # The port of the LDAP directory server
  port: (int)
  # The number of connections in the pool (defaults to 8)
  connectionNumber: (int)
  # The distinguished name used to make the LDAP requests. If not specified, request will be anonymous
  bindDN: (String)
  # The password used to make the LDAP requests.  
  bindPassword: (String)
  # Configuration for users
  user:
    # Base distinguished name where users can be found in the LDAP
    baseDN: (String)
    # Name of the attribute that is used to identify the user (defaults to 'uid') 
    idAttribute: (String)
    # Additional attributes of the user to retrieve
    additionalAttributes: (Set<String>)
    # If present, only consider users having a matching objectclass attribute
    objectClass: (String)
  group:
    # Base distinguished name where groups can be found in the LDAP
    baseDN: (String)
    # Name of the attribute that is used to reference membership (defaults to 'member') 
    memberAttribute: (String)
    # If present, only consider groups having a matching objectclass attribute
    objectClass: (String)
```
{{% /config %}}

{{% callout info %}}
Additional group/role and permission mapping is done in [security configuration]({{< relref "docs/seed/manual/security.md" >}}) 
as usual.
{{% /callout %}}

# Usage

## Retrieving attributes

### From the current user

When authenticating the user, the LDAP Realm also puts in the user principals an entry point to the user LDAP attributes: _LdapUserContext_. You can then call the LdapService to retrieve attributes.

```java
public class SomeClass {
    @Inject
    private SecuritySupport securitySupport;
    @Inject
    private LdapService ldapService;
    
    public void someMethod() {
        LdapUserContext userContext = securitySupport
                                        .getPrincipalsByType(LdapUserContext.class)
                                        .iterator()
                                        .next()
                                        .getPrincipal();
        String cn = ldapService.getAttributeValue(userContext, "cn")
    }
}
```

### For any user

You can also use the LdapService and LdapUserContext to retrieve user attributes from any user that you know the id

```java
public class SomeClass {
    @Inject
    private LdapService ldapService;
    
    public void someMethod() {
        LdapUserContext userContext = ldapService.findUser(userId);
        String cn = userContext.getAttributeValue(userContext, "cn");
    }
}
```

## Retrieve groups of a user

Once you have the user context you can also retrieve the list of the user groups

```java
public class SomeClass {
    @Inject
    private LdapService ldapService;
    
    public void someMethod() {
        LdapUserContext userContext = ldapService.findUser(userId);
        Set<String> groups  = userContext.retrieveUserGroups(userContext);
    }
}
```

# Going further

SeedStack uses [UnboundID](https://www.unboundid.com/) library to connect to the ldap. You can inject its core component into 
your class to use it. Note that the connections you take from the pool are already configured and ready to be used.

```java
public class SomeClass {
    @Inject
    private LDAPConnectionPool ldapConnectionPool;
    
    public void someMethod() {
        ldapConnectionPool.search(/* ... */);
    }
}
```
