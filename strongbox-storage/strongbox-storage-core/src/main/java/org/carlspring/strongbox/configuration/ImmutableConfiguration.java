package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.routing.RoutingRules;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableConfiguration
{

    private final Configuration delegate;

    public ImmutableConfiguration(final Configuration delegate)
    {
        this.delegate = delegate;
    }

    public String getId()
    {
        return delegate.getId();
    }

    public String getInstanceName()
    {
        return delegate.getInstanceName();
    }

    public String getVersion()
    {
        return delegate.getVersion();
    }

    public String getRevision()
    {
        return delegate.getRevision();
    }

    public String getBaseUrl()
    {
        return delegate.getBaseUrl();
    }

    public int getPort()
    {
        return delegate.getPort();
    }

    public ImmutableProxyConfiguration getProxyConfiguration()
    {
        final ProxyConfiguration source = delegate.getProxyConfiguration();
        return source != null ? new ImmutableProxyConfiguration(source) : null;
    }

    public ImmutableSessionConfiguration getSessionConfiguration()
    {
        final SessionConfiguration source = delegate.getSessionConfiguration();
        return source != null ? new ImmutableSessionConfiguration(source) : null;
    }

    public Map<String, ImmutableStorage> getStorages()
    {
        final Map<String, Storage> source = delegate.getStorages();
        return source != null ? ImmutableMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> new ImmutableStorage(e.getValue())))) : null;
    }

    public ImmutableStorage getStorage(final String storageId)
    {
        final Storage source = delegate.getStorage(storageId);
        return source != null ? new ImmutableStorage(source) : null;
    }

    public RoutingRules getRoutingRules()
    {
        return delegate.getRoutingRules();
    }

    public RemoteRepositoriesConfiguration getRemoteRepositoriesConfiguration()
    {
        return delegate.getRemoteRepositoriesConfiguration();
    }
}
