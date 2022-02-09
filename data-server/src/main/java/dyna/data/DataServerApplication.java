package dyna.data;

import dyna.data.conf.MybatisConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * 数据服务器主入口
 *
 * @author Lizw
 * @date 2022/1/16
 **/
@SpringBootApplication
@Import(MybatisConfig.class)
@ComponentScan(basePackages = {"dyna.common","dyna.data"})
public class DataServerApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(DataServerApplication.class, args);
	}
}
