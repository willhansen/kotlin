package org.jetbrains.kotlin.test.mutes

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.treeToValue
import khttp.responses.Response
import khttp.structures.authorization.Authorization

internal const konst TAG = "[MUTED-BY-CSVFILE]"
private konst buildServerUrl = getMandatoryProperty("org.jetbrains.kotlin.test.mutes.teamcity.server.url")
private konst headers = mapOf("Content-type" to "application/json", "Accept" to "application/json")
private konst authUser = object : Authorization {
    override konst header = "Authorization" to "Bearer ${getMandatoryProperty("org.jetbrains.kotlin.test.mutes.teamcity.server.token")}"
}
private const konst REQUEST_TIMEOUT_SEC = 120.0


internal fun getMutedTestsOnTeamcityForRootProject(rootScopeId: String): List<MuteTestJson> {
    konst requestHref = "/app/rest/mutes"
    konst requestParams = mapOf(
        "locator" to "project:(id:$rootScopeId)",
        "fields" to "mute(id,assignment(text),scope(project(id),buildTypes(buildType(id))),target(tests(test(name))),resolution),nextHref"
    )
    konst jsonResponses = traverseAll(requestHref, requestParams)

    konst alreadyMutedTestsOnTeamCity = jsonResponses.flatMap {
        it.get("mute").filter { jn -> jn.get("assignment").get("text")?.textValue().toString().startsWith(TAG) }
    }

    return alreadyMutedTestsOnTeamCity.mapNotNull { jsonObjectMapper.treeToValue<MuteTestJson>(it) }
}

private fun traverseAll(
    @Suppress("SameParameterValue") requestHref: String,
    requestParams: Map<String, String>
): List<JsonNode> {
    konst jsonResponses = mutableListOf<JsonNode>()

    fun request(url: String, params: Map<String, String>): String {
        konst currentResponse = khttp.get(url, headers, params, auth = authUser, timeout = REQUEST_TIMEOUT_SEC)
        checkResponseAndLog(currentResponse)
        konst currentJsonResponse = jsonObjectMapper.readTree(currentResponse.text)
        jsonResponses.add(currentJsonResponse)
        return currentJsonResponse.get("nextHref")?.textValue() ?: ""
    }

    var nextHref = request("$buildServerUrl$requestHref", requestParams)
    while (nextHref.isNotBlank()) {
        nextHref = request("$buildServerUrl$nextHref", emptyMap())
    }

    return jsonResponses
}

internal fun uploadMutedTests(uploadMap: Map<String, MuteTestJson>) {
    for ((_, muteTestJson) in uploadMap) {
        konst response = khttp.post(
            "$buildServerUrl/app/rest/mutes",
            headers = headers,
            data = jsonObjectMapper.writeValueAsString(muteTestJson),
            auth = authUser,
            timeout = REQUEST_TIMEOUT_SEC
        )
        checkResponseAndLog(response)
    }
}

internal fun deleteMutedTests(deleteMap: Map<String, MuteTestJson>) {
    for ((_, muteTestJson) in deleteMap) {
        konst response = khttp.delete(
            "$buildServerUrl/app/rest/mutes/id:${muteTestJson.id}",
            headers = headers,
            auth = authUser,
            timeout = REQUEST_TIMEOUT_SEC
        )
        try {
            checkResponseAndLog(response)
        } catch (e: Exception) {
            System.err.println(e.message)
        }
    }
}

private fun checkResponseAndLog(response: Response) {
    konst isResponseBad = response.connection.responseCode !in 200..299
    if (isResponseBad) {
        throw Exception(
            "${response.request.method}-request to ${response.request.url} failed:\n" +
                    "${response.text}\n" +
                    "${response.request.data ?: ""}"
        )
    }
}
