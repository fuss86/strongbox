package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.routing.ImmutableRoutingRules;
import org.carlspring.strongbox.storage.routing.RoutingRules;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableConfiguration
{

    private final String id;

    private final String instanceName;

    private final String version;

    private final String revision;

    private final String baseUrl;

    private final int port;

    private final ImmutableProxyConfiguration proxyConfiguration;

    private final ImmutableSessionConfiguration sessionConfiguration;

    private final ImmutableRemoteRepositoriesConfiguration remoteRepositoriesConfiguration;

    private final Map<String, ImmutableStorage> storages;

    private final ImmutableRoutingRules routingRules;

    public ImmutableConfiguration(final Configuration delegate)
    {

        id = delegate.getId();
        instanceName = delegate.getInstanceName();
        version = delegate.getVersion();
        revision = delegate.getRevision();
        baseUrl = delegate.getBaseUrl();
        port = delegate.getPort();
        proxyConfiguration = immuteProxyConfiguration(delegate.getProxyConfiguration());
        sessionConfiguration = immuteSessionConfiguration(delegate.getSessionConfiguration());
        remoteRepositoriesConfiguration = immuteRemoteRepositoriesConfiguration(
                delegate.getRemoteRepositoriesConfiguration());
        storages = immuteStorages(delegate.getStorages());
        routingRules = immuteRoutingRules(delegate.getRoutingRules());
    }


    private ImmutableProxyConfiguration immuteProxyConfiguration(final ProxyConfiguration source)
    {
        return source != null ? new ImmutableProxyConfiguration(source) : null;
    }

    private ImmutableSessionConfiguration immuteSessionConfiguration(final SessionConfiguration source)
    {
        return source != null ? new ImmutableSessionConfiguration(source) : null;
    }

    private Map<String, ImmutableStorage> immuteStorages(final Map<String, Storage> source)
    {
        return source != null ? ImmutableMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> new ImmutableStorage(e.getValue())))) : Collections.emptyMap();
    }

    private ImmutableRemoteRepositoriesConfiguration immuteRemoteRepositoriesConfiguration(final RemoteRepositoriesConfiguration source)
    {
        return source != null ? new ImmutableRemoteRepositoriesConfiguration(source) : null;
    }

    private ImmutableRoutingRules immuteRoutingRules(final RoutingRules source)
    {
        return source != null ? new ImmutableRoutingRules(source) : null;
    }

    public String getId()
    {
        return id;
    }

    public String getInstanceName()
    {
        return instanceName;
    }

    public String getVersion()
    {
        return version;
    }

    public String getRevision()
    {
        return revision;
    }

    public String getBaseUrl()
    {
        return baseUrl;
    }

    public int getPort()
    {
        return port;
    }

    public ImmutableProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public ImmutableSessionConfiguration getSessionConfiguration()
    {
        return sessionConfiguration;
    }

    public ImmutableRemoteRepositoriesConfiguration getRemoteRepositoriesConfiguration()
    {
        return remoteRepositoriesConfiguration;
    }

    public Map<String, ImmutableStorage> getStorages()
    {
        return storages;
    }

    public ImmutableStorage getStorage(final String storageId)
    {
        return storages.get(storageId);
    }

    public ImmutableRoutingRules getRoutingRules()
    {
        return routingRules;
    }
}
