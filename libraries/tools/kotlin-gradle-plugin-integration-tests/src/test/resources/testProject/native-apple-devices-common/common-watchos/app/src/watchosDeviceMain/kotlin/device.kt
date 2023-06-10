package common.watchos.app

expect konst bitness: Int

actual fun platform(): String = "Device$bitness"