package com.liquidator.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobStatusMessage {
    private String requestId;
    private String status; // "files-ready", "command-ready"
    private String command;
}
