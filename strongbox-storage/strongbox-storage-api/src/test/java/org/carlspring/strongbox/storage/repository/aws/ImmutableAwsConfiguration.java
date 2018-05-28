package org.carlspring.strongbox.storage.repository.aws;

import org.carlspring.strongbox.storage.repository.ImmutableCustomConfiguration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class ImmutableAwsConfiguration
        extends ImmutableCustomConfiguration
{

    private final String bucket;

    private final String key;

    public ImmutableAwsConfiguration(final AwsConfiguration delegate)
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
