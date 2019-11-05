package com.ab.msp.agent.utils;


import com.ab.msp.agent.model.ProtocolTypeEnum;

/**
 * Created by dayuanfeng on 2019/11/5.
 */
public class DynamicUtils {

	public static void main(String[] args) {
		getTargetProtocolTypeThenOperate("springcloud");
	}

	private static void getTargetProtocolTypeThenOperate(String str) {
		for (ProtocolTypeEnum protocolTypeEnum : ProtocolTypeEnum.values()) {
			if (protocolTypeEnum.name().equalsIgnoreCase(str)) {
//				protocolTypeEnum.export(null, null, null);
				break;
			}
		}
	}
}
