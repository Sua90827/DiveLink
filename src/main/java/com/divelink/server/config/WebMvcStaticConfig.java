package com.divelink.server.config;

import com.divelink.server.storage.StorageProperties;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcStaticConfig implements WebMvcConfigurer {
  private final StorageProperties props;
  public WebMvcStaticConfig(StorageProperties props) { this.props = props; }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    if ("local".equals(props.getType())) {
      Path base = Paths.get(props.getLocal().getBasePath()).toAbsolutePath();
      registry.addResourceHandler("/files/**")
          .addResourceLocations("file:" + base.toString() + "/")
          .setCachePeriod(3600);
    }
  }
}
