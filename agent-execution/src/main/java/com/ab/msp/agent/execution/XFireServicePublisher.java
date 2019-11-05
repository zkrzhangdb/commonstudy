package com.ab.msp.agent.execution;

import com.ab.msp.agent.bridge.ServiceExporter;
import com.ab.msp.agent.bridge.ServiceXFirePublisher;
import com.ab.msp.agent.execution.metadata.ServiceMetadata;
import javassist.ClassPool;
import org.codehaus.xfire.spring.ServiceBean;

public class XFireServicePublisher implements ServiceXFirePublisher {

	private ClassPool  pool;
	private ServiceBean serviceBean;
	private ServiceExporter callback;
	
	
	public XFireServicePublisher(){
		
	}
	
	public XFireServicePublisher(ClassPool pool, Object serviceBean, ServiceExporter callback) {
		this.pool = pool;
		this.serviceBean = (ServiceBean) serviceBean;
		this.callback = callback;
	}
	
	@Override
	public String publish() throws Throwable {

		ServiceMetadata metadata = ServiceMetadata.getXFireServiceMetadata(serviceBean, pool);
		if (metadata == null) {
			return null;
		}
		String beanName = serviceBean.getServiceClass().getName();
		Class<?> serviceClass = metadata.createInterface();
		Object classBean = createServiceInstance(metadata, serviceClass, serviceBean, beanName);
		callback.export(beanName, serviceClass, classBean);
		
		return serviceClass.getName();
	}
	
	private Object createServiceInstance(ServiceMetadata metadata, Class<?> serviceClass, ServiceBean s, String beanName) throws Throwable {
		Object instance = metadata.getSoapServiceClass().newInstance();
		return metadata.createImplObject(serviceClass, instance);
	} 

}
