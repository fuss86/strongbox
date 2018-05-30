package org.carlspring.strongbox.storage.indexing.downloader;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.ImmutableConfiguration;
import org.carlspring.strongbox.storage.repository.ImmutableRepository;
import org.carlspring.strongbox.storage.repository.remote.ImmutableRemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.index.updater.ResourceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class MockedIndexResourceFetcher
        implements ResourceFetcher
{

    private static final Logger logger = LoggerFactory.getLogger(MockedIndexResourceFetcher.class);

    private String storageId;

    private String repositoryId;

    @Inject
    private ConfigurationManager configurationManager;


    @Override
    public void connect(String repositoryId, String url)
            throws IOException
    {
        // In the real-world this would be just the repositoryId, but in the mock we're sending storageId:repositoryId

        String[] split = repositoryId.split(":");

        this.storageId = split[0];
        this.repositoryId = split[1];

        logger.debug("storageId:    " + this.storageId);
        logger.debug("repositoryId: " + this.repositoryId);
    }

    @Override
    public void disconnect()
            throws IOException
    {
    }

    @Override
    public InputStream retrieve(String name)
            throws IOException
    {
        logger.debug("Requesting index from " + name + "...");

        ImmutableRepository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);
        ImmutableRemoteRepository remoteRepository = repository.getRemoteRepository();

        String remoteUrl = remoteRepository.getUrl().substring(0, remoteRepository.getUrl().length() -
                                                                  (remoteRepository.getUrl().endsWith("/") ? 1 : 0));

        String subPath = remoteUrl.substring(remoteUrl.indexOf("/storages/") + 10, remoteUrl.length());

        String remoteStorageId = subPath.substring(0, subPath.indexOf('/'));
        String remoteRepositoryId = subPath.substring(subPath.lastIndexOf('/') + 1, subPath.length());

        File indexBaseDir = new File("target/strongbox-vault/storages/" + remoteStorageId + "/" + remoteRepositoryId +
                                     "/.index/local");
        File indexResourceFile = new File(indexBaseDir, name);

        logger.debug("indexResourceFile: " + indexResourceFile.getAbsolutePath());

        return new FileInputStream(indexResourceFile);
    }

    public ImmutableConfiguration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
