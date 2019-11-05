package com.ab.msp.agent.bridge;

import java.util.List;

public interface ServicePublisher {
	List<String> publish() throws Throwable;
	
}
