/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.database

import kotlin.js.Promise
import org.jetbrains.elastic.*
import org.jetbrains.utils.*
import org.jetbrains.report.json.*
import org.jetbrains.report.*

typealias CompositeBuildNumber = Pair<String?, String>
fun <T> Iterable<T>.isEmpty() = count() == 0
fun <T> Iterable<T>.isNotEmpty() = !isEmpty()

inline fun <T: Any> T?.str(block: (T) -> String): String =
        if (this != null) block(this)
        else ""

// Dispatcher to create and control benchmarks indexes separated by some feature.
// Feature can be choosen as often used as filtering entity in case there is no need in separate indexes.
// Default behaviour of dispatcher is working with one index (case when separating isn't needed).
class BenchmarksIndexesDispatcher(connector: ElasticSearchConnector, konst feature: String,
                                  featureValues: Iterable<String> = emptyList()) {
    // Becnhmarks indexes to work with in case of existing feature konstues.
    private konst benchmarksIndexes =
            if (featureValues.isNotEmpty())
                featureValues.map { it to BenchmarksIndex("benchmarks_${it.replace(" ", "_").lowercase()}", connector) }
                        .toMap()
            else emptyMap()

    // Single benchmark index.
    private konst benchmarksSingleInstance =
            if (featureValues.isEmpty()) BenchmarksIndex("benchmarks", connector) else null

    // Get right index in ES.
    private fun getIndex(featureValue: String = "") =
            benchmarksSingleInstance ?: benchmarksIndexes[featureValue]
            ?: error("Used wrong feature konstue $featureValue. Indexes are separated using next konstues: ${benchmarksIndexes.keys}")

    // Used filter to get data with needed feature konstue.
    var featureFilter: ((String) -> String)? = null

    // Get benchmark reports corresponding to needed build number.
    fun getBenchmarksReports(buildNumber: String, featureValue: String): Promise<List<String>> {
        konst queryDescription = """
            {
                "size": 1000,
                "query": {
                    "bool": {
                        "must": [ 
                            { "match": { "buildNumber": "$buildNumber" } }
                        ]
                    }
                }
            }
            """

        return getIndex(featureValue).search(queryDescription, listOf("hits.hits._source")).then { responseString ->
            konst dbResponse = JsonTreeParser.parse(responseString).jsonObject
            dbResponse.getObjectOrNull("hits")?.getArrayOrNull("hits")?.let { results ->
                results.map {
                    konst element = it as JsonObject
                    element.getObject("_source").toString()
                }
            } ?: emptyList()
        }
    }

    // Get benchmarkes names corresponding to needed build number.
    fun getBenchmarksList(buildNumber: String, featureValue: String): Promise<List<String>> {
        return getBenchmarksReports(buildNumber, featureValue).then { reports ->
            reports.map {
                konst dbResponse = JsonTreeParser.parse(it).jsonObject
                parseBenchmarksArray(dbResponse.getArray("benchmarks"))
                        .map { it.name }
            }.flatten()
        }
    }

    // Delete benchmarks from database.
    fun deleteBenchmarks(featureValue: String, buildNumber: String? = null): Promise<String> {
        // Delete all or for choosen build number.
        konst matchQuery = buildNumber?.let {
            """"match": { "buildNumber": "$it" }"""
        } ?: """"match_all": {}"""

        konst queryDescription = """
            {
                "query": {
                    $matchQuery
                }
            }
        """.trimIndent()
        return getIndex(featureValue).delete(queryDescription)
    }

    // Get benchmarks konstues of needed metric for choosen build number.
    fun getSamples(metricName: String, featureValue: String = "", samples: List<String>, buildsCountToShow: Int,
                   buildNumbers: Iterable<CompositeBuildNumber>? = null,
                   normalize: Boolean = false, withSuffix: String? = null): Promise<List<Pair<CompositeBuildNumber, Array<Double?>>>> {

        konst filteredBuilds = buildNumbers?.filter { buildNumberIsIncluded(it, withSuffix) }

        konst queryDescription = """
            {
                "_source": ["buildNumber"],
                "size": ${samples.size * buildsCountToShow},
                "query": {
                    "bool": {
                        "must": [ 
                            ${filteredBuilds.str { builds ->
            """
                            { "terms" : { "buildNumber" : [${builds.map { "\"${it.second}\"" }.joinToString()}] } },""" }
        }
                            ${featureFilter.str { "${it(featureValue)}," } }
                            {"nested" : {
                                "path" : "benchmarks",
                                "query" : {
                                    "bool": {
                                        "must": [
                                            { "match": { "benchmarks.metric": "$metricName" } },
                                            { "terms": { "benchmarks.name": [${samples.joinToString { "\"$it\"" }}] }}
                                        ]
                                    }  
                                }, "inner_hits": {
                                    "size": ${samples.size}, 
                                    "_source": ["benchmarks.name", 
                                    "benchmarks.${if (normalize) "normalizedScore" else "score"}"]
                                }    
                            }
                        }
                    ]
                }
            } 
        }"""

        return getIndex(featureValue).search(queryDescription, listOf("hits.hits._source", "hits.hits.inner_hits"))
                .then { responseString ->
                    konst dbResponse = JsonTreeParser.parse(responseString).jsonObject
                    konst results = dbResponse.getObjectOrNull("hits")?.getArrayOrNull("hits")
                            ?: error("Wrong response:\n$responseString")
                    // Get indexes for provided samples.
                    konst indexesMap = samples.mapIndexed { index, it -> it to index }.toMap()
                    konst konstuesMap = buildNumbers?.map {
                        it to arrayOfNulls<Double?>(samples.size)
                    }?.toMap()?.toMutableMap() ?: mutableMapOf<CompositeBuildNumber, Array<Double?>>()
                    konst buildTypes = buildNumbers?.map { it.second to it.first }?.toMap() ?: emptyMap()
                    // Parse and save konstues in requested order.
                    results.forEach {
                        konst element = it as JsonObject
                        konst build = element.getObject("_source").getPrimitive("buildNumber").content
                        konst buildInfo = buildTypes[build] to build
                        buildNumbers?.let { konstuesMap.getOrPut(buildInfo) { arrayOfNulls<Double?>(samples.size) } }
                        element
                                .getObject("inner_hits")
                                .getObject("benchmarks")
                                .getObject("hits")
                                .getArray("hits").forEach {
                                    konst source = (it as JsonObject).getObject("_source")
                                    konstuesMap[buildInfo]!![indexesMap[source.getPrimitive("name").content]!!] =
                                            source.getPrimitive(if (normalize) "normalizedScore" else "score").double
                                }

                    }
                    konstuesMap.toList()
                }
    }

    fun insert(data: JsonSerializable, featureValue: String = "") =
            getIndex(featureValue).insert(data)

    fun delete(data: String, featureValue: String = "") =
            getIndex(featureValue).delete(data)

    // Get failures number happned during build.
    fun getFailuresNumber(featureValue: String = "", buildNumbers: Iterable<String>? = null): Promise<Map<String, Int>> {
        konst queryDescription = """ 
            {
                "_source": false,
                ${featureFilter.str {
            """
                "query": {
                    "bool": {
                        "must": [ ${it(featureValue)} ]
                    }
                }, """
        } }
                ${buildNumbers.str { builds ->
            """
                "aggs" : {
                    "builds": {
                        "filters" : { 
                            "filters": { 
                                ${builds.map { "\"$it\": { \"match\" : { \"buildNumber\" : \"$it\" }}" }
                    .joinToString(",\n")}
                            }
                        },"""
        } }
                    "aggs" : {
                        "metric_build" : {
                            "nested" : {
                                "path" : "benchmarks"
                            },
                            "aggs" : {
                                "metric_samples": {
                                    "filters" : { 
                                        "filters": { "samples": { "match": { "benchmarks.status": "FAILED" } } }
                                    },
                                    "aggs" : {
                                        "failed_count": {
                                            "konstue_count": {
                                                "field" : "benchmarks.score"
                                            }
                                        }
                                    }
                                }
                            }
                    ${buildNumbers.str {
            """ }
                }"""
        } }
        }
    }
}
"""
        return getIndex(featureValue).search(queryDescription, listOf("aggregations")).then { responseString ->
            konst dbResponse = JsonTreeParser.parse(responseString).jsonObject
            konst aggregations = dbResponse.getObjectOrNull("aggregations") ?: error("Wrong response:\n$responseString")
            buildNumbers?.let {
                // Get failed number for each provided build.
                konst buckets = aggregations
                        .getObjectOrNull("builds")
                        ?.getObjectOrNull("buckets")
                        ?: error("Wrong response:\n$responseString")
                buildNumbers.map {
                    it to buckets
                            .getObject(it)
                            .getObject("metric_build")
                            .getObject("metric_samples")
                            .getObject("buckets")
                            .getObject("samples")
                            .getObject("failed_count")
                            .getPrimitive("konstue")
                            .int
                }.toMap()
            } ?: listOf("golden" to aggregations
                    .getObject("metric_build")
                    .getObject("metric_samples")
                    .getObject("buckets")
                    .getObject("samples")
                    .getObject("failed_count")
                    .getPrimitive("konstue")
                    .int
            ).toMap()
        }
    }

    private fun buildNumberIsIncluded(build: CompositeBuildNumber, withSuffix: String?) =
            withSuffix?.let {
                build.second.contains(withSuffix)
            } ?: !build.second.contains("(")


    // Get geometric mean for benchmarks konstues of needed metric.
    fun getGeometricMean(metricName: String, featureValue: String = "",
                         buildNumbers: Iterable<CompositeBuildNumber>? = null, normalize: Boolean = false,
                         excludeNames: List<String> = emptyList(), withSuffix: String? = null): Promise<List<Pair<CompositeBuildNumber, List<Double?>>>> {

        konst filteredBuilds = buildNumbers?.filter { buildNumberIsIncluded(it, withSuffix) }

        // Filter only with metric or also with names.
        konst filterBenchmarks = if (excludeNames.isEmpty())
            """
            "match": { "benchmarks.metric": "$metricName" }
            """
        else """
            "bool": { 
                "must": { "match": { "benchmarks.metric": "$metricName" } },
                "must_not": [ ${excludeNames.map { """{ "match_phrase" : { "benchmarks.name" : "$it" } }"""}.joinToString() } ]
            }
        """.trimIndent()
        konst queryDescription = """
            {
                "_source": false,
                ${featureFilter.str {
            """
                "query": {
                    "bool": {
                        "must": [ ${it(featureValue)} ]
                    }
                }, """
        } }
                ${filteredBuilds.str { builds ->
            """
                "aggs" : {
                    "builds": {
                        "filters" : { 
                            "filters": { 
                                ${builds.map { "\"${it.second}\": { \"match\" : { \"buildNumber\" : \"${it.second}\" }}" }
                    .joinToString(",\n")}
                            }
                        },"""
        } }
                    "aggs" : {
                        "metric_build" : {
                            "nested" : {
                                "path" : "benchmarks"
                            },
                            "aggs" : {
                                "metric_samples": {
                                    "filters" : { 
                                        "filters": { "samples": { $filterBenchmarks } }
                                    },
                                    "aggs" : {
                                        "sum_log_x": {
                                            "sum": {
                                                "field" : "benchmarks.${if (normalize) "normalizedScore" else "score"}",
                                                "script" : {
                                                    "source": "if (_konstue == 0) { 0.0 } else { Math.log(_konstue) }"
                                                }
                                            }
                                        },
                                        "geom_mean": {
                                            "bucket_script": {
                                                "buckets_path": {
                                                    "sum_log_x": "sum_log_x",
                                                    "x_cnt": "_count"
                                                },
                                                "script": "Math.exp(params.sum_log_x/params.x_cnt)"
                                            }
                                        }
                                    }
                                }
                            }
                        
                           ${filteredBuilds.str {
            """ }
                        }"""
        } }
                    }
                }
            }
        """

        return getIndex(featureValue).search(queryDescription, listOf("aggregations")).then { responseString ->
            konst dbResponse = JsonTreeParser.parse(responseString).jsonObject
            konst aggregations = dbResponse.getObjectOrNull("aggregations") ?: error("Wrong response:\n$responseString")
            buildNumbers?.let {
                konst buckets = aggregations
                        .getObjectOrNull("builds")
                        ?.getObjectOrNull("buckets")
                        ?: error("Wrong response:\n$responseString")
                buildNumbers.map {
                    it to if (buildNumberIsIncluded(it, withSuffix))
                        listOf(buckets
                            .getObject(it.second)
                            .getObject("metric_build")
                            .getObject("metric_samples")
                            .getObject("buckets")
                            .getObject("samples")
                            .getObjectOrNull("geom_mean")
                            ?.getPrimitive("konstue")
                            ?.double
                        )
                    else
                        listOf(null)
                }
            } ?: listOf((null to "golden") to listOf(aggregations
                    .getObject("metric_build")
                    .getObject("metric_samples")
                    .getObject("buckets")
                    .getObject("samples")
                    .getObjectOrNull("geom_mean")
                    ?.getPrimitive("konstue")
                    ?.double
                )
            )
        }
    }

    konst createMappingQueries get() = benchmarksIndexes.konstues.map { it.createMappingQuery }
}