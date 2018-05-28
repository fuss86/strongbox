package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.ImmutableCustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class ImmutableNugetRepositoryConfiguration
        extends ImmutableCustomRepositoryConfiguration
{

    private final String feedVersion;

    private final Integer remoteFeedPageSize;


    public ImmutableNugetRepositoryConfiguration(final NugetRepositoryConfiguration delegate)
    {
        this.feedVersion = delegate.getFeedVersion();
        this.remoteFeedPageSize = delegate.getRemoteFeedPageSize();
    }

    public String getFeedVersion()
    {
        return feedVersion;
    }

    public Integer getRemoteFeedPageSize()
    {
        return remoteFeedPageSize;
    }
}
