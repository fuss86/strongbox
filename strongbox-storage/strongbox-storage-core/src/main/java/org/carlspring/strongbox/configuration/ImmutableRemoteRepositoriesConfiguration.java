package org.carlspring.strongbox.configuration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableRemoteRepositoriesConfiguration
{

    private final ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration remoteRepositoryRetryArtifactDownloadConfiguration;

    private final int checkIntervalSeconds;

    private final int heartbeatThreadsNumber;

    public ImmutableRemoteRepositoriesConfiguration(final RemoteRepositoriesConfiguration delegate)
    {
        this.remoteRepositoryRetryArtifactDownloadConfiguration = immuteRemoteRepositoryRetryArtifactDownloadConfiguration(
                delegate.getRemoteRepositoryRetryArtifactDownloadConfiguration());
        this.checkIntervalSeconds = delegate.getCheckIntervalSeconds();
        this.heartbeatThreadsNumber = delegate.getHeartbeatThreadsNumber();
    }

    public ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration getRemoteRepositoryRetryArtifactDownloadConfiguration()
    {
        return remoteRepositoryRetryArtifactDownloadConfiguration;
    }

    private ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration immuteRemoteRepositoryRetryArtifactDownloadConfiguration(final RemoteRepositoryRetryArtifactDownloadConfiguration source)
    {
        return source != null ? new ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration(source) : null;
    }

    public int getCheckIntervalSeconds()
    {
        return checkIntervalSeconds;
    }

    public int getHeartbeatThreadsNumber()
    {
        return heartbeatThreadsNumber;
    }
}
