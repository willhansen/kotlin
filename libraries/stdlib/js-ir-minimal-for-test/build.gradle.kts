import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        nodejs()
    }
}

konst commonMainSources by task<Sync> {
    dependsOn(":kotlin-stdlib-js-ir:commonMainSources")
    from {
        konst fullCommonMainSources = tasks.getByPath(":kotlin-stdlib-js-ir:commonMainSources")
        exclude(
            listOf(
                "libraries/stdlib/unsigned/src/kotlin/UByteArray.kt",
                "libraries/stdlib/unsigned/src/kotlin/UIntArray.kt",
                "libraries/stdlib/unsigned/src/kotlin/ULongArray.kt",
                "libraries/stdlib/unsigned/src/kotlin/UMath.kt",
                "libraries/stdlib/unsigned/src/kotlin/UNumbers.kt",
                "libraries/stdlib/unsigned/src/kotlin/UShortArray.kt",
                "libraries/stdlib/unsigned/src/kotlin/UStrings.kt",
                "libraries/stdlib/common/src/generated/_Arrays.kt",
                "libraries/stdlib/common/src/generated/_Collections.kt",
                "libraries/stdlib/common/src/generated/_Comparisons.kt",
                "libraries/stdlib/common/src/generated/_Maps.kt",
                "libraries/stdlib/common/src/generated/_OneToManyTitlecaseMappings.kt",
                "libraries/stdlib/common/src/generated/_Sequences.kt",
                "libraries/stdlib/common/src/generated/_Sets.kt",
                "libraries/stdlib/common/src/generated/_Strings.kt",
                "libraries/stdlib/common/src/generated/_UArrays.kt",
                "libraries/stdlib/common/src/generated/_URanges.kt",
                "libraries/stdlib/common/src/generated/_UCollections.kt",
                "libraries/stdlib/common/src/generated/_UComparisons.kt",
                "libraries/stdlib/common/src/generated/_USequences.kt",
                "libraries/stdlib/common/src/kotlin/SequencesH.kt",
                "libraries/stdlib/common/src/kotlin/TextH.kt",
                "libraries/stdlib/common/src/kotlin/UMath.kt",
                "libraries/stdlib/common/src/kotlin/collections/**",
                "libraries/stdlib/common/src/kotlin/ioH.kt",
                "libraries/stdlib/src/kotlin/collections/**",
                "libraries/stdlib/src/kotlin/io/**",
                "libraries/stdlib/src/kotlin/properties/Delegates.kt",
                "libraries/stdlib/src/kotlin/random/URandom.kt",
                "libraries/stdlib/src/kotlin/text/**",
                "libraries/stdlib/src/kotlin/time/**",
                "libraries/stdlib/src/kotlin/util/KotlinVersion.kt",
                "libraries/stdlib/src/kotlin/util/Tuples.kt",
                "libraries/stdlib/src/kotlin/enums/**"
            )
        )
        fullCommonMainSources.outputs.files.singleFile
    }

    into("$buildDir/commonMainSources")
}

konst commonMainCollectionSources by task<Sync> {
    dependsOn(":kotlin-stdlib-js-ir:commonMainSources")
    from {
        konst fullCommonMainSources = tasks.getByPath(":kotlin-stdlib-js-ir:commonMainSources")
        include("libraries/stdlib/src/kotlin/collections/PrimitiveIterators.kt")
        fullCommonMainSources.outputs.files.singleFile
    }

    into("$buildDir/commonMainCollectionSources")
}

konst jsMainSources by task<Sync> {
    dependsOn(":kotlin-stdlib-js-ir:jsMainSources")

    from {
        konst fullJsMainSources = tasks.getByPath(":kotlin-stdlib-js-ir:jsMainSources")
        exclude(
            listOf(
                "libraries/stdlib/js/src/org.w3c/**",
                "libraries/stdlib/js/src/kotlin/char.kt",
                "libraries/stdlib/js/src/kotlin/collections.kt",
                "libraries/stdlib/js/src/kotlin/collections/**",
                "libraries/stdlib/js/src/kotlin/time/**",
                "libraries/stdlib/js/src/kotlin/console.kt",
                "libraries/stdlib/js/src/kotlin/coreDeprecated.kt",
                "libraries/stdlib/js/src/kotlin/date.kt",
                "libraries/stdlib/js/src/kotlin/grouping.kt",
                "libraries/stdlib/js/src/kotlin/ItemArrayLike.kt",
                "libraries/stdlib/js/src/kotlin/io/**",
                "libraries/stdlib/js/src/kotlin/json.kt",
                "libraries/stdlib/js/src/kotlin/promise.kt",
                "libraries/stdlib/js/src/kotlin/regexp.kt",
                "libraries/stdlib/js/src/kotlin/sequence.kt",
                "libraries/stdlib/js/src/kotlin/throwableExtensions.kt",
                "libraries/stdlib/js/src/kotlin/text/**",
                "libraries/stdlib/js/src/kotlin/reflect/KTypeHelpers.kt",
                "libraries/stdlib/js/src/kotlin/reflect/KTypeParameterImpl.kt",
                "libraries/stdlib/js/src/kotlin/reflect/KTypeImpl.kt",
                "libraries/stdlib/js/src/kotlin/dom/**",
                "libraries/stdlib/js/src/kotlin/browser/**",
                "libraries/stdlib/js/src/kotlinx/dom/**",
                "libraries/stdlib/js/src/kotlinx/browser/**",
                "libraries/stdlib/js/src/kotlin/enums/**"
            )
        )
        fullJsMainSources.outputs.files.singleFile
    }

    for (jsIrSrcDir in listOf("builtins", "runtime", "src")) {
        from("$rootDir/libraries/stdlib/js-ir/$jsIrSrcDir") {
            exclude(
                listOf(
                    "collectionsHacks.kt",
                    "generated/**",
                    "kotlin/text/**"
                )
            )
            into("libraries/stdlib/js-ir/$jsIrSrcDir")
        }
    }

    from("$rootDir/libraries/stdlib/js-ir-minimal-for-test/src")
    into("$buildDir/jsMainSources")
}

kotlin {
    sourceSets {
        konst commonMain by getting {
            kotlin.srcDir(files(commonMainSources.map { it.destinationDir }))
            kotlin.srcDir(files(commonMainCollectionSources.map { it.destinationDir }))
        }
        konst jsMain by getting {
            kotlin.srcDir(files(jsMainSources.map { it.destinationDir }))
        }
    }
}

tasks.withType<KotlinCompile<*>> {
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xallow-kotlin-package",
        "-opt-in=kotlin.ExperimentalMultiplatform",
        "-opt-in=kotlin.contracts.ExperimentalContracts",
        "-opt-in=kotlin.RequiresOptIn",
        "-opt-in=kotlin.ExperimentalUnsignedTypes",
        "-opt-in=kotlin.ExperimentalStdlibApi",
        "-XXLanguage:+RangeUntilOperator",
    )
}

tasks {
    compileKotlinMetadata {
        enabled = false
    }

    named("compileKotlinJs", KotlinCompile::class) {
        kotlinOptions.freeCompilerArgs += "-Xir-module-name=kotlin"
    }
}
