package com.liquidator.video_filtering;

import com.liquidator.filter_complex.VideoTimelineSegment;

import java.util.List;

public class FilterVideo {
    public String videoFilterCommand(List<VideoTimelineSegment> videoTimeline){
        StringBuilder filterCommand = new StringBuilder();
        int count = 0;
        for(VideoTimelineSegment segment: videoTimeline){
            count++;
            int i = segment.getVideoTrack();
            int start = segment.getStart();
            int end = segment.getEnd();
            filterCommand.append("[").append(i).append(":v]trim=start=").append(start).append(":end=").append(end).append(",setpts=PTS-STARTPTS[v").append(count-1).append("];")
                    .append("[").append(i).append(":a]atrim=start=").append(start).append(":end=").append(end).append(",asetpts=PTS-STARTPTS[a").append(count-1).append("];");
        }

        // concatenating video files
        for(int i = 0; i < count; i++){
            filterCommand.append("[v").append(i).append("]");
        }
        filterCommand.append("concat=n=").append(count).append(":v=1:a=0[outv];");

        // concatenating audio files
        for(int i = 0; i < count; i++){
            filterCommand.append("[a").append(i).append("]");
        }
        filterCommand.append("concat=n=").append(count).append(":v=0:a=1[outa];");

        return filterCommand.toString();
    }
}
