#1.项目说明
	agent-application 非侵入式对外暴露服务，主要提供web application相应功能页面及http restful接口；
	agent-main 非侵入式项目代理服务，未来需要配置到接入系统的javaagent中；
	agent-domain 非侵入式项目领域模型；
	agent-adapter 非侵入式项目适配层，目前提供非标准化参数（原服务）-标准话参数（新服务）、标准化参数（新服务）-非标准话参数（原服务）
    	的参数转换功能，未来会加入更多功能；
	agent-excution 非侵入式项目执行层，包括协议的转换、服务的订阅、发布等核心处理流程；
	agent-protocol 非侵入式项目协议处理层，目前只包含dubbo及SpringCloud实现，后续扩展更多的rpc协议；
	