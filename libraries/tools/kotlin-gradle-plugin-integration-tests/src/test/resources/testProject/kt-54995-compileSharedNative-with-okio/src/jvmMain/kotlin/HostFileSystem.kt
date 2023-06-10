import okio.FileSystem

actual konst HostFileSystem: FileSystem
    get() = FileSystem.SYSTEM

fun useCommonMain() = main()