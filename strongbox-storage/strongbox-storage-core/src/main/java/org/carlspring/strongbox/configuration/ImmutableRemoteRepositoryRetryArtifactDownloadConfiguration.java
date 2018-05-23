package org.carlspring.strongbox.configuration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration
{

    private final RemoteRepositoryRetryArtifactDownloadConfiguration delegate;

    public ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration(final RemoteRepositoryRetryArtifactDownloadConfiguration delegate)
    {
        this.delegate = delegate;
    }

    public int getTimeoutSeconds()
    {
        return delegate.getTimeoutSeconds();
    }

    public int getMaxNumberOfAttempts()
    {
        return delegate.getMaxNumberOfAttempts();
    }

    public int getMinAttemptsIntervalSeconds()
    {
        return delegate.getMinAttemptsIntervalSeconds();
    }
}
