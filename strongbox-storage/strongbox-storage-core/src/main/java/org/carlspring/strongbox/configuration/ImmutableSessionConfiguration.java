package org.carlspring.strongbox.configuration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
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
