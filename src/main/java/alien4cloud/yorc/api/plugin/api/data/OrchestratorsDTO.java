package alien4cloud.yorc.api.plugin.api.data;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrchestratorsDTO {


    private Set<OrchestratorDTO> orchestrators=new HashSet<>();

}
