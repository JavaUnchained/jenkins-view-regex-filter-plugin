package io.jenkins.plugins.sample;

import hudson.model.ListView;
import hudson.model.View;
import hudson.views.ViewJobFilter;
import org.junit.Test;
import org.jvnet.hudson.test.WithoutJenkins;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static io.jenkins.plugins.sample.AbstractIncludeExcludeJobFilter.IncludeExcludeType.excludeMatched;
import static io.jenkins.plugins.sample.AbstractIncludeExcludeJobFilter.IncludeExcludeType.includeMatched;
import static io.jenkins.plugins.sample.RegExJobFilter.ValueType.BUILD_VERSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class RegExJobFilterTest extends AbstractJenkinsTest {

	@Test
	@WithoutJenkins
	public void testBackwardsCompatibleDeserialization() throws IOException {
		InputStream xml = RegExJobFilter.class.getResourceAsStream("/RegExJobFilterTest/view.xml");
		ListView listView = (ListView) View.createViewFromXML("foo", xml);

		RegExJobFilter filter = (RegExJobFilter) listView.getJobFilters().iterator().next();
		assertThat(filter.getIncludeExcludeTypeString(), is(includeMatched.name()));
		assertThat(filter.getValueTypeString(), is(BUILD_VERSION.name()));
		assertThat(filter.getRegex(), is(".*"));
		assertThat(filter.isMatchName(), is(true));
		assertThat(filter.isMatchFullName(), is(false));
	}

	@Test
	public void testConfigRoundtrip() throws Exception {
		testConfigRoundtrip(
				"regex-view-1",
				new RegExJobFilter("NaMeRegEx", excludeMatched.name(), BUILD_VERSION.name(),
					false, true)
		);
	}

	private void testConfigRoundtrip(String viewName, RegExJobFilter... filters) throws Exception {
		List<RegExJobFilter> expectedFilters = new ArrayList<RegExJobFilter>();
		for (RegExJobFilter filter: filters) {
			expectedFilters.add(new RegExJobFilter(filter.getRegex(),
				filter.getIncludeExcludeTypeString(),
				filter.getValueTypeString(),
				filter.isMatchName(),
				filter.isMatchFullName()));
		}

		ListView view = createFilteredView(viewName, filters);
		j.configRoundtrip(view);

		ListView viewAfterRoundtrip = (ListView)j.getInstance().getView(viewName);
		assertFilterEquals(expectedFilters, viewAfterRoundtrip.getJobFilters());

		viewAfterRoundtrip.save();
		j.getInstance().reload();

		ListView viewAfterReload = (ListView)j.getInstance().getView(viewName);
		assertFilterEquals(expectedFilters, viewAfterReload.getJobFilters());
	}

	private void assertFilterEquals(List<RegExJobFilter> expectedFilters, List<ViewJobFilter> actualFilters) {
		assertThat(actualFilters.size(), is(expectedFilters.size()));
		for (int i = 0; i < actualFilters.size(); i++) {
			ViewJobFilter actualFilter = actualFilters.get(i);
			RegExJobFilter expectedFilter = expectedFilters.get(i);
			assertThat(actualFilter, instanceOf(RegExJobFilter.class));
			assertThat(((RegExJobFilter)actualFilter).getRegex(), is(expectedFilter.getRegex()));
			assertThat(((RegExJobFilter)actualFilter).getIncludeExcludeTypeString(), is(expectedFilter.getIncludeExcludeTypeString()));
			assertThat(((RegExJobFilter)actualFilter).getValueTypeString(), is(expectedFilter.getValueTypeString()));
			assertThat(((RegExJobFilter)actualFilter).isMatchName(), is(expectedFilter.isMatchName()));
			assertThat(((RegExJobFilter)actualFilter).isMatchFullName(), is(expectedFilter.isMatchFullName()));
		}
	}
}
