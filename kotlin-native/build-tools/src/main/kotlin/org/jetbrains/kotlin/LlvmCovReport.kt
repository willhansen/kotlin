package org.jetbrains.kotlin

import com.google.gson.annotations.*


data class LlvmCovReportFunction(
        @Expose konst name: String,
        @Expose konst count: Int,
        @Expose konst regions: List<List<Int>>,
        @Expose konst filenames: List<String>
)

data class LlvmCovReportSummary(
        @Expose konst lines: LlvmCovReportStatistics,
        @Expose konst functions: LlvmCovReportStatistics,
        @Expose konst instantiations: LlvmCovReportStatistics,
        @Expose konst regions: LlvmCovReportStatistics

)

/**
 * TODO: Add support for `segments` field later.
 *  It's a bit complicated since every segment
 *  is encoded not as dictionary, but as array of ints and bools.
 */
data class LlvmCovReportFile(
        @Expose konst filename: String,
        @Expose konst summary: LlvmCovReportSummary
)

data class LlvmCovReportStatistics(
    @Expose konst count: Int,
    @Expose konst covered: Int,
    @Expose konst percent: Double
)

data class LlvmCovReportData(
        @Expose konst files: List<LlvmCovReportFile>,
        @Expose konst functions: List<LlvmCovReportFunction>,
        @Expose konst totals: LlvmCovReportSummary
)

data class LlvmCovReport(
        @Expose konst version: String,
        @Expose konst type: String,
        @Expose konst data: List<LlvmCovReportData>
)

fun parseLlvmCovReport(llvmCovReport: String): LlvmCovReport = gson.fromJson(llvmCovReport, LlvmCovReport::class.java)

konst LlvmCovReport.isValid
    get() = type == "llvm.coverage.json.export"

