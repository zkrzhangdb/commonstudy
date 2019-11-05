package com.ab.msp.agent;


import com.ab.msp.agent.utils.AgentUtils;
import rx.Observable;
import rx.functions.Action1;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.jar.JarFile;

import static com.ab.msp.agent.utils.AgentUtils.doWithFiles;
import static com.ab.msp.agent.utils.AgentUtils.toURLs;


public class MspAgent {
	public static final String DUBBO_ZOOKEEPER = "dubbo_zookeeper_location".toUpperCase();
	public static final String DUBBO_SERVEPORT = "dubbo_protocol_port".toUpperCase();
	public static final String DUBBO_TIMEOUT = "dubbo_provider_timeout".toUpperCase();

	public static URL WEBPPPJAR;

	public static void premain(String args, Instrumentation instrumentation) throws Exception {
		System.out.println("*********  进来premain方法了   *****************");

		validateDubboEnvvars();

		File home = getHome();
		appendShareLibsToSystem(home, instrumentation);

		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			ClassLoader loader = new URLClassLoader(toURLs(new File(home, "lib")), ClassLoader.getSystemClassLoader());
			Thread.currentThread().setContextClassLoader(loader);
			startSpringApplication(loader);
		} finally {
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}
		instrumentation.addTransformer(new com.ab.msp.agent.instrumentation.MspAgentInstrumentation());
	}
	
	/*private static Path getHome() throws MalformedURLException {
		Path path = new File(MspAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath()).toPath();
		Path home = path.getParent();
		
		String jarName = path.toFile().getName().toLowerCase().replace("main", "webapp");
		WEBPPPJAR = path.resolveSibling(jarName).toUri().toURL();
		return home;
	}*/

	private static File getHome() throws MalformedURLException {
		//Path path = new File(MspAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath()).toPath();
		//Path home = path.getParent();

		File path = new File(MspAgent.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		File home = path.getParentFile();

		String jarName = path.getName().toLowerCase().replace("main", "webapp");
		WEBPPPJAR = new File(home, jarName).toURL();
		return home;
	}

	private static void validateDubboEnvvars() {
		for (String name : Arrays.asList(DUBBO_ZOOKEEPER, DUBBO_SERVEPORT, DUBBO_TIMEOUT)) {
			String variable = System.getProperty(name);
			if (variable == null || variable.isEmpty()) {
				variable = System.getenv(name);
				if (variable == null || variable.isEmpty()) {
					throw new RuntimeException("System variable \"" + name + "\" is not set.");
				}
			}
		}
	}
	
	/*private static void appendShareLibsToSystem(Path home, final Instrumentation instrumentation) throws IOException {
		doWithFiles(new FileCallback() {
			@Override
			public void call(File f) {
				try {
					instrumentation.appendToSystemClassLoaderSearch(new JarFile(f));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, home.resolve("share").toFile());
	}*/

	private static void appendShareLibsToSystem(File home, final Instrumentation instrumentation) throws IOException {
		doWithFiles(new AgentUtils.FileCallback() {
			@Override
			public void call(File f) {
				try {
					instrumentation.appendToSystemClassLoaderSearch(new JarFile(f));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, new File(home, "share"));
	}

	private static void startSpringApplication(ClassLoader agentLoader) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		Class<?> contextClass = agentLoader.loadClass("org.springframework.context.support.ClassPathXmlApplicationContext");
		Object obj = contextClass.getConstructor(String[].class).newInstance(new Object[]{new String[]{"classpath:/dubboContext.xml"}});

	}

	public static void main(String[] args) {
		hello("Ben","Grouge");
//		Yaml yaml = new Yaml(new Constructor(InstrumentationConfiguration.class));
//		File home = null;
//		try {
//			home = getHome();
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		}
//
//		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
//		try {
//			ClassLoader loader = new URLClassLoader(toURLs(new File(home, "lib")), ClassLoader.getSystemClassLoader());
//			Thread.currentThread().setContextClassLoader(loader);
//			InstrumentationConfiguration configuration;
//			if (InstrumnetationConfigurationMap == null ||
//					InstrumnetationConfigurationMap.size() < 1) {
//				InputStream inputStream = loader.getResourceAsStream(INSTRUMNETATION_CONFIGURATION_YAML);
//				if (inputStream == null) {
//					configuration = new InstrumentationConfiguration();
//					configuration.setRegistryAddress("zookeeper");
//					configuration.setTransferTo("dubbo");
//				} else {
//					configuration = (InstrumentationConfiguration) yaml.load(inputStream);
//					System.out.println(configuration);
//				}
//				InstrumnetationConfigurationMap.put(INSTRUMNETATION_CONFIGURATION_MAP, configuration);
//			}
//			configuration = (InstrumentationConfiguration) InstrumnetationConfigurationMap.get(INSTRUMNETATION_CONFIGURATION_MAP);
//			System.out.println("configuration" + configuration);
//		} finally {
//			Thread.currentThread().setContextClassLoader(oldClassLoader);
//		}
	}

	public static void hello(String... items){
		Observable.from(items).subscribe(new Action1() {

			@Override
			public void call(Object o) {
				System.out.println("o = [" + o + "]");
			}
		});
	}
}
