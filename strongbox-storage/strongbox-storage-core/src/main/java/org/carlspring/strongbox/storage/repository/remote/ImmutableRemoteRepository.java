package org.carlspring.strongbox.storage.repository.remote;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableRemoteRepository
{

    private final RemoteRepository delegate;

    public ImmutableRemoteRepository(final RemoteRepository delegate)
    {
        this.delegate = delegate;
    }

    public String getUrl()
    {
        return delegate.getUrl();
    }

    public boolean isDownloadRemoteIndexes()
    {
        return delegate.isDownloadRemoteIndexes();
    }

    public boolean isAutoBlocking()
    {
        return delegate.isAutoBlocking();
    }

    public boolean isChecksumValidation()
    {
        return delegate.isChecksumValidation();
    }

    public String getUsername()
    {
        return delegate.getUsername();
    }

    public String getPassword()
    {
        return delegate.getPassword();
    }

    public String getChecksumPolicy()
    {
        return delegate.getChecksumPolicy();
    }

    public Integer getCheckIntervalSeconds()
    {
        return delegate.getCheckIntervalSeconds();
    }

    public boolean isAllowsDirectoryBrowsing()
    {
        return delegate.isAllowsDirectoryBrowsing();
    }

    public boolean isAutoImportRemoteSSLCertificate()
    {
        return delegate.isAutoImportRemoteSSLCertificate();
    }
}
