package com.liquidator.filter_complex;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoTimelineSegment {
    private int videoTrack;
    private int start;
    private int end;
}
