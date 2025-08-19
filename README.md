# Read Me First

Original idea is from  
https://medium.com/@pavanapriya.u/implementing-a-two-level-cache-with-spring-boot-86a681e942ae

# Caffeine + Redis

The original idea is from  
https://medium.com/@ShantKhayalian/supercharging-spring-boot-apps-with-caffeine-redis-caching-strategies-f97a939142ff

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=5m

spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
```

code

```java

@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        CaffeineCacheManager caffeine = new CaffeineCacheManager("prices");
        caffeine.setCaffeine(Caffeine.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES));
        RedisCacheManager redis = RedisCacheManager.create(redisConnectionFactory);
        return new CompositeCacheManager(caffeine, redis);
    }
}

```
