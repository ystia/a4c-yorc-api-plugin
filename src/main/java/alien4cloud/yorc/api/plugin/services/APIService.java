package alien4cloud.yorc.api.plugin.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import alien4cloud.yorc.api.plugin.api.data.APIRequest;
import alien4cloud.yorc.api.plugin.api.data.APIResponse;
import alien4cloud.yorc.api.plugin.context.rest.TemplateManager;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * Sample of a plugin service.
 */
@Service
@Slf4j
public class APIService {

    private Map<String, TemplateManager> managersByOrchestrator = new HashMap<>();

    public void addOrchestratorManager(String orchestrator, TemplateManager manager) {
        managersByOrchestrator.put(orchestrator, manager);
    }

    public void removeOrchestrator(String orchestrator) {
        managersByOrchestrator.remove(orchestrator);
    }

    private TemplateManager getManager(String orchestratorName) {
        TemplateManager manager = managersByOrchestrator.get(orchestratorName);
        if (manager == null) {
            throw new RuntimeException("Can't find orchestrator named " + orchestratorName);
        }
        return manager;
    }


    public Set<String> getOrchestrators() {
        return managersByOrchestrator.keySet();
    }

    public APIResponse sendRequest(String orchestratorName, APIRequest request) {
        TemplateManager manager = getManager(orchestratorName);
        String url = manager.getYorcUrl() + request.getUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(request.getHeaders());
        HttpEntity<Object> entity = new HttpEntity<>(request.getPayload(), headers);
        Single<APIResponse> res = sendRequest(manager, url, HttpMethod.resolve(request.getMethod()), Object.class, entity)
                       .map(extractResponse());
        return res.blockingGet();
    }


    public static <T> Function<ResponseEntity<T>, APIResponse> extractResponse() {
        return (entity) -> {
            APIResponse resp = new APIResponse();
            resp.setStatus(entity.getStatusCodeValue());
            resp.setHeaders(entity.getHeaders());
            resp.setContent(entity.getBody());
            return  resp;
        };

    }


    private <T> Single<ResponseEntity<T>> sendRequest(TemplateManager manager, String url, HttpMethod method,
            Class<T> responseType, HttpEntity entity) {
        if (log.isTraceEnabled()) {
            log.trace("Yorc Request({},{}", method, url);

            if (entity.getHeaders() != null) {
                log.trace("Headers: {}", entity.getHeaders());
            }
        }

        return fromFuture(manager.get().exchange(url, method, entity, responseType)).onErrorResumeNext(throwable -> {
            if (throwable instanceof ExecutionException) {
                // Unwrap exception
                throwable = throwable.getCause();
            }
            return Single.error(throwable);
        });
    }

    private final <T> Single<T> fromFuture(ListenableFuture<T> future) {
        return Single.defer(() -> Single.create(source -> {
            future.addCallback(new ListenableFutureCallback<T>() {
                @Override
                public void onFailure(Throwable throwable) {
                    source.onError(throwable);
                }

                @Override
                public void onSuccess(T t) {
                    source.onSuccess(t);
                }
            });
        }));
    }

}
