package com.ab.msp.agent.execution;

import com.ab.msp.agent.bridge.ServiceCxfPublisher;
import com.ab.msp.agent.bridge.ServiceExporter;
import com.ab.msp.agent.execution.metadata.ServiceMetadata;
import javassist.ClassPool;
import org.apache.cxf.jaxws.support.JaxWsImplementorInfo;
import org.apache.cxf.jaxws.support.JaxWsServiceFactoryBean;

public class CxfServicePublisher implements ServiceCxfPublisher {

	private ClassPool  pool;
	private JaxWsServiceFactoryBean jaxWsServiceFactoryBean;
	private ServiceExporter callback;
	
	public CxfServicePublisher(){
		
	}
	
	public CxfServicePublisher(ClassPool pool, Object jaxWsServiceFactoryBean, ServiceExporter callback) {
		this.pool = pool;
		this.jaxWsServiceFactoryBean = (JaxWsServiceFactoryBean) jaxWsServiceFactoryBean;
		this.callback = callback;
	}
	
	public String publish() throws Throwable {

		JaxWsImplementorInfo jaxWsImplementorInfo = jaxWsServiceFactoryBean.getJaxWsImplementorInfo();
		Class<?> c = jaxWsImplementorInfo.getImplementorClass();
		
		ServiceMetadata metadata = ServiceMetadata.getCxfServiceMetadata(c, pool);
		if (metadata == null) {
			return null;
		}
		String beanName = c.getName();
		Class<?> serviceClass = metadata.createInterface();
		Object classBean = createServiceInstance(metadata, serviceClass, beanName);
		callback.export(beanName, serviceClass, classBean);
		
		return serviceClass.getName();
	}
	
	private Object createServiceInstance(ServiceMetadata metadata, Class<?> serviceClass, String beanName) throws Throwable {
		Object instance = metadata.getSoapServiceClass().newInstance();
		return metadata.createImplObject(serviceClass, instance);
	} 

}
