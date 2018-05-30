package org.carlspring.strongbox.storage.repository;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.repository.NugetRepositoryFeatures;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class NugetRepositoryFactory
        implements RepositoryFactory
{

    @Inject
    private NugetRepositoryFeatures nugetRepositoryFeatures;


    @Override
    public Repository createRepository(String repositoryId)
    {
        Repository repository = new Repository(repositoryId);
        repository.setLayout(NugetLayoutProvider.ALIAS);
        repository.setArtifactCoordinateValidators(nugetRepositoryFeatures.getDefaultArtifactCoordinateValidators());

        return repository;
    }

}
