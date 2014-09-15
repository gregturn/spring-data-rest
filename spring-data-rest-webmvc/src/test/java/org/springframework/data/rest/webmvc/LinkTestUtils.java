package org.springframework.data.rest.webmvc;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

public class LinkTestUtils {

	private final MockMvc mvc;
	private final LinkDiscoverers discoverers;


	public LinkTestUtils(MockMvc mvc, LinkDiscoverers discoverers) {
		this.mvc = mvc;
		this.discoverers = discoverers;
	}


	public ResultActions follow(Link link) throws Exception {
		return follow(link.expand().getHref());
	}

	public ResultActions follow(String href) throws Exception {
		return mvc.perform(get(href));
	}

	public List<Link> discover(String rel) throws Exception {
		return discover(new Link("/"), rel);
	}

	public Link discoverUnique(String rel) throws Exception {

		List<Link> discover = discover(rel);
		assertThat(discover, hasSize(1));
		return discover.get(0);
	}

	public List<Link> discover(Link root, String rel) throws Exception {

		MockHttpServletResponse response = mvc.perform(get(root.expand().getHref()).accept(WebTestUtils.DEFAULT_MEDIA_TYPE)).//
				andExpect(status().isOk()).//
				andExpect(hasLinkWithRel(rel)).//
				andReturn().getResponse();

		String s = response.getContentAsString();
		return getDiscoverer(response).findLinksWithRel(rel, s);
	}

	public Link discoverUnique(Link root, String rel) throws Exception {

		MockHttpServletResponse response = mvc.perform(get(root.expand().getHref()).accept(WebTestUtils.DEFAULT_MEDIA_TYPE)).//
				andExpect(status().isOk()).//
				andExpect(hasLinkWithRel(rel)).//
				andReturn().getResponse();

		return assertHasLinkWithRel(rel, response);
	}

	public Link assertHasLinkWithRel(String rel, MockHttpServletResponse response) throws Exception {

		String content = response.getContentAsString();
		Link link = getDiscoverer(response).findLinkWithRel(rel, content);

		assertThat("Expected to find link with rel " + rel + " but found none in " + content + "!", link,
				is(notNullValue()));

		return link;
	}

	public ResultMatcher hasLinkWithRel(final String rel) {

		return new ResultMatcher() {

			@Override
			public void match(MvcResult result) throws Exception {

				MockHttpServletResponse response = result.getResponse();
				String s = response.getContentAsString();

				assertThat("Expected to find link with rel " + rel + " but found none in " + s, //
						getDiscoverer(response).findLinkWithRel(rel, s), notNullValue());
			}
		};
	}

	public LinkDiscoverer getDiscoverer(MockHttpServletResponse response) {

		String contentType = response.getContentType();
		LinkDiscoverer linkDiscovererFor = discoverers.getLinkDiscovererFor(contentType);

		assertThat("Did not find a LinkDiscoverer for returned media type " + contentType + "!", linkDiscovererFor,
				is(notNullValue()));

		return linkDiscovererFor;
	}

}
