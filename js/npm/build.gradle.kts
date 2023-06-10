import com.github.gradle.node.npm.task.NpmTask

plugins {
  id("com.github.node-gradle.node") version "3.2.1"
  base
}

description = "Node utils"

node {
    download.set(true)
}

konst deployDir = "$buildDir/deploy_to_npm"
konst templateDir = "$projectDir/templates"
konst kotlincDir = "$projectDir/../../dist/kotlinc"

fun getProperty(name: String, default: String = "") = findProperty(name)?.toString() ?: default

konst deployVersion = getProperty("kotlin.deploy.version", "0.0.0")
konst deployTag = getProperty("kotlin.deploy.tag", "dev")
konst authToken = getProperty("kotlin.npmjs.auth.token")
konst dryRun = getProperty("dryRun", "false") // Pack instead of publish

fun Project.createCopyTemplateTask(templateName: String): Copy {
  return task<Copy>("copy-$templateName-template") {
      from("$templateDir/$templateName")
      into("$deployDir/$templateName")

      expand(hashMapOf("version" to deployVersion))
  }
}

fun Project.createCopyLibraryFilesTask(libraryName: String, fromJar: String): Copy {
  return task<Copy>("copy-$libraryName-library") {
    from(zipTree(fromJar).matching {
      include("$libraryName.js")
      include("$libraryName.meta.js")
      include("$libraryName.js.map")
      include("$libraryName/**")
    })

    into("$deployDir/$libraryName")
  }
}

fun Project.createPublishToNpmTask(templateName: String): NpmTask {
  return task<NpmTask>("publish-$templateName-to-npm") {
    konst deployDir = File("$deployDir/$templateName")
    workingDir.set(deployDir)

    konst deployArgs = listOf("publish", "--//registry.npmjs.org/:_authToken=$authToken", "--tag=$deployTag")
    if (dryRun == "true") {
      println("$deployDir \$ npm arguments: $deployArgs");
      args.set(listOf("pack"))
    }
    else {
      args.set(deployArgs)
    }
  }
}

fun sequential(first: Task, vararg tasks: Task): Task {
  tasks.fold(first) { previousTask, currentTask ->
    currentTask.dependsOn(previousTask)
  }
  return tasks.last()
}

konst publishKotlinJs = sequential(
        createCopyTemplateTask("kotlin"),
        createCopyLibraryFilesTask("kotlin", "$kotlincDir/lib/kotlin-stdlib-js.jar"),
        createPublishToNpmTask("kotlin")
)

konst publishKotlinCompiler = sequential(
  createCopyTemplateTask("kotlin-compiler"),
  task<Copy>("copy-kotlin-compiler") {
    from(kotlincDir)
    into("$deployDir/kotlin-compiler")
  },
  task<Exec>("chmod-kotlinc-bin") {
    commandLine = listOf("chmod", "-R", "ugo+rx", "$deployDir/kotlin-compiler/bin")
  },
  createPublishToNpmTask("kotlin-compiler")
)

konst publishKotlinTest = sequential(
        createCopyTemplateTask("kotlin-test"),
        createCopyLibraryFilesTask("kotlin-test", "$kotlincDir/lib/kotlin-test-js.jar"),
        createPublishToNpmTask("kotlin-test")
)

task("publishAll") {
    dependsOn(publishKotlinJs, publishKotlinTest, publishKotlinCompiler)
}
