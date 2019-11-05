package com.ab.msp.agent.instrumentation;

import com.ab.msp.agent.bridge.ServiceExporter;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;

/**
 * 字节码插桩静态代理类.
 * Created by dayuanfeng on 2019/10/10.
 */
public class MspAgentInstrumentation implements ClassFileTransformer {
	public static final String INSTRUMNETATION_CONFIGURATION_MAP = "InstrumnetationConfigurationMap";
	public static final String INSTRUMNETATION_CONFIGURATION_YAML = "META-INF/instrumentation2.yaml";
	public static HashMap<String, Object> InstrumnetationConfigurationMap = new HashMap<String, Object>();
	private static ServiceExporter exporter;


	static {
		//解析yaml文件.
		Yaml yaml = new Yaml(new Constructor(com.ab.msp.agent.model.InstrumentationConfiguration.class));
		com.ab.msp.agent.model.InstrumentationConfiguration configuration;
		if (InstrumnetationConfigurationMap == null ||
				InstrumnetationConfigurationMap.size() < 1) {
			InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(INSTRUMNETATION_CONFIGURATION_YAML);
			if (inputStream == null) {
				configuration = new com.ab.msp.agent.model.InstrumentationConfiguration();
				configuration.setRegistryAddress("zookeeper");
				configuration.setTransferTo("dubbo");
			} else {
				configuration = (com.ab.msp.agent.model.InstrumentationConfiguration) yaml.load(inputStream);
				System.out.println(configuration);
			}
			InstrumnetationConfigurationMap.put(INSTRUMNETATION_CONFIGURATION_MAP, configuration);
		}
		configuration = (com.ab.msp.agent.model.InstrumentationConfiguration) InstrumnetationConfigurationMap.get(INSTRUMNETATION_CONFIGURATION_MAP);
		System.out.println("configuration = [" + configuration + "]");
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		// 屏蔽
		String target = "javax.servlet.http.HttpServlet";
		if (target.equals(className)) {
			Yaml yaml = new Yaml(new Constructor(com.ab.msp.agent.model.InstrumentationConfiguration.class));
			com.ab.msp.agent.model.InstrumentationConfiguration configuration;
			if (InstrumnetationConfigurationMap == null ||
					InstrumnetationConfigurationMap.size() < 1) {
				InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(INSTRUMNETATION_CONFIGURATION_YAML);
				if (inputStream == null) {
					configuration = new com.ab.msp.agent.model.InstrumentationConfiguration();
					configuration.setRegistryAddress("zookeeper");
					configuration.setTransferTo("dubbo");
				} else {
					configuration = (com.ab.msp.agent.model.InstrumentationConfiguration) yaml.load(inputStream);
					System.out.println(configuration);
				}
				InstrumnetationConfigurationMap.put(INSTRUMNETATION_CONFIGURATION_MAP, configuration);
			}
			configuration = (com.ab.msp.agent.model.InstrumentationConfiguration) InstrumnetationConfigurationMap.get(INSTRUMNETATION_CONFIGURATION_MAP);
			System.out.println("configuration = [" + configuration + "]");
		}
		return new byte[0];
	}

	public void setExporter(ServiceExporter delegate) {
		this.exporter = delegate;
	}
}
