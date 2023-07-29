package com.exxeta.userservice.config;

import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {
  @Bean
  public ConfigurableServletWebServerFactory tomcatCustomizer() {
    TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
    Http2Protocol http2Protocol = new Http2Protocol();
    http2Protocol.setMaxConcurrentStreams(500);
    factory.addConnectorCustomizers(connector -> connector.addUpgradeProtocol(http2Protocol));
    return factory;
  }
}
