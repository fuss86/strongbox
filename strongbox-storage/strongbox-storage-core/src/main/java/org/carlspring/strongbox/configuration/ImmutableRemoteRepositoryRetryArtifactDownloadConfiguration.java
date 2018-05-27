package org.carlspring.strongbox.configuration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration
{

    private final int timeoutSeconds;

    private final int maxNumberOfAttempts;

    private final int minAttemptsIntervalSeconds;

    public ImmutableRemoteRepositoryRetryArtifactDownloadConfiguration(final RemoteRepositoryRetryArtifactDownloadConfiguration delegate)
    {
        this.timeoutSeconds = delegate.getTimeoutSeconds();
        this.maxNumberOfAttempts = delegate.getMaxNumberOfAttempts();
        this.minAttemptsIntervalSeconds = delegate.getMinAttemptsIntervalSeconds();
    }

    public int getTimeoutSeconds()
    {
        return timeoutSeconds;
    }

    public int getMaxNumberOfAttempts()
    {
        return maxNumberOfAttempts;
    }

    public int getMinAttemptsIntervalSeconds()
    {
        return minAttemptsIntervalSeconds;
    }
}
