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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.AbstractWebIntegrationTests;
import org.springframework.data.rest.webmvc.LinkTestUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.core.JsonPathLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Test Spring Data REST in the context of being locked down by Spring Security
 *
 * @author Greg Turnquist
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SecureJpaConfiguration.class, SecurityIntegrationTests.Config.class,
		SecurityConfiguration.class})
@Transactional
public class SecurityIntegrationTests extends AbstractWebIntegrationTests {

	@Autowired WebApplicationContext context;
	@Autowired SecurityChecker securityChecker;
	@Autowired SecurePersonRepository repository;

	LinkTestUtils linkTestUtils;

	@Configuration
	static class Config {

		@Bean
		public SecurityChecker securityChecker() {
			return new SpringSecurityChecker();
		}

		@Bean
		public LinkDiscoverer alpsLinkDiscoverer() {

			return new JsonPathLinkDiscoverer("$.descriptors[?(@.name == '%s')].href",
					MediaType.valueOf("application/alps+json"));
		}

	}

	@Override
	protected Iterable<String> expectedRootLinkRels() {
		return Arrays.asList("people");
	}

	@Override
	public void setUp() {

		super.setUp();
		linkTestUtils = new LinkTestUtils(mvc, discoverers);
	}

	@After
	public void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testSecuritySettings() {
		assertThat(securityChecker.secured(), is(true));
	}

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsForDeleteAll() {
		repository.deleteAll();
	}

	@Test(expected = AccessDeniedException.class)
	public void testUserCredentialsForDeleteAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user"));
		repository.deleteAll();
	}

	@Test
	public void testAdminCredentialsForDeleteAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
		repository.deleteAll();
	}

	/**
	 * TODO: Is this a Spring Data or Spring Security bug?
	 *
	 * The class is flagged with @Secured("ROLE_USER"), meaning findAll should require an authentication credential,
	 * but for some reason it does not. This needs to be solved before release.
	 */
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsForFindAll() {
		repository.findAll();
	}

	@Test
	public void testUserCredentialsForFindAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user"));
		repository.findAll();
	}

	@Test
	public void testAdminCredentialsForFindAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
		repository.findAll();
	}

	@Test
	public void testRestrictedAlpsData() throws Exception {

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		Link peopleLink = linkTestUtils.discoverUnique(profileLink.getHref(), "people");

		assertThat(peopleLink, is(notNullValue()));

		mvc.perform(get(peopleLink.getHref())).//
				//andDo(print()).//
				andExpect(jsonPath("$.descriptors[*].id", hasItems("get-people", "get-person", "create-people",
					"update-person", "patch-person", "delete-person")));
	}

}
