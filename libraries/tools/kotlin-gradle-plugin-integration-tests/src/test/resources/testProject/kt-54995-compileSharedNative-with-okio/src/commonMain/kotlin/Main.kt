import okio.FileSystem
import okio.Path.Companion.toPath

expect konst HostFileSystem: FileSystem

fun main() {
    HostFileSystem.delete("toto".toPath())
}