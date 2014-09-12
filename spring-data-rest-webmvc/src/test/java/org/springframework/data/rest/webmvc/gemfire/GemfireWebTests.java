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
package org.springframework.data.rest.webmvc.gemfire;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.data.rest.webmvc.AbstractWebIntegrationTests;
import org.springframework.data.rest.webmvc.mongodb.Receipt;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Oliver Gierke
 */
@ContextConfiguration(classes = GemfireRepositoryConfig.class)
public class GemfireWebTests extends AbstractWebIntegrationTests {

	ObjectMapper mapper = new ObjectMapper();

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.data.rest.webmvc.AbstractWebIntegrationTests#expectedRootLinkRels()
	 */
	@Override
	protected Iterable<String> expectedRootLinkRels() {
		return Arrays.asList("products");
	}

	@Test
	public void returnConflictWhenConcurrentlyEditingVersionedEntity() throws Exception {
		Link receiptLink = discoverUnique("receipts");

		Receipt receipt = new Receipt();
		receipt.setId("key");
		receipt.setAmount(new BigDecimal(50));
		receipt.setSaleItem("Springy Tacos");

		String stringReceipt = mapper.writeValueAsString(receipt);

		MockHttpServletResponse createdReceipt = postAndGet(receiptLink, stringReceipt, MediaType.APPLICATION_JSON);
		Link tacosLink = assertHasLinkWithRel("self", createdReceipt);
		assertJsonPathEquals("$.saleItem","Springy Tacos", createdReceipt);

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(tacosLink.getHref());
		String concurrencyTag = createdReceipt.getHeader("ETag");

		mvc.perform(patch(builder.build().toUriString())
				.content("{ \"saleItem\" : \"SpringyBurritos\" }").contentType(MediaType.APPLICATION_JSON)
				.header("If-Match",concurrencyTag))
				.andExpect(status().isNoContent());

		mvc.perform(patch(builder.build().toUriString())
				.content("{ \"saleItem\" : \"SpringyTequila\" }").contentType(MediaType.APPLICATION_JSON)
				.header("If-Match","\"falseETag\""))
				.andExpect(status().isConflict());
	}

}
