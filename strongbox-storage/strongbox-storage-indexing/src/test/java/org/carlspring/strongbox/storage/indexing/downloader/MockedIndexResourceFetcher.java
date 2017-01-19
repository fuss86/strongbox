package org.carlspring.strongbox.storage.indexing.downloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.index.updater.ResourceFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author carlspring
 */
public class MockedIndexResourceFetcher
        implements ResourceFetcher
{

    private static final Logger logger = LoggerFactory.getLogger(MockedIndexResourceFetcher.class);

    private String storageId;

    private String repositoryId;


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

        File indexBaseDir = new File("target/strongbox-vault/storages/" + storageId + "/" + repositoryId + "/.index");
        File indexResourceFile = new File(indexBaseDir, name);

        logger.debug("indexResourceFile: " + indexResourceFile.getAbsolutePath());

        return new FileInputStream(indexResourceFile);
    }

}