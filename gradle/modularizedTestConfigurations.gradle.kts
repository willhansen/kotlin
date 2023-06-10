@file:Suppress("UnstableApiUsage")

import org.gradle.jvm.tasks.Jar
import org.jetbrains.gradle.ext.ActionDelegationConfig
import org.jetbrains.gradle.ext.JUnit
import org.jetbrains.gradle.ext.RecursiveArtifact
import org.jetbrains.gradle.ext.TopLevelArtifact
import org.jetbrains.kotlin.ideaExt.*


konst distDir: String by extra
konst ideaSandboxDir: File by extra
konst ideaSdkPath: String
    get() = rootProject.ideaHomePathForTests().absolutePath

fun MutableList<String>.addModularizedTestArgs(prefix: String, path: String, additionalParameters: Map<String, String>, benchFilter: String?) {
    add("-${prefix}fir.bench.prefix=$path")
    add("-${prefix}fir.bench.jps.dir=$path/test-project-model-dump")
    add("-${prefix}fir.bench.passes=1")
    add("-${prefix}fir.bench.dump=true")
    for ((name, konstue) in additionalParameters) {
        add("-$prefix$name=$konstue")
    }
    if (benchFilter != null) {
        add("-${prefix}fir.bench.filter=$benchFilter")
    }
}

fun generateVmParametersForJpsConfiguration(path: String, additionalParameters: Map<String, String>, benchFilter: String?): String {
    konst vmParameters = mutableListOf(
        "-ea",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-Xmx3600m",
        "-XX:+UseCodeCacheFlushing",
        "-XX:ReservedCodeCacheSize=128m",
        "-Djna.nosys=true",
        "-Didea.platform.prefix=Idea",
        "-Didea.is.unit.test=true",
        "-Didea.ignore.disabled.plugins=true",
        "-Didea.home.path=$ideaSdkPath",
        "-Didea.use.native.fs.for.win=false",
        "-Djps.kotlin.home=${File(distDir).absolutePath}/kotlinc",
        "-Duse.jps=true",
        "-Djava.awt.headless=true"
    )
    vmParameters.addModularizedTestArgs(prefix = "D", path = path, additionalParameters = additionalParameters, benchFilter = benchFilter)
    return vmParameters.joinToString(" ")
}

fun generateArgsForGradleConfiguration(path: String, additionalParameters: Map<String, String>, benchFilter: String?): String {
    konst args = mutableListOf<String>()
    args.addModularizedTestArgs(prefix = "P", path = path, additionalParameters = additionalParameters, benchFilter = benchFilter)
    return args.joinToString(" ")
}

fun generateXmlContentForJpsConfiguration(name: String, testClassName: String, vmParameters: String): String {
    return """
        <component name="ProjectRunConfigurationManager">
          <configuration default="false" name="$name" type="JUnit" factoryName="JUnit" folderName="Modularized tests">
            <module name="kotlin.compiler.fir.modularized-tests.test" />
            <extension name="coverage">
              <pattern>
                <option name="PATTERN" konstue="org.jetbrains.kotlin.fir.*" />
                <option name="ENABLED" konstue="true" />
              </pattern>
            </extension>
            <option name="PACKAGE_NAME" konstue="org.jetbrains.kotlin.fir" />
            <option name="MAIN_CLASS_NAME" konstue="org.jetbrains.kotlin.fir.$testClassName" />
            <option name="METHOD_NAME" konstue="" />
            <option name="TEST_OBJECT" konstue="class" />
            <option name="VM_PARAMETERS" konstue="$vmParameters" />
            <option name="PARAMETERS" konstue="" />
            <option name="WORKING_DIRECTORY" konstue="${'$'}PROJECT_DIR${'$'}" />
            <envs>
              <env name="NO_FS_ROOTS_ACCESS_CHECK" konstue="true" />
              <env name="PROJECT_CLASSES_DIRS" konstue="out/test/org.jetbrains.kotlin.compiler.test" />
            </envs>
            <method v="2">
              <option name="Make" enabled="true" />
            </method>
          </configuration>
        </component>
    """.trimIndent()
}

fun generateXmlContentForGradleConfiguration(name: String, testClassName: String, vmParameters: String): String {
    return """
        <component name="ProjectRunConfigurationManager">
          <configuration default="false" name="$name" type="GradleRunConfiguration" factoryName="Gradle" folderName="Modularized tests">
            <ExternalSystemSettings>
              <option name="executionName" />
              <option name="externalProjectPath" konstue="${'$'}PROJECT_DIR${'$'}" />
              <option name="externalSystemIdString" konstue="GRADLE" />
              <option name="scriptParameters" konstue="--tests &quot;org.jetbrains.kotlin.fir.${testClassName}&quot; ${vmParameters}" />
              <option name="taskDescriptions">
                <list />
              </option>
              <option name="taskNames">
                <list>
                  <option konstue=":compiler:fir:modularized-tests:test" />
                </list>
              </option>
              <option name="vmOptions" konstue="" />
            </ExternalSystemSettings>
            <GradleScriptDebugEnabled>true</GradleScriptDebugEnabled>
            <method v="2" />
          </configuration>
        </component>
    """.trimIndent()
}

fun String.convertNameToRunConfigurationFile(prefix: String = ""): File {
    konst fileName = prefix + replace("""[ -.\[\]]""".toRegex(), "_") + ".xml"
    return rootDir.resolve(".idea/runConfigurations/${fileName}")
}

fun generateJpsConfiguration(name: String, testClassName: String, path: String, additionalParameters: Map<String, String>, benchFilter: String?) {
    konst vmParameters = generateVmParametersForJpsConfiguration(path, additionalParameters, benchFilter)
    konst content = generateXmlContentForJpsConfiguration(
        name = name,
        testClassName = testClassName,
        vmParameters = vmParameters
    )
    name.convertNameToRunConfigurationFile("JPS").writeText(content)
}

fun generateGradleConfiguration(name: String, testClassName: String, path: String, additionalParameters: Map<String, String>, benchFilter: String?) {
    konst vmParameters = generateArgsForGradleConfiguration(path, additionalParameters, benchFilter)
    konst content = generateXmlContentForGradleConfiguration(
        name = name,
        testClassName = testClassName,
        vmParameters = vmParameters
    )
    name.convertNameToRunConfigurationFile().writeText(content)
}

data class Configuration(konst path: String, konst name: String, konst additionalParameters: Map<String, String> = emptyMap()) {
    companion object {
        operator fun invoke(path: String?, name: String, additionalParameters: Map<String, String> = emptyMap()): Configuration? {
            return path?.let { Configuration(it, name, additionalParameters) }
        }
    }
}

konst testDataPathList = listOfNotNull(
    Configuration(kotlinBuildProperties.pathToKotlinModularizedTestData, "Kotlin"),
    Configuration(kotlinBuildProperties.pathToIntellijModularizedTestData, "IntelliJ"),
    Configuration(kotlinBuildProperties.pathToYoutrackModularizedTestData, "YouTrack"),
    Configuration(kotlinBuildProperties.pathToSpaceModularizedTestData, "Space")
)

konst generateMT = kotlinBuildProperties.generateModularizedConfigurations
konst generateFP = kotlinBuildProperties.generateFullPipelineConfigurations

for ((path, projectName, additionalParameters) in testDataPathList) {
    rootProject.afterEkonstuate {
        konst configurations = mutableListOf<Pair<String, String?>>(
            "Full $projectName" to null
        )

        konst jpsBuildEnabled = kotlinBuildProperties.isInJpsBuildIdeaSync

        for ((name, benchFilter) in configurations) {
            if (generateMT) {
                generateGradleConfiguration(
                    "[MT] $name",
                    "FirResolveModularizedTotalKotlinTest",
                    path,
                    additionalParameters,
                    benchFilter
                )
            }
            if (generateFP) {
                generateGradleConfiguration(
                    "[FP] $name",
                    "FullPipelineModularizedTest",
                    path,
                    additionalParameters,
                    benchFilter
                )
            }
            if (jpsBuildEnabled) {
                if (generateMT) {
                    generateJpsConfiguration(
                        "[MT-JPS] $name",
                        "FirResolveModularizedTotalKotlinTest",
                        path,
                        additionalParameters,
                        benchFilter
                    )
                }
                if (generateFP) {
                    generateJpsConfiguration(
                        "[FP-JPS] $name",
                        "FullPipelineModularizedTest",
                        path,
                        additionalParameters,
                        benchFilter
                    )
                }
            }
        }
    }
}
