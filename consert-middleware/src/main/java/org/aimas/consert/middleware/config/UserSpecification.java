package org.aimas.consert.middleware.config;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

@RDFNamespaces("orgconf=http://pervasive.semanticweb.org/ont/2014/06/consert/cmm/orgconf#")
@RDFBean("orgconf:CtxUserSpec")
public class UserSpecification extends AgentSpecification {
	
	private String contextDomainUserDocument;
	
	
	public UserSpecification() {}
	
	public UserSpecification(AgentAddressConfig agentAddress, AgentPolicy controlPolicy, AgentAddressConfig assignedManagerAddress, 
			String contextDomainUserDocument) {
		super(agentAddress, controlPolicy, assignedManagerAddress);
		
	    this.assignedManagerAddress = assignedManagerAddress;
	    this.contextDomainUserDocument = contextDomainUserDocument;
	}
	
	@RDF("orgconf:hasContextDomainUserDoc")
	public String getContextDomainUserModel() {
		return contextDomainUserDocument;
	}
	
	public boolean hasContextDomainUserModel() {
		return contextDomainUserDocument != null;
	}
}