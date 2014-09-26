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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.AbstractWebIntegrationTests;
import org.springframework.data.rest.webmvc.LinkTestUtils;
import org.springframework.data.rest.webmvc.alps.RootResourceInformationToAlpsDescriptorConverter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.core.JsonPathLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * Test Spring Data REST in the context of being locked down by Spring Security
 *
 * @author Greg Turnquist
 * @author Rob Winch
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SecureJpaConfiguration.class, SecurityIntegrationTests.Config.class,
		SecurityConfiguration.class})
@Transactional
public class SecurityIntegrationTests extends AbstractWebIntegrationTests {

	@Autowired WebApplicationContext context;
	@Autowired SpringSecurityChecker securityChecker;

	@Autowired MethodSecurityInterceptor smi;

	@Autowired SecuredPersonRepository personRepository;
	@Autowired PreAuthorizedOrderRepository orderRepository;
	@Autowired RootResourceInformationToAlpsDescriptorConverter alpsDescriptorConverter;

	@Autowired Pojo pojo;

	LinkTestUtils linkTestUtils;
	SecurityTestUtils securityTestUtils;

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

		@Bean
		public Pojo pojo() {
			return new Pojo();
		}

	}

	@Override
	protected Iterable<String> expectedRootLinkRels() {
		return Arrays.asList("people", "orders", "budgets");
	}

	@Override
	public void setUp() {

		super.setUp();
		linkTestUtils = new LinkTestUtils(mvc, discoverers);
		securityTestUtils = new SecurityTestUtils(smi);
		alpsDescriptorConverter.setSecurityChecker(securityChecker);
		securityChecker.setSmi(smi);
	}

	@Before
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

	@Test
	public void deleteAllPeopleAccessDeniedForUsers() throws Throwable {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER")));

		assertThat(securityTestUtils.hasAccess(personRepository, "deleteAll"), is(false));
	}

	@Test
	public void deleteAllPeopleAccessGrantedForAdmins() throws Throwable {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")));

		assertThat(securityTestUtils.hasAccess(personRepository, "deleteAll"), is(true));
	}

	//=================================================================

	/**
	 * TODO: Is this a Spring Data or Spring Security bug?
	 *
	 * The class is flagged with @Secured("ROLE_USER"), meaning findAll should require an authentication credential,
	 * but for some reason it does not. This needs to be solved before release.
	 */
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsForPeopleFindAll() {
		personRepository.findAll();
	}

	@Test
	public void findAllPeopleAccessGrantedForUsers() throws Throwable {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER")));

		assertThat(securityTestUtils.hasAccess(personRepository, "findAll"), is(true));
	}

	@Test
	public void findAllPeopleAccessGrantedForAdmins() throws Throwable {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")));

		assertThat(securityTestUtils.hasAccess(personRepository, "findAll"), is(true));
	}

	//=================================================================

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsForOrdersDeleteAll() {
		orderRepository.deleteAll();
	}

	@Test
	public void deleteAllOrdersAccessDeniedForUsers() throws Throwable {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER")));

		assertThat(securityTestUtils.hasAccess(orderRepository, "deleteAll"), is(false));
	}

	@Test
	public void deleteAllOrdersAccessGrantedForAdmins() throws Throwable {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")));

		assertThat(securityTestUtils.hasAccess(orderRepository, "deleteAll"), is(true));
	}

	//=================================================================

	/**
	 * TODO: Is this a Spring Data or Spring Security bug?
	 *
	 * The class is flagged with @Secured("ROLE_USER"), meaning findAll should require an authentication credential,
	 * but for some reason it does not. This needs to be solved before release.
	 */
	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsForOrdersFindAll() {
		orderRepository.findAll();
	}

	@Test
	public void findAllOrdersAccessGrantedForUsers() throws Throwable {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER")));

		assertThat(securityTestUtils.hasAccess(orderRepository, "findAll"), is(true));
	}

	@Test
	public void findAllOrdersAccessGrantedForAdmins() throws Throwable {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")));

		assertThat(securityTestUtils.hasAccess(orderRepository, "findAll"), is(true));
	}

	//=================================================================
	// At the root level, repos that are entirely blocked by security
	// annotations should not be visible.
	//=================================================================

	@Test
	public void testNoCredentialsForRootLinks() throws Exception {

		Link peopleLink = linkTestUtils.discoverUnique("/", "people");
		assertThat(peopleLink, is(nullValue()));

		Link ordersLink = linkTestUtils.discoverUnique("/", "orders");
		assertThat(peopleLink, is(nullValue()));

		Link budgetsLink = linkTestUtils.discoverUnique("/", "budgets");
		assertThat(budgetsLink, is(nullValue()));

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		assertThat(profileLink, is(notNullValue()));
	}

	@Test
	public void testUserCredentialsForRootLinks() throws Exception {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER")));

		Link peopleLink = linkTestUtils.discoverUnique("/", "people");
		assertThat(peopleLink, is(notNullValue()));

		Link ordersLink = linkTestUtils.discoverUnique("/", "orders");
		assertThat(peopleLink, is(notNullValue()));

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		assertThat(profileLink, is(notNullValue()));

		Link budgetsLink = linkTestUtils.discoverUnique("/", "budgets");
		assertThat(budgetsLink, is(nullValue()));
	}

	@Test
	public void testAdminCredentialsForRootLinks() throws Exception {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")));

		Link peopleLink = linkTestUtils.discoverUnique("/", "people");
		assertThat(peopleLink, is(notNullValue()));

		Link ordersLink = linkTestUtils.discoverUnique("/", "orders");
		assertThat(peopleLink, is(notNullValue()));

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		assertThat(profileLink, is(notNullValue()));

		Link budgetsLink = linkTestUtils.discoverUnique("/", "budgets");
		assertThat(budgetsLink, is(nullValue()));
	}

	@Test
	public void testManagerCredentialsForRootLinks() throws Exception {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_MANAGER")));

		Link peopleLink = linkTestUtils.discoverUnique("/", "people");
		assertThat(peopleLink, is(notNullValue()));

		Link ordersLink = linkTestUtils.discoverUnique("/", "orders");
		assertThat(peopleLink, is(notNullValue()));

		Link budgetsLink = linkTestUtils.discoverUnique("/", "budgets");
		assertThat(budgetsLink, is(notNullValue()));

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		assertThat(profileLink, is(notNullValue()));
	}

	//=================================================================
	// When exploring ALPS, repos that are blocked entirely shouldn't
	// appear as top level descriptors.
	// Operations that are blocked should be filtered out.
	//=================================================================

	@Test
	public void testNoCredentialsForAlps() throws Exception {

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");

		Link peopleLink = linkTestUtils.discoverUnique(profileLink.getHref(), "people");
		assertThat(peopleLink, is(nullValue()));

		Link ordersLink = linkTestUtils.discoverUnique(profileLink.getHref(), "orders");
		assertThat(ordersLink, is(nullValue()));

		Link budgetsLink = linkTestUtils.discoverUnique(profileLink.getHref(), "budgets");
		assertThat(ordersLink, is(nullValue()));
	}

	@Test
	public void testUserCredentialsForAlpsSecuredPeople() throws Exception {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER")));

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		Link peopleLink = linkTestUtils.discoverUnique(profileLink.getHref(), "people");

		assertThat(peopleLink, is(notNullValue()));

		mvc.perform(get(peopleLink.getHref())).//
				andDo(print()).//
				andExpect(jsonPath("$.descriptors[*].id", hasItems("get-people", "get-person", "create-people",
				"update-person", "patch-person"))).//
				andExpect(jsonPath("$.descriptors[*].id", not(hasItems("delete-person"))));
	}

	@Test
	public void testUserCredentialsForAlpsPreAuthorizedOrders() throws Exception {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER")));

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		Link ordersLink = linkTestUtils.discoverUnique(profileLink.getHref(), "orders");

		assertThat(ordersLink, is(notNullValue()));

		mvc.perform(get(ordersLink.getHref())).//
				andDo(print()).//
				andExpect(jsonPath("$.descriptors[*].id", hasItems("get-orders", "get-order", "create-orders",
				"update-order", "patch-order"))).//
				andExpect(jsonPath("$.descriptors[*].id", not(hasItems("delete-order"))));
	}

	@Test
	public void testAdminCredentialsForAlpsSecuredPeople() throws Exception {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")));

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		Link peopleLink = linkTestUtils.discoverUnique(profileLink.getHref(), "people");

		assertThat(peopleLink, is(notNullValue()));

		mvc.perform(get(peopleLink.getHref())).//
				andDo(print()).//
				andExpect(jsonPath("$.descriptors[*].id", hasItems("get-people", "get-person", "create-people",
				"update-person", "patch-person", "delete-person")));
	}

	@Test
	public void testAdminCredentialsForAlpsPreAuthorizedOrders() throws Exception {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")));

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		Link ordersLink = linkTestUtils.discoverUnique(profileLink.getHref(), "orders");

		assertThat(ordersLink, is(notNullValue()));

		mvc.perform(get(ordersLink.getHref())).//
				andDo(print()).//
				andExpect(jsonPath("$.descriptors[*].id", hasItems("get-orders", "get-order", "create-orders",
				"update-order", "patch-order", "delete-order")));
	}

	//=================================================================

	@Test(expected = AuthenticationCredentialsNotFoundException.class)
	public void testNoCredentialsOnNonSpringDataClassLevelSecurity() {
		pojo.nothing();
	}

	@Test(expected = AccessDeniedException.class)
	public void testUserCredentialsOnNonSpringDataClassLevelSecurity() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER")));
		pojo.nothing();
	}

	@Test
	public void testAdminCredentialsOnNonSpringDataClassLevelSecurity() {

		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "user",
				AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")));
		pojo.nothing();
	}

}
