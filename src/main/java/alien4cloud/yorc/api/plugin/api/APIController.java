package alien4cloud.yorc.api.plugin.api;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import alien4cloud.yorc.api.plugin.api.data.APIRequest;
import alien4cloud.yorc.api.plugin.api.data.APIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import alien4cloud.audit.annotation.Audit;
import alien4cloud.yorc.api.plugin.api.data.OrchestratorDTO;
import alien4cloud.yorc.api.plugin.api.data.OrchestratorsDTO;
import alien4cloud.yorc.api.plugin.services.APIService;
import alien4cloud.rest.model.RestResponse;
import alien4cloud.rest.model.RestResponseBuilder;
import io.swagger.annotations.ApiOperation;

/**
 * REST controller provided by the plugin. Defines the URL handlers for the
 * supported request paths.
 */
@RestController
@RequestMapping({ "/rest/yorc-api-plugin/v1", "/rest/yorc-api-plugin/latest" })
public class APIController {

    @Inject
    private APIService service;

    @ApiOperation(value = "Get the list of Yorc Orchestrators")
    @GetMapping(value = "/orchestrators", produces = MediaType.APPLICATION_JSON_VALUE)
    @Audit
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public RestResponse<OrchestratorsDTO> getOrchestrators(HttpServletRequest request) {
        Set<String> orchestrators = service.getOrchestrators();
        OrchestratorsDTO dto = new OrchestratorsDTO();
        for (String orchestratorName : orchestrators) {
            String uri = UriComponentsBuilder.fromUriString(request.getRequestURI()).pathSegment(orchestratorName)
                    .build(true).toUriString();
            dto.getOrchestrators().add(new OrchestratorDTO(orchestratorName, uri));
        }
        return RestResponseBuilder.<OrchestratorsDTO>builder().data(dto).build();
    }

    @ApiOperation(value = "Submit API request")
    @PostMapping(value = "/orchestrators/{orchestratorName}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Audit
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public RestResponse<APIResponse> submitRequest(@PathVariable String orchestratorName, @Valid @RequestBody APIRequest request) {
         return RestResponseBuilder.<APIResponse>builder().data(service.sendRequest(orchestratorName, request)).build();
     }

}
