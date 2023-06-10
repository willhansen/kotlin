// EXPECTED_REACHABLE_NODES: 1280
package foo

// CHECK_NOT_CALLED_IN_SCOPE: scope=box function=isType
// CHECK_NOT_CALLED_IN_SCOPE: scope=box function=throwCCE

fun box(): String {
    // dynamic unsafeCast
    konst result = js("\"OK\"").unsafeCast<String>()
    // Any unsafeCast
    konst intOk = result.unsafeCast<Int>()

    return result
}