package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.ImmutableCustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class ImmutableRawRepositoryConfiguration
        extends ImmutableCustomRepositoryConfiguration
{


    public ImmutableRawRepositoryConfiguration(final RawRepositoryConfiguration delegate)
    {
        // maybe one day I'll have some implementation here :)
    }

}
