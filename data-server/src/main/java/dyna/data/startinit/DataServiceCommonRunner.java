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

@Order(3) @Component public class DataServiceCommonRunner implements CommandLineRunner
{
	private static final String SCAN_BEAN_ANNOTATION_PACKAGE = "dyna.data.service";

	@Autowired private ConfigurableServiceImpl configurableService;

	@Override public void run(String... args) throws Exception
	{
		Set<Class<?>> set = PackageScanUtil.findImplementationByInterface(SCAN_BEAN_ANNOTATION_PACKAGE, Service.class);

		if (!SetUtils.isNullSet(set))
		{
			Map<Integer, String> orderServiceMap = new HashMap<>();
			Map<String, Class<?>> idServiceMap = new HashMap<>();

			for (Class<?> serviceClass : set)
			{
				if (serviceClass.isInterface() || Modifier.isAbstract(serviceClass.getModifiers()))
				{
					continue;
				}

				ServiceDefinition serviceDefinition = configurableService.getServiceDefinitionByclass(serviceClass.getSimpleName());

				Service service = (Service) SpringUtil.getBean(serviceClass);
				Order order = serviceClass.getAnnotation(Order.class);
				if (order != null)
				{
					int sequence = order.value();
					orderServiceMap.put(sequence, serviceDefinition.getId());
				}
				idServiceMap.put(serviceDefinition.getId(), serviceClass);
			}

			if (!SetUtils.isNullMap(orderServiceMap))
			{
				List<Integer> orderList = new ArrayList<>(orderServiceMap.keySet());
				Collections.sort(orderList, new Comparator<Integer>()
				{
					@Override public int compare(Integer o1, Integer o2)
					{
						return o1 - o2;
					}
				});

				for (Integer order : orderList)
				{
					this.initDataService(idServiceMap.get(orderServiceMap.get(order)));
					idServiceMap.remove(orderServiceMap.get(order));
				}
			}

			if (!SetUtils.isNullMap(idServiceMap))
			{
				idServiceMap.forEach((id, serviceClass) -> {
					this.initDataService(serviceClass);
				});
			}
		}
		DynaLogger.info("Data Server is ready!");
	}

	private void initDataService(Class<?> serviceClass)
	{
		Service service = (Service) SpringUtil.getBean(serviceClass);
		ServiceDefinition serviceDefinition = configurableService.getServiceDefinitionByclass(serviceClass.getSimpleName());
		DynaLogger.info(serviceDefinition.getDescription() + " init ...");
		service.init();
		DynaLogger.info(serviceDefinition.getDescription() + " init success");
	}
}
