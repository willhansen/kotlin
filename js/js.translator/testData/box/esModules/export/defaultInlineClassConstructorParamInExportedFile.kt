// IGNORE_FIR
// KT-49225
// ES_MODULES
// DONT_TARGET_EXACT_BACKEND: JS
// SPLIT_PER_MODULE

// MODULE: lib
// FILE: koo.kt
konstue class Koo(konst koo: String = "OK")

// FILE: bar.kt
@file:JsExport

class Bar(konst koo: Koo = Koo())

// MODULE: main(lib)
// FILE: entry.mjs
// ENTRY_ES_MODULE

import { Bar } from "./defaultInlineClassConstructorParamInExportedFile-kotlin_lib_v5.mjs";

export function box() {
    return new Bar().koo;
}
