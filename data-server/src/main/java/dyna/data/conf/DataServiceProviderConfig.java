package dyna.data.conf;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Lizw
 * @date 2022/1/20
 **/
@Configuration
@EnableDubbo(scanBasePackages = "dyna.data.service")
@PropertySource("classpath:/dubbo/dubbo-provider.properties")
public class DataServiceProviderConfig
{
}
