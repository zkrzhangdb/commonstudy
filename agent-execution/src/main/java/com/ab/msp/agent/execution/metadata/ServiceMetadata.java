package com.ab.msp.agent.execution.metadata;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.apache.axis.AxisEngine;
import org.apache.axis.constants.Scope;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.utils.cache.ClassCache;
import org.codehaus.xfire.spring.ServiceBean;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodCallback;
import org.springframework.util.ReflectionUtils.MethodFilter;

import javax.jws.WebMethod;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.ab.msp.agent.utils.AgentUtils.getCtClass;
import static com.ab.msp.agent.utils.AgentUtils.getCtClasses;

public abstract class ServiceMetadata {
	private static final ConcurrentMap<SOAPService, Holder<ServiceMetadata>> serviceMetadatas = 
			new ConcurrentHashMap<SOAPService, Holder<ServiceMetadata>>();
	
	private static final ConcurrentMap<ServiceBean, Holder<ServiceMetadata>> serviceMetadatasForXFire = 
			new ConcurrentHashMap<ServiceBean, Holder<ServiceMetadata>>();
	
	private static final ConcurrentMap<Class<?>, Holder<ServiceMetadata>> serviceMetadatasForCxf = 
			new ConcurrentHashMap<Class<?>, Holder<ServiceMetadata>>();

	public static ServiceMetadata getServiceMetadata(SOAPService s, Object scope, ClassPool pool) throws Throwable {
		Holder<ServiceMetadata> metadataHolder = serviceMetadatas.get(s);
		if (metadataHolder == null) {
			ClassCache cache = s.getEngine().getClassCache();
			String className = (String) s.getOption("className");
			Class<?> c = cache.lookup(className, AxisEngine.class.getClassLoader()).getJavaClass();
			
			final Holder<List<Method>> holder = new Holder<List<Method>>(new ArrayList<Method>());
			ReflectionUtils.doWithMethods(c, 
				new MethodCallback() {
					public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
						holder.getValue().add(method);
					}
				},
				new MethodFilter() {
					public boolean matches(Method method) {
						return !java.lang.reflect.Modifier.isStatic(method.getModifiers()) && 
								method.isAnnotationPresent(WebMethod.class);
					}
			});
			
			metadataHolder = new Holder<ServiceMetadata>(null);
			
			List<Method> methods = holder.getValue();
			if (!methods.isEmpty()) {
				if (scope.equals(Scope.APPLICATION)) {
					metadataHolder.setValue(new GlobalScopeServiceMetadata(pool, c, methods));
				} else if (scope.equals(Scope.REQUEST)) {
					metadataHolder.setValue(new RequestScopeServiceMetadata(pool, c, methods));
				}
			}
			serviceMetadatas.put(s, metadataHolder);
		}
		return metadataHolder.getValue();
	}
	
	public static ServiceMetadata getXFireServiceMetadata(ServiceBean s, ClassPool pool) throws Throwable {
		Holder<ServiceMetadata> metadataHolder = serviceMetadatasForXFire.get(s);
		if (metadataHolder == null) {
			
			Class<?> c = s.getServiceBean().getClass();
			
			final Holder<List<Method>> holder = new Holder<List<Method>>(new ArrayList<Method>());
			ReflectionUtils.doWithMethods(c, 
				new MethodCallback() {
					public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
						holder.getValue().add(method);
					}
				},
				new MethodFilter() {
					public boolean matches(Method method) {
						return !java.lang.reflect.Modifier.isStatic(method.getModifiers()) && 
								method.isAnnotationPresent(WebMethod.class);
					}
			});
			
			metadataHolder = new Holder<ServiceMetadata>(null);
			List<Method> methods = holder.getValue();
			if (!methods.isEmpty()) {
				metadataHolder.setValue(new GlobalScopeServiceMetadata(pool, c, methods));
			}
			serviceMetadatasForXFire.put(s, metadataHolder);
		}
		return metadataHolder.getValue();
	}
	
	public static ServiceMetadata getCxfServiceMetadata(Class<?> clazz, ClassPool pool) throws Throwable {
		Holder<ServiceMetadata> metadataHolder = serviceMetadatasForCxf.get(clazz);
		if (metadataHolder == null) {
			Class<?> c = clazz;
			final Holder<List<Method>> holder = new Holder<List<Method>>(new ArrayList<Method>());
			ReflectionUtils.doWithMethods(c, 
				new MethodCallback() {
					public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
						holder.getValue().add(method);
					}
				},
				new MethodFilter() {
					public boolean matches(Method method) {
						return !java.lang.reflect.Modifier.isStatic(method.getModifiers()) && 
								method.isAnnotationPresent(WebMethod.class);
					}
			});
			
			metadataHolder = new Holder<ServiceMetadata>(null);
			
			List<Method> methods = holder.getValue();
			if (!methods.isEmpty()) {
				metadataHolder.setValue(new GlobalScopeServiceMetadata(pool, c, methods));
			}
			serviceMetadatasForCxf.put(clazz, metadataHolder);
		}
		return metadataHolder.getValue();
	}
	
	private List<MethodMetadata> methodMetadatas;
	private Class<?>  	soapServiceClass;
	private ClassPool 	classPool;
	
	protected ServiceMetadata(ClassPool pool, Class<?> c, List<Method> methods) throws Throwable {
		this.classPool = pool;
		this.soapServiceClass = c;
		
		this.methodMetadatas = new ArrayList<MethodMetadata>();
		Map<Method, CtMethod> ctms = getCtMethods(this.soapServiceClass, methods);
		
		for (Entry<Method, CtMethod> e : ctms.entrySet()) {
			methodMetadatas.add(methodMetadata(this.soapServiceClass, e.getValue(), e.getKey()));
		}
	}
	
	private Map<Method, CtMethod> getCtMethods(Class<?> c, List<Method> methods) throws NotFoundException {
		Map<Method, CtMethod> ctms = new HashMap<Method, CtMethod>();
		while (!methods.isEmpty() && c != Object.class) {
			CtClass ctc = getCtClass(classPool, c);
			try {
				Iterator<Method> it = methods.iterator();
				while (it.hasNext()) {
					try {
						Method method = it.next();
						ctms.put(method, getCtMethod(ctc, method));
						it.remove();
					} catch (NotFoundException e) {
						continue;
					}
				}
			} finally {
				ctc.detach();
			}
			c = c.getSuperclass();
		}
		return ctms;
	}
	
	private CtMethod getCtMethod(CtClass ctc, Method m) throws NotFoundException {
		return ctc.getDeclaredMethod(m.getName(), getCtClasses(classPool, m.getParameterTypes()));
	}

	protected final List<MethodMetadata> getMethodMetadatas() {
		return methodMetadatas;
	}

	protected final ClassPool getClassPool() {
		return classPool;
	}
	
	public Class<?> getSoapServiceClass() {
		return soapServiceClass;
	}
	
	protected abstract MethodMetadata methodMetadata(Class<?> soapServiceClass, CtMethod ctm, Method method) throws Throwable;
	
	public final Class<?> createInterface() throws Throwable {
		classPool.makePackage(classPool.getClassLoader(), soapServiceClass.getPackage().getName());
		classPool.appendClassPath(new ClassClassPath(soapServiceClass));
		CtClass interfaceClass = classPool.makeInterface(soapServiceClass.getName().concat("Service"));

		for (MethodMetadata mmd : this.methodMetadatas) {
			createMethod(interfaceClass, mmd);
		}
		
		System.out.println("********  生成接口"+interfaceClass.getName()+"成功      **********");
		/*try {
			interfaceClass.writeFile("C:/Users/freed/Desktop/javassist");
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}*/
		return interfaceClass.toClass();
	};
	
	protected final CtMethod createMethod(CtClass ctc, MethodMetadata methodMetadata) throws CannotCompileException, NotFoundException {
		return createMethod(ctc, methodMetadata, null);
	}
	
	protected final CtMethod createMethod(CtClass ctc, MethodMetadata methodMetadata, CharSequence src) throws CannotCompileException, NotFoundException {
		StringBuilder builder = new StringBuilder("public ").append(methodMetadata.getReturnType().getName()).
				append(" ").append(methodMetadata.getMethodName()).append("(");
		methodMetadata.appendDeclaredParameters(builder);
		builder.append(") ");
		methodMetadata.appendThrowableTypes(builder).append(";");
		CtMethod ctm = CtMethod.make(builder.toString(), ctc);
		
		if (src != null && src.length() > 0) {
			ctm.setBody(src.toString());
		}
		ctc.addMethod(ctm);
		return ctm;
	}
	
	public final Object createImplObject(Class<?> c, Object... args) throws Throwable {
		CtClass ctc = classPool.makeClass(c.getName() + "Bean");
		CtClass interfaceCtClass = classPool.getCtClass(c.getName());
		ctc.addInterface(interfaceCtClass);
		
		createImplementFields(ctc);
		createImplementConstructor(ctc);
		for (MethodMetadata mmd : this.methodMetadatas) {
			createMethod(ctc, mmd, mmd.createBody());
		}
		
		System.out.println("********  生成接口实现类"+ctc.getName()+"成功      **********");
		/*try {
			ctc.writeFile("C:/Users/freed/Desktop/javassist");
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}*/
		return createDelegate(ctc.toClass(), args);
	};
	
	protected abstract void createImplementFields(CtClass ctc) throws Throwable;
	protected abstract void createImplementConstructor(CtClass ctc) throws Throwable;
	protected abstract Object createDelegate(Class<?> c, Object... args) throws Throwable;

	private static class Holder<T> {
		private T value;

		public Holder(T value) {
			this.value = value;
		}

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}
	}
}
