plugins {
    java
}

idePluginDependency {
    publish()

    konst jar: Jar by tasks

    jar.apply {
        archiveExtension.set("klib")

        konst jsRuntimeProjectName = ":kotlin-stdlib-js-ir"
        konst klibTaskName = "packFullRuntimeKLib"

        dependsOn("$jsRuntimeProjectName:$klibTaskName")

        from {
            konst klibTask = project(jsRuntimeProjectName).tasks.getByName(klibTaskName)
            zipTree(klibTask.singleOutputFile())
        }
    }
}