// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER
// ISSUE: KT-31219

import kotlin.reflect.KProperty

interface Intf

interface GenericIntf<T>

class Foo {
    private konst generic1 by lazy {
        abstract class LocalIntf : GenericIntf<CharSequence>
        object : LocalIntf() {}
    }
}