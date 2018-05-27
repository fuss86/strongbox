package org.carlspring.strongbox.storage.repository.remote.heartbeat;

import org.carlspring.strongbox.storage.repository.remote.ImmutableRemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.monitor.RemoteRepositoryHeartbeatMonitorStrategy;

import javax.annotation.Nonnull;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Przemyslaw Fusik
 */
class RemoteRepositoryHeartbeatMonitor
        implements Runnable
{

    private static final Logger logger = LoggerFactory.getLogger(RemoteRepositoryHeartbeatMonitor.class);

    private final ImmutableRemoteRepository remoteRepository;

    private final RemoteRepositoryAlivenessCacheManager remoteRepositoryCacheManager;

    private final RemoteRepositoryHeartbeatMonitorStrategy monitorStrategy;

    RemoteRepositoryHeartbeatMonitor(@Nonnull RemoteRepositoryAlivenessCacheManager remoteRepositoryCacheManager,
                                     @Nonnull RemoteRepositoryHeartbeatMonitorStrategy monitorStrategy,
                                     @Nonnull ImmutableRemoteRepository remoteRepository)
    {
        Objects.requireNonNull(remoteRepositoryCacheManager);
        Objects.requireNonNull(monitorStrategy);
        Objects.requireNonNull(remoteRepository);

        this.remoteRepositoryCacheManager = remoteRepositoryCacheManager;
        this.monitorStrategy = monitorStrategy;
        this.remoteRepository = remoteRepository;
    }

    @Override
    public void run()
    {
        boolean isAlive = false;
        try
        {
            isAlive = monitorStrategy.isAlive(remoteRepository.getUrl());
        }
        catch (Exception ex)
        {
            logger.error("Problem determining remote repository [" + remoteRepository.getUrl() + "] aliveness", ex);
        }

        logger.debug("Thread name is [{}]. Remote repository [{}] is alive ? [{}]", Thread.currentThread().getName(),
                     remoteRepository.getUrl(),
                     isAlive);
        remoteRepositoryCacheManager.put(remoteRepository, isAlive);
    }
}
