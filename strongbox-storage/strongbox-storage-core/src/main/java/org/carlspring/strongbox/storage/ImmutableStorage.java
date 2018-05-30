package org.carlspring.strongbox.storage;

import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class ImmutableStorage
{

    private final String id;

    private final String basedir;

    private final Map<String, ImmutableRepository> repositories;

    public ImmutableStorage(final Storage delegate)
    {
        this.id = delegate.getId();
        this.basedir = delegate.getBasedir();
        this.repositories = immuteRepositories(delegate.getRepositories());
    }

    private Map<String, ImmutableRepository> immuteRepositories(final Map<String, Repository> source)
    {
        return source != null ? ImmutableMap.copyOf(source.entrySet().stream().collect(
                toMap(Map.Entry::getKey, e -> new ImmutableRepository(e.getValue(), this)))) : Collections.emptyMap();
    }

    public ImmutableRepository getRepository(final String repositoryId)
    {
        return repositories.get(repositoryId);
    }

    public boolean containsRepository(final String repository)
    {
        return repositories.containsKey(repository);
    }

    public String getId()
    {
        return id;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public Map<String, ImmutableRepository> getRepositories()
    {
        return repositories;
    }
}
