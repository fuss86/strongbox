package org.carlspring.strongbox.storage.repository;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableHttpConnectionPool
{

    private final HttpConnectionPool delegate;


    public ImmutableHttpConnectionPool(final HttpConnectionPool delegate)
    {
        this.delegate = delegate;
    }

    public int getAllocatedConnections()
    {
        return delegate.getAllocatedConnections();
    }
}
