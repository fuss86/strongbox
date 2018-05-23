package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.ImmutableCustomRepositoryConfiguration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableNugetRepositoryConfiguration
        extends ImmutableCustomRepositoryConfiguration
{

    private final NugetRepositoryConfiguration delegate;

    public ImmutableNugetRepositoryConfiguration(final NugetRepositoryConfiguration delegate)
    {
        this.delegate = delegate;
    }

    public String getFeedVersion()
    {
        return delegate.getFeedVersion();
    }

    public Integer getRemoteFeedPageSize()
    {
        return delegate.getRemoteFeedPageSize();
    }
}
