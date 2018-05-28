package org.carlspring.strongbox.providers.layout;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.NpmRepositoryFeatures;
import org.carlspring.strongbox.repository.NpmRepositoryManagementStrategy;
import org.carlspring.strongbox.repository.RepositoryManagementStrategy;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Sergey Bespalov
 *
 */
@Component
public class NpmLayoutProvider
        extends AbstractLayoutProvider<NpmArtifactCoordinates>
{
    private static final Logger logger = LoggerFactory.getLogger(NpmLayoutProvider.class);

    public static final String ALIAS = "npm";

    @Inject
    private NpmRepositoryManagementStrategy npmRepositoryManagementStrategy;

    @Inject
    private ArtifactManagementService npmArtifactManagementService;

    @Inject
    private NpmRepositoryFeatures npmRepositoryFeatures;


    @Override
    @PostConstruct
    public void register()
    {
        layoutProviderRegistry.addProvider(ALIAS, this);

        logger.info("Registered layout provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public NpmArtifactCoordinates getArtifactCoordinates(String path)
    {
        return NpmArtifactCoordinates.parse(path);
    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String metadataPath)
    {

    }

    @Override
    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                String basePath)
    {

    }

    @Override
    public void rebuildIndexes(String storageId,
                               String repositoryId,
                               String basePath,
                               boolean forceRegeneration)
    {

    }

    @Override
    public boolean isMetadata(String path)
    {
        return path.endsWith("package.json") || path.endsWith("package-lock.json")
                || path.endsWith("npm-shrinkwrap.json");
    }

    @Override
    public RepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return npmRepositoryManagementStrategy;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return npmRepositoryFeatures.getDefaultArtifactCoordinateValidators();
    }

    @Override
    public ArtifactManagementService getArtifactManagementService()
    {
        return npmArtifactManagementService;
    }

    @Override
    public Set<String> getDigestAlgorithmSet()
    {
        return Stream.of(MessageDigestAlgorithms.SHA_1)
                     .collect(Collectors.toSet());
    }

    @Override
    public RepositoryPath resolve(ImmutableRepository repository,
                                  URI resource)
    {
        NpmArtifactCoordinates c = NpmArtifactCoordinates.of(resource);
        return resolve(repository, c);
    }
    
}
