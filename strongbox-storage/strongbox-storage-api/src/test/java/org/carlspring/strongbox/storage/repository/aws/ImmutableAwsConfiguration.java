package org.carlspring.strongbox.storage.repository.aws;

import org.carlspring.strongbox.storage.repository.ImmutableCustomConfiguration;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableAwsConfiguration
        extends ImmutableCustomConfiguration
{

    private final AwsConfiguration awsConfiguration;

    public ImmutableAwsConfiguration(final AwsConfiguration awsConfiguration)
    {
        this.awsConfiguration = awsConfiguration;
    }

    public String getBucket()
    {
        return awsConfiguration.getBucket();
    }

    public String getKey()
    {
        return awsConfiguration.getKey();
    }
}
