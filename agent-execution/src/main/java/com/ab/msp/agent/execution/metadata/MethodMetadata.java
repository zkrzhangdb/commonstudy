package com.ab.msp.agent.execution.metadata;

import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

abstract class MethodMetadata {
	private Class<?> soapServiceClass;
	private int modifier;
	private Class<?> returnType;
	private String methodName;
	private Class<?>[] parameterTypes;
	private String[] parameterNames;
	private Class<?>[] throwableTypes;
	
	public MethodMetadata(Class<?> soapServiceClass, CtMethod ctm, Method method) throws Throwable {
		this.soapServiceClass = soapServiceClass;
		this.modifier = method.getModifiers();
		this.methodName = method.getName();
		this.returnType = method.getReturnType();
		this.throwableTypes = method.getExceptionTypes();
		this.parameterTypes = method.getParameterTypes();

	    if (parameterTypes.length > 0) {
	    	int staticIndex = Modifier.isStatic(ctm.getModifiers()) ? 0 : 1; 
			MethodInfo methodInfo = ctm.getMethodInfo();  
		    CodeAttribute codeAttribute = methodInfo.getCodeAttribute();  
		    LocalVariableAttribute localVariableAttribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);  
		    
		    parameterNames = new String[parameterTypes.length];
	    	for (int i = 0; i < parameterTypes.length; i++) { 
	    		//parameterNames[i] = localVariableAttribute.variableName(staticIndex + i);
	    		
	    		parameterNames[i] = "str"+i;
	    	}
	    }
	}
	
	public String getMethodName() {
		return methodName;
	}

	public Class<?> getReturnType() {
		return returnType;
	}
	
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public String[] getParameterNames() {
		return parameterNames;
	}

	public Class<?>[] getThrowableTypes() {
		return throwableTypes;
	}
	
	public Class<?> getSoapServiceClass() {
		return soapServiceClass;
	}
	
	protected final CharSequence createBody() {
		StringBuilder body = new StringBuilder("{\n");
		if (modifier == Modifier.PUBLIC) {
			createPublicBody(body);
		} else {
			createNonPublicBody(body);
		}
		return body.append("\n}");
	}
	
	protected final StringBuilder appendDeclaredParameters(StringBuilder body) {
		return appendDeclaredParameters(body, true);
	}
	
	protected final StringBuilder appendDeclaredParameters(StringBuilder body, boolean first) {
		if (parameterTypes != null && parameterTypes.length > 0) {
			for (int i = 0; i < parameterTypes.length; i++) {
				if (first) {
					first = false;
				} else {
					body.append(", ");
				}
				body.append(parameterTypes[i].getName()).append(" ").append(parameterNames[i]);
			}
		}
		return body;
	}
	
	protected final StringBuilder appendParameterTypes(StringBuilder body) {
		return appendParameterTypes(body, true);
	}
	
	protected final StringBuilder appendParameterTypes(StringBuilder body, boolean first) {
		Class<?>[] types = getParameterTypes();
		if (types != null && types.length > 0) {
			for (int i = 0; i < types.length; i++) {
				if (first) {
					first = false;
				} else {
					body.append(", ");
				}
				body.append(types[i].getName()).append(".class");
			}
		}
		return body;
	}
	
	protected final StringBuilder appendThrowableTypes(StringBuilder body) {
		if (throwableTypes != null && throwableTypes.length > 0) {
			body.append("throws ");
			
			boolean first = true;
			for (int i = 0; i < throwableTypes.length; i++) {
				if (first) {
					first = false;
				} else {
					body.append(", ");
				}
				body.append(throwableTypes[i].getName());
			}
		}
		return body;
	}
	
	protected final StringBuilder appendParameterNames(StringBuilder body) {
		return appendParameterNames(body, true);
	}
	
	protected final StringBuilder appendParameterNames(StringBuilder body, boolean first) {
		String[] params = getParameterNames();
		if (params != null && params.length > 0) {
			int staticIndex = Modifier.isStatic(modifier) ? 0 : 1; 
			for (int i = 0; i < params.length; i++) {
				if (first) {
					first = false;
				} else {
					body.append(", ");
				}
				body.append("$").append(staticIndex + i);
			}
		}
		return body;
	}
	
	protected abstract void createNonPublicBody(StringBuilder body);
	protected abstract void createPublicBody(StringBuilder body);
}
