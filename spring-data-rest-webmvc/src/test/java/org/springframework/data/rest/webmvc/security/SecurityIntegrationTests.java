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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.AbstractControllerIntegrationTests;
import org.springframework.data.rest.webmvc.ResourceType;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.data.rest.webmvc.jpa.Book;
import org.springframework.data.rest.webmvc.jpa.JpaRepositoryConfig;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test Spring Data REST in the context of being locked down by Spring Security
 *
 * @author Greg Turnquist
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JpaRepositoryConfig.class, SecurityIntegrationTests.SecurityConfiguration.class})
@Transactional
public class SecurityIntegrationTests extends AbstractControllerIntegrationTests {

	@Autowired AbstractSecurityChecker securityChecker;

	@Test
	public void testSecuredRoot() {

		assertThat(securityChecker.secured(), is(true));

		RootResourceInformation info = getResourceInformation(Book.class);
		assertThat(info.supports(HttpMethod.GET, ResourceType.COLLECTION), is(true));
		assertThat(info.supports(HttpMethod.POST, ResourceType.COLLECTION), is(true));
	}

	@Configuration
	static class SecurityConfiguration {

		@Bean
		public SecurityChecker securityChecker() {
			return new SecurityChecker();
		}

	}

}
