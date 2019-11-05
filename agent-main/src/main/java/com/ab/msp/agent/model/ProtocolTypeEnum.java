package com.ab.msp.agent.model;

import com.ab.msp.agent.bridge.ServiceExporter;
import javassist.ClassPool;
import javassist.NotFoundException;

/**
 * Created by dayuanfeng on 2019/11/5.
 */
public enum ProtocolTypeEnum implements ServiceExporter{
	DUBBO{
		@Override
		public void export(String serviceName, Class<?> serviceClass, Object serviceBean) {
			System.out.println("export DUBBO ");
		}

		@Override
		public void unexport(String serviceName) {
		}

		@Override
		public ClassPool getClassPool(ClassLoader loader) throws NotFoundException {
			return null;
		}
	},
	SPRINGCLOUD{
		@Override
		public void export(String serviceName, Class<?> serviceClass, Object serviceBean) {
			System.out.println("export SPRINGCLOUD ");

		}

		@Override
		public void unexport(String serviceName) {

		}

		@Override
		public ClassPool getClassPool(ClassLoader loader) throws NotFoundException {
			return null;
		}
	}
}
