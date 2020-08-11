package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.Util;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TopLevelItem;
import hudson.util.FormValidation;
import hudson.util.RunList;
import hudson.views.ViewJobFilter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExJobFilter extends AbstractIncludeExcludeJobFilter {

    public enum ValueType {
        BUILD_VERSION {
            @Override
            void doGetMatchValues(TopLevelItem item, Options options, List<String> values) {
                if (item.getAllJobs() != null) {
                    ArrayList<Job> jobs = new ArrayList<>(item.getAllJobs());
                    if(options.matchName){
                        for (Job job : jobs) {
                            RunList runList = job.getBuilds();
                            Iterator iterator = runList.listIterator();
                            while (iterator.hasNext()) {
                                Run run = (Run) iterator.next();
                                values.add(run.getFullDisplayName());
                            }
                        }
                    }
                    if(options.matchFullName){
                        for (Job job : jobs) {
                            values.add(job.getLastBuild().getFullDisplayName());
                        }
                    }
                }
            }
        };

        abstract void doGetMatchValues(TopLevelItem item, Options options, List<String> values);

        public List<String> getMatchValues(TopLevelItem item, Options options) {
            List<String> values = new ArrayList<String>();
            doGetMatchValues(item, options, values);
            return values;
        }
    }

    public static class Options {
        public final boolean matchName;
        public final boolean matchFullName;

        public Options(boolean matchName, boolean matchFullName) {
            this.matchName = matchName;
            this.matchFullName = matchFullName;
        }
    }

    transient private ValueType valueType;
    private String valueTypeString;
    private String regex;
    transient private Pattern pattern;
    private boolean matchName;
    private boolean matchFullName;

    public RegExJobFilter(String regex, String includeExcludeTypeString, String valueTypeString) {
        this(regex, includeExcludeTypeString, valueTypeString, true, false);
    }

    @DataBoundConstructor
    public RegExJobFilter(String regex, String includeExcludeTypeString, String valueTypeString,
                          boolean matchName, boolean matchFullName) {
        super(includeExcludeTypeString);
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.valueTypeString = valueTypeString;
        this.valueType = ValueType.valueOf(valueTypeString);
        this.matchName = matchName;
        this.matchFullName = matchFullName;
        initOptions();
    }

    private void initOptions() {
        if (!this.matchName && !this.matchFullName) {
            this.matchName = true;
        }
    }

    Object readResolve() {
        if (regex != null) {
            pattern = Pattern.compile(regex);
        }
        if (valueTypeString != null) {
            valueType = ValueType.valueOf(valueTypeString);
        }
        initOptions();
        return super.readResolve();
    }

    public boolean matches(TopLevelItem item) {
        List<String> matchValues = valueType.getMatchValues(item, getOptions());
        for (String matchValue: matchValues) {
            if (matchValue != null &&
                    pattern.matcher(matchValue).matches()) {
                return true;
            }
        }
        return false;
    }

    public String getValueTypeString() {
        return valueTypeString;
    }

    public String getRegex() {
        return regex;
    }

    public boolean isMatchFullName() {
        return matchFullName;
    }

    public boolean isMatchName() {
        return matchName;
    }

    public Options getOptions() {
        return new Options(matchName, matchFullName);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<ViewJobFilter> {
        @Override
        public String getDisplayName() {
            return io.jenkins.plugins.sample.filters.Messages.RegExJobFilter_DisplayName();
        }

        public FormValidation doCheckRegex(@QueryParameter String value ) throws IOException, ServletException, InterruptedException  {
            String v = Util.fixEmpty(value);
            if (v != null) {
                try {
                    Pattern.compile(v);
                } catch (PatternSyntaxException pse) {
                    return FormValidation.error(pse.getMessage());
                }
            }
            return FormValidation.ok();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/regex-view-by-build-name/help-regex-job-filter.html";
        }
    }
}