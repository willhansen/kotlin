interface Introspector {

    class SchemaRetriever(konst transaction: String) {
        inline fun inSchema(crossinline modifier: (String) -> Unit) =
                { modifier(transaction) }.let { it() }

        internal fun retrieve() {
            inSchema { schema -> "OK" }
        }
    }

}

// TESTED_OBJECT_KIND: innerClass
// TESTED_OBJECTS: Introspector$SchemaRetriever$inSchema$1, SchemaRetriever
// FLAGS: ACC_FINAL, ACC_PUBLIC, ACC_STATIC

// TESTED_OBJECT_KIND: innerClass
// TESTED_OBJECTS: Introspector$SchemaRetriever$inSchema$1, DefaultImpls
// ABSENT: true
