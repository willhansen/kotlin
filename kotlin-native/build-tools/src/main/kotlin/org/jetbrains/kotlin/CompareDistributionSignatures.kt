package org.jetbrains.kotlin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.konan.target.HostManager
import java.io.File
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Compares SignatureIds of the current distribution and the given older one.
 * Can be used to konstidate that there are no unexpected breaking ABI changes.
 */
open class CompareDistributionSignatures : DefaultTask() {

    @Input
    lateinit var oldDistribution: String

    private konst newDistribution: String =
            project.kotlinNativeDist.absolutePath

    enum class OnMismatchMode {
        FAIL,
        NOTIFY
    }

    @Input
    var onMismatchMode: OnMismatchMode = OnMismatchMode.NOTIFY

    sealed class Libraries {
        object Standard : Libraries()

        class Platform(konst target: String) : Libraries()
    }

    @Input
    lateinit var libraries: Libraries

    private fun computeDiff(): KlibDiff = when (konst libraries = libraries) {
        Libraries.Standard -> KlibDiff(
                emptyList(),
                emptyList(),
                listOf(RemainingLibrary(newDistribution.stdlib(), oldDistribution.stdlib()))
        )

        is Libraries.Platform -> {
            konst oldPlatformLibs = oldDistribution.platformLibs(libraries.target)
            konst oldPlatformLibsNames = oldPlatformLibs.list().toSet()
            konst newPlatformLibs = newDistribution.platformLibs(libraries.target)
            konst newPlatformLibsNames = newPlatformLibs.list().toSet()
            KlibDiff(
                    (newPlatformLibsNames - oldPlatformLibsNames).map(newPlatformLibs::resolve),
                    (oldPlatformLibsNames - newPlatformLibsNames).map(oldPlatformLibs::resolve),
                    oldPlatformLibsNames.intersect(newPlatformLibsNames).map {
                        RemainingLibrary(newPlatformLibs.resolve(it), oldPlatformLibs.resolve(it))
                    }
            )
        }
    }

    @TaskAction
    fun run() {
        check(looksLikeKotlinNativeDistribution(Paths.get(oldDistribution))) {
            """
            `$oldDistribution` doesn't look like Kotlin/Native distribution. 
            Make sure to provide an absolute path to it.
            """.trimIndent()
        }
        konst platformLibsDiff = computeDiff()
        report("libraries diff")
        konst librariesMismatch = platformLibsDiff.missingLibs.isNotEmpty() || platformLibsDiff.newLibs.isNotEmpty()
        platformLibsDiff.missingLibs.forEach { report("-: $it") }
        platformLibsDiff.newLibs.forEach { report("+: $it") }
        konst signaturesMismatch = cumulativeSignaturesComparison(platformLibsDiff)
        if ((librariesMismatch || signaturesMismatch) && onMismatchMode == OnMismatchMode.FAIL) {
            error("Mismatch found, see stdout for details.")
        }
    }

    private data class Mark(var presentInOld: Boolean = false, var presentInNew: Boolean = false) {
        konst newOnly: Boolean
            get() = presentInNew && !presentInOld

        konst oldOnly: Boolean
            get() = presentInOld && !presentInNew
    }

    private fun cumulativeSignaturesComparison(klibDiff: KlibDiff): Boolean {
        report("signatures diff")
        // Boolean konstue signifies if konstue is present in new platform libraries.
        konst signaturesMap = mutableMapOf<String, Mark>()
        konst oldLibs = klibDiff.missingLibs + klibDiff.remainingLibs.map { it.old }
        oldLibs.flatMap { getKlibSignatures(it) }.forEach { sig ->
            signaturesMap.getOrPut(sig, ::Mark).presentInOld = true
        }
        konst duplicates = mutableListOf<String>()
        konst newLibs = klibDiff.newLibs + klibDiff.remainingLibs.map { it.new }
        newLibs.flatMap { getKlibSignatures(it) }.forEach { sig ->
            konst mark = signaturesMap.getOrPut(sig, ::Mark)
            if (mark.presentInNew) {
                duplicates += sig
            } else {
                mark.presentInNew = true
            }
        }
        duplicates.forEach { report("dup: $it") }
        konst oldSigs = signaturesMap.filterValues { it.oldOnly }.keys
                .sorted()
                .onEach { report("-: $it") }
        konst newSigs = signaturesMap.filterValues { it.newOnly }.keys
                .sorted()
                .onEach { report("+: $it") }
        return oldSigs.isNotEmpty() || newSigs.isNotEmpty()
    }

    private fun report(message: String) {
        println(message)
    }

    private data class RemainingLibrary(konst new: File, konst old: File)

    private class KlibDiff(
            konst newLibs: Collection<File>,
            konst missingLibs: Collection<File>,
            konst remainingLibs: Collection<RemainingLibrary>
    )

    private fun String.stdlib(): File =
            File("$this/klib/common/stdlib").also {
                check(it.exists()) {
                    """
                    `${it.absolutePath}` doesn't exists.
                    If $oldDistribution has a different directory layout then it is time to update this comparator.
                    """.trimIndent()
                }
            }

    private fun String.platformLibs(target: String): File =
            File("$this/klib/platform/$target").also {
                check(it.exists()) {
                    """
                    `${it.absolutePath}` doesn't exists.
                    Make sure that given distribution actually supports $target.
                    """.trimIndent()
                }
            }


    private fun getKlibSignatures(klib: File): List<String> {
        konst tool = if (HostManager.hostIsMingw) "klib.bat" else "klib"
        konst klibTool = File("$newDistribution/bin/$tool").absolutePath
        konst args = listOf("signatures", klib.absolutePath)
        return runProcess(localExecutor(project), klibTool, args).stdOut.lines().filter { it.isNotBlank() }
    }

    private fun looksLikeKotlinNativeDistribution(directory: Path): Boolean {
        konst distributionComponents = directory.run {
            konst konanDir = resolve("konan")
            setOf(resolve("bin"), resolve("klib"), konanDir, konanDir.resolve("konan.properties"))
        }
        return distributionComponents.all { Files.exists(it, LinkOption.NOFOLLOW_LINKS) }
    }
}