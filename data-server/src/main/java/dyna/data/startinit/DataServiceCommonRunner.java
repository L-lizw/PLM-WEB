package dyna.data.startinit;


import dyna.common.conf.ServiceDefinition;
import dyna.common.log.DynaLogger;
import dyna.common.util.PackageScanUtil;
import dyna.common.util.SetUtils;
import dyna.data.common.util.SpringUtil;
import dyna.data.conf.yml.ConfigurableServiceImpl;
import dyna.net.service.ApplicationService;
import dyna.net.service.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Lizw
 * @date 2022/2/8
 **/

@Order(2)
@Component
public class DataServiceCommonRunner implements CommandLineRunner
{
	private static final String SCAN_BEAN_ANNOTATION_PACKAGE = "dyna.data.service";

	@Autowired
	private ConfigurableServiceImpl configurableService;

	@Override public void run(String... args) throws Exception
	{
		Set<Class<?>> set = PackageScanUtil.findImplementationByInterface(SCAN_BEAN_ANNOTATION_PACKAGE, Service.class);

		if(!SetUtils.isNullSet(set))
		{
			Map<Integer, Class<?>> orderServiceMap = new HashMap<>();

			for(Class<?> serviceClass:set)
			{
				if(serviceClass.isInterface() || Modifier.isAbstract(serviceClass.getModifiers()))
				{
					continue;
				}

				ServiceDefinition serviceDefinition = configurableService.getServiceDefinitionByclass(serviceClass.getSimpleName());

				ApplicationService service = (ApplicationService) SpringUtil.getBean(serviceClass);
				Order order = serviceClass.getAnnotation(Order.class);
				if(order!=null)
				{
					int sequence = order.value();
					orderServiceMap.put(sequence, serviceClass);
					continue;
				}
				DynaLogger.info(serviceDefinition.getDescription()+" init ...");
				service.init(serviceDefinition);
				DynaLogger.info(serviceDefinition.getDescription()+" init success");
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
					ApplicationService service = (ApplicationService) SpringUtil.getBean(orderServiceMap.get(order));
					ServiceDefinition serviceDefinition = configurableService.getServiceDefinitionByclass(orderServiceMap.get(order).getSimpleName());
					DynaLogger.info(serviceDefinition.getDescription()+" init ...");
					service.init(serviceDefinition);
					DynaLogger.info(serviceDefinition.getDescription()+" init success");
				}
			}
		}
	}
}
