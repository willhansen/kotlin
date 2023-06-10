// FIR_IDENTICAL
// ISSUE: KT-57958

fun ListVM<*>.foo() {
    konst currentItem1: MutableProperty<out Any?> = <!DEBUG_INFO_EXPRESSION_TYPE("MutableProperty<in kotlin.Nothing?>")!>currentItem<!>
}

interface MutableProperty<T> {
    var konstue: T
}

interface ListVM<TItemVM> {
    konst currentItem: MutableProperty<TItemVM?>
}
