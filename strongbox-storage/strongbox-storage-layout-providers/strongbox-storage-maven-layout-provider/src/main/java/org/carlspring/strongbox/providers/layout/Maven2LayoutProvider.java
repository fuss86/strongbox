package org.carlspring.strongbox.providers.layout;


import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.MavenIndexerDisabledCondition;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathHandler;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.repository.MavenRepositoryManagementStrategy;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.storage.search.SearchResults;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component("maven2LayoutProvider")
@Conditional(MavenIndexerDisabledCondition.class)
public class Maven2LayoutProvider
        extends AbstractLayoutProvider<MavenArtifactCoordinates>
        implements RepositoryPathHandler
{

    public static final String ALIAS = "Maven 2";

    private static final Logger logger = LoggerFactory.getLogger(Maven2LayoutProvider.class);

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    @Inject
    private ArtifactManagementService mavenArtifactManagementService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private MavenRepositoryManagementStrategy mavenRepositoryManagementStrategy;

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @PostConstruct
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
    public MavenArtifactCoordinates getArtifactCoordinates(String path)
    {
        if (path == null || !ArtifactUtils.isArtifact(path))
        {
            return null;
        }

        MavenArtifactCoordinates coordinates;
        if (isMetadata(path))
        {
            MavenArtifact artifact = MavenArtifactUtils.convertPathToArtifact(path);
            coordinates = new MavenArtifactCoordinates(artifact);
        }
        else
        {
            coordinates = new MavenArtifactCoordinates(path);
        }

        return coordinates;
    }

    @Override
    public boolean isMetadata(String path)
    {
        return path.endsWith(".pom") || path.endsWith(".xml");
    }

    @Override
    public Set<String> getDefaultArtifactCoordinateValidators()
    {
        return mavenRepositoryFeatures.getDefaultArtifactCoordinateValidators();
    }

    
    
    @Override
    public void delete(RepositoryPath repositoryPath,
                       boolean force)
        throws IOException,
        SearchException
    {
        logger.debug("Removing " + repositoryPath + "...");

        ImmutableRepository repository = repositoryPath.getRepository();
        ImmutableStorage storage = repository.getStorage();
        RepositoryPath repositoryPathRelative = repositoryPath.relativize();
        
        if (!Files.isDirectory(repositoryPath))
        {
            delete(repositoryPath);
        }
        else
        {
            List<String> artifactCoordinateElements = StreamSupport.stream(repositoryPathRelative.spliterator(), false)
                                                                   .map(p -> p.toString())
                                                                   .collect(Collectors.toList());
            StringBuffer groupId = new StringBuffer();
            for (int i = 0; i < artifactCoordinateElements.size() - 2; i++)
            {
                String element = artifactCoordinateElements.get(i);
                groupId.append((groupId.length() == 0) ? element : "." + element);
            }

            String artifactId = artifactCoordinateElements.get(artifactCoordinateElements.size() - 2);
            String version = artifactCoordinateElements.get(artifactCoordinateElements.size() - 1);

            Path pomFilePath = repositoryPathRelative.resolve(artifactId + "-" + version + ".pom");

            // If there is a pom file, read it.
            if (Files.exists(resolve(repository).resolve(pomFilePath)))
            {
                // Run a search against the index and get a list of all the artifacts matching this exact GAV
                SearchRequest request = new SearchRequest(storage.getId(),
                                                          repository.getId(),
                                                          "+g:" + groupId + " " +
                                                          "+a:" + artifactId + " " +
                                                          "+v:" + version,
                                                          MavenIndexerSearchProvider.ALIAS);

                try
                {
                    SearchResults results = artifactSearchService.search(request);

                    for (SearchResult result : results.getResults())
                    {
                        delete(resolve(repository, result.getArtifactCoordinates()));
                    }
                }
                catch (SearchException e)
                {
                    logger.error(e.getMessage(), e);
                }
            }
            // Otherwise, this is either not an artifact directory, or not a valid Maven artifact
        }

        deleteMetadata(storage.getId(), repository.getId(), repositoryPathRelative.toString());

        super.delete(repositoryPath, force);
    }


    protected void delete(RepositoryPath directory)
            throws IOException
    {


    }

    @Override
    public void deleteMetadata(String storageId,
                               String repositoryId,
                               String path)
    {
        ImmutableStorage storage = getConfiguration().getStorage(storageId);
        ImmutableRepository repository = storage.getRepository(repositoryId);

        try
        {
            RepositoryPath artifactPath = resolve(repository).resolve(path);
            RepositoryPath artifactBasePath = artifactPath;
            RepositoryPath artifactIdLevelPath = artifactBasePath.getParent();

            if (Files.exists(artifactPath))
            {

                RepositoryFileAttributes artifactFileAttributes = Files.readAttributes(artifactPath,
                                                                                       RepositoryFileAttributes.class);

                if (!artifactFileAttributes.isDirectory())
                {
                    artifactBasePath = artifactBasePath.getParent();
                    artifactIdLevelPath = artifactIdLevelPath.getParent();

                    // This is at the version level
                    try (Stream<Path> pathStream = Files.list(artifactBasePath))
                    {
                        Path pomPath = pathStream.filter(
                                p -> p.getFileName().toString().endsWith(".pom")).findFirst().orElse(null);

                        if (pomPath != null)
                        {
                            String version = ArtifactUtils.convertPathToArtifact(path).getVersion() != null ?
                                             ArtifactUtils.convertPathToArtifact(path).getVersion() :
                                             pomPath.getParent().getFileName().toString();

                            deleteMetadataAtVersionLevel(artifactBasePath, version);
                        }
                    }

                }
            }
            else
            {
                artifactBasePath = artifactBasePath.getParent();
                artifactIdLevelPath = artifactIdLevelPath.getParent();
            }

            if (Files.exists(artifactIdLevelPath))
            {
                // This is at the artifact level
                try (Stream<Path> pathStream = Files.list(artifactIdLevelPath))
                {
                    Path mavenMetadataPath = pathStream.filter(p -> p.getFileName()
                                                                     .toString()
                                                                     .endsWith("maven-metadata.xml"))
                                                       .findFirst()
                                                       .orElse(null);

                    if (mavenMetadataPath != null)
                    {
                        String version = FilenameUtils.getName(artifactBasePath.toString());

                        deleteMetadataAtArtifactLevel((RepositoryPath) mavenMetadataPath.getParent(), version);
                    }
                }
            }
        }
        catch (IOException | XmlPullParserException e)
        {
            // We won't do anything in this case because it doesn't have an impact to the deletion
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteMetadataAtVersionLevel(RepositoryPath metadataBasePath,
                                             String version)
            throws IOException,
                   XmlPullParserException
    {
        if (ArtifactUtils.isSnapshot(version) && Files.exists(metadataBasePath))
        {
            Metadata metadataVersionLevel = mavenMetadataManager.readMetadata(metadataBasePath);
            if (metadataVersionLevel != null && metadataVersionLevel.getVersioning() != null &&
                metadataVersionLevel.getVersioning().getVersions().contains(version))
            {
                metadataVersionLevel.getVersioning().getVersions().remove(version);

                MetadataHelper.setLastUpdated(metadataVersionLevel.getVersioning());

                mavenMetadataManager.storeMetadata(metadataBasePath,
                                                   null,
                                                   metadataVersionLevel,
                                                   MetadataType.SNAPSHOT_VERSION_LEVEL);
            }
        }
    }

    public void deleteMetadataAtArtifactLevel(RepositoryPath artifactPath,
                                              String version)
            throws IOException,
                   XmlPullParserException
    {
        Metadata metadataVersionLevel = mavenMetadataManager.readMetadata(artifactPath);
        if (metadataVersionLevel != null && metadataVersionLevel.getVersioning() != null)
        {
            metadataVersionLevel.getVersioning().getVersions().remove(version);

            if (version.equals(metadataVersionLevel.getVersioning().getLatest()))
            {
                MetadataHelper.setLatest(metadataVersionLevel);
            }

            if (version.equals(metadataVersionLevel.getVersioning().getRelease()))
            {
                MetadataHelper.setRelease(metadataVersionLevel);
            }

            MetadataHelper.setLastUpdated(metadataVersionLevel.getVersioning());

            mavenMetadataManager.storeMetadata(artifactPath,
                                               null,
                                               metadataVersionLevel,
                                               MetadataType.ARTIFACT_ROOT_LEVEL);
        }
    }

    @Override
    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                String basePath)
            throws IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        artifactMetadataService.rebuildMetadata(storageId, repositoryId, basePath);
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
    public MavenRepositoryManagementStrategy getRepositoryManagementStrategy()
    {
        return mavenRepositoryManagementStrategy;
    }

    @Override
    public ArtifactManagementService getArtifactManagementService()
    {
        return mavenArtifactManagementService;
    }

    protected RepositoryPathHandler getRepositoryPathHandler()
    {
        return this;
    }

    @Override
    public void postProcess(final RepositoryPath path)
            throws IOException
    {
        // do nothing
    }
}
