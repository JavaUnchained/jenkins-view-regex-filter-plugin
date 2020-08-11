package io.jenkins.plugins.sample;

import hudson.model.*;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.DescribableList;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.jenkins.plugins.sample.JobType.*;
import static org.mockito.Mockito.*;

public class JobMocker<T extends Job> {


    T job;

    public JobMocker(Class<T> jobClass, Class... interfaces) {
        MockSettings settings = withSettings();
        if (interfaces.length > 0) {
            settings = settings.extraInterfaces(interfaces);
        }
        this.job = Mockito.mock(jobClass, settings.defaultAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (DescribableList.class.isAssignableFrom(invocationOnMock.getMethod().getReturnType())) {
                    return new DescribableList(mock(Saveable.class), new ArrayList());
                }
                return null;
            }
        }));
    }

    public static <C extends Job> JobMocker<C> jobOfType(JobType<C> jobType) {
        return new JobMocker(jobType.getJobClass(), jobType.getInterfaces());
    }

    public static JobMocker<FreeStyleProject> freeStyleProject() {
        return jobOfType(FREE_STYLE_PROJECT);
    }

    public JobMocker<T> name(String name) {
        when(job.getName()).thenReturn(name);
        when(job.toString()).thenReturn(name);
        return this;
    }

    public JobMocker<T> fullName(String fullName) {
        when(job.getFullName()).thenReturn(fullName);
        return this;
    }

    public JobMocker<T> displayName(String fullName) {
        when(job.getDisplayName()).thenReturn(fullName);
        return this;
    }

    public JobMocker<T> fullDisplayName(String fullDisplayName) {
        when(job.getFullDisplayName()).thenReturn(fullDisplayName);
        return this;
    }

    public JobMocker<T> parent(final ItemGroup parent) {
        when(job.getParent()).thenAnswer(new Answer<ItemGroup>() {
            @Override
            public ItemGroup answer(InvocationOnMock invocationOnMock) throws Throwable {
                return parent;
            }
        });
        return this;
    }

    public JobMocker<T> desc(String name) {
        when(job.getDescription()).thenReturn(name);
        return this;
    }

    public JobMocker<T> trigger(String spec) {
        Trigger trigger = mock(Trigger.class);
        when(trigger.getSpec()).thenReturn(spec);

        Map<TriggerDescriptor, Trigger<?>> triggers = new HashMap<TriggerDescriptor, Trigger<?>>();
        triggers.put(mock(TriggerDescriptor.class), trigger);

        if (job instanceof AbstractProject) { // TODO replace this and next by ParameterizedJobMixIn.ParameterizedJob
            when(((AbstractProject)job).getTriggers()).thenReturn(triggers);
        }
        if (instanceOf(job, WORKFLOW_JOB)) {
            when(((WorkflowJob)job).getTriggers()).thenReturn(triggers);
        }
        return this;
    }

    public JobMocker<T> assignedLabel(String label) {
        if (job instanceof AbstractProject) {
            when(((AbstractProject)job).getAssignedLabelString()).thenReturn(label);
        }
        return this;
    }

    public TopLevelItem asItem() {
        return (TopLevelItem)job;
    }
}
