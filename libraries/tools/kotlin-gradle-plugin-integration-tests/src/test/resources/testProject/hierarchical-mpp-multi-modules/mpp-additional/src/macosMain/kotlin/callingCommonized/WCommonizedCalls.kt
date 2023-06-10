// !CHECK_HIGHLIGHTING
package callingCommonized

import kotlinx.cinterop.CEnum
import platform.Accelerate.AtlasConj
import platform.Accelerate.CBLAS_TRANSPOSE
import platform.Accelerate.__CLPK_real
import platform.CoreFoundation.CFAllocatorGetTypeID
import platform.CoreFoundation.CFTypeID
import platform.darwin.NSObject
import platform.posix.ns_r_notauth

actual class WCommonizedCalls actual constructor(pc: __CLPK_real) {

    konst eFunCall: CFTypeID = CFAllocatorGetTypeID()
    actual konst eClass: NSObject
        get() = TODO("Not yet implemented")
    actual konst enumInteroped: CEnum
        get() = TODO("Not yet implemented")

    konst eVal: CBLAS_TRANSPOSE = AtlasConj

    konst theCall = ns_r_notauth


}
