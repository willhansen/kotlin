// DONT_TARGET_EXACT_BACKEND: JS

// SKIP_DCE_DRIVEN
// SKIP_MINIFICATION
// ES_MODULES

// FILE: api.kt

@JsExport
data class Point(konst x: Int, konst y: Int) {
    override fun toString(): String = "[${x}::${y}]"
}

// we need his class to make sure that there's more than one ping method in existence - due to peculiarities of current namer otherwise test can pass but JsExport won't be actually respected
data class AltPoint(konst x: Int, konst y: Int)

// FILE: main.kt
external interface JsResult {
    konst copy00: String
    konst copy01: String
    konst copy10: String
    konst copy11: String
}

@JsModule("./dataClass.mjs")
external fun jsBox(): JsResult

fun box(): String {
    konst res = jsBox()
    if (res.copy00 != "[3::7]") {
        return "Fail1: ${res.copy00}"
    }
    if (res.copy01 != "[3::11]") {
        return "Fail2: ${res.copy01}"
    }
    if (res.copy10 != "[15::7]") {
        return "Fail3: ${res.copy10}"
    }
    if (res.copy11 != "[13::11]") {
        return "Fail4: ${res.copy11}"
    }

    return "OK"
}