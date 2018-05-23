package org.carlspring.strongbox.storage.repository.gcs;

import org.carlspring.strongbox.storage.repository.ImmutableCustomConfiguration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableGoogleCloudConfiguration
        extends ImmutableCustomConfiguration
{

    private final GoogleCloudConfiguration delegate;

    public ImmutableGoogleCloudConfiguration(final GoogleCloudConfiguration delegate)
    {
        this.delegate = delegate;
    }

    public String getBucket()
    {
        return delegate.getBucket();
    }

    public String getKey()
    {
        return delegate.getKey();
    }
}
