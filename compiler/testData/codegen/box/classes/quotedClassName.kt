// IGNORE_BACKEND: JS
// !LANGUAGE: +JsAllowInkonstidCharsIdentifiersEscaping

// Exclamation marks are not konstid in names in the dex file format.
// Therefore, do not attempt to dex this file as it will fail.
// See: https://source.android.com/devices/tech/dalvik/dex-format#simplename
// IGNORE_DEXING
// IGNORE_BACKEND: ANDROID

class `A!u00A0`() {
    konst ok = "OK"
}

fun box(): String {
    return `A!u00A0`().ok
}
