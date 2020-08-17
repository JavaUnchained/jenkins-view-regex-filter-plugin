package io.jenkins.plugins.sample;

import hudson.model.*;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.tasks.Maven;
import hudson.util.DescribableList;
import jenkins.model.ParameterizedJobMixIn;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import static io.jenkins.plugins.sample.JobType.FREE_STYLE_PROJECT;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class JobMocker<T extends Job> {

    public enum MavenBuildStep {
        PRE, POST
    }

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

    public JobMocker<T> result(Result result) {
        Build lastBuild = mock(Build.class);
        when(lastBuild.getResult()).thenReturn(result);
        when(job.getLastCompletedBuild()).thenReturn(lastBuild);
        return this;
    }

    public JobMocker<T> disabled(boolean disabled) {
        if (job instanceof ParameterizedJobMixIn.ParameterizedJob) {
            when(((ParameterizedJobMixIn.ParameterizedJob) job).isDisabled()).thenReturn(disabled);
        }
        return this;
    }

    public JobMocker<T> building(boolean building) {
        when(job.isBuilding()).thenReturn(building);
        return this;
    }

    public JobMocker<T> inQueue(boolean inQueue) {
        when(job.isInQueue()).thenReturn(inQueue);
        return this;
    }

    public JobMocker<T> lastBuild(AbstractBuild build) {
        when(job.getLastBuild()).thenReturn(build);
        return this;
    }

    public JobMocker<T> lastBuilds(AbstractBuild... builds) {
        if (builds.length > 0) {
            lastBuild(builds[0]);
            for (int i = 0; i < builds.length - 1; i++) {
                when(builds[i].getPreviousBuild()).thenReturn(builds[i + 1]);
            }
        }
        return this;
    }

    public JobMocker<T> permissions(Permission... permissions) {
        ACL acl = mock(ACL.class);
        for (Permission permission: permissions) {
            when(acl.hasPermission(permission)).thenReturn(true);
        }
        when(job.getACL()).thenReturn(acl);
        return this;
    }

    private Maven mockMaven(final String targets, final String name, final String properties, final String opts) {
        final Maven.MavenInstallation mavenInstallation = mock(Maven.MavenInstallation.class);
        when(mavenInstallation.getName()).thenReturn(name);

        return new Maven(targets, name, "", properties, opts) {
            @Override
            public MavenInstallation getMaven() {
                return mavenInstallation;
            }
        };
    }

    public JobMocker<T> assignedLabel(String label) {
        if (job instanceof AbstractProject) {
            when(((AbstractProject)job).getAssignedLabelString()).thenReturn(label);
        }
        return this;
    }

    public JobMocker<T> parameters(ParameterDefinition... definitions) {
        ParametersDefinitionProperty prop = new ParametersDefinitionProperty(definitions);
        when(job.getProperty(ParametersDefinitionProperty.class)).thenReturn(prop);
        return this;
    }

    public JobMocker<T> upstream(AbstractProject... upstreamProjects) {
        if (job instanceof AbstractProject) {
            when(((AbstractProject)job).getUpstreamProjects()).thenReturn(asList(upstreamProjects));
        }
        return this;
    }

    public JobMocker<T> downstream(AbstractProject... downstreamProjects) {
        if (job instanceof AbstractProject) {
            when(((AbstractProject)job).getDownstreamProjects()).thenReturn(asList(downstreamProjects));
        }
        return this;
    }

    public T asJob() {
        return job;
    }

    public TopLevelItem asItem() {
        return (TopLevelItem)job;
    }
}
