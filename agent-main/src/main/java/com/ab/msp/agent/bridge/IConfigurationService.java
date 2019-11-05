package com.ab.msp.agent.bridge;

import com.ab.msp.agent.model.ConfigurationMetadata;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by dayuanfeng on 2019/11/5.
 */
public interface IConfigurationService<T extends ConfigurationMetadata> {

	T getConfiguration(ClassLoader loader, String[] params);

	T getConfiguration(ClassLoader loader, InputStream inputStream);

	T getConfiguration(ClassLoader loader, URL[] urls);

}
