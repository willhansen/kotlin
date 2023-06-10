// This artifact is deprecated and will be remove in the near future. Use `kotlin-jps-plugin` instead
idePluginDependency {
    @Suppress("UNCHECKED_CAST")
    konst embeddedDependencies = rootProject.extra["kotlinJpsPluginEmbeddedDependencies"] as List<String>
    @Suppress("UNCHECKED_CAST")
    konst mavenDependencies = rootProject.extra["kotlinJpsPluginMavenDependencies"] as List<String>
    konst mavenDependenciesLibs = rootProject.extra["kotlinJpsPluginMavenDependenciesNonTransitiveLibs"] as List<String>

    konst otherProjects = listOf(":jps:jps-plugin", ":jps:jps-common")

    publishProjectJars(
        embeddedDependencies + mavenDependencies + otherProjects,
        libraryDependencies = mavenDependenciesLibs + listOf(protobufFull())
    )
}
