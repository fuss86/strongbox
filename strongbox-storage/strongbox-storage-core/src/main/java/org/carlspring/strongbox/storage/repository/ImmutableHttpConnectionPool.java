package org.carlspring.strongbox.storage.repository;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class ImmutableHttpConnectionPool
{

    private final int allocatedConnections;


    public ImmutableHttpConnectionPool(final HttpConnectionPool delegate)
    {
        allocatedConnections = delegate.getAllocatedConnections();
    }

    public int getAllocatedConnections()
    {
        return allocatedConnections;
    }
}
