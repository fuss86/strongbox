package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.ImmutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author Kate Novik.
 */
public class RemoveTimestampedMavenSnapshotCronJob
        extends JavaCronJob
{

    @Inject
    private MavenRepositoryFeatures mavenRepositoryFeatures;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private JobManager manager;


    @Override
    public void executeTask(CronTaskConfiguration config)
            throws Throwable
    {
        String storageId = config.getProperty("storageId");
        String repositoryId = config.getProperty("repositoryId");
        String basePath = config.getProperty("basePath");

        // The number of artifacts to keep
        int numberToKeep = config.getProperty("numberToKeep") != null ?
                           Integer.valueOf(config.getProperty("numberToKeep")) :
                           10;

        // The period to keep artifacts (the number of days)
        int keepPeriod = config.getProperty("keepPeriod") != null ?
                         Integer.valueOf(config.getProperty("keepPeriod")) :
                         30;

        if (storageId == null)
        {
            Map<String, ImmutableStorage> storages = getStorages();
            for (String storage : storages.keySet())
            {
                removeTimestampedSnapshotArtifacts(storage, numberToKeep, keepPeriod);
            }
        }
        else if (repositoryId == null)
        {
            removeTimestampedSnapshotArtifacts(storageId, numberToKeep, keepPeriod);
        }
        else
        {
            mavenRepositoryFeatures.removeTimestampedSnapshots(storageId,
                                                               repositoryId,
                                                               basePath,
                                                               numberToKeep,
                                                               keepPeriod);
        }
    }

    /**
     * To remove timestamped snapshot artifacts in repositories
     *
     * @param storageId    path of storage
     * @param numberToKeep the number of artifacts to keep
     * @param keepPeriod   the period to keep artifacts (the number of days)
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void removeTimestampedSnapshotArtifacts(String storageId,
                                                    int numberToKeep,
                                                    int keepPeriod)
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        Map<String, ImmutableRepository> repositories = getRepositories(storageId);

        repositories.forEach((repositoryId, repository) ->
                             {
                                 if (repository.getPolicy().equals(RepositoryPolicyEnum.SNAPSHOT.getPolicy()))
                                 {
                                     try
                                     {
                                        mavenRepositoryFeatures.removeTimestampedSnapshots(storageId,
                                                                                           repositoryId,
                                                                                           null,
                                                                                           numberToKeep,
                                                                                           keepPeriod);
                                     }
                                     catch (IOException e)
                                     {
                                         logger.error(e.getMessage(), e);
                                     }
                                 }
                             });
    }

    private Map<String, ImmutableStorage> getStorages()
    {
        return configurationManager.getConfiguration().getStorages();
    }

    private Map<String, ImmutableRepository> getRepositories(String storageId)
    {
        return getStorages().get(storageId).getRepositories();
    }

}
