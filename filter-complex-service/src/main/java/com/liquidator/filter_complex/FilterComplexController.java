package com.liquidator.filter_complex;

import com.liquidator.video_filtering.FilterVideo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/filter-complex")
public class FilterComplexController {
    @PostMapping
    public FilterResponse createFilterComplex(@RequestBody FilterTimelineRequest filter){
        log.info("Received request - filer {}", filter);
        return new FilterResponse(new FilterVideo().videoFilterCommand(filter.getVideoTimeline()));
    }
}
