package io.jenkins.plugins.sample;

import java.util.Arrays;

import static io.jenkins.plugins.sample.AbstractIncludeExcludeJobFilter.IncludeExcludeType.includeMatched;

public class ViewJobFilters {

    public enum NameOptions {
        MATCH_NAME, MATCH_FULL_NAME
    }

    public static RegExJobFilter nameRegex(String regex, NameOptions... options) {
        return new RegExJobFilter(
                regex,
                includeMatched.name(),
                RegExJobFilter.ValueType.BUILD_VERSION.name(),
                Arrays.asList(options).contains(NameOptions.MATCH_NAME),
                Arrays.asList(options).contains(NameOptions.MATCH_FULL_NAME));
    }

}
