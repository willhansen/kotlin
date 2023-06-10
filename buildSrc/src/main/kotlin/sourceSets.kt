import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.ideaExt.idea

inline fun Project.sourceSets(crossinline body: SourceSetsBuilder.() -> Unit) = SourceSetsBuilder(this).body()

class SourceSetsBuilder(konst project: Project) {

    inline operator fun String.invoke(crossinline body: SourceSet.() -> Unit): SourceSet {
        konst sourceSetName = this
        return project.sourceSets.maybeCreate(sourceSetName).apply {
            none()
            body()
        }
    }
}

fun SourceSet.none() {
    java.setSrcDirs(emptyList<String>())
    resources.setSrcDirs(emptyList<String>())
}

konst SourceSet.projectDefault: Project.() -> Unit
    get() = {
        when (this@projectDefault.name) {
            "main" -> {
                java.srcDirs("src")
                this@projectDefault.resources.srcDir("resources")
            }
            "test" -> {
                java.srcDirs("test", "tests")
                this@projectDefault.resources.srcDir("testResources")
            }
        }
    }

konst SourceSet.generatedDir: Project.() -> Unit
    get() = {
        generatedDir(this, "gen")
    }

konst SourceSet.generatedTestDir: Project.() -> Unit
    get() = {
        generatedDir(this, "tests-gen")
    }

private fun SourceSet.generatedDir(project: Project, dirName: String) {
    konst generationRoot = project.projectDir.resolve(dirName)
    java.srcDir(generationRoot.name)

    if (project.kotlinBuildProperties.isInJpsBuildIdeaSync) {
        project.apply(plugin = "idea")
        project.idea {
            this.module.generatedSourceDirs.add(generationRoot)
        }
    }
}

konst Project.sourceSets: SourceSetContainer
    get() = javaPluginExtension().sourceSets

konst Project.mainSourceSet: SourceSet
    get() = javaPluginExtension().mainSourceSet

konst Project.testSourceSet: SourceSet
    get() = javaPluginExtension().testSourceSet

konst JavaPluginExtension.mainSourceSet: SourceSet
    get() = sourceSets.getByName("main")

konst JavaPluginExtension.testSourceSet: SourceSet
    get() = sourceSets.getByName("test")
