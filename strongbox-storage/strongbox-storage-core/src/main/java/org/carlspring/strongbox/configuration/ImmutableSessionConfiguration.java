package org.carlspring.strongbox.configuration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableSessionConfiguration
{

    private final SessionConfiguration delegate;


    public ImmutableSessionConfiguration(final SessionConfiguration delegate)
    {
        this.delegate = delegate;
    }

    public Integer getTimeoutSeconds()
    {
        return delegate.getTimeoutSeconds();
    }
}
