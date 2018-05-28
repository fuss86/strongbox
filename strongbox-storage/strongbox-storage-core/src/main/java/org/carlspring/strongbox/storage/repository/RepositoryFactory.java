package org.carlspring.strongbox.storage.repository;

/**
 * @author carlspring
 */
public interface RepositoryFactory
{

    Repository createRepository(String repositoryId);

}
