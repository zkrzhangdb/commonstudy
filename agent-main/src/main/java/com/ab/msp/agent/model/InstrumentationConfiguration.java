package com.ab.msp.agent.model;

import java.util.List;

/**
 * 字节码插桩配置类.
 * Created by dayuanfeng on 2019/10/10.
 */
public class InstrumentationConfiguration {
	private String clientCallMethodType;
	private String transferTo;
	private String registryAddress;
	private List<String> instrumentationList;

	public String getClientCallMethodType() {
		return clientCallMethodType;
	}

	public void setClientCallMethodType(String clientCallMethodType) {
		this.clientCallMethodType = clientCallMethodType;
	}

	public String getTransferTo() {
		return transferTo;
	}

	public void setTransferTo(String transferTo) {
		this.transferTo = transferTo;
	}

	public String getRegistryAddress() {
		return registryAddress;
	}

	public void setRegistryAddress(String registryAddress) {
		this.registryAddress = registryAddress;
	}

	public List<String> getInstrumentationList() {
		return instrumentationList;
	}

	public void setInstrumentationList(List<String> instrumentationList) {
		this.instrumentationList = instrumentationList;
	}

	@Override
	public String toString() {
		return "InstrumentationConfiguration{" +
				"clientCallMethodType='" + clientCallMethodType + '\'' +
				", transferTo='" + transferTo + '\'' +
				", registryAddress='" + registryAddress + '\'' +
				", instrumentationList=" + instrumentationList +
				'}';
	}
}
