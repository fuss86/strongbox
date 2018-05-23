package org.carlspring.strongbox.configuration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableRemoteRepositoriesConfiguration
{

    private final RemoteRepositoriesConfiguration delegate;

    public ImmutableRemoteRepositoriesConfiguration(final RemoteRepositoriesConfiguration delegate)
    {
        this.delegate = delegate;
    }

    public ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration getRemoteRepositoryRetryArtifactDownloadConfiguration()
    {
        final RemoteRepositoryRetryArtifactDownloadConfiguration source = delegate.getRemoteRepositoryRetryArtifactDownloadConfiguration();
        return source != null ? new ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration(source) : null;
    }

    public int getCheckIntervalSeconds()
    {
        return delegate.getCheckIntervalSeconds();
    }

    public int getHeartbeatThreadsNumber()
    {
        return delegate.getHeartbeatThreadsNumber();
    }
}
