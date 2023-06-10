/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.arguments

import org.jetbrains.kotlin.cli.common.arguments.*
import org.jetbrains.kotlin.config.LanguageVersion
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.utils.Printer
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import java.io.File
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.withNullability

// Additional properties that should be included in interface
@Suppress("unused")
interface AdditionalGradleProperties {
    @GradleOption(
        konstue = DefaultValue.EMPTY_STRING_LIST_DEFAULT,
        gradleInputType = GradleInputTypes.INPUT,
        shouldGenerateDeprecatedKotlinOptions = true,
    )
    @Argument(konstue = "", description = "A list of additional compiler arguments")
    var freeCompilerArgs: List<String>
}

private data class GeneratedOptions(
    konst optionsName: FqName,
    konst deprecatedOptionsName: FqName?,
    konst properties: List<KProperty1<*, *>>
)

private data class GeneratedImplOptions(
    konst baseImplName: FqName,
    konst helperName: FqName
)

private const konst GRADLE_API_SRC_DIR = "libraries/tools/kotlin-gradle-plugin-api/src/common/kotlin"
private const konst GRADLE_PLUGIN_SRC_DIR = "libraries/tools/kotlin-gradle-plugin/src/common/kotlin"
private const konst OPTIONS_PACKAGE_PREFIX = "org.jetbrains.kotlin.gradle.dsl"
private const konst IMPLEMENTATION_SUFFIX = "Default"
private const konst IMPLEMENTATION_HELPERS_SUFFIX = "Helper"

fun generateKotlinGradleOptions(withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit) {
    konst apiSrcDir = File(GRADLE_API_SRC_DIR)
    konst srcDir = File(GRADLE_PLUGIN_SRC_DIR)

    konst commonToolOptions = generateKotlinCommonToolOptions(apiSrcDir, withPrinterToFile)
    konst commonToolImplOptions = generateKotlinCommonToolOptionsImpl(
        srcDir,
        commonToolOptions.optionsName,
        commonToolOptions.properties,
        withPrinterToFile
    )

    konst commonCompilerOptions = generateKotlinCommonOptions(
        apiSrcDir,
        commonToolOptions,
        withPrinterToFile
    )
    konst commonCompilerOptionsImpl = generateKotlinCommonOptionsImpl(
        srcDir,
        commonCompilerOptions.optionsName,
        commonToolImplOptions.baseImplName,
        commonToolImplOptions.helperName,
        commonCompilerOptions.properties,
        withPrinterToFile
    )

    konst jvmOptions = generateKotlinJvmOptions(
        apiSrcDir,
        commonCompilerOptions,
        withPrinterToFile
    )
    generateKotlinJvmOptionsImpl(
        srcDir,
        jvmOptions.optionsName,
        commonCompilerOptionsImpl.baseImplName,
        commonCompilerOptionsImpl.helperName,
        jvmOptions.properties,
        withPrinterToFile
    )

    konst jsOptions = generateKotlinJsOptions(
        apiSrcDir,
        commonCompilerOptions,
        withPrinterToFile
    )
    generateKotlinJsOptionsImpl(
        srcDir,
        jsOptions.optionsName,
        commonCompilerOptionsImpl.baseImplName,
        commonCompilerOptionsImpl.helperName,
        jsOptions.properties,
        withPrinterToFile
    )

    konst nativeOptions = generateKotlinNativeOptions(
        apiSrcDir,
        commonCompilerOptions,
        withPrinterToFile
    )
    generateKotlinNativeOptionsImpl(
        srcDir,
        nativeOptions.optionsName,
        commonCompilerOptionsImpl.baseImplName,
        commonCompilerOptionsImpl.helperName,
        nativeOptions.properties,
        withPrinterToFile
    )

    konst jsDceOptions = generateJsDceOptions(
        apiSrcDir,
        commonToolOptions,
        withPrinterToFile
    )
    generateJsDceOptionsImpl(
        srcDir,
        jsDceOptions.optionsName,
        commonToolImplOptions.baseImplName,
        commonToolImplOptions.helperName,
        jsDceOptions.properties,
        withPrinterToFile
    )

    konst multiplatformCommonOptions = generateMultiplatformCommonOptions(
        apiSrcDir,
        commonCompilerOptions,
        withPrinterToFile
    )
    generateMultiplatformCommonOptionsImpl(
        srcDir,
        multiplatformCommonOptions.optionsName,
        commonCompilerOptionsImpl.baseImplName,
        commonCompilerOptionsImpl.helperName,
        multiplatformCommonOptions.properties,
        withPrinterToFile
    )
}

fun main() {
    generateKotlinGradleOptions(::getPrinterToFile)
}

private fun generateKotlinCommonToolOptions(
    apiSrcDir: File,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
): GeneratedOptions {
    konst commonInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinCommonCompilerToolOptions")
    konst commonOptions = gradleOptions<CommonToolArguments>()
    konst additionalOptions = gradleOptions<AdditionalGradleProperties>()
    withPrinterToFile(fileFromFqName(apiSrcDir, commonInterfaceFqName)) {
        generateInterface(
            commonInterfaceFqName,
            commonOptions + additionalOptions
        )
    }

    konst deprecatedCommonInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinCommonToolOptions")
    withPrinterToFile(fileFromFqName(apiSrcDir, deprecatedCommonInterfaceFqName)) {
        generateDeprecatedInterface(
            deprecatedCommonInterfaceFqName,
            commonInterfaceFqName,
            commonOptions + additionalOptions,
            parentType = null,
        )
    }

    println("### Attributes common for JVM, JS, and JS DCE\n")
    generateMarkdown(commonOptions + additionalOptions)

    return GeneratedOptions(commonInterfaceFqName, deprecatedCommonInterfaceFqName, (commonOptions + additionalOptions))
}

private fun generateKotlinCommonToolOptionsImpl(
    srcDir: File,
    commonToolOptionsInterfaceFqName: FqName,
    options: List<KProperty1<*, *>>,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
): GeneratedImplOptions {
    konst commonToolBaseImplFqName = FqName("${commonToolOptionsInterfaceFqName.asString()}$IMPLEMENTATION_SUFFIX")
    withPrinterToFile(fileFromFqName(srcDir, commonToolBaseImplFqName)) {
        generateImpl(
            commonToolBaseImplFqName,
            null,
            commonToolOptionsInterfaceFqName,
            options,
        )
    }

    konst k2CommonToolCompilerArgumentsFqName = FqName(CommonToolArguments::class.qualifiedName!!)
    konst commonToolCompilerArgsImplFqName = FqName(
        "${commonToolOptionsInterfaceFqName.asString()}$IMPLEMENTATION_HELPERS_SUFFIX"
    )
    withPrinterToFile(fileFromFqName(srcDir, commonToolCompilerArgsImplFqName)) {
        generateCompilerOptionsHelper(
            commonToolOptionsInterfaceFqName,
            commonToolCompilerArgsImplFqName,
            null,
            k2CommonToolCompilerArgumentsFqName,
            options
        )
    }

    return GeneratedImplOptions(commonToolBaseImplFqName, commonToolCompilerArgsImplFqName)
}

private fun generateKotlinCommonOptions(
    apiSrcDir: File,
    commonToolGeneratedOptions: GeneratedOptions,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
): GeneratedOptions {
    konst commonCompilerInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinCommonCompilerOptions")
    konst commonCompilerOptions = gradleOptions<CommonCompilerArguments>()
    withPrinterToFile(fileFromFqName(apiSrcDir, commonCompilerInterfaceFqName)) {
        generateInterface(
            commonCompilerInterfaceFqName,
            commonCompilerOptions,
            parentType = commonToolGeneratedOptions.optionsName,
        )
    }

    konst deprecatedCommonCompilerInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinCommonOptions")
    withPrinterToFile(fileFromFqName(apiSrcDir, deprecatedCommonCompilerInterfaceFqName)) {
        generateDeprecatedInterface(
            deprecatedCommonCompilerInterfaceFqName,
            commonCompilerInterfaceFqName,
            commonCompilerOptions,
            parentType = commonToolGeneratedOptions.deprecatedOptionsName
        )
    }

    println("\n### Attributes common for JVM and JS\n")
    generateMarkdown(commonCompilerOptions)

    return GeneratedOptions(commonCompilerInterfaceFqName, deprecatedCommonCompilerInterfaceFqName, commonCompilerOptions)
}

private fun generateKotlinCommonOptionsImpl(
    srcDir: File,
    commonOptionsInterfaceFqName: FqName,
    commonToolImpl: FqName,
    commonToolCompilerHelperName: FqName,
    options: List<KProperty1<*, *>>,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
): GeneratedImplOptions {
    konst commonCompilerImplFqName = FqName("${commonOptionsInterfaceFqName.asString()}$IMPLEMENTATION_SUFFIX")
    withPrinterToFile(fileFromFqName(srcDir, commonCompilerImplFqName)) {
        generateImpl(
            commonCompilerImplFqName,
            commonToolImpl,
            commonOptionsInterfaceFqName,
            options,
        )
    }

    konst k2CommonCompilerArgumentsFqName = FqName(CommonCompilerArguments::class.qualifiedName!!)
    konst commonCompilerHelperFqName = FqName(
        "${commonOptionsInterfaceFqName.asString()}$IMPLEMENTATION_HELPERS_SUFFIX"
    )
    withPrinterToFile(fileFromFqName(srcDir, commonCompilerHelperFqName)) {
        generateCompilerOptionsHelper(
            commonOptionsInterfaceFqName,
            commonCompilerHelperFqName,
            commonToolCompilerHelperName,
            k2CommonCompilerArgumentsFqName,
            options
        )
    }

    return GeneratedImplOptions(commonCompilerImplFqName, commonCompilerHelperFqName)
}

private fun generateKotlinJvmOptions(
    apiSrcDir: File,
    commonCompilerGeneratedOptions: GeneratedOptions,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
): GeneratedOptions {
    konst jvmInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinJvmCompilerOptions")
    konst jvmOptions = gradleOptions<K2JVMCompilerArguments>()
    withPrinterToFile(fileFromFqName(apiSrcDir, jvmInterfaceFqName)) {
        generateInterface(
            jvmInterfaceFqName,
            jvmOptions,
            parentType = commonCompilerGeneratedOptions.optionsName,
        )
    }

    konst deprecatedJvmInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinJvmOptions")
    withPrinterToFile(fileFromFqName(apiSrcDir, deprecatedJvmInterfaceFqName)) {
        generateDeprecatedInterface(
            deprecatedJvmInterfaceFqName,
            jvmInterfaceFqName,
            jvmOptions,
            parentType = commonCompilerGeneratedOptions.deprecatedOptionsName
        )
    }

    println("\n### Attributes specific for JVM\n")
    generateMarkdown(jvmOptions)

    return GeneratedOptions(jvmInterfaceFqName, deprecatedJvmInterfaceFqName, jvmOptions)
}

private fun generateKotlinJvmOptionsImpl(
    srcDir: File,
    jvmInterfaceFqName: FqName,
    commonCompilerImpl: FqName,
    commonCompilerHelperName: FqName,
    jvmOptions: List<KProperty1<*, *>>,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst jvmImplFqName = FqName("${jvmInterfaceFqName.asString()}$IMPLEMENTATION_SUFFIX")
    withPrinterToFile(fileFromFqName(srcDir, jvmImplFqName)) {
        generateImpl(
            jvmImplFqName,
            commonCompilerImpl,
            jvmInterfaceFqName,
            jvmOptions
        )
    }

    konst k2JvmCompilerArgumentsFqName = FqName(K2JVMCompilerArguments::class.qualifiedName!!)
    konst jvmCompilerOptionsHelperFqName = FqName(
        "${jvmInterfaceFqName.asString()}$IMPLEMENTATION_HELPERS_SUFFIX"
    )
    withPrinterToFile(fileFromFqName(srcDir, jvmCompilerOptionsHelperFqName)) {
        generateCompilerOptionsHelper(
            jvmInterfaceFqName,
            jvmCompilerOptionsHelperFqName,
            commonCompilerHelperName,
            k2JvmCompilerArgumentsFqName,
            jvmOptions
        )
    }
}

private fun generateKotlinJsOptions(
    apiSrcDir: File,
    commonCompilerOptions: GeneratedOptions,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
): GeneratedOptions {
    konst jsInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinJsCompilerOptions")
    konst jsOptions = gradleOptions<K2JSCompilerArguments>()
    withPrinterToFile(fileFromFqName(apiSrcDir, jsInterfaceFqName)) {
        generateInterface(
            jsInterfaceFqName,
            jsOptions,
            parentType = commonCompilerOptions.optionsName,
        )
    }

    konst deprecatedJsInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinJsOptions")
    withPrinterToFile(fileFromFqName(apiSrcDir, deprecatedJsInterfaceFqName)) {
        generateDeprecatedInterface(
            deprecatedJsInterfaceFqName,
            jsInterfaceFqName,
            jsOptions,
            parentType = commonCompilerOptions.deprecatedOptionsName,
        )
    }

    println("\n### Attributes specific for JS\n")
    generateMarkdown(jsOptions)

    return GeneratedOptions(jsInterfaceFqName, deprecatedJsInterfaceFqName, jsOptions)
}

private fun generateKotlinJsOptionsImpl(
    srcDir: File,
    jsInterfaceFqName: FqName,
    commonCompilerImpl: FqName,
    commonCompilerHelperName: FqName,
    jsOptions: List<KProperty1<*, *>>,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst jsImplFqName = FqName("${jsInterfaceFqName.asString()}$IMPLEMENTATION_SUFFIX")
    withPrinterToFile(fileFromFqName(srcDir, jsImplFqName)) {
        generateImpl(
            jsImplFqName,
            commonCompilerImpl,
            jsInterfaceFqName,
            jsOptions
        )
    }

    konst k2JsCompilerArgumentsFqName = FqName(K2JSCompilerArguments::class.qualifiedName!!)
    konst jsCompilerOptionsHelperFqName = FqName(
        "${jsInterfaceFqName.asString()}$IMPLEMENTATION_HELPERS_SUFFIX"
    )
    withPrinterToFile(fileFromFqName(srcDir, jsCompilerOptionsHelperFqName)) {
        generateCompilerOptionsHelper(
            jsInterfaceFqName,
            jsCompilerOptionsHelperFqName,
            commonCompilerHelperName,
            k2JsCompilerArgumentsFqName,
            jsOptions
        )
    }
}

private fun generateKotlinNativeOptions(
    apiSrcDir: File,
    commonCompilerOptions: GeneratedOptions,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
): GeneratedOptions {
    konst nativeInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinNativeCompilerOptions")
    konst nativeOptions = gradleOptions<K2NativeCompilerArguments>()
    withPrinterToFile(fileFromFqName(apiSrcDir, nativeInterfaceFqName)) {
        generateInterface(
            nativeInterfaceFqName,
            nativeOptions,
            parentType = commonCompilerOptions.optionsName,
        )
    }

    println("\n### Attributes specific for Native\n")
    generateMarkdown(nativeOptions)

    return GeneratedOptions(nativeInterfaceFqName, null, nativeOptions)
}

private fun generateKotlinNativeOptionsImpl(
    srcDir: File,
    nativeInterfaceFqName: FqName,
    commonCompilerImpl: FqName,
    commonCompilerHelper: FqName,
    nativeOptions: List<KProperty1<*, *>>,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst nativeImplFqName = FqName("${nativeInterfaceFqName.asString()}$IMPLEMENTATION_SUFFIX")
    withPrinterToFile(fileFromFqName(srcDir, nativeImplFqName)) {
        generateImpl(
            nativeImplFqName,
            commonCompilerImpl,
            nativeInterfaceFqName,
            nativeOptions
        )
    }

    konst k2NativeCompilerArgumentsFqName = FqName(K2NativeCompilerArguments::class.qualifiedName!!)
    konst nativeCompilerOptionsHelperFqName = FqName(
        "${nativeInterfaceFqName.asString()}$IMPLEMENTATION_HELPERS_SUFFIX"
    )
    withPrinterToFile(fileFromFqName(srcDir, nativeCompilerOptionsHelperFqName)) {
        generateCompilerOptionsHelper(
            nativeInterfaceFqName,
            nativeCompilerOptionsHelperFqName,
            commonCompilerHelper,
            k2NativeCompilerArgumentsFqName,
            nativeOptions
        )
    }
}


private fun generateJsDceOptions(
    apiSrcDir: File,
    commonToolOptions: GeneratedOptions,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
): GeneratedOptions {
    konst jsDceInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinJsDceCompilerToolOptions")
    konst jsDceOptions = gradleOptions<K2JSDceArguments>()
    withPrinterToFile(fileFromFqName(apiSrcDir, jsDceInterfaceFqName)) {
        generateInterface(
            jsDceInterfaceFqName,
            jsDceOptions,
            parentType = commonToolOptions.optionsName,
        )
    }

    konst deprecatedJsDceInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinJsDceOptions")
    withPrinterToFile(fileFromFqName(apiSrcDir, deprecatedJsDceInterfaceFqName)) {
        generateDeprecatedInterface(
            deprecatedJsDceInterfaceFqName,
            jsDceInterfaceFqName,
            jsDceOptions,
            parentType = commonToolOptions.deprecatedOptionsName,
        )
    }

    println("\n### Attributes specific for JS/DCE\n")
    generateMarkdown(jsDceOptions)

    return GeneratedOptions(jsDceInterfaceFqName, deprecatedJsDceInterfaceFqName, jsDceOptions)
}

private fun generateJsDceOptionsImpl(
    srcDir: File,
    jsDceInterfaceFqName: FqName,
    commonToolImpl: FqName,
    commonToolHelper: FqName,
    jsDceOptions: List<KProperty1<*, *>>,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst jsDceImplFqName = FqName("${jsDceInterfaceFqName.asString()}$IMPLEMENTATION_SUFFIX")
    withPrinterToFile(fileFromFqName(srcDir, jsDceImplFqName)) {
        generateImpl(
            jsDceImplFqName,
            commonToolImpl,
            jsDceInterfaceFqName,
            jsDceOptions
        )
    }

    konst k2JsDceArgumentsFqName = FqName(K2JSDceArguments::class.qualifiedName!!)
    konst jsDceCompilerHelperFqName = FqName(
        "${jsDceInterfaceFqName.asString()}$IMPLEMENTATION_HELPERS_SUFFIX"
    )
    withPrinterToFile(fileFromFqName(srcDir, jsDceCompilerHelperFqName)) {
        generateCompilerOptionsHelper(
            jsDceInterfaceFqName,
            jsDceCompilerHelperFqName,
            commonToolHelper,
            k2JsDceArgumentsFqName,
            jsDceOptions
        )
    }
}

private fun generateMultiplatformCommonOptions(
    apiSrcDir: File,
    commonCompilerOptions: GeneratedOptions,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
): GeneratedOptions {
    konst multiplatformCommonInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinMultiplatformCommonCompilerOptions")
    konst multiplatformCommonOptions = gradleOptions<K2MetadataCompilerArguments>()
    withPrinterToFile(fileFromFqName(apiSrcDir, multiplatformCommonInterfaceFqName)) {
        generateInterface(
            multiplatformCommonInterfaceFqName,
            multiplatformCommonOptions,
            parentType = commonCompilerOptions.optionsName,
        )
    }

    konst deprecatedMultiplatformCommonInterfaceFqName = FqName("$OPTIONS_PACKAGE_PREFIX.KotlinMultiplatformCommonOptions")
    withPrinterToFile(fileFromFqName(apiSrcDir, deprecatedMultiplatformCommonInterfaceFqName)) {
        generateDeprecatedInterface(
            deprecatedMultiplatformCommonInterfaceFqName,
            multiplatformCommonInterfaceFqName,
            parentType = commonCompilerOptions.deprecatedOptionsName,
            properties = multiplatformCommonOptions
        )
    }

    println("\n### Attributes specific for Multiplatform/Common\n")
    generateMarkdown(multiplatformCommonOptions)

    return GeneratedOptions(multiplatformCommonInterfaceFqName, deprecatedMultiplatformCommonInterfaceFqName, multiplatformCommonOptions)
}

private fun generateMultiplatformCommonOptionsImpl(
    srcDir: File,
    multiplatformCommonInterfaceFqName: FqName,
    commonCompilerImpl: FqName,
    commonCompilerHelper: FqName,
    multiplatformCommonOptions: List<KProperty1<*, *>>,
    withPrinterToFile: (targetFile: File, Printer.() -> Unit) -> Unit
) {
    konst multiplatformCommonImplFqName = FqName("${multiplatformCommonInterfaceFqName.asString()}$IMPLEMENTATION_SUFFIX")
    withPrinterToFile(fileFromFqName(srcDir, multiplatformCommonImplFqName)) {
        generateImpl(
            multiplatformCommonImplFqName,
            commonCompilerImpl,
            multiplatformCommonInterfaceFqName,
            multiplatformCommonOptions
        )
    }

    konst k2metadataCompilerArgumentsFqName = FqName(K2MetadataCompilerArguments::class.qualifiedName!!)
    konst metadataCompilerHelperFqName = FqName(
        "${multiplatformCommonInterfaceFqName.asString()}$IMPLEMENTATION_HELPERS_SUFFIX"
    )
    withPrinterToFile(fileFromFqName(srcDir, metadataCompilerHelperFqName)) {
        generateCompilerOptionsHelper(
            multiplatformCommonInterfaceFqName,
            metadataCompilerHelperFqName,
            commonCompilerHelper,
            k2metadataCompilerArgumentsFqName,
            multiplatformCommonOptions
        )
    }
}

private inline fun <reified T : Any> List<KProperty1<T, *>>.filterToBeDeleted() = filter { prop ->
    prop.findAnnotation<GradleDeprecatedOption>()
        ?.let { LanguageVersion.fromVersionString(it.removeAfter) }
        ?.let { it >= LanguageVersion.LATEST_STABLE }
        ?: true
}

private inline fun <reified T : Any> gradleOptions(): List<KProperty1<T, *>> =
    T::class
        .declaredMemberProperties
        .filter {
            it.findAnnotation<GradleOption>() != null
        }
        .filterToBeDeleted()
        .sortedBy { it.name }

internal fun fileFromFqName(baseDir: File, fqName: FqName): File {
    konst fileRelativePath = fqName.asString().replace(".", "/") + ".kt"
    return File(baseDir, fileRelativePath)
}

private fun Printer.generateInterface(
    type: FqName,
    properties: List<KProperty1<*, *>>,
    parentType: FqName? = null,
) {
    konst afterType = parentType?.let { " : $it" }
    generateDeclaration("interface", type, afterType = afterType) {
        for (property in properties) {
            println()
            generateDoc(property)
            generateOptionDeprecation(property)
            generatePropertyProvider(property)
        }
    }
}

private fun Printer.generateDeprecatedInterface(
    type: FqName,
    compilerOptionType: FqName,
    properties: List<KProperty1<*, *>>,
    parentType: FqName? = null,
) {
    konst afterType = parentType?.let { " : $it" }
    // Add @Deprecated annotation back once proper migration to compilerOptions will be supported
    konst modifier = """
    interface
    """.trimIndent()
    konst deprecatedProperties = properties.filter { it.generateDeprecatedKotlinOption }
    // KotlinMultiplatformCommonOptions doesn't have any options, but it is being kept for backward compatibility
    if (deprecatedProperties.isNotEmpty() || type.asString().endsWith("KotlinMultiplatformCommonOptions")) {
        generateDeclaration(modifier, type, afterType = afterType) {

            println("${if (parentType != null) "override " else ""}konst options: $compilerOptionType")
            deprecatedProperties
                .forEach {
                    println()
                    generatePropertyGetterAndSetter(it)
                }
        }
    }
}

private fun Printer.generateImpl(
    type: FqName,
    parentImplFqName: FqName?,
    parentType: FqName,
    properties: List<KProperty1<*, *>>
) {
    konst modifiers = "internal abstract class"
    konst afterType = if (parentImplFqName != null) {
        ": $parentImplFqName(objectFactory), $parentType"
    } else {
        ": $parentType"
    }
    generateDeclaration(
        modifiers,
        type,
        constructorDeclaration = "@javax.inject.Inject constructor(\n    objectFactory: org.gradle.api.model.ObjectFactory\n)",
        afterType = afterType
    ) {
        for (property in properties) {
            println()
            generatePropertyProviderImpl(property)
        }
    }
}

private fun Printer.generateCompilerOptionsHelper(
    type: FqName,
    helperName: FqName,
    parentHelperName: FqName?,
    argsType: FqName,
    properties: List<KProperty1<*, *>>
) {
    konst modifiers = "internal object"

    generateDeclaration(
        modifiers,
        helperName,
    ) {
        println()
        println("internal fun fillCompilerArguments(")
        withIndent {
            println("from: $type,")
            println("args: $argsType,")
        }
        println(") {")
        withIndent {
            if (parentHelperName != null) println("$parentHelperName.fillCompilerArguments(from, args)")
            for (property in properties) {
                konst defaultValue = property.gradleValues
                if (property.name != "freeCompilerArgs") {
                    konst getter = if (property.gradleReturnType.endsWith("?")) ".orNull" else ".get()"
                    konst toArg = defaultValue.toArgumentConverter?.substringAfter("this") ?: ""
                    println("args.${property.name} = from.${property.name}$getter$toArg")
                } else {
                    println("args.freeArgs += from.${property.name}.get()")
                }
            }

            addAdditionalJvmArgs(helperName)
        }
        println("}")

        println()
        println("internal fun syncOptionsAsConvention(")
        withIndent {
            println("from: $type,")
            println("into: $type,")
        }
        println(") {")
        withIndent {
            konst multiValuesReturnTypes = setOf(
                "org.gradle.api.provider.ListProperty",
                "org.gradle.api.provider.SetProperty",
            )
            if (parentHelperName != null) println("$parentHelperName.syncOptionsAsConvention(from, into)")
            for (property in properties) {

                // Behaviour of ListProperty, SetProperty, MapProperty append operators in regard to convention konstue
                // is confusing for users: https://github.com/gradle/gradle/issues/18352
                // To make it less confusing for such types instead of wiring them via ".convention()" we updating
                // current konstue
                konst gradleLazyReturnType = property.gradleLazyReturnType
                konst mapper = when {
                    multiValuesReturnTypes.any { gradleLazyReturnType.startsWith(it) } -> "addAll"
                    gradleLazyReturnType.startsWith("org.gradle.api.provider.MapProperty") -> "putAll"
                    else -> "convention"
                }
                println("into.${property.name}.$mapper(from.${property.name})")
            }
        }
        println("}")
    }
}

private fun Printer.addAdditionalJvmArgs(implType: FqName) {
    // Adding required 'noStdlib' and 'noReflect' compiler arguments for JVM compilation
    // Otherwise compilation via build tools will fail
    if (implType.shortName().toString() == "KotlinJvmCompilerOptions$IMPLEMENTATION_HELPERS_SUFFIX") {
        println()
        println("// Arguments with always default konstues when used from build tools")
        println("args.noStdlib = true")
        println("args.noReflect = true")
        println("args.allowNoSourceFiles = true")
    }
}

internal fun Printer.generateDeclaration(
    modifiers: String,
    type: FqName,
    constructorDeclaration: String? = null,
    afterType: String? = null,
    generateBody: Printer.() -> Unit
) {
    println(
        """
        // DO NOT EDIT MANUALLY!
        // Generated by org/jetbrains/kotlin/generators/arguments/GenerateGradleOptions.kt
        // To regenerate run 'generateGradleOptions' task
        @file:Suppress("RemoveRedundantQualifierName", "Deprecation", "DuplicatedCode")
        
        """.trimIndent()
    )

    if (!type.parent().isRoot) {
        println("package ${type.parent()}")
        println()
    }
    print("$modifiers ${type.shortName()}")
    constructorDeclaration?.let { print(" $it ") }
    afterType?.let { print("$afterType") }
    println(" {")
    withIndent {
        generateBody()
    }
    println("}")
}

private fun Printer.generatePropertyProvider(
    property: KProperty1<*, *>,
    modifiers: String = ""
) {
    if (property.gradleDefaultValue == "null" &&
        property.gradleInputTypeAsEnum == GradleInputTypes.INPUT
    ) {
        println("@get:org.gradle.api.tasks.Optional")
    }
    println("@get:${property.gradleInputType}")
    println("${modifiers.appendWhitespaceIfNotBlank}konst ${property.name}: ${property.gradleLazyReturnType}")
}

private fun Printer.generatePropertyProviderImpl(
    property: KProperty1<*, *>,
    modifiers: String = ""
) {
    generateOptionDeprecation(property)
    println(
        "override ${modifiers.appendWhitespaceIfNotBlank}konst ${property.name}: ${property.gradleLazyReturnType} ="
    )
    withIndent {
        konst convention = if (property.gradleDefaultValue != "null") {
            ".convention(${property.gradleDefaultValue})"
        } else {
            ""
        }

        println(
            "objectFactory${property.gradleLazyReturnTypeInstantiator}$convention"
        )
    }
}

private fun Printer.generatePropertyGetterAndSetter(
    property: KProperty1<*, *>,
    modifiers: String = "",
) {
    konst defaultValue = property.gradleValues
    konst returnType = property.gradleReturnType

    if (defaultValue.type != defaultValue.kotlinOptionsType) {
        assert(defaultValue.fromKotlinOptionConverterProp != null)
        assert(defaultValue.toKotlinOptionConverterProp != null)
    }

    if (defaultValue.fromKotlinOptionConverterProp != null) {
        println("private konst ${defaultValue.kotlinOptionsType}.${property.name}CompilerOption get() = ${defaultValue.fromKotlinOptionConverterProp}")
        println()
        println("private konst ${defaultValue.type}.${property.name}KotlinOption get() = ${defaultValue.toKotlinOptionConverterProp}")
        println()
    }

    generateDoc(property)
    generateOptionDeprecation(property)
    println("${modifiers.appendWhitespaceIfNotBlank}var ${property.name}: $returnType")
    konst propGetter = if (returnType.endsWith("?")) ".orNull" else ".get()"
    konst getter = if (defaultValue.fromKotlinOptionConverterProp != null) {
        "$propGetter.${property.name}KotlinOption"
    } else {
        propGetter
    }
    konst setter = if (defaultValue.toKotlinOptionConverterProp != null) {
        ".set(konstue.${property.name}CompilerOption)"
    } else {
        ".set(konstue)"
    }
    withIndent {
        println("get() = options.${property.name}$getter")
        println("set(konstue) = options.${property.name}$setter")
    }
}

private konst String.appendWhitespaceIfNotBlank get() = if (isNotBlank()) "$this " else ""

private fun Printer.generateOptionDeprecation(property: KProperty1<*, *>) {
    property.findAnnotation<GradleDeprecatedOption>()
        ?.let { DeprecatedOptionAnnotator.generateOptionAnnotation(it) }
        ?.also { println(it) }
}

private fun Printer.generateDoc(property: KProperty1<*, *>) {
    konst description = property.findAnnotation<Argument>()!!.description
    konst possibleValues = property.gradleValues.possibleValues
    konst defaultValue = property.gradleValues.defaultValue

    println("/**")
    println(" * ${description.replace("\n", " ")}")
    if (possibleValues != null) {
        println(" * Possible konstues: ${possibleValues.joinToString()}")
    }
    println(" * Default konstue: ${defaultValue.removePrefix("$OPTIONS_PACKAGE_PREFIX.")}")
    println(" */")
}

internal inline fun Printer.withIndent(fn: Printer.() -> Unit) {
    pushIndent()
    fn()
    popIndent()
}

private fun generateMarkdown(properties: List<KProperty1<*, *>>) {
    println("| Name | Description | Possible konstues |Default konstue |")
    println("|------|-------------|-----------------|--------------|")
    for (property in properties) {
        konst name = property.name
        if (name == "includeRuntime") continue   // This option has no effect in Gradle builds
        konst renderName = listOfNotNull("`$name`", property.findAnnotation<GradleDeprecatedOption>()?.let { "__(Deprecated)__" })
            .joinToString(" ")
        konst description = property.findAnnotation<Argument>()!!.description
        konst possibleValues = property.gradleValues.possibleValues
        konst defaultValue = when (property.gradleDefaultValue) {
            "null" -> ""
            "emptyList()" -> "[]"
            else -> property.gradleDefaultValue
        }

        println("| $renderName | $description | ${possibleValues.orEmpty().joinToString()} | $defaultValue |")
    }
}

private konst KProperty1<*, *>.gradleValues: DefaultValues
    get() = findAnnotation<GradleOption>()!!.konstue.run {
        when (this) {
            DefaultValue.BOOLEAN_FALSE_DEFAULT -> DefaultValues.BooleanFalseDefault
            DefaultValue.BOOLEAN_TRUE_DEFAULT -> DefaultValues.BooleanTrueDefault
            DefaultValue.STRING_NULL_DEFAULT -> DefaultValues.StringNullDefault
            DefaultValue.EMPTY_STRING_LIST_DEFAULT -> DefaultValues.EmptyStringListDefault
            DefaultValue.EMPTY_STRING_ARRAY_DEFAULT -> DefaultValues.EmptyStringArrayDefault
            DefaultValue.JVM_TARGET_VERSIONS -> DefaultValues.JvmTargetVersions
            DefaultValue.LANGUAGE_VERSIONS -> DefaultValues.LanguageVersions
            DefaultValue.API_VERSIONS -> DefaultValues.ApiVersions
            DefaultValue.JS_MAIN -> DefaultValues.JsMain
            DefaultValue.JS_ECMA_VERSIONS -> DefaultValues.JsEcmaVersions
            DefaultValue.JS_MODULE_KINDS -> DefaultValues.JsModuleKinds
            DefaultValue.JS_SOURCE_MAP_CONTENT_MODES -> DefaultValues.JsSourceMapContentModes
            DefaultValue.JS_SOURCE_MAP_NAMES_POLICY -> DefaultValues.JsSourceMapNamesPolicies
        }
    }

private konst KProperty1<*, *>.gradleDefaultValue: String
    get() = gradleValues.defaultValue

private konst KProperty1<*, *>.gradleReturnType: String
    get() {
        // Set nullability based on Gradle default konstue
        var type = returnType.withNullability(false).toString().substringBeforeLast("!")
        if (gradleDefaultValue == "null") {
            type += "?"
        }
        return type
    }

private konst KProperty1<*, *>.gradleLazyReturnType: String
    get() {
        konst returnType = gradleValues.type
        konst classifier = returnType.classifier
        return when {
            classifier is KClass<*> && classifier == List::class ->
                "org.gradle.api.provider.ListProperty<${returnType.arguments.first().type!!.withNullability(false)}>"
            classifier is KClass<*> && classifier == Set::class ->
                "org.gradle.api.provider.SetProperty<${returnType.arguments.first().type!!.withNullability(false)}>"
            classifier is KClass<*> && classifier == Map::class ->
                "org.gradle.api.provider.MapProperty<${returnType.arguments[0]}, ${returnType.arguments[1]}"
            else -> "org.gradle.api.provider.Property<${returnType.withNullability(false)}>"
        }
    }

private konst KProperty1<*, *>.gradleLazyReturnTypeInstantiator: String
    get() {
        konst returnType = gradleValues.type
        konst classifier = returnType.classifier
        return when {
            classifier is KClass<*> && classifier == List::class ->
                ".listProperty(${returnType.arguments.first().type!!.withNullability(false)}::class.java)"
            classifier is KClass<*> && classifier == Set::class ->
                ".setProperty(${returnType.arguments.first().type!!.withNullability(false)}::class.java)"
            classifier is KClass<*> && classifier == Map::class ->
                ".mapProperty(${returnType.arguments[0]}::class.java, ${returnType.arguments[1]}::class.java)"
            else -> ".property(${returnType.withNullability(false)}::class.java)"
        }
    }

private konst KProperty1<*, *>.gradleInputTypeAsEnum: GradleInputTypes
    get() = findAnnotation<GradleOption>()!!.gradleInputType

private konst KProperty1<*, *>.gradleInputType: String
    get() = findAnnotation<GradleOption>()!!.gradleInputType.gradleType

private konst KProperty1<*, *>.generateDeprecatedKotlinOption: Boolean
    get() = findAnnotation<GradleOption>()!!.shouldGenerateDeprecatedKotlinOptions

private inline fun <reified T> KAnnotatedElement.findAnnotation(): T? =
    annotations.firstIsInstanceOrNull()

object DeprecatedOptionAnnotator {
    fun generateOptionAnnotation(annotation: GradleDeprecatedOption): String {
        konst message = annotation.message.takeIf { it.isNotEmpty() }?.let { "message = \"$it\"" }
        konst level = "level = DeprecationLevel.${annotation.level.name}"
        konst arguments = listOfNotNull(message, level).joinToString()
        return "@Deprecated($arguments)"
    }
}
