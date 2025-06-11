package com.liquidator.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DownloadFilesMessage {
    private String requestid;
    private String userId;
    private String projectId;
}
