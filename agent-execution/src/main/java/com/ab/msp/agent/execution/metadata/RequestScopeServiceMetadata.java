package com.ab.msp.agent.execution.metadata;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import java.lang.reflect.Method;
import java.util.List;

class RequestScopeServiceMetadata extends ServiceMetadata {
	public RequestScopeServiceMetadata(ClassPool pool, Class<?> c, List<Method> methods) throws Throwable {
		super(pool, c, methods);
	}

	@Override
	protected void createImplementFields(CtClass ctc) throws Throwable {
	}

	@Override
	protected void createImplementConstructor(CtClass ctc) throws Throwable {
		CtConstructor constructor = new CtConstructor(new CtClass[] {}, ctc);
		constructor.setBody("{}");
		ctc.addConstructor(constructor);
	}
	
	@Override
	protected Object createDelegate(Class<?> c, Object... args) throws Throwable {
		return c.getConstructor(new Class[] {}).newInstance();
	}

	@Override
	protected MethodMetadata methodMetadata(Class<?> soapServiceClass, CtMethod ctm, Method method) throws Throwable {
		return new RequestScopeMethodMetadata(soapServiceClass, ctm, method);
	}
	
	private static class RequestScopeMethodMetadata extends MethodMetadata {
		public RequestScopeMethodMetadata(Class<?> soapServiceClass, CtMethod ctm, Method method) throws Throwable {
			super(soapServiceClass, ctm, method);
		}
		
		@Override
		protected void createNonPublicBody(StringBuilder body) {
			String className = getSoapServiceClass().getName();
			body.append("Method _method = ").append(className).append(".class.getMethod(\"").append(getMethodName()).append("\", new Class<?>[] {");
			appendParameterTypes(body);
			body.append("});\n");
			
			body.append("_method.setAccessible(true);\n");
			body.append(className).append(" _delegate = new ").append(className).append("();\n");
			
			if (!getReturnType().isAssignableFrom(Void.class)) {
				body.append("return ");
			}
			body.append("_method.invoke(_delegate, new Object[] {");
			appendParameterNames(body);
			body.append("});");
		}

		@Override
		protected void createPublicBody(StringBuilder body) {
			String soapServiceName = getSoapServiceClass().getName();
			body.append(soapServiceName).append(" _delegate = new ").append(soapServiceName).append("();\n");
			
			if (!getReturnType().isAssignableFrom(Void.class)) {
				body.append("return ");
			}
			body.append("_delegate.").append(getMethodName()).append("(");
			appendParameterNames(body);
			body.append(");");
		}
	}
}
