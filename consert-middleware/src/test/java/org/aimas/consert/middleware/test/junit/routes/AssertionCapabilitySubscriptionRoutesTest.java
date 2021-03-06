package org.aimas.consert.middleware.test.junit.routes;

import java.io.IOException;

import org.aimas.consert.middleware.agents.AgentConfig;
import org.aimas.consert.middleware.agents.CtxCoord;
import org.aimas.consert.middleware.agents.OrgMgr;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Unit test for AssertionCapabilitySubscription routes
 */
@RunWith(VertxUnitRunner.class)
public class AssertionCapabilitySubscriptionRoutesTest {

	private static final String POST_QUERY = "@prefix protocol: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#> .\n"
			+ "@prefix assertion-capability-subscription: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapabilitySubscription/> .\n"
			+ "@prefix agent-spec: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/> .\n"
			+ "@prefix agent-address: <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentAddress/> .\n"
			+ "@prefix rdfbeans: <http://viceversatech.com/rdfbeans/2.0/> .\n\n"
			+ "protocol:AssertionCapabilitySubscription rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AssertionCapabilitySubscription\"^^xsd:string .\n"
			+ "protocol:AgentSpec rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentSpec\"^^xsd:string .\n"
			+ "protocol:AgentAddress rdfbeans:bindingClass \"org.aimas.consert.middleware.model.AgentAddress\"^^xsd:string .\n\n"
			+ "assertion-capability-subscription:foo a protocol:AssertionCapabilitySubscription ;\n"
			+ "    protocol:hasCapabilityQuery \"the capability query\"^^xsd:string ;\n"
			+ "    protocol:hasSubscriber agent-spec:CtxUser .\n"
			+ "agent-spec:CtxUser a protocol:AgentSpec ;\n"
			+ "    protocol:hasAddress agent-address:CtxUserAddress ;\n"
			+ "    protocol:hasIdentifier \"CtxUser1\" .\n"
			+ "agent-address:CtxUserAddress a protocol:AgentAddress ;\n"
			+ "    protocol:ipAddress \"127.0.0.1\"^^xsd:string ;\n"
			+ "    protocol:port \"8082\"^^xsd:int .\n";

	private Vertx vertx;
	private AgentConfig ctxCoord;
	private String resourceUUID;
	private HttpClient httpClient;

	@Before
	public void setUp(TestContext context) throws IOException {

		this.ctxCoord = new AgentConfig("127.0.0.1", 8081);

		// Start Vert.x server for CtxCoord
		this.vertx = Vertx.vertx();
		
		// Deploy the required verticles for the queries
		this.vertx.deployVerticle(OrgMgr.class.getName(), new DeploymentOptions().setWorker(true), res -> {
			this.vertx.deployVerticle(CtxCoord.class.getName(), new DeploymentOptions().setWorker(true), context.asyncAssertSuccess());
		});

		this.httpClient = this.vertx.createHttpClient();
	}

	@After
	public void tearDown(TestContext context) {

		this.vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testPost(TestContext context) {

		Async async = context.async();
		this.post(context, async);
		async.await();
	}

	public void post(TestContext context, Async async) {

		// POST: insert data that we will try to fetch in the test methods

		this.httpClient.post(this.ctxCoord.getPort(), this.ctxCoord.getAddress(),
				"/api/v1/coordination/assertion_capability_subscriptions/", new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 201) {
							context.fail("Failed to create AssertionCapabilitySubscription, code " + resp.statusCode());
							async.complete();
						} else {

							// Get the created resource's UUID to make requests on it later
							resp.bodyHandler(new Handler<Buffer>() {

								@Override
								public void handle(Buffer buffer) {
									resourceUUID = buffer.toString();
									async.complete();
								}

							});
						}
					}
				}).putHeader("content-type", "text/turtle").end(POST_QUERY);
	}

	@Test
	public void testGetOne(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost);
		asyncPost.await();

		Async async = context.async();

		// GET one

		this.httpClient.get(ctxCoord.getPort(), ctxCoord.getAddress(),
				"/api/v1/coordination/assertion_capability_subscriptions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp2) {

						if (resp2.statusCode() != 200) {
							context.fail("Failed to get AssertionCapabilitySubscription");
							async.complete();
						}

						resp2.bodyHandler(new Handler<Buffer>() {

							@Override
							public void handle(Buffer buffer2) {

								context.assertTrue(buffer2.toString().trim().replace("\r", "").replace("\n", "").replace("\t", "").contains(
										"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapabilitySubscription/foo> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasCapabilityQuery> \"the capability query\" ;"
												+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriber> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxUser> ;"
												+ "a <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapabilitySubscription> ."));
								async.complete();
							}
						});
					}
				}).end();
	}

	@Test
	public void testPut(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost);
		asyncPost.await();

		Async async = context.async();

		String updated = POST_QUERY.replace("CtxUser", "CtxQueryHandler");

		// PUT

		this.httpClient.put(ctxCoord.getPort(), ctxCoord.getAddress(),
				"/api/v1/coordination/assertion_capability_subscriptions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to get AssertionCapabilitySubscription");
							async.complete();
						}

						// GET one to see whether the update has really been made

						httpClient.get(ctxCoord.getPort(), ctxCoord.getAddress(),
								"/api/v1/coordination/assertion_capability_subscriptions/" + resourceUUID + "/",
								new Handler<HttpClientResponse>() {

									@Override
									public void handle(HttpClientResponse resp2) {

										if (resp2.statusCode() != 200) {
											context.fail("Failed to get AssertionCapabilitySubscription");
											async.complete();
										}

										resp2.bodyHandler(new Handler<Buffer>() {

											@Override
											public void handle(Buffer buffer2) {

												context.assertTrue(buffer2.toString().trim().replace("\r", "").replace("\n", "").replace("\t", "").contains(
														"<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapabilitySubscription/foo> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasCapabilityQuery> \"the capability query\" ;"
																+ "<http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#hasSubscriber> <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AgentSpec/CtxQueryHandler> ;"
																+ "a <http://pervasive.semanticweb.org/ont/2017/07/consert/protocol#AssertionCapabilitySubscription> ."));
												async.complete();
											}
										});
									}
								}).end();
					}
				}).putHeader("content-type", "text/turtle").end(updated);
	}

	@Test
	public void testDelete(TestContext context) {

		Async asyncPost = context.async();
		this.post(context, asyncPost);
		asyncPost.await();

		Async async = context.async();

		// DELETE

		this.httpClient.delete(ctxCoord.getPort(), ctxCoord.getAddress(),
				"/api/v1/coordination/assertion_capability_subscriptions/" + this.resourceUUID + "/",
				new Handler<HttpClientResponse>() {

					@Override
					public void handle(HttpClientResponse resp) {

						if (resp.statusCode() != 200) {
							context.fail("Failed to delete AssertionCapabilitySubscription");
							async.complete();
						}

						// GET one to see whether the deletion has really been made

						httpClient.get(ctxCoord.getPort(), ctxCoord.getAddress(),
								"/api/v1/coordination/assertion_capability_subscriptions/" + resourceUUID + "/",
								new Handler<HttpClientResponse>() {

									@Override
									public void handle(HttpClientResponse resp2) {

										context.assertEquals(404, resp2.statusCode());
										async.complete();
									}
								}).end();
					}
				}).end();
	}
}
