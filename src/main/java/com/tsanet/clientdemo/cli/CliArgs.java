package com.tsanet.clientdemo.cli;

import java.util.Optional;

public final class CliArgs {
    private CliArgs() {
    }

    public static Optional<Long> companyId(String[] args) {
        return valueAfter(args, "--company-id").map(Long::parseLong);
    }

    public static Optional<String> token(String[] args) {
        return valueAfter(args, "--token");
    }

    public static Optional<String> search(String[] args) {
        return valueAfter(args, "--search");
    }

    public static Optional<String> caseNumber(String[] args) {
        return valueAfter(args, "--case-number");
    }

    public static Optional<String> summary(String[] args) {
        return valueAfter(args, "--summary");
    }

    public static Optional<String> description(String[] args) {
        return valueAfter(args, "--description");
    }

    public static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static Optional<String> valueAfter(String[] args, String flag) {
        for (int i = 0; i < args.length; i++) {
            if (flag.equals(args[i]) && i + 1 < args.length) {
                return Optional.of(args[i + 1]);
            }
        }
        return Optional.empty();
    }
}
