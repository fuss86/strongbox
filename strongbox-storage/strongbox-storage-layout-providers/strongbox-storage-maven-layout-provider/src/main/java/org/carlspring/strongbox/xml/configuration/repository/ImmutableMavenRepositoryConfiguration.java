package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.ImmutableCustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class ImmutableMavenRepositoryConfiguration
        extends ImmutableCustomRepositoryConfiguration
{

    private final boolean indexingEnabled;

    private final boolean indexingClassNamesEnabled;

    public ImmutableMavenRepositoryConfiguration(final MavenRepositoryConfiguration delegate)
    {
        this.indexingEnabled = delegate.isIndexingEnabled();
        this.indexingClassNamesEnabled = delegate.isIndexingClassNamesEnabled();
    }

    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    public boolean isIndexingClassNamesEnabled()
    {
        return indexingClassNamesEnabled;
    }
}

