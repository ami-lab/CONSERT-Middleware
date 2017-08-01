package org.aimas.consert.middleware.protocol;

import org.aimas.consert.middleware.model.ContextSubscription;

/**
 * Bean class for resource containing required information to make stateless
 * REST calls for ContextSubscriptions
 */
public class ContextSubscriptionResource {

	private String resourceURI; // URI to use to access the resource
	private String initiatorURI; // URI to use for communications with the
									// initiator agent
	private String participantURI; // URI to use for communications with the
									// participant agent

	private String request; // content of the request
	private ContextSubscription contextSubscription; // content of the
														// ContextSubscription

	private String initiatorCallbackURI; // URI to use for the callback on the
											// initiator
	private RequestState state; // current state in the protocol

	public String getResourceURI() {
		return resourceURI;
	}

	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}

	public String getInitiatorURI() {
		return initiatorURI;
	}

	public void setInitiatorURI(String initiatorURI) {
		this.initiatorURI = initiatorURI;
	}

	public String getParticipantURI() {
		return participantURI;
	}

	public void setParticipantURI(String participantURI) {
		this.participantURI = participantURI;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public ContextSubscription getContextSubscription() {
		return contextSubscription;
	}

	public void setContextSubscription(ContextSubscription contextSubcsription) {
		this.contextSubscription = contextSubcsription;
	}

	public String getInitiatorCallbackURI() {
		return initiatorCallbackURI;
	}

	public void setInitiatorCallbackURI(String initiatorCallbackURI) {
		this.initiatorCallbackURI = initiatorCallbackURI;
	}

	public RequestState getState() {
		return state;
	}

	public void setState(RequestState state) {
		this.state = state;
	}
}