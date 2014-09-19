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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.Ignore;
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
	@Autowired SecurePersonRepository personRepository;
	@Autowired SecureOrderRepository orderRepository;

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
		return Arrays.asList("people", "orders");
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

	//=================================================================

	@Test
	public void testSecuritySettings() {
		assertThat(securityChecker.secured(), is(true));
	}

	//=================================================================

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsForPeopleDeleteAll() {
		personRepository.deleteAll();
	}

	@Test(expected = AccessDeniedException.class)
	public void testUserCredentialsForPeopleDeleteAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user"));
		personRepository.deleteAll();
	}

	@Test
	public void testAdminCredentialsForPeopleDeleteAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
		personRepository.deleteAll();
	}

	//=================================================================

	/**
	 * TODO: Is this a Spring Data or Spring Security bug?
	 *
	 * The class is flagged with @Secured("ROLE_USER"), meaning findAll should require an authentication credential,
	 * but for some reason it does not. This needs to be solved before release.
	 */
	@Ignore
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsForPeopleFindAll() {
		personRepository.findAll();
	}

	@Test
	public void testUserCredentialsForPeopleFindAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user"));
		personRepository.findAll();
	}

	@Test
	public void testAdminCredentialsForPeopleFindAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
		personRepository.findAll();
	}

	//=================================================================

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsForOrdersDeleteAll() {
		orderRepository.deleteAll();
	}

	@Test(expected = AccessDeniedException.class)
	public void testUserCredentialsForOrdersDeleteAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user"));
		orderRepository.deleteAll();
	}

	@Test
	public void testAdminCredentialsForOrdersDeleteAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
		orderRepository.deleteAll();
	}

	//=================================================================

	/**
	 * TODO: Is this a Spring Data or Spring Security bug?
	 *
	 * The class is flagged with @Secured("ROLE_USER"), meaning findAll should require an authentication credential,
	 * but for some reason it does not. This needs to be solved before release.
	 */
	@Ignore
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsForOrdersFindAll() {
		orderRepository.findAll();
	}

	@Test
	public void testUserCredentialsForOrdersFindAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user"));
		orderRepository.findAll();
	}

	@Test
	public void testAdminCredentialsForOrdersFindAll() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("admin", "admin"));
		orderRepository.findAll();
	}

	//=================================================================

	@Test
	public void testNoCredentialsForAlpsPeoplePeople() throws Exception {

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		Link peopleLink = linkTestUtils.discoverUnique(profileLink.getHref(), "people");

		assertThat(peopleLink, is(notNullValue()));

		mvc.perform(get(peopleLink.getHref())).//
				//andDo(print()).//
				andExpect(jsonPath("$.descriptors[*].id", hasItems("get-people", "get-person", "create-people",
					"update-person", "patch-person", "delete-person")));
	}

	@Test
	public void testNoCredentialsForAlpsOrders() throws Exception {

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		Link ordersLink = linkTestUtils.discoverUnique(profileLink.getHref(), "orders");

		assertThat(ordersLink, is(notNullValue()));

		mvc.perform(get(ordersLink.getHref())).//
				andDo(print()).//
				andExpect(jsonPath("$.descriptors[*].id", hasItems("get-orders", "get-order", "create-orders",
				"update-order", "patch-order", "delete-order")));
	}

}
