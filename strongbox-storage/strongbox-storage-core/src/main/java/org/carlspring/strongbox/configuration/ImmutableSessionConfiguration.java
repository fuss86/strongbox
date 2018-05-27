package org.carlspring.strongbox.configuration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableSessionConfiguration
{

    private final Integer timeoutSeconds;


    public ImmutableSessionConfiguration(final SessionConfiguration delegate)
    {
        this.timeoutSeconds = delegate.getTimeoutSeconds();
    }

    public Integer getTimeoutSeconds()
    {
        return timeoutSeconds;
    }
}
