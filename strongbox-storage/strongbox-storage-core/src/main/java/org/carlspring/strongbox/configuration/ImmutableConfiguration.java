package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.ImmutableHttpConnectionPool;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.routing.ImmutableRoutingRules;
import org.carlspring.strongbox.storage.routing.RoutingRules;

import javax.annotation.concurrent.Immutable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
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

    public List<ImmutableRepository> getRepositoriesWithLayout(String storageId,
                                                               String layout)
    {
        Stream<ImmutableRepository> repositories;
        if (storageId != null)
        {
            ImmutableStorage storage = getStorage(storageId);
            if (storage != null)
            {
                repositories = storage.getRepositories().values().stream();
            }
            else
            {
                return Collections.emptyList();
            }
        }
        else
        {
            repositories = getStorages().values().stream().flatMap(
                    storage -> storage.getRepositories().values().stream());
        }

        return repositories.filter(repository -> repository.getLayout().equals(layout))
                           .collect(Collectors.toList());
    }

    public List<ImmutableRepository> getGroupRepositories()
    {
        List<ImmutableRepository> groupRepositories = new ArrayList<>();

        for (ImmutableStorage storage : getStorages().values())
        {
            groupRepositories.addAll(storage.getRepositories()
                                            .values()
                                            .stream()
                                            .filter(repository -> repository.getType()
                                                                            .equals(RepositoryTypeEnum.GROUP.getType()))
                                            .collect(Collectors.toList()));
        }

        return groupRepositories;
    }

    public ImmutableRepository getRepository(String storageId,
                                             String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId);
    }

    public List<ImmutableRepository> getGroupRepositoriesContaining(String storageId,
                                                                    String repositoryId)
    {
        List<ImmutableRepository> groupRepositories = new ArrayList<>();

        ImmutableStorage storage = getStorage(storageId);

        groupRepositories.addAll(storage.getRepositories()
                                        .values()
                                        .stream()
                                        .filter(repository -> repository.getType()
                                                                        .equals(RepositoryTypeEnum.GROUP.getType()))
                                        .filter(repository -> repository.getGroupRepositories()
                                                                        .keySet()
                                                                        .contains(repositoryId))
                                        .collect(Collectors.toList()));

        return groupRepositories;
    }

    public ImmutableHttpConnectionPool getHttpConnectionPoolConfiguration(String storageId,
                                                                          String repositoryId)
    {
        return getStorage(storageId).getRepository(repositoryId).getHttpConnectionPool();
    }

}
