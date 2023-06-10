//KT-2195 error "Only safe calls are allowed ..." but it is function param (konst)
package foo

private fun <T> sendCommand(errorCallback: (()->Unit)? = null) {
    if (errorCallback != null) {
        <!DEBUG_INFO_SMARTCAST!>errorCallback<!>()
    }
}