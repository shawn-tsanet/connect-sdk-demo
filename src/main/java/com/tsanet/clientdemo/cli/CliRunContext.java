package com.tsanet.clientdemo.cli;

import org.springframework.stereotype.Component;

@Component
public class CliRunContext {
    private boolean batchMode;
    private boolean plainOutput;

    public void configure(boolean batchMode, boolean plainOutput) {
        this.batchMode = batchMode;
        this.plainOutput = plainOutput;
    }

    public boolean isPlainOutput() {
        return plainOutput || batchMode;
    }
}
