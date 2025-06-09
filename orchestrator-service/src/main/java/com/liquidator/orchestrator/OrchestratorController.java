package com.liquidator.orchestrator;

import com.liquidator.execute_command.ExecuteCommand;
import com.liquidator.filter_complex.FilterResponse;
import com.liquidator.filter_complex.FilterTimelineRequest;
import com.liquidator.input_service.CommandResponse;
import com.liquidator.input_service.Input;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@RequestMapping("/api/v1/process")
public class OrchestratorController {

    private final RestTemplate restTemplate;

    public OrchestratorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public void export(@RequestBody ProcessRequest request){

        log.info("Request for export {}", request);

        // input for input-processing-service
        Input input = request.getInput();
        //filterTimelineRequest for filter-complex-service
        FilterTimelineRequest filter = request.getFilterTimelineRequest();

        CommandResponse inputResponse = restTemplate.postForEntity(
                "http://INPUT-PROCESSING/api/v1/input",
                input,
                CommandResponse.class
        ).getBody();

        FilterResponse filterResponse = restTemplate.postForEntity(
                "http://FILTER-COMPLEX/api/v1/filter-complex",
                filter,
                FilterResponse.class
        ).getBody();

        log.info("Response that we have gotten - {}, {}", inputResponse, filterResponse);
        assert inputResponse != null;
        assert filterResponse != null;
        String command = buildCompleteCommand(inputResponse.command(), filterResponse.filterCommand());

        restTemplate.postForEntity(
                "http://EXEC-COMMAND/api/v1/execute-command",
                new ExecuteCommand(command),
                null
        );

    }

    private String buildCompleteCommand(String input, String filterComplex){
        return "ffmpeg -y "+input+" -filter_complex \""+filterComplex+"\" -map \"[outv]\" -map \"[outa]\" -c:v ffv1 -c:a flac output.mkv";
    }
}
