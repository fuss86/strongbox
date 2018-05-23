package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.ImmutableCustomRepositoryConfiguration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableMavenRepositoryConfiguration
        extends ImmutableCustomRepositoryConfiguration
{

    private final MavenRepositoryConfiguration delegate;


    public ImmutableMavenRepositoryConfiguration(final MavenRepositoryConfiguration delegate)
    {
        this.delegate = delegate;
    }

    public boolean isIndexingEnabled()
    {
        return delegate.isIndexingEnabled();
    }

    public boolean isIndexingClassNamesEnabled()
    {
        return delegate.isIndexingClassNamesEnabled();
    }
}

