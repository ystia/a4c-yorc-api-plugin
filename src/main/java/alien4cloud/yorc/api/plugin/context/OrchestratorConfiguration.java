package alien4cloud.yorc.api.plugin.context;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrchestratorConfiguration {

    private String urlYorc = "http://127.0.0.1:8800";

    private Integer connectionMaxPoolSize = 20;

    private Integer connectionTtl = 300;

    private Integer connectionEvictionPeriod = 60;

    private Integer connectionMaxIdleTime = 120;

    private Boolean insecureTLS = true;

    private Integer connectionTimeout = 10;

    private Integer socketTimeout = 900;

    private Integer IOThreadCount = 4;

    private Integer executorThreadPoolSize = 4;

    private String orchestratorName = "yorc";

    private String caCertificate;
    private String clientKey;
    private String clientCertificate;

    public void setProperty(String propName, Object propValue) {
        switch (propName) {
        case "urlYorc":
            setUrlYorc((String) propValue);
            break;
        case "orchestratorName":
            setOrchestratorName((String) propValue);
            break;
        case "connectionMaxPoolSize":
            setConnectionMaxPoolSize((Integer) propValue);
            break;
        case "connectionTtl":
            setConnectionTtl((Integer) propValue);
            break;
        case "connectionEvictionPeriod":
            setConnectionEvictionPeriod((Integer) propValue);
            break;
        case "connectionMaxIdleTime":
            setConnectionMaxIdleTime((Integer) propValue);
            break;
        case "connectionTimeout":
            setConnectionTimeout((Integer) propValue);
            break;
        case "socketTimeout":
            setSocketTimeout((Integer) propValue);
            break;
        case "IOThreadCount":
            setIOThreadCount((Integer) propValue);
            break;
        case "caCertificate":
            setCaCertificate((String) propValue);
            break;
        case "clientKey":
            setClientKey((String) propValue);
            break;
        case "clientCertificate":
            setClientCertificate((String) propValue);
            break;
        }
    }

}
