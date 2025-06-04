package com.liquidator.orchestrator;

import com.liquidator.filter_complex.FilterTimelineRequest;
import com.liquidator.input_service.Input;
import lombok.Data;

@Data
public class ProcessRequest {
    private Input input;
    private FilterTimelineRequest filterTimelineRequest;
}
