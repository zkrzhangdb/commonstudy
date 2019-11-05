package com.ab.msp.agent.execution;

import com.ab.msp.agent.bridge.ServiceExporter;
import com.ab.msp.agent.bridge.ServicePublisher;
import com.ab.msp.agent.execution.metadata.ServiceMetadata;
import javassist.ClassPool;
import org.apache.axis.AxisEngine;
import org.apache.axis.WSDDEngineConfiguration;
import org.apache.axis.constants.Scope;
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDService;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.session.Session;

import java.util.ArrayList;
import java.util.List;

public class AxisServicePublisher implements ServicePublisher {
	private ClassPool  pool;
	private AxisEngine engine;
	private ServiceExporter callback;
	
	public AxisServicePublisher(){
		
	}
	
	public AxisServicePublisher(ClassPool pool, Object engine, ServiceExporter callback) {
		this.pool = pool;
		this.engine = (AxisEngine) engine;
		this.callback = callback;
	}
	
	public List<String> publish() throws Throwable {
		WSDDEngineConfiguration config = (WSDDEngineConfiguration) engine.getConfig();
		WSDDDeployment registry = config.getDeployment();
		List<String> serviceNames = new ArrayList<String>();
		for (WSDDService service : registry.getServices()) {
			SOAPService s = (SOAPService) service.getInstance(registry);
			Scope scope = Scope.getScope((String) s.getOption("scope"), Scope.DEFAULT);
			
			ServiceMetadata metadata = ServiceMetadata.getServiceMetadata(s, scope, pool);
			if (metadata == null) {
				continue;
			}
			
			String beanName = s.getName();
			Class<?> serviceClass = metadata.createInterface();
			Object serviceBean = createServiceInstance(metadata, serviceClass, s, scope, beanName);
			callback.export(beanName, serviceClass, serviceBean);
			serviceNames.add(serviceClass.getName());
		}
		return serviceNames;
	}
	
	private Object createServiceInstance(ServiceMetadata metadata, Class<?> serviceClass, SOAPService s, Scope scope, String beanName) throws Throwable {
		if (Scope.APPLICATION.equals(scope)) {
			Session session = engine.getApplicationSession();
			Object instance = session.get(beanName);
			if (instance == null) {
				instance = metadata.getSoapServiceClass().newInstance();
				session.set(beanName, instance);
			}
			return metadata.createImplObject(serviceClass, instance);
		}
		return metadata.createImplObject(serviceClass);
	} 
}
