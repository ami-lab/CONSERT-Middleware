package org.aimas.consert.middleware.agents;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class Test {

	public static void main(String[] args) {
		
		Vertx vertx = Vertx.vertx();
		
		vertx.deployVerticle(CtxCoord.class.getName(), res -> {
			
			JsonObject config0 = new JsonObject().put("id", 0);
			JsonObject config1 = new JsonObject().put("id", 1);
			
			vertx.deployVerticle(CtxSensorLLA.class.getName(), new DeploymentOptions().setConfig(config0));
			vertx.deployVerticle(CtxSensorPosition.class.getName(), new DeploymentOptions().setConfig(config1));
		});
	}
}