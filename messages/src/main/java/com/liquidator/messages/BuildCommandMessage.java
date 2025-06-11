package com.liquidator.messages;

import com.liquidator.filter_complex.FilterTimelineRequest;
import com.liquidator.input_service.Input;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuildCommandMessage {
    private String requestid;
    private Input input;
    private FilterTimelineRequest filter;
}
