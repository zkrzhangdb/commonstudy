package com.ab.msp.agent.utils;

import java.net.URL;
import java.net.URLClassLoader;

public class CompositeClassLoader extends URLClassLoader {
	private ClassLoader other;
	
	public CompositeClassLoader(ClassLoader parent, ClassLoader other, URL... urls) {
		super(urls, parent);
		this.other = other;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (name.startsWith("org.springframework") || name.startsWith("javassist")) {
			return other.loadClass(name);
		}
		
		try {
			return super.loadClass(name);
		} catch (ClassNotFoundException e) {
			if (other != null) {
				return other.loadClass(name);
			}
			throw e;
		}
	}
	
	@Override
	public URL getResource(String name) {
		URL resource = super.getResource(name);
		if (resource == null && other != null) {
			resource = other.getResource(name);
		}
		return resource;
	}
}
