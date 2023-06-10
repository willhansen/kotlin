// EXPECTED_REACHABLE_NODES: 1281
package foo

external interface Chrome {
    konst extension: Extension
}

external interface Extension {
    konst lastError: LastError?
}

external interface LastError {
    konst message: String
}

external konst chrome: Chrome = definedExternally

fun box(): String {
    konst lastError = chrome.extension.lastError?.message
    return if (lastError == null) "OK" else "fail"
}