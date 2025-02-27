// IGNORE_FIR
// KT-49225
// DONT_TARGET_EXACT_BACKEND: JS
// ES_MODULES
// SPLIT_PER_MODULE

// MODULE: lib
// FILE: lib.kt
konstue class Koo(konst koo: String = "OK")

@JsExport
class Bar(konst koo: Koo = Koo())

// MODULE: main(lib)
// FILE: entry.mjs
// ENTRY_ES_MODULE

import { Bar } from "./defaultInlineClassConstructorParam-kotlin_lib_v5.mjs";

export function box() {
    return new Bar().koo;
}