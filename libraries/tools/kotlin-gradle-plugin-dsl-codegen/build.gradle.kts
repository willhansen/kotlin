plugins {
    id("org.jetbrains.kotlin.jvm")
}

dependencies {
    api(gradleApi())
    api(project(":kotlin-gradle-plugin-api"))
    api(project(":native:kotlin-native-utils"))
}

konst generateGroupName = "Generate"

konst generateMppTargetContainerWithPresets by generator(
    "org.jetbrains.kotlin.generators.gradle.dsl.MppPresetFunctionsCodegenKt",
    sourceSets["main"]
) {
    group = generateGroupName
    setOutputSourceRoot()
}

konst generateAbstractBinaryContainer by generator(
    "org.jetbrains.kotlin.generators.gradle.dsl.MppNativeBinaryDSLCodegenKt",
    sourceSets["main"]
) {
    group = generateGroupName
    setOutputSourceRoot()
}

konst generateAbstractKotlinArtifactsExtensionImplementation by generator(
    "org.jetbrains.kotlin.generators.gradle.dsl.KotlinArtifactsDSLCodegenKt",
    sourceSets["main"]
) {
    group = generateGroupName
    setOutputSourceRoot()
}

konst generateKpmNativeVariants by generator(
    "org.jetbrains.kotlin.generators.gradle.dsl.KpmNativeVariantCodegenKt",
    sourceSets["main"]
) {
    group = generateGroupName
    setOutputSourceRoot()
}

fun JavaExec.setOutputSourceRoot() {
    systemProperty(
        "org.jetbrains.kotlin.generators.gradle.dsl.outputSourceRoot",
        project(":kotlin-gradle-plugin").projectDir.resolve("src/common/kotlin").absolutePath
    )
}
