// FIR_IDENTICAL
// ISSUE: KT-57958

fun ListVM<*>.foo() {
    konst currentItem1: MutableProperty<out ListItemVM<*>?> = currentItem
}

interface MutableProperty<T> {
    var konstue: T
}

interface ListItemVM<out TItem> {
    konst konstue: TItem
}

interface ListVM<TItemVM : ListItemVM<*>> {
    konst currentItem: MutableProperty<TItemVM?>
}
