package org.aimas.consert.middleware.agents;

import org.aimas.consert.middleware.protocol.RouteConfig;
import org.aimas.consert.middleware.protocol.RouteConfigV1;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class CtxUser extends AbstractVerticle {

	private final String CONFIG_FILE = "agents.properties";  // path to the configuration file for this agent
	
	private static Vertx vertx = Vertx.vertx(); // Vertx instance
	private Router router;                      // router for communication with this agent
	
	private AgentConfig agentConfig;  // configuration values for this agent
	private String host;              // where this agent is hosted
	
	private AgentConfig ctxCoord;         // configuration to communicate with the CtxCoord agent
	private AgentConfig ctxQueryHandler;  // configuration to communicate with the CtxQueryHandler agent
	private AgentConfig orgMgr;           // configuration to communicate with the OrgMgr agent 
	
	
	public static void main(String[] args) {
		
		CtxUser.vertx.deployVerticle(CtxUser.class.getName());		
	}
	
	@Override
	public void start() {
		
		// Create router
		RouteConfig routeConfig = new RouteConfigV1();
		this.router = routeConfig.createRouterUsage(vertx, this);
		
		// Read configuration
		try {
			
			Configuration config = new PropertiesConfiguration(CONFIG_FILE);
			
			this.agentConfig = AgentConfig.readCtxUserConfig(config);
			this.host = config.getString("CtxUser.host");
			
			this.ctxCoord = AgentConfig.readCtxCoordConfig(config);
			this.ctxQueryHandler = AgentConfig.readCtxQueryHandlerConfig(config);
			this.orgMgr = AgentConfig.readOrgMgrConfig(config);
			
		} catch (ConfigurationException e) {
			System.err.println("Error while reading configuration file '" + CONFIG_FILE + "': " + e.getMessage());
			e.printStackTrace();
		}
		
		// Start server
		CtxUser.vertx.createHttpServer()
			.requestHandler(router::accept)
			.listen(this.agentConfig.getPort(), this.host, res -> {
				if (res.succeeded()) {
					System.out.println("Started CtxUser on port " + this.agentConfig.getPort() + " host " + this.host);
				} else {
					System.out.println("Failed to start CtxUser on port " + this.agentConfig.getPort() + " host " +
						this.host);
				}
			});
	}
}