package com.ab.msp.agent.protocol.dubbo;

import com.ab.msp.agent.bridge.ServiceExporter;
import com.alibaba.dubbo.common.bytecode.ClassGenerator;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.config.spring.ServiceBean;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DubboServiceExporter implements ServiceExporter, ApplicationContextAware {
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void export(String serviceName, Class<?> serviceClass, Object serviceBean) {
		AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
		ServiceConfig serviceConfig = (ServiceConfig) beanFactory.createBean(ServiceBean.class);
		((SingletonBeanRegistry) beanFactory).registerSingleton(serviceName, serviceConfig);
		beanFactory.autowireBean(serviceConfig);
		
		serviceConfig.setInterface(serviceClass);
		serviceConfig.setPath(serviceClass.getName());
		serviceConfig.setRef(serviceBean);

/*		// 当前应用配置
		ApplicationConfig application = new ApplicationConfig();
		application.setName("cx");

// 连接注册中心配置
		RegistryConfig registry = new RegistryConfig();
		registry.setAddress("10.20.130.230:9090");

// 服务提供者协议配置
		ProtocolConfig protocol = new ProtocolConfig();
		protocol.setName("dubbo");
		protocol.setPort(20880);
		protocol.setThreads(200);

		serviceConfig.setApplication(application);
		serviceConfig.setRegistry(registry); // 多个注册中心可以用setRegistries()
		serviceConfig.setProtocol(protocol); // 多个协议可以用setProtocols()
		serviceConfig.setVersion("1.0.0");*/

		serviceConfig.export();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void unexport(String serviceName) {
		ServiceConfig config = applicationContext.getBean(serviceName, ServiceBean.class);
		if (config != null) {
			config.unexport();
		}
	}

	@Override
	public ClassPool getClassPool(ClassLoader loader) throws NotFoundException {
		return ClassGenerator.getClassPool(loader);
	}
}
