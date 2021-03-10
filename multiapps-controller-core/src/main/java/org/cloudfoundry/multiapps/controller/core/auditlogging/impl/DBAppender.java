package org.cloudfoundry.multiapps.controller.core.auditlogging.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.cloudfoundry.multiapps.controller.core.auditlogging.UserInfoProvider;
import org.cloudfoundry.multiapps.controller.core.util.UserInfo;
import org.slf4j.event.LoggingEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;

class DBAppender extends AbstractAppender {

    interface LogEventAdapter {
        void eventToStatement(String category, LoggingEvent event, UserInfo userInfo, PreparedStatement statement) throws SQLException;
    }

    interface ExceptionHandler {
        void handleException(Exception e);
    }

    private static final Level LEVEL = Level.ALL;
    private static final ThresholdFilter FILTER = ThresholdFilter.createFilter(LEVEL, Filter.Result.ACCEPT, Filter.Result.DENY);
    private static final PatternLayout LAYOUT = PatternLayout.createDefaultLayout();
    private static final String DEFAULT_NAME = "OPERATION";

    private final DataSource dataSource;
    private final LogEventAdapter eventAdapter;
    private final String sql;
    private final ExceptionHandler exceptionHandler;
    private final UserInfoProvider userInfoProvider;
    private final String name;

    DBAppender(DataSource dataSource, String sql, LogEventAdapter eventAdapter, ExceptionHandler exceptionHandler,
               UserInfoProvider userInfoProvider, String name) {
        super(DEFAULT_NAME, FILTER, LAYOUT, false, null);
        this.dataSource = Objects.requireNonNull(dataSource);
        this.sql = Objects.requireNonNull(sql);
        this.eventAdapter = Objects.requireNonNull(eventAdapter);
        this.exceptionHandler = Objects.requireNonNull(exceptionHandler);
        this.userInfoProvider = userInfoProvider;
        this.name = name == null ? DEFAULT_NAME : name;
    }

    @Override
    public void append(LogEvent event) {
        try (Connection connection = dataSource.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            eventAdapter.eventToStatement(getName(), (LoggingEvent) event, userInfoProvider.getUserInfo(), stmt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            exceptionHandler.handleException(e);
        }
    }
}
