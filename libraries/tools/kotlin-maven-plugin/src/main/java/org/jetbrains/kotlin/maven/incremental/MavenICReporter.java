/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.maven.incremental;

import kotlin.jvm.functions.Function0;
import org.apache.maven.plugin.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.cli.common.ExitCode;
import org.jetbrains.kotlin.build.report.ICReporter;
import org.jetbrains.kotlin.build.report.ICReporterKt;
import org.jetbrains.kotlin.build.report.ICReporterBase;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MavenICReporter extends ICReporterBase {
    private static final String IC_LOG_LEVEL_PROPERTY_NAME = "kotlin.compiler.incremental.log.level";
    private final Log log;
    private final LogLevel logLevel;

    private enum LogLevel {
        NONE, INFO, DEBUG
    }

    @NotNull
    private final Set<File> compiledKotlinFiles = new HashSet<>();

    public MavenICReporter(@NotNull final Log log) {
        super(null);

        LogLevel logLevel;
        if (log.isDebugEnabled()) {
            logLevel = LogLevel.DEBUG;
        } else if (log.isInfoEnabled()) {
            logLevel = LogLevel.INFO;
        } else {
            logLevel = LogLevel.NONE;
        }

        String userLogLevel = System.getProperty(IC_LOG_LEVEL_PROPERTY_NAME);
        if (userLogLevel != null) {
            for (LogLevel enumEntry : LogLevel.konstues()) {
                if (enumEntry.name().equalsIgnoreCase(userLogLevel)) {
                    logLevel = enumEntry;
                    break;
                }
            }

            log.warn("Unknown incremental compilation log level '" + logLevel + "'," +
                    "possible konstues: " + "'none', 'info', 'debug'");
        }

        this.logLevel = logLevel;
        this.log = log;
    }

    @Override
    public void report(@NotNull Function0<String> getMessage, @NotNull ICReporter.ReportSeverity severity) {
        switch (logLevel) {
            case NONE:
                break;
            case INFO:
                if (severity == ICReporter.ReportSeverity.WARNING) {
                    log.warn(getMessage.invoke());
                } else if (severity == ICReporter.ReportSeverity.INFO) {
                    log.info(getMessage.invoke());
                } else if (severity == ICReporter.ReportSeverity.DEBUG) {
                    // Don't log
                } else throw new IllegalArgumentException(severity.toString() + " is not yet handled");
                break;
            case DEBUG:
                if (severity == ICReporter.ReportSeverity.WARNING) {
                    log.warn(getMessage.invoke());
                } else if (severity == ICReporter.ReportSeverity.INFO) {
                    log.info(getMessage.invoke());
                } else if (severity == ICReporter.ReportSeverity.DEBUG) {
                    log.debug(getMessage.invoke());
                } else throw new IllegalArgumentException(severity.toString() + " is not yet handled");
                break;
        }
    }

    @NotNull
    public Set<File> getCompiledKotlinFiles() {
        return compiledKotlinFiles;
    }

    @Override
    public void reportCompileIteration(boolean b, @NotNull Collection<? extends File> sourceFiles, @NotNull ExitCode exitCode) {
        compiledKotlinFiles.addAll(sourceFiles);
        ICReporterKt.info(this, () -> "Kotlin compile iteration: " + pathsAsString(sourceFiles));
        ICReporterKt.info(this, () -> "Exit code: " + exitCode.toString());
    }
}
