package com.ab.msp.agent.bridge;

import javassist.ClassPool;
import javassist.NotFoundException;

public interface ServiceExporter {
	void export(String serviceName, Class<?> serviceClass, Object serviceBean);
	void unexport(String serviceName);
	
	ClassPool getClassPool(ClassLoader loader) throws NotFoundException;
}
