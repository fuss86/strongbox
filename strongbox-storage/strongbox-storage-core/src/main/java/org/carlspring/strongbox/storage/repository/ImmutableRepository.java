package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.ImmutableProxyConfiguration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.remote.ImmutableRemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.ImmutableCustomRepositoryConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableRepository
{

    private final Repository delegate;


    public ImmutableRepository(final Repository delegate)
    {
        this.delegate = delegate;
    }

    public String getId()
    {
        return delegate.getId();
    }

    public String getBasedir()
    {
        return delegate.getBasedir();
    }

    public String getPolicy()
    {
        return delegate.getPolicy();
    }

    public String getImplementation()
    {
        return delegate.getImplementation();
    }

    public String getLayout()
    {
        return delegate.getLayout();
    }

    public String getType()
    {
        return delegate.getType();
    }

    public boolean isSecured()
    {
        return delegate.isSecured();
    }

    public String getStatus()
    {
        return delegate.getStatus();
    }

    public boolean isInService()
    {
        return delegate.isInService();
    }

    public boolean isTrashEnabled()
    {
        return delegate.isTrashEnabled();
    }

    public boolean allowsDeletion()
    {
        return delegate.allowsDeletion();
    }

    public boolean allowsForceDeletion()
    {
        return delegate.allowsForceDeletion();
    }

    public boolean allowsDeployment()
    {
        return delegate.allowsDeployment();
    }

    public boolean allowsRedeployment()
    {
        return delegate.allowsRedeployment();
    }

    public boolean allowsDirectoryBrowsing()
    {
        return delegate.allowsDirectoryBrowsing();
    }

    public boolean isChecksumHeadersEnabled()
    {
        return delegate.isChecksumHeadersEnabled();
    }

    public ImmutableProxyConfiguration getProxyConfiguration()
    {
        final ProxyConfiguration source = delegate.getProxyConfiguration();
        return source != null ? new ImmutableProxyConfiguration(source) : null;
    }

    public ImmutableRemoteRepository getRemoteRepository()
    {
        final RemoteRepository source = delegate.getRemoteRepository();
        return source != null ? new ImmutableRemoteRepository(source) : null;
    }

    public Map<String, String> getGroupRepositories()
    {
        final Map<String, String> source = delegate.getGroupRepositories();
        return source != null ? ImmutableMap.copyOf(source) : Collections.emptyMap();
    }

    public boolean acceptsSnapshots()
    {
        return delegate.acceptsSnapshots();
    }

    public boolean acceptsReleases()
    {
        return delegate.acceptsReleases();
    }

    public ImmutableStorage getStorage()
    {
        final Storage source = delegate.getStorage();
        return source != null ? new ImmutableStorage(source) : null;
    }

    public ImmutableHttpConnectionPool getHttpConnectionPool()
    {
        final HttpConnectionPool source = delegate.getHttpConnectionPool();
        return source != null ? new ImmutableHttpConnectionPool(source) : null;
    }

    public List<ImmutableCustomConfiguration> getCustomConfigurations()
    {
        final List<CustomConfiguration> source = delegate.getCustomConfigurations();
        return source != null ? ImmutableList.copyOf(source.stream().map(CustomConfiguration::getImmutable).collect(
                Collectors.toList())) : Collections.emptyList();
    }

    public ImmutableCustomRepositoryConfiguration getRepositoryConfiguration()
    {
        final CustomRepositoryConfiguration source = delegate.getRepositoryConfiguration();
        return source != null ? source.getImmutable() : null;
    }

    public boolean isAllowsForceDeletion()
    {
        return delegate.isAllowsForceDeletion();
    }

    public boolean isAllowsDeployment()
    {
        return delegate.isAllowsDeployment();
    }

    public boolean isAllowsRedeployment()
    {
        return delegate.isAllowsRedeployment();
    }

    public boolean isAllowsDelete()
    {
        return delegate.isAllowsDelete();
    }

    public boolean isAllowsDirectoryBrowsing()
    {
        return delegate.isAllowsDirectoryBrowsing();
    }

    public boolean isHostedRepository()
    {
        return delegate.isHostedRepository();
    }

    public boolean isProxyRepository()
    {
        return delegate.isProxyRepository();
    }

    public boolean isGroupRepository()
    {
        return delegate.isGroupRepository();
    }

    public boolean isVirtualRepository()
    {
        return delegate.isVirtualRepository();
    }

    public long getArtifactMaxSize()
    {
        return delegate.getArtifactMaxSize();
    }

    public Map<String, String> getArtifactCoordinateValidators()
    {
        final Map<String, String> source = delegate.getArtifactCoordinateValidators();
        return source != null ? ImmutableMap.copyOf(source) : Collections.emptyMap();
    }
}
