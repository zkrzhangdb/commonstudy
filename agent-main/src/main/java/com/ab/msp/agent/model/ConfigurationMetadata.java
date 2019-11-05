package com.ab.msp.agent.model;

import java.io.Serializable;

/**
 * Created by dayuanfeng on 2019/11/5.
 */
public class ConfigurationMetadata implements Serializable {
	private static final long serialVersionUID = 1770258961703080683L;
	private String productCode;
	private String appCode;
	private WebserviceTypeEnum serviceType;
	private ProtocolTypeEnum protocolType;
	private String serverClientType;
	private RegistryTypeEnum registryType;

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	public WebserviceTypeEnum getServiceType() {
		return serviceType;
	}

	public void setServiceType(WebserviceTypeEnum serviceType) {
		this.serviceType = serviceType;
	}

	public ProtocolTypeEnum getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(ProtocolTypeEnum protocolType) {
		this.protocolType = protocolType;
	}

	public String getServerClientType() {
		return serverClientType;
	}

	public void setServerClientType(String serverClientType) {
		this.serverClientType = serverClientType;
	}

	public RegistryTypeEnum getRegistryType() {
		return registryType;
	}

	public void setRegistryType(RegistryTypeEnum registryType) {
		this.registryType = registryType;
	}
}
