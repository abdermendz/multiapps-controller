package org.cloudfoundry.multiapps.controller.persistence.services;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.message.MessageFactory;

import java.io.File;

public class ProcessLogger extends Logger {

    private static final String NULL_LOGGER_NAME = "Null logger";
    private static final LoggerContext LOGGER_CONTEXT = (LoggerContext) LogManager.getContext(false);
    private static final Configuration CONFIGURATION = LOGGER_CONTEXT.getConfiguration();
    private static final MessageFactory MESSAGE_FACTORY = LogManager.getLogger()
                                                                    .getMessageFactory();

    private Logger logger;
    private File log;
    private String logName;
    protected final String spaceId;
    protected final String processId;
    protected final String activityId;

    public ProcessLogger(Logger logger, File log, String logName, String spaceId, String processId, String activityId) {
        super(LOGGER_CONTEXT, logName, MESSAGE_FACTORY);
        this.logger = logger;
        this.log = log;
        this.logName = logName;
        this.spaceId = spaceId;
        this.processId = processId;
        this.activityId = activityId;
    }

    public ProcessLogger(String spaceId, String processId, String activityId) {
        super(LOGGER_CONTEXT, NULL_LOGGER_NAME, MESSAGE_FACTORY);
        this.spaceId = spaceId;
        this.processId = processId;
        this.activityId = activityId;
    }

    @Override
    public void info(Object message) {
        logger.info(message);
    }

    @Override
    public void debug(Object message) {
        logger.debug(message);
    }

    @Override
    public void error(Object message) {
        logger.error(message);
    }

    @Override
    public void error(Object message, Throwable t) {
        logger.error(message, t);
    }

    @Override
    public void trace(Object message) {
        logger.trace(message);
    }

    @Override
    public void warn(Object message) {
        logger.warn(message);
    }

    @Override
    public void warn(Object message, Throwable t) {
        logger.warn(message, t);
    }

    public synchronized void removeAllAppenders() {
        if (!logger.getAppenders()
                   .isEmpty()) {
            for (String appenderName : logger.getAppenders()
                                             .keySet()) {
                CONFIGURATION.getRootLogger()
                             .removeAppender(appenderName);
            }
            LOGGER_CONTEXT.updateLoggers();
        }
    }

    public String getProcessId() {
        return processId;
    }

    public String getActivityId() {
        return activityId;
    }

    public synchronized void persistLogFile(ProcessLogsPersistenceService processLogsPersistenceService) {
        if (log.exists()) {
            processLogsPersistenceService.persistLog(spaceId, processId, log, logName);
        }
    }

    public synchronized void deleteLogFile() {
        FileUtils.deleteQuietly(log);
    }

}
