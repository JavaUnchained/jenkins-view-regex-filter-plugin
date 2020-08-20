package io.jenkins.plugins.sample;

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.Job;
import hudson.model.ListView;
import hudson.model.View;
import hudson.views.ViewJobFilter;
import io.jenkins.plugins.sample.TestHelpers.AbstractJenkinsTest;
import io.jenkins.plugins.sample.TestHelpers.JobType;
import jenkins.model.Jenkins;
import org.junit.Test;
import org.jvnet.hudson.test.WithoutJenkins;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static io.jenkins.plugins.sample.AbstractIncludeExcludeJobFilter.IncludeExcludeType.excludeMatched;
import static io.jenkins.plugins.sample.AbstractIncludeExcludeJobFilter.IncludeExcludeType.includeMatched;
import static io.jenkins.plugins.sample.TestHelpers.JobMocker.jobOfType;
import static io.jenkins.plugins.sample.TestHelpers.JobType.*;
import static io.jenkins.plugins.sample.RegExJobFilter.ValueType.BUILD_VERSION;
import static io.jenkins.plugins.sample.TestHelpers.ViewJobFilters.NameOptions.*;
import static io.jenkins.plugins.sample.TestHelpers.ViewJobFilters.folderNameRegex;
import static io.jenkins.plugins.sample.TestHelpers.ViewJobFilters.nameRegex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegExJobFilterTest extends AbstractJenkinsTest {

	@Test
	@WithoutJenkins
	public void testName() {
		assertFalse(nameRegex(".*", MATCH_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));
		assertFalse(nameRegex(".*", MATCH_FULL_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));
		assertFalse(nameRegex(".*", MATCH_DISPLAY_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));
		assertFalse(nameRegex(".*", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));

		for (JobType<? extends Job> type: availableJobTypes(FREE_STYLE_PROJECT, MATRIX_PROJECT)) {
			assertFalse(nameRegex(".*", MATCH_NAME).matches(jobOfType(type).name(null).asItem()));
			assertTrue(nameRegex(".*", MATCH_NAME).matches(jobOfType(type).name("").asItem()));
			assertTrue(nameRegex("Foo", MATCH_NAME).matches(jobOfType(type).name("Foo").asItem()));
			assertFalse(nameRegex("Foo", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));
			assertTrue(nameRegex("Foo.*", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));
			assertFalse(nameRegex("bar", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));
			assertTrue(nameRegex(".*bar", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));
			assertTrue(nameRegex(".ooba.", MATCH_NAME).matches(jobOfType(type).name("Foobar").asItem()));

			assertFalse(nameRegex(".*", MATCH_FULL_NAME).matches(jobOfType(type).fullName(null).asItem()));
			assertTrue(nameRegex(".*", MATCH_FULL_NAME).matches(jobOfType(type).fullName("").asItem()));
			assertTrue(nameRegex("folder/Foo", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foo").asItem()));
			assertFalse(nameRegex("folder/Foo", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
			assertTrue(nameRegex("folder/Foo.*", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
			assertFalse(nameRegex("folder/bar", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
			assertTrue(nameRegex("folder/.*bar", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
			assertTrue(nameRegex("folder/.ooba.", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));
			assertTrue(nameRegex(".*der/Foobar", MATCH_FULL_NAME).matches(jobOfType(type).fullName("folder/Foobar").asItem()));

			assertFalse(nameRegex(".*", MATCH_DISPLAY_NAME).matches(jobOfType(type).displayName(null).asItem()));
			assertTrue(nameRegex(".*", MATCH_DISPLAY_NAME).matches(jobOfType(type).displayName("").asItem()));
			assertTrue(nameRegex("Foo", MATCH_DISPLAY_NAME).matches(jobOfType(type).displayName("Foo").asItem()));
			assertFalse(nameRegex("Foo", MATCH_DISPLAY_NAME).matches(jobOfType(type).displayName("Foobar").asItem()));
			assertTrue(nameRegex("Foo.*", MATCH_DISPLAY_NAME).matches(jobOfType(type).displayName("Foobar").asItem()));
			assertFalse(nameRegex("bar", MATCH_DISPLAY_NAME).matches(jobOfType(type).displayName("Foobar").asItem()));
			assertTrue(nameRegex(".*bar", MATCH_DISPLAY_NAME).matches(jobOfType(type).displayName("Foobar").asItem()));
			assertTrue(nameRegex(".ooba.", MATCH_DISPLAY_NAME).matches(jobOfType(type).displayName("Foobar").asItem()));

			assertFalse(nameRegex(".*", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).fullDisplayName(null).asItem()));
			assertTrue(nameRegex(".*", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).fullDisplayName("").asItem()));
			assertTrue(nameRegex("folder » Foo", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).fullDisplayName("folder » Foo").asItem()));
			assertFalse(nameRegex("folder » Foo", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).fullDisplayName("folder » Foobar").asItem()));
			assertTrue(nameRegex("folder » Foo.*", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).fullDisplayName("folder » Foobar").asItem()));
			assertFalse(nameRegex("folder » bar", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).fullDisplayName("folder » Foobar").asItem()));
			assertTrue(nameRegex("folder » .*bar", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).fullDisplayName("folder » Foobar").asItem()));
			assertTrue(nameRegex("folder » .ooba.", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).fullDisplayName("folder » Foobar").asItem()));
			assertTrue(nameRegex(".*der » Foobar", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).fullDisplayName("folder » Foobar").asItem()));
		}
	}

	@Test
	public void testFolderName() {
		Jenkins jenkins = j.getInstance();

		assertFalse(folderNameRegex(".*", MATCH_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));
		assertFalse(folderNameRegex(".*", MATCH_FULL_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));
		assertFalse(folderNameRegex(".*", MATCH_DISPLAY_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));
		assertFalse(folderNameRegex(".*", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(TOP_LEVEL_ITEM).asItem()));

		for (JobType<? extends Job> type: availableJobTypes(FREE_STYLE_PROJECT, MATRIX_PROJECT)) {
			assertFalse(folderNameRegex(".*", MATCH_NAME).matches(jobOfType(type).parent(new Folder(jenkins, null)).asItem()));
			assertTrue(folderNameRegex(".*", MATCH_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "")).asItem()));
			assertTrue(folderNameRegex("Foo", MATCH_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foo")).asItem()));
			assertFalse(folderNameRegex("Foo", MATCH_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex("Foo.*", MATCH_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertFalse(folderNameRegex("bar", MATCH_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex(".*bar", MATCH_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex(".ooba.", MATCH_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));

			assertFalse(folderNameRegex(".*", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(jenkins, null)).asItem()));
			assertTrue(folderNameRegex(".*", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "")).asItem()));
			assertTrue(folderNameRegex("Foo", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foo")).asItem()));
			assertFalse(folderNameRegex("Foo", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex("Foo.*", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertFalse(folderNameRegex("bar", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex(".*bar", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex(".ooba.", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));

			assertTrue(folderNameRegex("folder/Foo", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foo")).asItem()));
			assertFalse(folderNameRegex("folder/Foo", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertTrue(folderNameRegex("folder/Foo.*", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertFalse(folderNameRegex("folder/bar", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertTrue(folderNameRegex("folder/.*bar", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertTrue(folderNameRegex("folder/.ooba.", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertTrue(folderNameRegex(".*der/Foobar", MATCH_FULL_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));

			assertFalse(folderNameRegex(".*", MATCH_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, null)).asItem()));
			assertTrue(folderNameRegex(".*", MATCH_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "")).asItem()));
			assertTrue(folderNameRegex("Foo", MATCH_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foo")).asItem()));
			assertFalse(folderNameRegex("Foo", MATCH_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex("Foo.*", MATCH_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertFalse(folderNameRegex("bar", MATCH_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex(".*bar", MATCH_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex(".ooba.", MATCH_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));

			assertFalse(folderNameRegex(".*", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, null)).asItem()));
			assertTrue(folderNameRegex(".*", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "")).asItem()));
			assertTrue(folderNameRegex("Foo", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foo")).asItem()));
			assertFalse(folderNameRegex("Foo", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex("Foo.*", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertFalse(folderNameRegex("bar", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex(".*bar", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));
			assertTrue(folderNameRegex(".ooba.", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(jenkins, "Foobar")).asItem()));

			assertTrue(folderNameRegex("folder » Foo", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foo")).asItem()));
			assertFalse(folderNameRegex("folder » Foo", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertTrue(folderNameRegex("folder » Foo.*", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertFalse(folderNameRegex("folder » bar", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertTrue(folderNameRegex("folder » .*bar", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertTrue(folderNameRegex("folder » .ooba.", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
			assertTrue(folderNameRegex(".*der » Foobar", MATCH_FULL_DISPLAY_NAME).matches(jobOfType(type).parent(new Folder(new Folder(jenkins, "folder"), "Foobar")).asItem()));
		}
	}

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
					false, true, false, true)
		);
	}

	private void testConfigRoundtrip(String viewName, RegExJobFilter... filters) throws Exception {
		List<RegExJobFilter> expectedFilters = new ArrayList<RegExJobFilter>();
		for (RegExJobFilter filter: filters) {
			expectedFilters.add(new RegExJobFilter(filter.getRegex(),
				filter.getIncludeExcludeTypeString(),
				filter.getValueTypeString(),
				filter.isMatchName(),
				filter.isMatchFullName(),
					filter.isMatchDisplayName(),
					filter.isMatchFullDisplayName()));
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
