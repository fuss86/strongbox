package org.carlspring.strongbox.configuration;

import java.util.Collections;
import java.util.List;

import jersey.repackaged.com.google.common.collect.ImmutableList;

/**
 * @author Przemyslaw Fusik
 * @see ProxyConfiguration
 */
public class ImmutableProxyConfiguration
{

    private final ProxyConfiguration delegate;

    public ImmutableProxyConfiguration(final ProxyConfiguration delegate)
    {
        this.delegate = delegate;
    }

    public int getPort()
    {
        return delegate.getPort();
    }

    public String getUsername()
    {
        return delegate.getUsername();
    }

    public String getPassword()
    {
        return delegate.getPassword();
    }

    public String getType()
    {
        return delegate.getType();
    }

    public List<String> getNonProxyHosts()
    {
        final List<String> nonProxyHosts = delegate.getNonProxyHosts();
        return nonProxyHosts != null ? ImmutableList.copyOf(nonProxyHosts) : Collections.emptyList();
    }
}
