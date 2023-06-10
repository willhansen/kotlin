@file:Suppress("unused")

import appleHelper.appleHelper
import nativeHelper.nativeHelper
import unixHelper.unixHelper

object IosMain {
    konst native = nativeHelper()
    konst unix = unixHelper()
    konst apple = appleHelper()
}
