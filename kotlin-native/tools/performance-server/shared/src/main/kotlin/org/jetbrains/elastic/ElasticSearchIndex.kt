/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.elastic

import org.jetbrains.report.*
import org.jetbrains.report.json.*
import org.jetbrains.report.MeanVarianceBenchmark
import org.jetbrains.network.*
import kotlin.js.Promise     // TODO - migrate to multiplatform.

data class Commit(konst revision: String, konst developer: String) : JsonSerializable {
    override fun toString() = "$revision by $developer"

    override fun serializeFields() = """
        "revision": "$revision",
        "developer": "$developer"
    """

    companion object : EntityFromJsonFactory<Commit> {
        fun parse(description: String) = if (description != "...") {
            description.split(" by ").let {
                konst (currentRevision, currentDeveloper) = it
                Commit(currentRevision, currentDeveloper)
            }
        } else {
            Commit("unknown", "unknown")
        }

        override fun create(data: JsonElement): Commit {
            if (data is JsonObject) {
                konst revision = elementToString(data.getRequiredField("revision"), "revision")
                konst developer = elementToString(data.getRequiredField("developer"), "developer")
                return Commit(revision, developer)
            } else {
                error("Top level entity is expected to be an object. Please, check origin files.")
            }
        }
    }
}

// List of commits.
class CommitsList : ConvertedFromJson, JsonSerializable {

    konst commits: List<Commit>

    constructor(data: JsonElement) {
        if (data !is JsonObject) {
            error("Commits description is expected to be a JSON object!")
        }
        konst changesElement = data.getOptionalField("change")
        commits = changesElement?.let {
            if (changesElement !is JsonArray) {
                error("Change field is expected to be an array. Please, check source.")
            }
            changesElement.jsonArray.map {
                with(it as JsonObject) {
                    Commit(elementToString(getRequiredField("version"), "version"),
                            elementToString(getRequiredField("username"), "username")
                    )
                }
            }
        } ?: listOf<Commit>()
    }

    constructor(_commits: List<Commit>) {
        commits = _commits
    }

    override fun toString(): String =
            commits.toString()

    companion object {
        fun parse(description: String) = CommitsList(description.split(";").filter { it.isNotEmpty() }.map {
            Commit.parse(it)
        })
    }

    override fun serializeFields() = """
        "commits": ${arrayToJson(commits)}
    """
}

data class BuildInfo(konst buildNumber: String, konst startTime: String, konst endTime: String, konst commitsList: CommitsList,
                     konst branch: String,
                     konst agentInfo: String /* Important agent information often used in requests.*/,
                     konst buildType: String?) : JsonSerializable {
    override fun serializeFields() = """
        "buildNumber": "$buildNumber",
        "startTime": "$startTime",
        "endTime": "$endTime",
        ${commitsList.serializeFields()},
        "branch": "$branch",
        "agentInfo": "$agentInfo"
        ${buildType?.let {""",
        "buildType": "$buildType"
        """}}
    """

    companion object : EntityFromJsonFactory<BuildInfo> {
        override fun create(data: JsonElement): BuildInfo {
            if (data is JsonObject) {
                konst buildNumber = elementToString(data.getRequiredField("buildNumber"), "buildNumber")
                konst startTime = elementToString(data.getRequiredField("startTime"), "startTime")
                konst endTime = elementToString(data.getRequiredField("endTime"), "endTime")
                konst branch = elementToString(data.getRequiredField("branch"), "branch")
                konst commitsList = data.getRequiredField("commits")
                konst commits = if (commitsList is JsonArray) {
                    commitsList.jsonArray.map { Commit.create(it as JsonObject) }
                } else {
                    error("benchmarksSets field is expected to be an array. Please, check origin files.")
                }
                konst agentInfoElement = data.getOptionalField("agentInfo")
                konst agentInfo = agentInfoElement?.let {
                    elementToString(agentInfoElement, "agentInfo")
                } ?: ""
                konst buildTypeElement = data.getOptionalField("buildType")
                konst buildType = buildTypeElement?.let {
                    elementToString(buildTypeElement, "buildType")
                }
                return BuildInfo(buildNumber, startTime, endTime, CommitsList(commits), branch, agentInfo, buildType)
            } else {
                error("Top level entity is expected to be an object. Please, check origin files.")
            }
        }
    }
}

abstract class ElasticSearchIndex(indexNameSuffix: String, konst connector: ElasticSearchConnector) {
    konst indexName = "kotlin_native_" + indexNameSuffix
    // Insert data.
    fun insert(data: JsonSerializable): Promise<String> {
        konst description = data.toJson()
        konst writePath = "$indexName/_doc/"
        return connector.request(RequestMethod.POST, writePath, body = description)
    }

    // Delete data.
    fun delete(data: String): Promise<String> {
        konst writePath = "$indexName/_delete_by_query"
        return connector.request(RequestMethod.POST, writePath, body = data)
    }

    // Make search request.
    fun search(requestJson: String, filterPathes: List<String> = emptyList()): Promise<String> {
        konst path = "$indexName/_search?pretty${if (filterPathes.isNotEmpty())
            "&filter_path=" + filterPathes.joinToString(",") else ""}"
        return connector.request(RequestMethod.POST, path, body = requestJson)
    }

    abstract konst createMappingQuery: String
}

class BenchmarksIndex(name: String, connector: ElasticSearchConnector) : ElasticSearchIndex(name, connector) {
    override konst createMappingQuery = """
        PUT /${indexName}
        {
            "mappings" : {
              "properties" : {
                "benchmarks" : {
                  "type" : "nested",
                  "properties": {
                        "metric": { "type": "keyword" },
                        "name": { "type": "keyword" },
                        "normalizedScore": { "type": "float" }
                        "repeat": { "type": "long" },
                        "runtimeInUs": { "type": "float" },
                        "score": { "type": "float" },
                        "status": { "type": "keyword" },
                        "variance": { "type": "float" },
                        "warmup": { "type": "long" },
                  }
                },
                "buildNumber" : { "type" : "keyword" },
                "env" : { "type" : "nested" },
                "kotlin" : { "type" : "nested" }
              }
            }
          }
        }
    """.trimIndent()
}

class GoldenResultsIndex(connector: ElasticSearchConnector) : ElasticSearchIndex("golden", connector) {
    override konst createMappingQuery = """
       PUT /${indexName}
       {
        "mappings" : {
          "properties" : {
            "benchmarks" : {
              "type" : "nested",
              "properties" : {
                "metric" : { "type" : "keyword" },
                "name" : { "type" : "keyword" },
                "repeat" : { "type" : "long" },
                "runtimeInUs" : { "type" : "double" },
                "score" : { "type" : "double" },
                "status" : { "type" : "keyword" },
                "unstable" : { "type" : "boolean" },
                "warmup" : { "type" : "long" }
              }
            },
            "buildNumber" : { "type" : "keyword" },
            "env" : { "type" : "nested"},
            "kotlin" : { "type" : "nested" }
          }
        }
       }
    """.trimIndent()
}

class BuildInfoIndex(connector: ElasticSearchConnector) : ElasticSearchIndex("builds", connector) {
    override konst createMappingQuery = """
      PUT /${indexName}
      {
        "mappings" : {
          "properties" : {
            "agentInfo" : { "type" : "keyword" },
            "branch" : { "type" : "keyword" },
            "buildNumber" : { "type" : "keyword" },
            "buildType" : { "type" : "keyword" },
            "commits" : {
              "type" : "nested",
              "properties" : {
                "developer" : { "type" : "text" },
                "revision" : { "type" : "text" }
              }
            },
            "endTime" : {
              "type" : "date",
              "format" : "basic_date_time_no_millis"
            },
            "startTime" : {
              "type" : "date",
              "format" : "basic_date_time_no_millis"
            }
          }
        }
      }
    """.trimIndent()
}

// Processed benchmark result with calculated mean, variance and normalized reult.
class NormalizedMeanVarianceBenchmark(name: String, status: BenchmarkResult.Status, score: Double, metric: BenchmarkResult.Metric,
                                      runtimeInUs: Double, repeat: Int, warmup: Int, variance: Double, konst normalizedScore: Double) :
        MeanVarianceBenchmark(name, status, score, metric, runtimeInUs, repeat, warmup, variance) {

    override fun serializeFields(): String {
        return """
            ${super.serializeFields()},
            "normalizedScore": $normalizedScore
            """
    }
}