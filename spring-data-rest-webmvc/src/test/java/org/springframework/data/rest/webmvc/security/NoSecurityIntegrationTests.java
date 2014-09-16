/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.rest.webmvc.security;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.AbstractWebIntegrationTests;
import org.springframework.data.rest.webmvc.jpa.JpaRepositoryConfig;
import org.springframework.hateoas.Link;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test Spring Data REST in the context of being locked down by Spring Security
 *
 * @author Greg Turnquist
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaRepositoryConfig.class, NoSecurityIntegrationTests.NoSecurityConfiguration.class})
@Transactional
public class NoSecurityIntegrationTests extends AbstractWebIntegrationTests {

	@Autowired ApplicationContext context;
	@Autowired AbstractSecurityChecker securityChecker;

	@Override
	protected Iterable<String> expectedRootLinkRels() {
		return Arrays.asList("people", "authors", "books");
	}

	@Override
	public void setUp() {
		super.setUp();
	}

	@Test
	public void testSecuritySettings() {

		assertThat(securityChecker.secured(), is(false));
		assertThat(context.getBean(NoSecurityChecker.class), notNullValue());
	}

	@Test
	public void testGettingPeople() throws Exception {

		Link peopleLink = linkTestUtils.discoverUnique("people");
	}

	@Configuration
	static class NoSecurityConfiguration {

		@Bean
		public NoSecurityChecker securityChecker() {
			return new NoSecurityChecker();
		}

	}

}
