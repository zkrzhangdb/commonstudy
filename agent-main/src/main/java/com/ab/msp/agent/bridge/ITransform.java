package com.ab.msp.agent.bridge;

import java.io.Serializable;

/**
 * Created by dayuanfeng on 2019/11/5.
 */
public interface ITransform<S extends Serializable, T extends Serializable> {

	/**
	 * 根据映射规则从源类型转为目标类型.
	 * @param s
	 * @param mapping
	 * @return
	 */
	T to(S s, String[] mapping);

	/**
	 * 根据映射规则从目标类型转为源类型.
	 * @param t
	 * @param mapping
	 * @return
	 */
	S from(T t, String[] mapping);

}
