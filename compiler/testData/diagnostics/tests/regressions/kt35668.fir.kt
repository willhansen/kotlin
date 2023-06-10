// !DIAGNOSTICS: -UNUSED_PARAMETER

fun main() {
    konst baseDir: String? = ""
    konst networkParameters: String? = ""
    if (baseDir != null) {
        if (networkParameters != null) {
            Unit
        } else if (true){
            return
        } else {
            return
        }
    } else {
        return
    }

    networkParameters.length // unsafe call
}
