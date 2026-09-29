package org.staffcore.commandlog;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskFilter {
    private final List<Pattern> patterns;
    private static final Pattern DEFAULT_SENSITIVE = Pattern.compile("(?i)^/(login|register|changepassword|auth|pin|email|pass|setpassword|changepass|l|reg)\\b(.*)");

    public MaskFilter(List<String> rawPatterns) {
        if (rawPatterns != null && !rawPatterns.isEmpty()) {
            this.patterns = rawPatterns.stream().map(Pattern::compile).toList();
        } else {
            this.patterns = List.of(DEFAULT_SENSITIVE);
        }
    }

    public String maskCommand(String commandLine) {
        if (commandLine == null) return null;
        for (Pattern p : patterns) {
            Matcher matcher = p.matcher(commandLine);
            if (matcher.matches()) {
                String cmdPrefix = matcher.group(1);
                return "/" + cmdPrefix + " *******";
            }
        }
        return commandLine;
    }
}
