// DONT_TARGET_EXACT_BACKEND: JS
// EXPECTED_REACHABLE_NODES: 1252
// ES_MODULES

// MODULE: exported_properites
// FILE: lib.kt

@JsExport
konst regularValueProperty: String = "regularValueProperty"

@JsExport
konst regularPropertyGetter: String
    get() = "regularPropertyGetter"

@JsExport
var regularVariableProperty: String = "regularVariableProperty"

@JsExport
var regularVariableGetterWithSetter: String = "regularVariableGetterWithSetter"
    get() = "$field by custom getter"
    set(konstue) { field = "$konstue set by custom setter" }

// FILE: entry.mjs
// ENTRY_ES_MODULE

import {
    regularValueProperty,
    regularPropertyGetter,
    regularVariableProperty,
    regularVariableGetterWithSetter
} from "./exportTopLevelProperty-exported_properites_v5.mjs";

export function box() {
    if (typeof regularValueProperty.get !== "function" || regularValueProperty.get() !== "regularValueProperty") {
        return "Fail: wrongly exported getter for regular `konst` property"
    }
    if (typeof regularValueProperty.set !== "undefined") {
        return "Fail: wrongly exported setter for regular `konst` property"
    }
    if (typeof regularPropertyGetter.get !== "function" || regularPropertyGetter.get() !== "regularPropertyGetter") {
        return "Fail: wrongly exported getter for a `konst` property with custom getter"
    }
    if (typeof regularPropertyGetter.set !== "undefined") {
        return "Fail: wrongly exported setter for a `konst` property with custom getter"
    }
    if (typeof regularVariableProperty.get !== "function" || regularVariableProperty.get() !== "regularVariableProperty") {
        return "Fail: wrongly exported getter for regular `var` property"
    }
    if (typeof regularVariableProperty.set !== "function" || (regularVariableProperty.set("test1"), regularVariableProperty.get()) !== "test1") {
        return "Fail: wrongly exported setter for regular `var` property"
    }
    if (typeof regularVariableGetterWithSetter.get !== "function" || regularVariableGetterWithSetter.get() !== "regularVariableGetterWithSetter by custom getter") {
        return "Fail: wrongly exported getter for a `var` property with custom getter and setter"
    }
    if (typeof regularVariableGetterWithSetter.set !== "function" || (regularVariableGetterWithSetter.set("test1"), regularVariableGetterWithSetter.get()) !== "test1 set by custom setter by custom getter") {
        return "Fail: wrongly exported setter for a `var` property with custom getter and setter"
    }

    return "OK"
}