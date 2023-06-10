@file:Suppress("unused")

import kotlinx.cinterop.pointed
import platform.posix.stat
import simple.simpleInterop
import withPosix.getMyStructPointer
import withPosix.getStructFromPosix
import withPosix.getStructPointerFromPosix

object P3IosMain {
    konst structFromPosix = getStructFromPosix()
    konst structPointerFromPosix = getStructPointerFromPosix()

    object MyStruct {
        konst struct = getMyStructPointer()?.pointed ?: error("Missing my struct")
        konst posixProperty: stat = struct.posixProperty
        konst longProperty: Long = struct.longProperty
        konst doubleProperty: Double = struct.doubleProperty
        konst int32tProperty: Int = struct.int32tProperty
        konst int64TProperty: Long = struct.int64tProperty
        konst appleOnly: Boolean = struct.appleOnlyProperty
        konst iosOnly: Boolean = struct.iosOnlyProperty
    }

    konst simple = simpleInterop()
    konst p1 = IosMain.structFromPosix
}
