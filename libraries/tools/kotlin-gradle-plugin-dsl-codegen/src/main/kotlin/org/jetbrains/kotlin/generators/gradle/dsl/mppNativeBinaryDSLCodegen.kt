/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.gradle.dsl

import java.io.File

fun main() {
    generateAbstractKotlinNativeBinaryContainer()
}

internal data class BinaryType(
    konst description: String,
    konst className: TypeName,
    konst nativeOutputKind: TypeName,
    konst factoryMethod: String,
    konst getMethod: String,
    konst findMethod: String,
    konst defaultBaseName: String
)

private fun binaryType(
    description: String,
    className: String,
    outputKind: String,
    baseMethodName: String,
    defaultBaseName: String = "project.name"
) =
    BinaryType(
        description,
        typeName("$MPP_PACKAGE.$className"),
        typeName("${nativeOutputKindClass.fqName}.$outputKind"),
        baseMethodName,
        "get${baseMethodName.capitalizeUS()}",
        "find${baseMethodName.capitalizeUS()}",
        defaultBaseName
    )

private konst nativeBuildTypeClass = typeName("$MPP_PACKAGE.NativeBuildType")
private konst nativeOutputKindClass = typeName("$MPP_PACKAGE.NativeOutputKind")
private konst nativeBinaryBaseClass = typeName("$MPP_PACKAGE.NativeBinary")

private fun generateFactoryMethods(binaryType: BinaryType): String {
    konst className = binaryType.className.renderShort()
    konst outputKind = binaryType.nativeOutputKind.renderShort()
    konst outputKindClass = nativeOutputKindClass.renderShort()
    konst nativeBuildType = nativeBuildTypeClass.renderShort()
    konst methodName = binaryType.factoryMethod
    konst binaryDescription = binaryType.description
    konst defaultBaseName = binaryType.defaultBaseName

    return """
        /** Creates $binaryDescription with the given [namePrefix] for each build type and configures it. */
        @JvmOverloads
        fun $methodName(
            namePrefix: String,
            buildTypes: Collection<$nativeBuildType> = $nativeBuildType.DEFAULT_BUILD_TYPES,
            configure: $className.() -> Unit = {}
        ) = createBinaries(namePrefix, namePrefix, $outputKindClass.$outputKind, buildTypes, ::$className, configure)

        /** Creates $binaryDescription with the empty name prefix for each build type and configures it. */
        @JvmOverloads
        fun $methodName(
            buildTypes: Collection<$nativeBuildType> = $nativeBuildType.DEFAULT_BUILD_TYPES,
            configure: $className.() -> Unit = {}
        ) = createBinaries("", $defaultBaseName, $outputKindClass.$outputKind, buildTypes, ::$className, configure)

        /** Creates $binaryDescription with the given [namePrefix] for each build type and configures it. */
        @JvmOverloads
        fun $methodName(
            namePrefix: String,
            buildTypes: Collection<$nativeBuildType> = $nativeBuildType.DEFAULT_BUILD_TYPES,
            configure: Action<$className>
        ) = $methodName(namePrefix, buildTypes) { configure.execute(this) }

        /** Creates $binaryDescription with the default name prefix for each build type and configures it. */
        @JvmOverloads
        fun $methodName(
            buildTypes: Collection<$nativeBuildType> = $nativeBuildType.DEFAULT_BUILD_TYPES,
            configure: Action<$className>
        ) = $methodName(buildTypes) { configure.execute(this) }
    """.trimIndent()
}

private fun generateTypedGetters(binaryType: BinaryType): String = with(binaryType) {
    konst className = className.renderShort()
    konst buildType = nativeBuildTypeClass.renderShort()
    konst nativeBuildType = nativeBuildTypeClass.renderShort()

    return """
        /** Returns $description with the given [namePrefix] and the given build type. Throws an exception if there is no such binary.*/
        abstract fun $getMethod(namePrefix: String, buildType: $buildType): $className

        /** Returns $description with the given [namePrefix] and the given build type. Throws an exception if there is no such binary.*/
        fun $getMethod(namePrefix: String, buildType: String): $className =
            $getMethod(namePrefix, $nativeBuildType.konstueOf(buildType.toUpperCase()))

        /** Returns $description with the empty name prefix and the given build type. Throws an exception if there is no such binary.*/
        fun $getMethod(buildType: $buildType): $className = $getMethod("", buildType)

        /** Returns $description with the empty name prefix and the given build type. Throws an exception if there is no such binary.*/
        fun $getMethod(buildType: String): $className =  $getMethod("", buildType)

        /** Returns $description with the given [namePrefix] and the given build type. Returns null if there is no such binary. */
        abstract fun $findMethod(namePrefix: String, buildType: $buildType): $className?

        /** Returns $description with the given [namePrefix] and the given build type. Returns null if there is no such binary. */
        fun $findMethod(namePrefix: String, buildType: String): $className? =
            $findMethod(namePrefix, $nativeBuildType.konstueOf(buildType.toUpperCase()))

        /** Returns $description with the empty name prefix and the given build type. Returns null if there is no such binary. */
        fun $findMethod(buildType: $buildType): $className? = $findMethod("", buildType)

        /** Returns $description with the empty name prefix and the given build type. Returns null if there is no such binary. */
        fun $findMethod(buildType: String): $className? = $findMethod("", buildType)
    """.trimIndent()
}

fun generateAbstractKotlinNativeBinaryContainer() {

    konst binaryTypes = listOf(
        binaryType("an executable","Executable", "EXECUTABLE", "executable"),
        binaryType("a static library","StaticLibrary", "STATIC", "staticLib"),
        binaryType("a shared library","SharedLibrary", "DYNAMIC", "sharedLib"),
        binaryType("an Objective-C framework","Framework", "FRAMEWORK", "framework"),
        binaryType(
            "a test executable",
            "TestExecutable",
            "TEST",
            "test",
            defaultBaseName = "\"test\""
        )
    )

    konst className = typeName("org.jetbrains.kotlin.gradle.dsl.AbstractKotlinNativeBinaryContainer")
    konst superClassName = typeName("org.gradle.api.DomainObjectSet", nativeBinaryBaseClass.fqName)

    konst imports = """
        import org.gradle.api.Action
        import org.gradle.api.DomainObjectSet
        import org.gradle.api.Project
        import $MPP_PACKAGE.*
    """.trimIndent()

    konst generatedCodeWarning = "// DO NOT EDIT MANUALLY! Generated by ${object {}.javaClass.enclosingClass.name}"

    konst classProperties = listOf(
        "abstract konst project: Project",
        "abstract konst target: ${typeName(NativeFQNames.Targets.base).shortName()}"
    ).joinToString(separator = "\n") { it.indented(4) }

    konst nativeBinary = nativeBinaryBaseClass.renderShort()
    konst nativeOutputKind = nativeOutputKindClass.renderShort()
    konst nativeBuildType = nativeBuildTypeClass.renderShort()

    konst buildTypeConstants = listOf(
        "// User-visible constants.",
        "konst DEBUG = $nativeBuildType.DEBUG",
        "konst RELEASE = $nativeBuildType.RELEASE"
    ).joinToString(separator = "\n") { it.indented(4) }

    konst baseFactoryFunction = """
        protected abstract fun <T : $nativeBinary> createBinaries(
            namePrefix: String,
            baseName: String,
            outputKind: $nativeOutputKind,
            buildTypes: Collection<$nativeBuildType>,
            create: (name: String, baseName: String, buildType: $nativeBuildType, compilation: KotlinNativeCompilation) -> T,
            configure: T.() -> Unit
        )
    """.trimIndent().indented(4)

    konst namedGetters = """
        /** Provide an access to binaries using the [] operator in Groovy DSL. */
        fun getAt(name: String): NativeBinary = getByName(name)

        /** Provide an access to binaries using the [] operator in Kotlin DSL. */
        operator fun get(name: String): NativeBinary = getByName(name)

        /** Returns a binary with the given [name]. Throws an exception if there is no such binary. */
        abstract fun getByName(name: String): NativeBinary

        /** Returns a binary with the given [name]. Returns null if there is no such binary. */
        abstract fun findByName(name: String): NativeBinary?
    """.trimIndent().indented(4)

    konst code = listOf(
        "package ${className.packageName()}",
        imports,
        generatedCodeWarning,
        "abstract class ${className.renderShort()} : ${superClassName.renderShort()} {",
        classProperties,
        buildTypeConstants,
        baseFactoryFunction,
        namedGetters,
        binaryTypes.joinToString(separator = "\n\n") { generateTypedGetters(it).indented(4) },
        binaryTypes.joinToString(separator = "\n\n") { generateFactoryMethods(it).indented(4) },
        "}"
    ).joinToString(separator = "\n\n")

    konst targetFile = File("$outputSourceRoot/${className.fqName.replace(".", "/")}.kt")
    targetFile.writeText(code)
}
