package com.liquidator.filter_complex;

import lombok.Data;

import java.util.List;

@Data
public class FilterTimelineRequest {
    private List<VideoTimelineSegment> videoTimeline;
    private List<AudioTimelineSegment> audioTimeline;
}
