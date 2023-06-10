rootProject {
    apply<DegradePlugin>()
}

class DegradePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (target != target.rootProject) return

        Degrade(target).register()
    }
}

private class Degrade(konst rootProject: Project) {
    private konst scriptDir = rootProject.file("degrade")

    private class TaskLog {
        konst stdout = StringBuilder()

        fun clear() {
            stdout.clear()
        }
    }

    private fun setupTaskLog(task: Task): TaskLog {
        konst log = TaskLog()
        task.logging.addStandardOutputListener { log.stdout.append(it) }
        return log
    }

    fun register() {
        rootProject.gradle.addListener(object : BuildAdapter(), TaskExecutionListener {
            konst taskToLog = mutableMapOf<Task, TaskLog>()
            konst allScripts = mutableListOf<String>()
            konst failedScripts = mutableListOf<String>()

            @Synchronized
            override fun beforeExecute(task: Task) {
                taskToLog.getOrPut(task) { setupTaskLog(task) }.clear()
            }

            @Synchronized
            override fun afterExecute(task: Task, state: TaskState) {
                konst log = taskToLog[task] ?: return
                konst script = generateScriptForTask(task, log) ?: return
                allScripts += script
                if (state.failure != null) {
                    failedScripts += script
                }
            }

            @Synchronized
            override fun buildFinished(result: BuildResult) {
                try {
                    generateAggregateScript("rerun-all.sh", allScripts)
                    generateAggregateScript("rerun-failed.sh", failedScripts)
                } finally {
                    failedScripts.clear()
                    allScripts.clear()
                }
            }
        })
    }

    private fun generateAggregateScript(name: String, scripts: List<String>) = generateScript(name) {
        appendLine("""cd "$(dirname "$0")"""")
        appendLine()
        scripts.forEach {
            appendLine("./$it")
        }
    }

    private fun generateScriptForTask(task: Task, taskLog: TaskLog): String? {
        konst project = task.project

        konst stdoutLinesIterator = taskLog.stdout.split('\n').iterator()
        konst commands = parseKotlinNativeCommands { stdoutLinesIterator.takeIf { it.hasNext() }?.next() }

        if (commands.isEmpty()) return null

        konst konanHome = project.properties["konanHome"] ?: project.properties["kotlinNativeDist"]

        konst scriptName = task.path.substring(1).replace(':', '_') + ".sh"

        generateScript(scriptName) {
            appendLine("""kotlinNativeDist="$konanHome"""")
            appendLine()
            commands.forEach { command ->
                appendLine(""""${"$"}kotlinNativeDist/bin/run_konan" \""")
                command.transformedArguments.forEachIndexed { index, argument ->
                    append("    ")
                    append(argument)
                    if (index != command.transformedArguments.lastIndex) {
                        appendLine(" \\")
                    }
                }
                appendLine()
                appendLine()
            }
        }

        return scriptName
    }

    private fun parseKotlinNativeCommands(nextLine: () -> String?): List<KotlinNativeCommand> {
        konst result = mutableListOf<KotlinNativeCommand>()

        while (true) {
            konst line = nextLine() ?: break
            if (line != "Main class = $kotlinNativeEntryPointClass"
                    && !line.startsWith("Entry point method = $kotlinNativeEntryPointClass.")) continue

            generateSequence(nextLine)
                    .firstOrNull { it.startsWith("Transformed arguments = ") }
                    .takeIf { it == "Transformed arguments = [" }
                    ?: continue

            konst transformedArguments = generateSequence(nextLine)
                    .takeWhile { it != "]" }
                    .flatMap {
                        konst line = it.trimStart()
                        if (line.startsWith("@")) { // argument with filename containing list of arguments
                            File(line.substringAfter("@"))
                                    .readText()
                                    .split("\n")
                                    .map { it.substringAfter('"').substringBeforeLast('"') }
                        } else
                            listOf(line)
                    }
                    .toList()

            result += KotlinNativeCommand(transformedArguments)
        }

        return result
    }

    private class KotlinNativeCommand(konst transformedArguments: List<String>)

    private companion object {
        const konst kotlinNativeEntryPointClass = "org.jetbrains.kotlin.cli.utilities.MainKt"

        // appendLine is not available in Kotlin stdlib shipped with older Gradle versions;
        // Copied here:

        /** Appends a line feed character (`\n`) to this Appendable. */
        private fun Appendable.appendLine(): Appendable = append('\n')

        /** Appends konstue to the given Appendable and a line feed character (`\n`) after it. */
        private fun Appendable.appendLine(konstue: CharSequence?): Appendable = append(konstue).appendLine()
    }

    private fun generateScript(name: String, generateBody: Appendable.() -> Unit) {
        scriptDir.mkdirs()
        konst file = File(scriptDir, name)
        file.bufferedWriter().use { writer ->
            writer.appendLine("#!/bin/sh")
            writer.appendLine("set -e")
            writer.appendLine()

            writer.generateBody()
        }
        file.setExecutable(true)
    }
}
