package org.aimas.consert.middleware.protocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxSensor;
import org.aimas.consert.middleware.model.AgentAddress;
import org.aimas.consert.middleware.model.AssertionUpdateMode;
import org.aimas.consert.middleware.model.tasking.AlterUpdateModeCommand;
import org.aimas.consert.middleware.model.tasking.StartUpdatesCommand;
import org.aimas.consert.middleware.model.tasking.StopUpdatesCommand;
import org.aimas.consert.middleware.model.tasking.TaskingCommand;
import org.cyberborean.rdfbeans.RDFBeanManager;
import org.cyberborean.rdfbeans.exceptions.RDFBeanException;
import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import io.vertx.ext.web.RoutingContext;

/**
 * Defines the routes for a CtxSensor agent in version 1
 */
public class RouteConfigV1Sensing extends RouteConfigV1 {
	
	private static final String START_UPDATES_COMMAND_URI =
			"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#StartUpdatesCommand";
	private static final String STOP_UPDATES_COMMAND_URI =
			"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#StopUpdatesCommand";
	private static final String ALTER_UPDATE_MODE_COMMAND_URI =
			"http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AlterUpdateModeCommand";
	
	private CtxSensor ctxSensor;  // the agent that can be accessed with the defined routes
	private AssertionUpdateMode defaultUpdateMode;  // the default update mode used when starting updates

	
	public RouteConfigV1Sensing(CtxSensor ctxSensor) {
		this.ctxSensor = ctxSensor;
		this.defaultUpdateMode = new AssertionUpdateMode();
		this.defaultUpdateMode.setUpdateMode(AssertionUpdateMode.TIME_BASED);
		this.defaultUpdateMode.setUpdateRate(500);
		//this.defaultUpdateMode.setUpdateMode(AssertionUpdateMode.CHANGE_BASED);
		//this.defaultUpdateMode.setUpdateRate(0);
	}

	/**
	 * PUT tasking command
	 * 
	 * @param rtCtx the routing context
	 */
	public void handlePutTaskingCommand(RoutingContext rtCtx) {
		
		String rdf = rtCtx.getBodyAsString();

		RepositoryConnection conn = this.ctxSensor.getRepository().getConnection();
		RDFBeanManager manager = new RDFBeanManager(conn);
		
		TaskingCommand taskingCommand = null;

		try {
			// Insertion in RDF store
			Model model = Rio.parse(new ByteArrayInputStream(rdf.getBytes()), "", RDFFormat.TURTLE);
			conn.add(model);

			// Getting the object we just inserted
			for (Statement s : model) {
				
				String rdfClass = s.getObject().stringValue();
				
				if (rdfClass.equals(START_UPDATES_COMMAND_URI)) {
					taskingCommand = manager.get(s.getSubject(), StartUpdatesCommand.class);
					break;
				} else if (rdfClass.equals(STOP_UPDATES_COMMAND_URI)) {
					taskingCommand = manager.get(s.getSubject(), StopUpdatesCommand.class);
					break;
				} else if (rdfClass.equals(ALTER_UPDATE_MODE_COMMAND_URI)) {
					taskingCommand = manager.get(s.getSubject(), AlterUpdateModeCommand.class);
					break;
				}
			}

			conn.close();
			
			AgentAddress address = taskingCommand.getTargetAgent().getAddress();
			AgentConfig agentConfig = this.ctxSensor.getAgentConfig();
			
			// make sure that the CtxSensor is the recipient of the tasking command, and if so, execute the command
			if(address.getIpAddress().equals(agentConfig.getAddress()) && address.getPort() == agentConfig.getPort()) {
				
				if(taskingCommand instanceof StartUpdatesCommand) {
					this.ctxSensor.startUpdates(((StartUpdatesCommand) taskingCommand).getTargetAssertion(),
							this.defaultUpdateMode);
				} else if(taskingCommand instanceof StopUpdatesCommand) {
					this.ctxSensor.stopUpdates(((StopUpdatesCommand) taskingCommand).getTargetAssertion());
				} else if(taskingCommand instanceof AlterUpdateModeCommand) {
					this.ctxSensor.alterUpdates(((AlterUpdateModeCommand) taskingCommand).getTargetAssertion(),
							((AlterUpdateModeCommand) taskingCommand).getUpdateMode());
				} else {
					rtCtx.response().setStatusCode(400).setStatusMessage("Error").end();
					return;
				}
			} else {
				rtCtx.response().setStatusCode(400).setStatusMessage("Error").end();
				return;
			}

			// Answer with code 200
			rtCtx.response().setStatusCode(200).end();
		} catch (RDF4JException | RDFBeanException | IOException e) {

			conn.close();
			System.err.println("Error while getting tasking command: " + e.getMessage());
			e.printStackTrace();
			rtCtx.response().setStatusCode(400).setStatusMessage("Error").end();
		}
	}
}
