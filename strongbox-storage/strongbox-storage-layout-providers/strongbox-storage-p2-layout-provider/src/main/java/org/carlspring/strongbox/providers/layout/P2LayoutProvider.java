package org.carlspring.strongbox.providers.layout;


import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.P2ArtifactCoordinates;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.layout.p2.P2ArtifactReader;
import org.carlspring.strongbox.repository.P2RepositoryFeatures;
import org.carlspring.strongbox.repository.P2RepositoryManagementStrategy;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class P2LayoutProvider
        extends AbstractLayoutProvider<P2ArtifactCoordinates>
{

    private static final Logger logger = LoggerFactory.getLogger(P2LayoutProvider.class);

    public static final String ALIAS = "P2 Repository";

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private P2RepositoryManagementStrategy p2RepositoryManagementStrategy;

    @Inject
    private ArtifactManagementService p2ArtifactManagementService;

    @Inject
    private P2RepositoryFeatures p2RepositoryFeatures;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Override
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
    public P2ArtifactCoordinates getArtifactCoordinates(String path)
    {
        return P2ArtifactCoordinates.create(path);
    }

    public boolean isMetadata(String path)
    {
        return "content.xml".equals(path) || "artifacts.xml".equals(path) || "artifacts.jar".equals(path) ||
               "content.jar".equals(path);
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
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void rebuildIndexes(String storageId,
                               String repositoryId,
                               String basePath,
                               boolean forceRegeneration)
    {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public boolean containsArtifact(ImmutableRepository repository,
                                    ArtifactCoordinates coordinates)
            throws IOException
    {
        if (coordinates != null)
        {
            final RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

            P2ArtifactCoordinates artifact = P2ArtifactReader.getArtifact(repositoryPath.toString(),
                                                                          coordinates.toPath());
            return coordinates.equals(artifact);
        }
        return false;
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return p2RepositoryFeatures.getDefaultArtifactCoordinateValidators();
    }

    @Override
    public boolean containsPath(ImmutableRepository repository,
                                String path)
            throws IOException
    {
        return containsArtifact(repository, P2ArtifactCoordinates.create(path));
    }

    @Override
    public P2RepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return p2RepositoryManagementStrategy;
    }

    @Override
    public ArtifactManagementService getArtifactManagementService()
    {
        return p2ArtifactManagementService;
    }

}
