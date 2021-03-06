package org.cloudfoundry.multiapps.controller.shutdown.client;

import java.util.UUID;

import org.cloudfoundry.multiapps.common.util.JsonUtil;
import org.cloudfoundry.multiapps.common.util.MiscUtil;
import org.cloudfoundry.multiapps.controller.core.model.ApplicationShutdown;
import org.cloudfoundry.multiapps.controller.shutdown.client.configuration.ShutdownClientConfiguration;
import org.cloudfoundry.multiapps.controller.shutdown.client.configuration.ShutdownConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationInstanceShutdownExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationInstanceShutdownExecutor.class);

    private static final long SHUTDOWN_POLLING_INTERVAL = 3000L;

    private final ShutdownConfiguration shutdownConfiguration;
    private final ShutdownClientFactory shutdownClientFactory;

    public ApplicationInstanceShutdownExecutor(ShutdownConfiguration shutdownConfiguration, ShutdownClientFactory shutdownClientFactory) {
        this.shutdownConfiguration = shutdownConfiguration;
        this.shutdownClientFactory = shutdownClientFactory;
    }

    public void execute(UUID applicationGuid, int applicationInstanceIndex) {
        ShutdownClient shutdownClient = createShutdownClient();
        shutdown(shutdownClient, applicationGuid, applicationInstanceIndex);
    }

    private ShutdownClient createShutdownClient() {
        return shutdownClientFactory.createShutdownClient(createShutdownClientConfiguration());
    }

    private ShutdownClientConfiguration createShutdownClientConfiguration() {
        return new ShutdownClientConfiguration(shutdownConfiguration);
    }

    private static void shutdown(ShutdownClient shutdownClient, UUID applicationGuid, int applicationInstanceIndex) {
        ApplicationShutdown shutdown = shutdownClient.triggerShutdown(applicationGuid, applicationInstanceIndex);
        while (!hasFinished(shutdown)) {
            print(shutdown);
            MiscUtil.sleep(SHUTDOWN_POLLING_INTERVAL);
            shutdown = shutdownClient.getStatus(applicationGuid, applicationInstanceIndex);
        }
    }

    private static boolean hasFinished(ApplicationShutdown shutdown) {
        return ApplicationShutdown.Status.FINISHED.equals(shutdown.getStatus());
    }

    private static void print(ApplicationShutdown shutdown) {
        LOGGER.info("Shutdown status of application with GUID {}, instance {}: {}", shutdown.getApplicationId(),
                    shutdown.getApplicationInstanceIndex(), JsonUtil.toJson(shutdown, true));
    }

}
