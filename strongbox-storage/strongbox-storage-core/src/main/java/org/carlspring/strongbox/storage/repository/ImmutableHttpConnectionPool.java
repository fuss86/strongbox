package org.carlspring.strongbox.storage.repository;

/**
 * @author Przemyslaw Fusik
 */
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
