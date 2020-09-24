package alien4cloud.yorc.api.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import alien4cloud.exception.NotFoundException;
import alien4cloud.model.orchestrators.Orchestrator;
import alien4cloud.orchestrators.services.OrchestratorConfigurationService;
import alien4cloud.orchestrators.services.OrchestratorService;
import alien4cloud.utils.ClassLoaderUtil;
import alien4cloud.yorc.api.plugin.context.ContextConfiguration;
import alien4cloud.yorc.api.plugin.context.OrchestratorConfiguration;
import alien4cloud.yorc.api.plugin.context.rest.TemplateManager;
import alien4cloud.yorc.api.plugin.services.APIService;
import lombok.extern.slf4j.Slf4j;

/**
 * Plugin spring context configuration.
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Order(ManagementServerProperties.ACCESS_OVERRIDE_ORDER)
@Configuration
@ComponentScan(basePackages = { "alien4cloud.yorc.api.plugin" }, excludeFilters = {
        @Filter(type = FilterType.REGEX, pattern = { "alien4cloud\\.yorc\\.api\\.plugin\\.context\\..*" }) })
@Slf4j
public class AppConfiguration {
    @Inject
    private OrchestratorService orchestratorService;

    @Inject
    private OrchestratorConfigurationService orchestratorConfigurationService;

    @Inject
    private APIService service;

    @Inject
    private ApplicationContext appContext;

    private List<AnnotationConfigApplicationContext> createdContexts = new ArrayList<>();

    @PostConstruct
    private void init() {

        List<Orchestrator> orchestrators = orchestratorService.getAllEnabledOrchestrators();
        if (orchestrators != null && !orchestrators.isEmpty()) {

            for (Orchestrator orchestrator : orchestrators) {
                if (!"alien4cloud-yorc-provider".equals(orchestrator.getPluginId())) {
                    continue;
                }
                String urlCompatibleOrchName = orchestrator.getName().replaceAll("\\W", "_");
                OrchestratorConfiguration OrchestratorConfiguration = new OrchestratorConfiguration();
                log.info("Found orchestrator {}, id : {}, state : {}", orchestrator.getName(), orchestrator.getId(),
                        orchestrator.getState());
                try {
                    alien4cloud.model.orchestrators.OrchestratorConfiguration config = orchestratorConfigurationService
                            .getConfigurationOrFail(orchestrator.getId());
                    @SuppressWarnings("unchecked")
                    Map<String, String> yorcConfig = (Map<String, String>) config.getConfiguration();
                    for (Entry<String, String> entry : yorcConfig.entrySet()) {
                        OrchestratorConfiguration.setProperty(entry.getKey(), entry.getValue());
                    }
                    OrchestratorConfiguration.setOrchestratorName(urlCompatibleOrchName);
                    log.info("Set Yorc API plugin configuration for Yorc (url : {})", OrchestratorConfiguration.getUrlYorc());
                } catch (NotFoundException e) {
                    log.error("Can't find configuration for orchestrator named {}", orchestrator.getName(), e);
                    continue;
                }

                AnnotationConfigApplicationContext orchestratorContext = new AnnotationConfigApplicationContext();

                createdContexts.add(orchestratorContext);
                orchestratorContext.setParent(appContext);
                orchestratorContext.setClassLoader(appContext.getClassLoader());

                ClassLoaderUtil.runWithContextClassLoader(appContext.getClassLoader(), () -> {
                    orchestratorContext.getBeanFactory().registerResolvableDependency(OrchestratorConfiguration.class,
                            OrchestratorConfiguration);
                    orchestratorContext.register(ContextConfiguration.class);
                    orchestratorContext.refresh();
                });

                TemplateManager manager = orchestratorContext.getBean(TemplateManager.class);
                service.addOrchestratorManager(urlCompatibleOrchName, manager);
            }
        }
    }

    @PreDestroy
    private void teardown() {
        for (AnnotationConfigApplicationContext context : createdContexts) {
            TemplateManager manager = context.getBean(TemplateManager.class);
            manager.term();
            context.close();
        }
        createdContexts.clear();
    }

}
