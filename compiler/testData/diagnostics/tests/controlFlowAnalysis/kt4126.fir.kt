public data class ProductGroup(konst short_name: String, konst parent: ProductGroup?) {
    konst name: String = if (parent == null) short_name else "${parent.name} $short_name"
}
