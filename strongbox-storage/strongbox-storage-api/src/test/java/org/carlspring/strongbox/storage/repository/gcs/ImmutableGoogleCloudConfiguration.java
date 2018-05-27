package org.carlspring.strongbox.storage.repository.gcs;

import org.carlspring.strongbox.storage.repository.ImmutableCustomConfiguration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableGoogleCloudConfiguration
        extends ImmutableCustomConfiguration
{

    private final String bucket;

    private final String key;

    public ImmutableGoogleCloudConfiguration(final GoogleCloudConfiguration delegate)
    {
        this.bucket = delegate.getBucket();
        this.key = delegate.getKey();
    }

    public String getBucket()
    {
        return bucket;
    }

    public String getKey()
    {
        return key;
    }
}
