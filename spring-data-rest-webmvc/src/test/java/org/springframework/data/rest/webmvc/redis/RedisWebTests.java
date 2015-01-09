package org.springframework.data.rest.webmvc.redis;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.rest.webmvc.AbstractWebIntegrationTests;

import org.springframework.test.context.ContextConfiguration;
import redis.embedded.RedisServer;

@ContextConfiguration(classes = RedisRepoConfig.class)
public class RedisWebTests extends AbstractWebIntegrationTests {

	private RedisServer server;

	@Override
	protected Iterable<String> expectedRootLinkRels() {
		return Arrays.asList("people");
	}

	@Before
	public void configure() throws IOException {
		server = RedisServer.builder().port(9000).build();
		server.start();
	}

	@Test
	public void noop() {
		System.out.println("Here we go!");
	}

	@After
	public void tearDown() throws InterruptedException {
		server.stop();
	}
}
