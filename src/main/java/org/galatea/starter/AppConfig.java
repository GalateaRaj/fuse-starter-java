package org.galatea.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Logger;
import lombok.extern.slf4j.Slf4j;
import net.sf.aspect4log.aspect.LogAspect;
import org.galatea.starter.domain.SettlementMission;
import org.galatea.starter.service.IAgreementTransformer;
import org.galatea.starter.service.PriceFinderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
@EnableAspectJAutoProxy
@EnableCaching
@EnableFeignClients
public class AppConfig {



  /**
   * create an RestTemplate to use with @Autowire
   */
  @Bean
  public RestTemplate restTemplate() { return new RestTemplate(); }

  /**
   * create an ObjectMapper to use with @Autowire
   */
  @Bean
  public ObjectMapper objectMapper() { return new ObjectMapper(); }

  /**
   * Create a LogAspect for use with the SpringAOP @Log annotation.
   */
  @Bean
  public LogAspect createLogAspect() {
    return new LogAspect();
  }

  /**
   * Returns an anonymous class implementing the IAgreementTransformer interface. Demonstrates the
   * use of a lambda function which can stand in as an anonymous class with a single method:
   * https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html
   */
  @Bean
  public IAgreementTransformer agreementTransformer() {
    return agreement -> SettlementMission.builder().instrument(agreement.getInstrument())
        .externalParty(agreement.getExternalParty()).depot("DTC").qty(agreement.getQty())
        .direction("B".equals(agreement.getBuySell()) ? "REC" : "DEL").version(0L).build();
  }

  /**
   * CacheManager that contains the Cache Configuration. Spring will use this cache if any
   * cache annotations are used.
   */
  @Bean
  public CacheManager cacheManager(final EhCacheManagerFactoryBean ehCacheCacheManagerFactoryBean) {
    return new EhCacheCacheManager(ehCacheCacheManagerFactoryBean.getObject());
  }

  /**
   * Creates factory bean for cache manager using a cache config file.
   *
   * @return factory bean for the EhCache to be passed to EhCacheCacheManager.
   */
  @Bean
  public EhCacheManagerFactoryBean ehCacheCacheManagerFactoryBean(
          @Value("${cache-config}") final String cacheConfig) {
    EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
    cmfb.setConfigLocation(new ClassPathResource(cacheConfig));
    cmfb.setShared(true);
    return cmfb;
  }

  /**
   * Set the Feign log level for interfaces annotated with @FeignClient.
   *
   * @return the Feign log level.
   */
  @Bean
  public Logger.Level logLevel() {
    return Logger.Level.BASIC;
  }

}
