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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.AbstractWebIntegrationTests;
import org.springframework.data.rest.webmvc.alps.RootResourceInformationToAlpsDescriptorConverter;
import org.springframework.data.rest.webmvc.jpa.JpaRepositoryConfig;
import org.springframework.data.rest.webmvc.jpa.PersonRepository;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.core.JsonPathLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test Spring Data REST in the context of being locked down by Spring Security
 *
 * @author Greg Turnquist
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaRepositoryConfig.class, NoSecurityIntegrationTests.Config.class})
@Transactional
public class NoSecurityIntegrationTests extends AbstractWebIntegrationTests {

	@Autowired ApplicationContext context;
	@Autowired NoSecurityChecker securityChecker;

	@Autowired PersonRepository personRepository;
	@Autowired RootResourceInformationToAlpsDescriptorConverter alpsDescriptorConverter;

	@Configuration
	static class Config {

		@Bean
		public NoSecurityChecker securityChecker() {
			return new NoSecurityChecker();
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
		alpsDescriptorConverter.setSecurityChecker(securityChecker);
	}

	@Test
	public void testSecuritySettings() {
		assertThat(securityChecker.secured(), is(false));
	}

	@Test
	public void testThatPersonDeleteAllIsAvailable() {
		personRepository.deleteAll();
	}

	@Test
	public void testThatPersonFindAllIsAvailable() {
		personRepository.findAll();
	}

	@Test
	public void testThatAllRootLinksAreVisible() throws Exception {

		Link peopleLink = linkTestUtils.discoverUnique("/", "people");
		assertThat(peopleLink, is(notNullValue()));

		Link ordersLink = linkTestUtils.discoverUnique("/", "orders");
		assertThat(peopleLink, is(notNullValue()));

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		assertThat(profileLink, is(notNullValue()));
	}

	@Test
	public void testAlpsPeople() throws Exception {

		Link profileLink = linkTestUtils.discoverUnique("/", "profile");
		Link peopleLink = linkTestUtils.discoverUnique(profileLink.getHref(), "people");

		mvc.perform(get(peopleLink.getHref())).//
				andDo(print()).//
				andExpect(jsonPath("$.descriptors[*].id", hasItems("get-people", "get-person", "create-people",
				"update-person", "patch-person", "delete-person")));
	}

}
