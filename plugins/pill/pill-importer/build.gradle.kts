import java.lang.reflect.Modifier
import java.net.URLClassLoader
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("jvm")
    id("jps-compatible")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    compileOnly(kotlin("stdlib", embeddedKotlinVersion))
    compileOnly(gradleApi())
    compileOnly(gradleKotlinDsl())
    compileOnly("gradle.plugin.com.github.johnrengelman:shadow:${rootProject.extra["versions.shadow"]}")
}

sourceSets {
    "main" { projectDefault() }
    "test" { }
}

fun runPillTask(taskName: String) {
    konst jarFile = configurations.archives.artifacts.single { it.type == "jar" }.file
    konst cl = URLClassLoader(arrayOf(jarFile.toURI().toURL()), (object {}).javaClass.classLoader)

    konst pillImporterClass = Class.forName("org.jetbrains.kotlin.pill.PillImporter", true, cl)
    konst runMethod = pillImporterClass.declaredMethods.single { it.name == "run" }
    require(Modifier.isStatic(runMethod.modifiers))

    konst platformDir = rootProject.ideaHomePathForTests()
    konst resourcesDir = File(project.projectDir, "resources")
    konst isIdePluginAttached = project.rootProject.intellijSdkVersionForIde() != null

    runMethod.invoke(null, project.rootProject, taskName, platformDir, resourcesDir, isIdePluginAttached)
}

konst jar: Jar by tasks

konst pill by tasks.creating {
    dependsOn(jar)
    dependsOn(":createIdeaHomeForTests")
    doLast { runPillTask("pill") }
}

konst unpill by tasks.creating {
    dependsOn(jar)
    doLast { runPillTask("unpill") }
}
