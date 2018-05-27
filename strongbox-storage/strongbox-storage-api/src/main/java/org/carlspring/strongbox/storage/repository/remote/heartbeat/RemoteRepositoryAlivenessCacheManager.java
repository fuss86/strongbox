package org.carlspring.strongbox.storage.repository.remote.heartbeat;

import org.carlspring.strongbox.storage.repository.remote.ImmutableRemoteRepository;

import javax.inject.Inject;
import java.util.Objects;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RemoteRepositoryAlivenessCacheManager
        implements DisposableBean
{

    private final Cache cache;

    @Inject
    RemoteRepositoryAlivenessCacheManager(CacheManager cacheManager)
    {
        cache = cacheManager.getCache("remoteRepositoryAliveness");
        Objects.requireNonNull(cache, "remoteRepositoryAliveness cache configuration was not provided");
    }

    public boolean isAlive(ImmutableRemoteRepository remoteRepository)
    {
        return BooleanUtils.isNotFalse(cache.get(remoteRepository.getUrl(), Boolean.class));
    }

    public void put(ImmutableRemoteRepository remoteRepository,
                    boolean aliveness)
    {
        cache.put(remoteRepository.getUrl(), Boolean.valueOf(aliveness));
    }

    @Override
    public void destroy()
            throws Exception
    {
        cache.clear();
    }
}
