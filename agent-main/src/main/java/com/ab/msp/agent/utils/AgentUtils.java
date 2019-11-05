package com.ab.msp.agent.utils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class AgentUtils {
	private static FileFilter classPathFilter;
	
	public static CtClass getCtClass(ClassPool pool, Class<?> c) throws NotFoundException {
		return pool.get(c.getName());
	}
	
	public static CtClass[] getCtClasses(ClassPool pool, Class<?>... classes) throws NotFoundException {
		if (classes == null || classes.length == 0) {
			return new CtClass[] {};
		}
		
		CtClass[] ctcs = new CtClass[classes.length];
		for (int i = 0; i < ctcs.length; i++) {
			ctcs[i] = getCtClass(pool, classes[i]);
		}
		return ctcs;
	}
	
	public static URL[] toURLs(File... files) {
		final List<URL> URLs = new ArrayList<URL>();
		doWithFiles(new FileCallback() {
			public void call(File f) {
				try {
					URLs.add(f.toURI().toURL());
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}, files);
		return URLs.toArray(new URL[] {});
	}
	
	public static void doWithFiles(FileCallback callback, File... files) {
		if (files != null && files.length > 0) {
			for (File f : files) {
				if (f.isDirectory()) {
					doWithFiles(callback, f.listFiles(getClassPathFilter()));
				} else {
					callback.call(f);
				}
			}
		}
	} 
	
	public static FileFilter getClassPathFilter() {
		if (classPathFilter == null) {
			classPathFilter = new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
				}
			};
		}
		return classPathFilter;
	}
	
	public static interface FileCallback {
		void call(File f);
	}
}
