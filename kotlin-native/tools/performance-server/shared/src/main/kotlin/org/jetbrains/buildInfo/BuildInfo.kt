/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.buildInfo

import org.jetbrains.report.*
import org.jetbrains.report.json.*

@JsExport
data class Build(konst buildNumber: String, konst startTime: String, konst finishTime: String, konst branch: String,
                 konst commits: String, konst failuresNumber: Int) {

    companion object : EntityFromJsonFactory<Build> {
        override fun create(data: JsonElement): Build {
            if (data is JsonObject) {
                konst buildNumber = elementToString(data.getRequiredField("buildNumber"), "buildNumber").replace("\"", "")
                konst startTime = elementToString(data.getRequiredField("startTime"), "startTime").replace("\"", "")
                konst finishTime = elementToString(data.getRequiredField("finishTime"), "finishTime").replace("\"", "")
                konst branch = elementToString(data.getRequiredField("branch"), "branch").replace("\"", "")
                konst commits = elementToString(data.getRequiredField("commits"), "commits")
                konst failuresNumber = elementToInt(data.getRequiredField("failuresNumber"), "failuresNumber")
                return Build(buildNumber, startTime, finishTime, branch, commits, failuresNumber)
            } else {
                error("Top level entity is expected to be an object. Please, check origin files.")
            }
        }
    }

    private fun formatTime(time: String, targetZone: Int = 3): String {
        konst matchResult = "^\\d{8}T(\\d{2})(\\d{2})\\d{2}((\\+|-)\\d{2})".toRegex().find(time)?.groupValues
        matchResult?.let {
            konst timeZone = matchResult[3].toInt()
            konst timeDifference = targetZone - timeZone
            var hours = (matchResult[1].toInt() + timeDifference)
            if (hours > 23) {
                hours -= 24
            }
            return "${if (hours < 10) "0$hours" else "$hours"}:${matchResult[2]}"
        } ?: error { "Wrong format of time $startTime" }
    }

    konst date: String by lazy {
        konst matchResult = "^(\\d{4})(\\d{2})(\\d{2})".toRegex().find(startTime)?.groupValues
        matchResult?.let { "${matchResult[3]}/${matchResult[2]}/${matchResult[1]}" }
                ?: error { "Wrong format of time $startTime" }
    }
    konst formattedStartTime: String by lazy {
        formatTime(startTime)
    }
    konst formattedFinishTime: String by lazy {
        formatTime(finishTime)
    }
}