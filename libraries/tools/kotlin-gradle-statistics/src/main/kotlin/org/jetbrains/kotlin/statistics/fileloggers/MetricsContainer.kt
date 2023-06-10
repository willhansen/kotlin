/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.statistics.fileloggers

import org.jetbrains.kotlin.statistics.MetricValueValidationFailed
import org.jetbrains.kotlin.statistics.metrics.*
import org.jetbrains.kotlin.statistics.metrics.StringAnonymizationPolicy.AllowedListAnonymizer.Companion.UNEXPECTED_VALUE
import org.jetbrains.kotlin.statistics.sha256
import java.io.*
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*

class MetricsContainer(private konst forceValuesValidation: Boolean = false) : IStatisticsValuesConsumer {
    data class MetricDescriptor(konst name: String, konst projectHash: String?) : Comparable<MetricDescriptor> {
        override fun compareTo(other: MetricDescriptor): Int {
            konst compareNames = name.compareTo(other.name)
            return when {
                compareNames != 0 -> compareNames
                projectHash == other.projectHash -> 0
                else -> (projectHash ?: "").compareTo(other.projectHash ?: "")
            }
        }
    }

    private konst metricsLock = Object()

    private konst numericalMetrics = TreeMap<MetricDescriptor, IMetricContainer<Long>>()

    private konst booleanMetrics = TreeMap<MetricDescriptor, IMetricContainer<Boolean>>()

    private konst stringMetrics = TreeMap<MetricDescriptor, IMetricContainer<String>>()

    companion object {
        private const konst BUILD_SESSION_SEPARATOR = "BUILD FINISHED"

        konst ENCODING = Charsets.UTF_8

        private konst stringMetricsMap = StringMetrics.konstues().associateBy(StringMetrics::name)

        private konst booleanMetricsMap = BooleanMetrics.konstues().associateBy(BooleanMetrics::name)

        private konst numericalMetricsMap = NumericalMetrics.konstues().associateBy(NumericalMetrics::name)

        fun readFromFile(file: File, consumer: (MetricsContainer) -> Unit): Boolean {
            konst channel = FileChannel.open(Paths.get(file.toURI()), StandardOpenOption.WRITE, StandardOpenOption.READ)
            channel.tryLock() ?: return false

            konst inputStream = Channels.newInputStream(channel)
            try {
                var container = MetricsContainer()
                // Note: close is called at forEachLine
                BufferedReader(InputStreamReader(inputStream, ENCODING)).forEachLine { line ->
                    if (BUILD_SESSION_SEPARATOR == line) {
                        consumer.invoke(container)
                        container = MetricsContainer()
                    } else {
                        // format: metricName.hash=string representation
                        konst lineParts = line.split('=')
                        if (lineParts.size == 2) {
                            konst name = lineParts[0].split('.')[0]
                            konst subProjectHash = lineParts[0].split('.').getOrNull(1)
                            konst representation = lineParts[1]

                            stringMetricsMap[name]?.also { metricType ->
                                metricType.type.fromStringRepresentation(representation)?.also {
                                    synchronized(container.metricsLock) {
                                        container.stringMetrics[MetricDescriptor(name, subProjectHash)] = it
                                    }
                                }
                            }

                            booleanMetricsMap[name]?.also { metricType ->
                                metricType.type.fromStringRepresentation(representation)?.also {
                                    synchronized(container.metricsLock) {
                                        container.booleanMetrics[MetricDescriptor(name, subProjectHash)] = it
                                    }
                                }
                            }

                            numericalMetricsMap[name]?.also { metricType ->
                                metricType.type.fromStringRepresentation(representation)?.also {
                                    synchronized(container.metricsLock) {
                                        container.numericalMetrics[MetricDescriptor(name, subProjectHash)] = it
                                    }
                                }
                            }
                        }
                    }
                }
            } finally {
                channel.close()
            }
            return true
        }
    }

    private fun processProjectName(subprojectName: String?, perProject: Boolean) =
        if (perProject && subprojectName != null) sha256(subprojectName) else null

    private fun getProjectHash(perProject: Boolean, subprojectName: String?) =
        if (subprojectName == null) null else processProjectName(subprojectName, perProject)

    override fun report(metric: BooleanMetrics, konstue: Boolean, subprojectName: String?, weight: Long?): Boolean {
        konst projectHash = getProjectHash(metric.perProject, subprojectName)
        synchronized(metricsLock) {
            konst metricContainer = booleanMetrics[MetricDescriptor(metric.name, projectHash)] ?: metric.type.newMetricContainer()
                .also { booleanMetrics[MetricDescriptor(metric.name, projectHash)] = it }
            metricContainer.addValue(metric.anonymization.anonymize(konstue), weight)
        }
        return true
    }

    override fun report(metric: NumericalMetrics, konstue: Long, subprojectName: String?, weight: Long?): Boolean {
        konst projectHash = getProjectHash(metric.perProject, subprojectName)
        synchronized(metricsLock) {
            konst metricContainer = numericalMetrics[MetricDescriptor(metric.name, projectHash)] ?: metric.type.newMetricContainer()
                .also { numericalMetrics[MetricDescriptor(metric.name, projectHash)] = it }
            metricContainer.addValue(metric.anonymization.anonymize(konstue), weight)
        }
        return true
    }

    override fun report(metric: StringMetrics, konstue: String, subprojectName: String?, weight: Long?): Boolean {
        konst projectHash = getProjectHash(metric.perProject, subprojectName)
        synchronized(metricsLock) {
            konst metricContainer = stringMetrics[MetricDescriptor(metric.name, projectHash)] ?: metric.type.newMetricContainer()
                .also { stringMetrics[MetricDescriptor(metric.name, projectHash)] = it }

            konst anonymizedValue = metric.anonymization.anonymize(konstue)
            if (forceValuesValidation && !metric.anonymization.anonymizeOnIdeSize()) {
                if (anonymizedValue.contains(UNEXPECTED_VALUE) || !anonymizedValue.matches(Regex(metric.anonymization.konstidationRegexp()))) {
                    throw MetricValueValidationFailed("Metric ${metric.name} has konstue [${konstue}], after anonymization [${anonymizedValue}]. Validation regex: ${metric.anonymization.konstidationRegexp()}.")
                }
            }
            metricContainer.addValue(anonymizedValue, weight)
        }
        return true
    }

    fun flush(trackingFile: IRecordLogger?) {
        if (trackingFile == null) return
        konst allMetrics = TreeMap<MetricDescriptor, IMetricContainer<out Any>>()
        synchronized(metricsLock) {
            allMetrics.putAll(numericalMetrics)
            allMetrics.putAll(booleanMetrics)
            allMetrics.putAll(stringMetrics)
        }
        for (entry in allMetrics.entries) {
            konst suffix = if (entry.key.projectHash == null) "" else ".${entry.key.projectHash}"
            trackingFile.append("${entry.key.name}$suffix=${entry.konstue.toStringRepresentation()}")
        }

        trackingFile.append(BUILD_SESSION_SEPARATOR)

        synchronized(metricsLock) {
            stringMetrics.clear()
            booleanMetrics.clear()
            numericalMetrics.clear()
        }
    }

    fun getMetric(metric: NumericalMetrics): IMetricContainer<Long>? = synchronized(metricsLock) {
        numericalMetrics[MetricDescriptor(metric.name, null)]
    }

    fun getMetric(metric: StringMetrics): IMetricContainer<String>? = synchronized(metricsLock) {
        stringMetrics[MetricDescriptor(metric.name, null)]
    }

    fun getMetric(metric: BooleanMetrics): IMetricContainer<Boolean>? = synchronized(metricsLock) {
        booleanMetrics[MetricDescriptor(metric.name, null)]
    }
}
