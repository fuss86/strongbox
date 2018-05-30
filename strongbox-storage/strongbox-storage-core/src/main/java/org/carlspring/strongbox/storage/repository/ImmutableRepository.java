package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.ImmutableProxyConfiguration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.remote.ImmutableRemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.ImmutableCustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class ImmutableRepository
{

    private final String id;

    private final String basedir;

    private final String policy;

    private final String implementation;

    private final String layout;

    private final String type;

    private final boolean secured;

    private final String status;

    private final long artifactMaxSize;

    private final boolean trashEnabled;

    private final boolean allowsForceDeletion;

    private final boolean allowsDeployment;

    private final boolean allowsRedeployment;

    private final boolean allowsDelete;

    private final boolean allowsDirectoryBrowsing;

    private final boolean checksumHeadersEnabled;

    private final ImmutableProxyConfiguration proxyConfiguration;

    private final ImmutableRemoteRepository remoteRepository;

    private final ImmutableHttpConnectionPool httpConnectionPool;

    private final List<ImmutableCustomConfiguration> customConfigurations;

    private final ImmutableCustomRepositoryConfiguration repositoryConfiguration;

    private final Map<String, String> groupRepositories;

    private final Map<String, String> artifactCoordinateValidators;

    private final ImmutableStorage storage;

    public ImmutableRepository(final Repository delegate)
    {
        this(delegate, null);
    }

    public ImmutableRepository(final Repository delegate,
                               final ImmutableStorage storage)
    {
        this.id = delegate.getId();
        this.policy = delegate.getPolicy();
        this.implementation = delegate.getImplementation();
        this.layout = delegate.getLayout();
        this.type = delegate.getType();
        this.secured = delegate.isSecured();
        this.status = delegate.getStatus();
        this.artifactMaxSize = delegate.getArtifactMaxSize();
        this.trashEnabled = delegate.isTrashEnabled();
        this.allowsForceDeletion = delegate.isAllowsForceDeletion();
        this.allowsDeployment = delegate.isAllowsDeployment();
        this.allowsRedeployment = delegate.isAllowsRedeployment();
        this.allowsDelete = delegate.isAllowsDelete();
        this.allowsDirectoryBrowsing = delegate.isAllowsDirectoryBrowsing();
        this.checksumHeadersEnabled = delegate.isChecksumHeadersEnabled();
        this.proxyConfiguration = immuteProxyConfiguration(delegate.getProxyConfiguration());
        this.remoteRepository = immuteRemoteRepository(delegate.getRemoteRepository());
        this.httpConnectionPool = immuteHttpConnectionPool(delegate.getHttpConnectionPool());
        this.customConfigurations = immuteCustomConfigurations(delegate.getCustomConfigurations());
        this.repositoryConfiguration = immuteCustomRepositoryConfiguration(delegate.getRepositoryConfiguration());
        this.groupRepositories = immuteGroupRepositories(delegate.getGroupRepositories());
        this.artifactCoordinateValidators = immuteArtifactCoordinateValidators(
                delegate.getArtifactCoordinateValidators());
        this.storage = storage != null ? storage : immuteStorage(delegate.getStorage());
        this.basedir = delegate.getBasedir();
    }

    private ImmutableProxyConfiguration immuteProxyConfiguration(final ProxyConfiguration source)
    {
        return source != null ? new ImmutableProxyConfiguration(source) : null;
    }

    private ImmutableRemoteRepository immuteRemoteRepository(final RemoteRepository source)
    {
        return source != null ? new ImmutableRemoteRepository(source) : null;
    }

    private Map<String, String> immuteGroupRepositories(final Map<String, String> source)
    {
        return source != null ? ImmutableMap.copyOf(source) : Collections.emptyMap();
    }

    private ImmutableStorage immuteStorage(final Storage source)
    {
        return source != null ? new ImmutableStorage(source) : null;
    }

    private ImmutableHttpConnectionPool immuteHttpConnectionPool(final HttpConnectionPool source)
    {
        return source != null ? new ImmutableHttpConnectionPool(source) : null;
    }

    private List<ImmutableCustomConfiguration> immuteCustomConfigurations(final List<CustomConfiguration> source)
    {
        return source != null ? ImmutableList.copyOf(source.stream().map(CustomConfiguration::getImmutable).collect(
                Collectors.toList())) : Collections.emptyList();
    }

    private ImmutableCustomRepositoryConfiguration immuteCustomRepositoryConfiguration(final CustomRepositoryConfiguration source)
    {
        return source != null ? source.getImmutable() : null;
    }


    private Map<String, String> immuteArtifactCoordinateValidators(final Map<String, String> source)
    {
        return source != null ? ImmutableMap.copyOf(source) : Collections.emptyMap();
    }

    public String getId()
    {
        return id;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public String getPolicy()
    {
        return policy;
    }

    public String getImplementation()
    {
        return implementation;
    }

    public String getLayout()
    {
        return layout;
    }

    public String getType()
    {
        return type;
    }

    public boolean isSecured()
    {
        return secured;
    }

    public String getStatus()
    {
        return status;
    }

    public long getArtifactMaxSize()
    {
        return artifactMaxSize;
    }

    public boolean isTrashEnabled()
    {
        return trashEnabled;
    }

    public boolean isAllowsForceDeletion()
    {
        return allowsForceDeletion;
    }

    public boolean isAllowsDeployment()
    {
        return allowsDeployment;
    }

    public boolean isAllowsRedeployment()
    {
        return allowsRedeployment;
    }

    public boolean isAllowsDelete()
    {
        return allowsDelete;
    }

    public boolean isAllowsDirectoryBrowsing()
    {
        return allowsDirectoryBrowsing;
    }

    public boolean isChecksumHeadersEnabled()
    {
        return checksumHeadersEnabled;
    }

    public ImmutableProxyConfiguration getProxyConfiguration()
    {
        return proxyConfiguration;
    }

    public ImmutableRemoteRepository getRemoteRepository()
    {
        return remoteRepository;
    }

    public ImmutableHttpConnectionPool getHttpConnectionPool()
    {
        return httpConnectionPool;
    }

    public List<ImmutableCustomConfiguration> getCustomConfigurations()
    {
        return customConfigurations;
    }

    public ImmutableCustomRepositoryConfiguration getRepositoryConfiguration()
    {
        return repositoryConfiguration;
    }

    public Map<String, String> getGroupRepositories()
    {
        return groupRepositories;
    }

    public Map<String, String> getArtifactCoordinateValidators()
    {
        return artifactCoordinateValidators;
    }

    public ImmutableStorage getStorage()
    {
        return storage;
    }

    public boolean isHostedRepository()
    {
        return RepositoryTypeEnum.HOSTED.getType().equals(type);
    }

    public boolean isProxyRepository()
    {
        return RepositoryTypeEnum.PROXY.getType().equals(type);
    }

    public boolean isGroupRepository()
    {
        return RepositoryTypeEnum.GROUP.getType().equals(type);
    }

    public boolean isInService()
    {
        return RepositoryStatusEnum.IN_SERVICE.getStatus().equalsIgnoreCase(getStatus());
    }

    public boolean acceptsSnapshots()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsSnapshots();
    }

    public boolean acceptsReleases()
    {
        return RepositoryPolicyEnum.ofPolicy(getPolicy()).acceptsReleases();
    }
}
