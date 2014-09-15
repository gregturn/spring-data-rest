/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.data.rest.webmvc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Helper methods for web integration testing.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class WebTestUtils {

	public static MediaType DEFAULT_MEDIA_TYPE = org.springframework.hateoas.MediaTypes.HAL_JSON;

	private MockMvc mvc;

	public WebTestUtils(MockMvc mvc) {
		this.mvc = mvc;
	}

	/**
	 * Initializes web tests. Will register a {@link MockHttpServletRequest} for the current thread.
	 */
	public static void initWebTest() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);
	}

	public static void assertAllowHeaders(HttpEntity<?> response, HttpMethod... methods) {

		HttpHeaders headers = response.getHeaders();

		assertThat(headers.getAllow(), hasSize(methods.length));
		assertThat(headers.getAllow(), hasItems(methods));
	}

	public MockHttpServletResponse request(String href, MediaType contentType) throws Exception {
		return mvc.perform(get(href).accept(contentType)). //
				andExpect(status().isOk()). //
				andExpect(content().contentType(contentType)). //
				andReturn().getResponse();
	}

	public MockHttpServletResponse request(Link link) throws Exception {
		return request(link.expand().getHref());
	}

	public MockHttpServletResponse request(Link link, MediaType mediaType) throws Exception {
		return request(link.expand().getHref(), mediaType);
	}

	public MockHttpServletResponse request(String href) throws Exception {
		return request(href, DEFAULT_MEDIA_TYPE);
	}


}
