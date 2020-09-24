package alien4cloud.yorc.api.plugin.api.data;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class APIRequest {
    private String url;
    private String method;
    private Map<String, List<String>> headers;
    private Object payload;
}
