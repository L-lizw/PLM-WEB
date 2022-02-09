package dyna.app.conf;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Lizw
 * @date 2022/2/9
 **/
@Configuration
@EnableDubbo(scanBasePackages = "dyna.app")
@PropertySource("classpath:/dubbo/dubbo-application.properties")
public class DataServerConnectConfig
{
}
