package io.jenkins.plugins.sample;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Job;
import hudson.model.ListView;
import hudson.model.TopLevelItem;
import hudson.model.View;
import hudson.views.ViewJobFilter;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.jvnet.hudson.test.WithoutJenkins;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static io.jenkins.plugins.sample.AbstractIncludeExcludeJobFilter.IncludeExcludeType.*;
import static io.jenkins.plugins.sample.JobMocker.freeStyleProject;
import static io.jenkins.plugins.sample.JobMocker.jobOfType;
import static io.jenkins.plugins.sample.JobType.*;
import static io.jenkins.plugins.sample.RegExJobFilter.ValueType.BUILD_VERSION;
import static io.jenkins.plugins.sample.ViewJobFilters.NameOptions.*;
import static io.jenkins.plugins.sample.ViewJobFilters.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegExJobFilterTest extends AbstractJenkinsTest {

//	@Test
//	@WithoutJenkins
//	public void testName() {
//		assertFalse(nameRegex(".*", MATCH_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));
//		assertFalse(nameRegex(".*", MATCH_FULL_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));
//
//		for (JobType<? extends Job> type: availableJobTypes(FREE_STYLE_PROJECT, MATRIX_PROJECT)) {
//			assertFalse(nameRegex(".*", MATCH_NAME).matches(jobOfType(type).name(null).asItem()));
//			assertTrue(nameRegex(".*", MATCH_NAME).matches(jobOfType(type).name("").asItem()));
//			assertTrue(nameRegex("Foo", MATCH_NAME).matches(jobOfType(type).name("Foo").asItem()));
//			assertFalse(nameRegex("Foo", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));
//			assertTrue(nameRegex("Foo.*", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));
//			assertFalse(nameRegex("bar", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));
//			assertTrue(nameRegex(".*bar", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));
//			assertTrue(nameRegex(".ooba.", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));
//
//			assertFalse(nameRegex(".*", MATCH_FULL_NAME).matches(jobOfType(type).fullName(null).asItem()));
//			assertTrue(nameRegex(".*", MATCH_FULL_NAME).matches(jobOfType(type).fullName("").asItem()));
//			assertTrue(nameRegex("folder/Foo", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foo").asItem()));
//			assertFalse(nameRegex("folder/Foo", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
//			assertTrue(nameRegex("folder/Foo.*", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
//			assertFalse(nameRegex("folder/bar", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
//			assertTrue(nameRegex("folder/.*bar", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
//			assertTrue(nameRegex("folder/.ooba.", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
//			assertTrue(nameRegex(".*der/Foobar", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
//		}
//	}

//	@Test
//	@WithoutJenkins
//	public void testBackwardsCompatibleDeserialization() throws IOException {
//		InputStream xml = RegExJobFilter.class.getResourceAsStream("/RegExJobFilterTest/view.xml");
//		ListView listView = (ListView) View.createViewFromXML("foo", xml);
//
//		RegExJobFilter filter = (RegExJobFilter) listView.getJobFilters().iterator().next();
//		assertThat(filter.getIncludeExcludeTypeString(), is(includeMatched.name()));
//		assertThat(filter.getValueTypeString(), is(BUILD_VERSION.name()));
//		assertThat(filter.getRegex(), is(".*"));
//		assertThat(filter.isMatchName(), is(true));
//		assertThat(filter.isMatchFullName(), is(false));
//	}

	@Test
	public void testConfigRoundtrip() throws Exception {
		testConfigRoundtrip(
				"regex-view-1",
				new RegExJobFilter("NaMeRegEx", excludeMatched.name(), BUILD_VERSION.name(),
					false, true)
		);
	}

//	@Test
//	public void testHelpExample() {
//		List<TopLevelItem> all = asList(
//				freeStyleProject().name("0-Test_Job").asItem(),
//				freeStyleProject().name("1-Test_Job").trigger("@midnight").asItem(),
//				freeStyleProject().name("2-Job").asItem(),
//				freeStyleProject().name("3-Test_Job").trigger("@daily").asItem(),
//				freeStyleProject().name("4-Job").trigger("@midnight").asItem(),
//				freeStyleProject().name("5-Test_Job").trigger("@midnight").asItem(),
//				freeStyleProject().name("6-Test_Job").asItem()
//		);
//
//		List<TopLevelItem> filtered = new ArrayList<TopLevelItem>();
//
//		RegExJobFilter includeTests = new RegExJobFilter(".*Test.*", includeMatched.name(), BUILD_VERSION.name());
//		filtered = includeTests.filter(filtered, all, null);
//		assertThat(filtered, is(asList(
//				all.get(0),
//				all.get(1),
//				all.get(3),
//				all.get(5),
//				all.get(6)
//		)));
//	}

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
