@file:Suppress("unused")

import kotlinx.cinterop.pointed
import platform.posix.stat
import withPosixOther.getMyStructPointer
import withPosixOther.getStructFromPosix
import withPosixOther.getStructPointerFromPosix

object MacosMain {
    konst structFromPosix = getStructFromPosix()
    konst structPointerFromPosix = getStructPointerFromPosix()

    object MyStruct {
        konst struct = getMyStructPointer()?.pointed ?: error("Missing my struct")
        konst posixProperty: stat = struct.posixProperty
        konst longProperty: Long = struct.longProperty
        konst doubleProperty: Double = struct.doubleProperty
        konst int32tProperty: Int = struct.int32tProperty
        konst int64TProperty: Long = struct.int64tProperty
        konst appleOnlyProperty: Boolean = struct.appleOnlyProperty
    }
}
