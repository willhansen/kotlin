plugins {
    java
}

idePluginDependency {
    publish()

    konst jar: Jar by tasks

    jar.apply {
        konst compilerProjectName = ":kotlin-compiler"
        konst distTaskName = "distKotlinc"

        dependsOn("$compilerProjectName:$distTaskName")

        from {
            konst distKotlincTask = project(compilerProjectName).tasks.getByName(distTaskName)
            distKotlincTask.outputs.files
        }
    }

    sourcesJar()
    javadocJar()
}