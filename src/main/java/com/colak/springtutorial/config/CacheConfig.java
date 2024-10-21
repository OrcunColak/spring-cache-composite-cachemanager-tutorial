package com.colak.springtutorial.config;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.support.CompositeCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheManager redisCacheManager = createRedisCacheManager(redisConnectionFactory);
        JCacheCacheManager ehCacheManager = createEhCacheManager();

        // Create CompositeCacheManager with both JCacheCacheManager (EhCache) and RedisCacheManager
        CompositeCacheManager compositeCacheManager = new CompositeCacheManager(ehCacheManager, redisCacheManager);
        // Fallback to no-op cache if both caches are unavailable
        compositeCacheManager.setFallbackToNoOpCache(true);
        return compositeCacheManager;
    }

    private RedisCacheManager createRedisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Configure the Redis cache
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // Cache entry expiration time
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(redisConnectionFactory))
                .cacheDefaults(redisCacheConfiguration)
                .build();
    }

    private JCacheCacheManager createEhCacheManager() {
        // Configure the EhCache using JCache API
        CachingProvider cachingProvider = Caching.getCachingProvider();
        javax.cache.CacheManager ehCacheManager = cachingProvider.getCacheManager();

        // Create EhCache configuration
        ehCacheManager.createCache("employees", Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Object.class, Object.class, ResourcePoolsBuilder.heap(100))
                        .build())
        );

        return new JCacheCacheManager(ehCacheManager);
    }
}
