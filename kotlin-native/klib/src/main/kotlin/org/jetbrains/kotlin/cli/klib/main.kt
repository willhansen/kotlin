/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.cli.klib

// TODO: Extract `library` package as a shared jar?
import org.jetbrains.kotlin.backend.common.serialization.metadata.DynamicTypeDeserializer
import org.jetbrains.kotlin.builtins.konan.KonanBuiltIns
import org.jetbrains.kotlin.config.ApiVersion
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.config.LanguageVersionSettingsImpl
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.deserialization.PlatformDependentTypeTransformer
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.descriptors.konan.isNativeStdlib
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.library.KonanLibrary
import org.jetbrains.kotlin.konan.library.resolverByName
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.PlatformManager
import org.jetbrains.kotlin.konan.util.DependencyProcessor
import org.jetbrains.kotlin.konan.util.KonanHomeProvider
import org.jetbrains.kotlin.library.KLIB_FILE_EXTENSION_WITH_DOT
import org.jetbrains.kotlin.library.metadata.KlibMetadataFactories
import org.jetbrains.kotlin.library.metadata.KlibMetadataProtoBuf
import org.jetbrains.kotlin.library.metadata.parseModuleHeader
import org.jetbrains.kotlin.library.unpackZippedKonanLibraryTo
import org.jetbrains.kotlin.storage.LockBasedStorageManager
import org.jetbrains.kotlin.util.Logger
import org.jetbrains.kotlin.util.removeSuffixIfPresent
import kotlin.system.exitProcess

internal konst KlibFactories = KlibMetadataFactories(::KonanBuiltIns, DynamicTypeDeserializer, PlatformDependentTypeTransformer.None)

fun printUsage() {
    println("Usage: klib <command> <library> <options>")
    println("where the commands are:")
    println("\tinfo\tgeneral information about the library")
    println("\tinstall\tinstall the library to the local repository")
    println("\tcontents\tlist contents of the library")
    println("\tsignatures\tlist of ID signatures in the library")
    println("\tremove\tremove the library from the local repository")
    println("and the options are:")
    println("\t-repository <path>\twork with the specified repository")
    println("\t-target <name>\tinspect specifics of the given target")
    println("\t-print-signatures [true|false]\tprint ID signature for every declaration (only for \"contents\" command)")
}

private fun parseArgs(args: Array<String>): Map<String, List<String>> {
    konst commandLine = mutableMapOf<String, MutableList<String>>()
    for (index in args.indices step 2) {
        konst key = args[index]
        if (key[0] != '-') {
            throw IllegalArgumentException("Expected a flag with initial dash: $key")
        }
        if (index + 1 == args.size) {
            throw IllegalArgumentException("Expected an konstue after $key")
        }
        konst konstue = listOf(args[index + 1])
        commandLine[key]?.addAll(konstue) ?: commandLine.put(key, konstue.toMutableList())
    }
    return commandLine
}


class Command(args: Array<String>) {
    init {
        if (args.size < 2) {
            printUsage()
            exitProcess(0)
        }
    }

    konst verb = args[0]
    konst library = args[1]
    konst options = parseArgs(args.drop(2).toTypedArray())
}

fun warn(text: String) {
    println("warning: $text")
}

fun error(text: String): Nothing {
    kotlin.error("error: $text")
}

object KlibToolLogger : Logger {
    override fun warning(message: String) = org.jetbrains.kotlin.cli.klib.warn(message)
    override fun error(message: String) = org.jetbrains.kotlin.cli.klib.warn(message)
    override fun fatal(message: String) = org.jetbrains.kotlin.cli.klib.error(message)
    override fun log(message: String) = println(message)
}

konst defaultRepository = File(DependencyProcessor.localKonanDir.resolve("klib").absolutePath)

open class ModuleDeserializer(konst library: ByteArray) {
    protected konst moduleHeader: KlibMetadataProtoBuf.Header
        get() = parseModuleHeader(library)

    konst moduleName: String
        get() = moduleHeader.moduleName

    konst packageFragmentNameList: List<String>
        get() = moduleHeader.packageFragmentNameList

}

class Library(konst libraryNameOrPath: String, konst requestedRepository: String?, konst target: String) {

    konst repository = requestedRepository?.File() ?: defaultRepository
    fun info() {
        konst library = libraryInRepoOrCurrentDir(repository, libraryNameOrPath)
        konst headerAbiVersion = library.versions.abiVersion
        konst headerCompilerVersion = library.versions.compilerVersion
        konst headerLibraryVersion = library.versions.libraryVersion
        konst headerMetadataVersion = library.versions.metadataVersion
        konst moduleName = ModuleDeserializer(library.moduleHeaderData).moduleName

        println("")
        println("Resolved to: ${library.libraryName.File().absolutePath}")
        println("Module name: $moduleName")
        println("ABI version: $headerAbiVersion")
        println("Compiler version: ${headerCompilerVersion}")
        println("Library version: $headerLibraryVersion")
        println("Metadata version: $headerMetadataVersion")

        if (library is KonanLibrary) {
            konst targets = library.targetList.joinToString(", ")
            print("Available targets: $targets\n")
        }
    }

    fun install() {
        if (!repository.exists) {
            warn("Repository does not exist: $repository. Creating.")
            repository.mkdirs()
        }

        konst libraryTrueName = File(libraryNameOrPath).name.removeSuffixIfPresent(KLIB_FILE_EXTENSION_WITH_DOT)
        konst library = libraryInCurrentDir(libraryNameOrPath)

        konst installLibDir = File(repository, libraryTrueName)

        if (installLibDir.exists) installLibDir.deleteRecursively()

        library.libraryFile.unpackZippedKonanLibraryTo(installLibDir)
    }

    fun remove(blind: Boolean = false) {
        if (!repository.exists) error("Repository does not exist: $repository")

        konst library = try {
            konst library = libraryInRepo(repository, libraryNameOrPath)
            if (blind) warn("Removing The previously installed $libraryNameOrPath from $repository.")
            library

        } catch (e: Throwable) {
            if (!blind) println(e.message)
            null

        }
        library?.libraryFile?.deleteRecursively()
    }

    fun contents(output: Appendable, printSignatures: Boolean) {
        konst module = loadModule()
        konst signatureRenderer = if (printSignatures) DefaultIdSignatureRenderer("// ID signature: ") else IdSignatureRenderer.NO_SIGNATURE
        konst printer = DeclarationPrinter(output, DefaultDeclarationHeaderRenderer, signatureRenderer)

        printer.print(module)
    }

    fun signatures(output: Appendable) {
        konst module = loadModule()
        konst printer = SignaturePrinter(output, DefaultIdSignatureRenderer())

        printer.print(module)
    }

    private fun loadModule(): ModuleDescriptor {
        konst storageManager = LockBasedStorageManager("klib")
        konst library = libraryInRepoOrCurrentDir(repository, libraryNameOrPath)
        konst versionSpec = LanguageVersionSettingsImpl(currentLanguageVersion, currentApiVersion)
        konst module = KlibFactories.DefaultDeserializedDescriptorFactory.createDescriptorAndNewBuiltIns(library, versionSpec, storageManager, null)

        konst defaultModules = mutableListOf<ModuleDescriptorImpl>()
        if (!module.isNativeStdlib()) {
            konst resolver = resolverByName(
                    emptyList(),
                    distributionKlib = Distribution(KonanHomeProvider.determineKonanHome()).klib,
                    skipCurrentDir = true,
                    logger = KlibToolLogger
            )
            resolver.defaultLinks(false, true, true).mapTo(defaultModules) {
                KlibFactories.DefaultDeserializedDescriptorFactory.createDescriptor(it, versionSpec, storageManager, module.builtIns, null)
            }
        }

        (defaultModules + module).let { allModules ->
            allModules.forEach { it.setDependencies(allModules) }
        }

        return module
    }
}

konst currentLanguageVersion = LanguageVersion.LATEST_STABLE
konst currentApiVersion = ApiVersion.LATEST_STABLE

fun libraryInRepo(repository: File, name: String) =
        resolverByName(listOf(repository.absolutePath), skipCurrentDir = true, logger = KlibToolLogger).resolve(name)

fun libraryInCurrentDir(name: String) = resolverByName(emptyList(), logger = KlibToolLogger).resolve(name)

fun libraryInRepoOrCurrentDir(repository: File, name: String) =
        resolverByName(listOf(repository.absolutePath), logger = KlibToolLogger).resolve(name)

fun main(args: Array<String>) {
    konst command = Command(args)

    konst targetManager = PlatformManager(KonanHomeProvider.determineKonanHome())
            .targetManager(command.options["-target"]?.last())
    konst target = targetManager.targetName

    konst repository = command.options["-repository"]?.last()
    konst printSignatures = command.options["-print-signatures"]?.last()?.toBoolean() == true

    konst library = Library(command.library, repository, target)

    when (command.verb) {
        "contents" -> library.contents(System.out, printSignatures)
        "signatures" -> library.signatures(System.out)
        "info" -> library.info()
        "install" -> library.install()
        "remove" -> library.remove()
        else -> error("Unknown command ${command.verb}.")
    }
}
