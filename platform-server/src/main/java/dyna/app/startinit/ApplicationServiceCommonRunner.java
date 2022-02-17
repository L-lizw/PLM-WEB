package dyna.app.startinit;

import dyna.app.conf.yml.ConfigurableServiceImpl;
import dyna.app.util.SpringUtil;
import dyna.common.conf.ServiceDefinition;
import dyna.common.log.DynaLogger;
import dyna.common.util.SetUtils;
import dyna.net.service.ApplicationService;
import org.apache.poi.hssf.record.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author Lizw
 * @date 2022/2/8
 **/

@Order(2)
@Component
public class ApplicationServiceCommonRunner implements CommandLineRunner
{
	@Autowired
	private ConfigurableServiceImpl configurableService;

	@Override public void run(String... args) throws Exception
	{
		Map<String, ApplicationService> serviceMap = SpringUtil.getApplicationContext().getBeansOfType(ApplicationService.class);
		if(!SetUtils.isNullMap(serviceMap))
		{
			Map<Integer,ApplicationService> orderServiceMap = new HashMap<>();


			for(ApplicationService service:serviceMap.values())
			{
				Class<? extends ApplicationService> clazz = service.getClass();
				Order order = clazz.getAnnotation(Order.class);
				if(order!=null)
				{
					int sequence = order.value();
					orderServiceMap.put(sequence, service);
					continue;
				}
				ServiceDefinition serviceDefinition = configurableService.getServiceDefinition(clazz.getSimpleName());
				DynaLogger.info(serviceDefinition.getDescription()+"init ...");
				service.init(serviceDefinition);
				DynaLogger.info(serviceDefinition.getDescription()+"init success");
			}

			if(!SetUtils.isNullMap(orderServiceMap))
			{
				List<Integer> orderList = new ArrayList<>(orderServiceMap.keySet());
				Collections.sort(orderList, new Comparator<Integer>()
				{
					@Override public int compare(Integer o1, Integer o2)
					{
						return o1-o2;
					}
				});

				for(Integer order:orderList)
				{
					ApplicationService service = orderServiceMap.get(order);
					ServiceDefinition serviceDefinition = configurableService.getServiceDefinition(service.getClass().getSimpleName());
					DynaLogger.info(serviceDefinition.getDescription()+"init ...");
					service.init(serviceDefinition);
					DynaLogger.info(serviceDefinition.getDescription()+"init success");
				}
			}
		}
	}
}
