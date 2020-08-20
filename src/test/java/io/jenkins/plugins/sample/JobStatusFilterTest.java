package io.jenkins.plugins.sample;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.TopLevelItem;
import hudson.views.ViewJobFilter;
import io.jenkins.plugins.sample.TestHelpers.AbstractJenkinsTest;
import io.jenkins.plugins.sample.TestHelpers.JobType;
import org.junit.Test;
import org.jvnet.hudson.test.WithoutJenkins;

import java.util.List;

import static io.jenkins.plugins.sample.TestHelpers.JobMocker.jobOfType;
import static io.jenkins.plugins.sample.TestHelpers.JobType.*;
import static io.jenkins.plugins.sample.TestHelpers.ViewJobFilters.jobStatus;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class JobStatusFilterTest extends AbstractJenkinsTest {

	@Test
	@WithoutJenkins
	public void testMatch() {
		assertFalse(jobStatus(true, true, true, true, true).matches(mock(TopLevelItem.class)));

		for (JobType<? extends Job> type: availableJobTypes(FREE_STYLE_PROJECT, MATRIX_PROJECT)) {
			assertFalse(jobStatus(true, true, true, true, true).matches(jobOfType(type).asItem()));

			assertTrue(jobStatus(true, false, false, false, false).matches(jobOfType(type).result(Result.UNSTABLE).asItem()));
			assertFalse(jobStatus(true, false, false, false, false).matches(jobOfType(type).result(Result.FAILURE).asItem()));
			assertFalse(jobStatus(true, false, false, false, false).matches(jobOfType(type).result(Result.ABORTED).asItem()));
			assertFalse(jobStatus(true, false, false, false, false).matches(jobOfType(type).result(Result.SUCCESS).asItem()));
			assertFalse(jobStatus(true, false, false, false, false).matches(jobOfType(type).disabled(true).asItem()));

			assertFalse(jobStatus(false, true, false, false, false).matches(jobOfType(type).result(Result.UNSTABLE).asItem()));
			assertTrue(jobStatus(false, true, false, false, false).matches(jobOfType(type).result(Result.FAILURE).asItem()));
			assertFalse(jobStatus(false, true, false, false, false).matches(jobOfType(type).result(Result.ABORTED).asItem()));
			assertFalse(jobStatus(false, true, false, false, false).matches(jobOfType(type).result(Result.SUCCESS).asItem()));
			assertFalse(jobStatus(false, true, false, false, false).matches(jobOfType(type).disabled(true).asItem()));

			assertFalse(jobStatus(false, false, true, false, false).matches(jobOfType(type).result(Result.UNSTABLE).asItem()));
			assertFalse(jobStatus(false, false, true, false, false).matches(jobOfType(type).result(Result.FAILURE).asItem()));
			assertTrue(jobStatus(false, false, true, false, false).matches(jobOfType(type).result(Result.ABORTED).asItem()));
			assertFalse(jobStatus(false, false, true, false, false).matches(jobOfType(type).result(Result.SUCCESS).asItem()));
			assertFalse(jobStatus(false, false, true, false, false).matches(jobOfType(type).disabled(true).asItem()));

			assertFalse(jobStatus(false, false, false, false, true).matches(jobOfType(type).result(Result.UNSTABLE).asItem()));
			assertFalse(jobStatus(false, false, false, false, true).matches(jobOfType(type).result(Result.FAILURE).asItem()));
			assertFalse(jobStatus(false, false, false, false, true).matches(jobOfType(type).result(Result.ABORTED).asItem()));
			assertTrue(jobStatus(false, false, false, false, true).matches(jobOfType(type).result(Result.SUCCESS).asItem()));
			assertFalse(jobStatus(false, false, false, false, true).matches(jobOfType(type).disabled(true).asItem()));

			assertTrue("works on " + type.getJobClass().getSimpleName(), jobStatus(false, false, false, true, false).matches(jobOfType(type).disabled(true).asItem()));
			assertFalse(jobStatus(false, false, false, true, false).matches(jobOfType(type).disabled(false).asItem()));

			assertTrue(jobStatus(true, true, false, false, false).matches(jobOfType(type).result(Result.UNSTABLE).asItem()));
			assertTrue(jobStatus(true, true, false, false, false).matches(jobOfType(type).result(Result.FAILURE).asItem()));
			assertFalse(jobStatus(true, true, false, false, false).matches(jobOfType(type).result(Result.ABORTED).asItem()));
			assertFalse(jobStatus(true, true, false, false, false).matches(jobOfType(type).result(Result.SUCCESS).asItem()));
		}
	}

	private void assertFilterEquals(List<JobStatusFilter> expectedFilters, List<ViewJobFilter> actualFilters) {
		assertThat(actualFilters.size(), is(expectedFilters.size()));
		for (int i = 0; i < actualFilters.size(); i++) {
			ViewJobFilter actualFilter = actualFilters.get(i);
			JobStatusFilter expectedFilter = expectedFilters.get(i);
			assertThat(actualFilter, instanceOf(JobStatusFilter.class));
			assertThat(((JobStatusFilter)actualFilter).isAborted(), is(expectedFilter.isAborted()));
			assertThat(((JobStatusFilter)actualFilter).isDisabled(), is(expectedFilter.isDisabled()));
			assertThat(((JobStatusFilter)actualFilter).isFailed(), is(expectedFilter.isFailed()));
			assertThat(((JobStatusFilter)actualFilter).isStable(), is(expectedFilter.isStable()));
			assertThat(((JobStatusFilter)actualFilter).isUnstable(), is(expectedFilter.isUnstable()));
			assertThat(((JobStatusFilter)actualFilter).getIncludeExcludeTypeString(), is(expectedFilter.getIncludeExcludeTypeString()));
		}
	}

}
