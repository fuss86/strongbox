package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.ImmutableCustomRepositoryConfiguration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableRawRepositoryConfiguration
        extends ImmutableCustomRepositoryConfiguration
{

    private final RawRepositoryConfiguration delegate;


    public ImmutableRawRepositoryConfiguration(final RawRepositoryConfiguration delegate)
    {
        this.delegate = delegate;
    }

}
