package com.ab.msp.agent.execution.metadata;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;

import java.lang.reflect.Method;
import java.util.List;

import static com.ab.msp.agent.utils.AgentUtils.getCtClass;

class GlobalScopeServiceMetadata extends ServiceMetadata {
	public GlobalScopeServiceMetadata(ClassPool pool, Class<?> c, List<Method> methods) throws Throwable {
		super(pool, c, methods);
	}
	
	@Override
	protected void createImplementFields(CtClass ctc) throws Throwable {
		StringBuilder builder = new StringBuilder("private ").append(getSoapServiceClass().getName()).append(" _delegate;");
		CtField field = CtField.make(builder.toString(), ctc);
		ctc.addField(field);
	}

	@Override
	protected void createImplementConstructor(CtClass ctc) throws Throwable {
		ClassPool pool = ctc.getClassPool();
		CtConstructor constructor = new CtConstructor(new CtClass[] {getCtClass(pool, getSoapServiceClass())}, ctc);
		constructor.setModifiers(Modifier.PUBLIC);
		constructor.setBody("{this._delegate = $1;}");
		ctc.addConstructor(constructor);
	}
	
	@Override
	protected Object createDelegate(Class<?> c, Object... args) throws Throwable {
		return c.getConstructor(new Class[] {getSoapServiceClass()}).newInstance(args);
	}

	@Override
	protected MethodMetadata methodMetadata(Class<?> soapServiceClass, CtMethod ctm, Method method) throws Throwable {
		return new GlobalScopeMethodMetadata(soapServiceClass, ctm, method);
	}
	
	private static class GlobalScopeMethodMetadata extends MethodMetadata {
		public GlobalScopeMethodMetadata(Class<?> soapServiceClass, CtMethod ctm, Method method) throws Throwable {
			super(soapServiceClass, ctm, method);
		}
		
		@Override
		protected void createNonPublicBody(StringBuilder body) {
			String className = getSoapServiceClass().getName();
			body.append("Method _method = ").append(className).append(".class.getMethod(\"").append(getMethodName()).append("\", new Class<?>[] {");
			appendParameterTypes(body);
			body.append("});\n");
			
			body.append("_method.setAccessible(true);\n");
			
			if (!getReturnType().isAssignableFrom(Void.class)) {
				body.append("return ");
			}
			body.append("_method.invoke(_delegate, new Object[] {");
			appendParameterNames(body);
			body.append("});");
		}
		
		protected void createPublicBody(StringBuilder body) {
			if (!getReturnType().isAssignableFrom(Void.class)) {
				body.append("return ");
			}
			body.append("_delegate.").append(getMethodName()).append("(");
			appendParameterNames(body);
			body.append(");");
		}
	}
}
