package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
public class ImmutableStorage
{

    private final Storage delegate;


    public ImmutableStorage(final Storage delegate)
    {
        this.delegate = delegate;
    }

    public boolean containsRepository(final String repository)
    {
        return delegate.containsRepository(repository);
    }

    public String getId()
    {
        return delegate.getId();
    }

    public String getBasedir()
    {
        return delegate.getBasedir();
    }

    public Map<String, ImmutableRepository> getRepositories()
    {
        final Map<String, Repository> source = delegate.getRepositories();
        return source != null ? ImmutableMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> new ImmutableRepository(e.getValue())))) : null;
    }

    public ImmutableRepository getRepository(final String repositoryId)
    {
        final Repository source = delegate.getRepository(repositoryId);
        return source != null ? new ImmutableRepository(source) : null;
    }

    public boolean hasRepositories()
    {
        return delegate.hasRepositories();
    }

    public boolean existsOnFileSystem()
    {
        return delegate.existsOnFileSystem();
    }
}
