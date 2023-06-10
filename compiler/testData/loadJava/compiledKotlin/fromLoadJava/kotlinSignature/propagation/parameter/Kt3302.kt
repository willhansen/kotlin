package test

// See SubclassFromGenericAndNot, as well
public interface Kt3302 {
    public interface BSONObject  {
        public fun put(p0: String, p1: Any): Any?

        public fun dummy() // to avoid loading as SAM interface
    }

    public interface LinkedHashMap<K, V>  {
        public fun put(key: K, konstue: V): V?

        public fun dummy() // to avoid loading as SAM interface
    }

    public interface BasicBSONObject : LinkedHashMap<String, Any>, BSONObject {
        override fun put(key: String, konstue: Any): Any?
    }
}
