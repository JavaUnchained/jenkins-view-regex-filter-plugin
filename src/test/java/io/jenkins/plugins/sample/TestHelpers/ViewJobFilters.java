package io.jenkins.plugins.sample.TestHelpers;

import io.jenkins.plugins.sample.JobStatusFilter;
import io.jenkins.plugins.sample.RegExJobFilter;

import java.util.Arrays;

import static io.jenkins.plugins.sample.AbstractIncludeExcludeJobFilter.IncludeExcludeType.includeMatched;

public class ViewJobFilters {


    public enum NameOptions {
        MATCH_NAME, MATCH_FULL_NAME, MATCH_DISPLAY_NAME, MATCH_FULL_DISPLAY_NAME;

    }
    public static RegExJobFilter nameRegex(String regex, NameOptions... options) {
        return new RegExJobFilter(
                regex,
                includeMatched.name(),
                RegExJobFilter.ValueType.NAME.name(),
                Arrays.asList(options).contains(NameOptions.MATCH_NAME),
                Arrays.asList(options).contains(NameOptions.MATCH_FULL_NAME),
                Arrays.asList(options).contains(NameOptions.MATCH_DISPLAY_NAME),
                Arrays.asList(options).contains(NameOptions.MATCH_FULL_DISPLAY_NAME));
    }

    public static RegExJobFilter folderNameRegex(String regex, NameOptions... options) {
        return new RegExJobFilter(
                regex,
                includeMatched.name(),
                RegExJobFilter.ValueType.FOLDER_NAME.name(),
                Arrays.asList(options).contains(NameOptions.MATCH_NAME),
                Arrays.asList(options).contains(NameOptions.MATCH_FULL_NAME),
                Arrays.asList(options).contains(NameOptions.MATCH_DISPLAY_NAME),
                Arrays.asList(options).contains(NameOptions.MATCH_FULL_DISPLAY_NAME));
    }

    public static JobStatusFilter jobStatus(
            boolean unstable,
            boolean failed,
            boolean aborted,
            boolean disabled,
            boolean stable) {
        return new JobStatusFilter(
                unstable,
                failed,
                aborted,
                disabled,
                stable,
                includeMatched.name());
    }
}
