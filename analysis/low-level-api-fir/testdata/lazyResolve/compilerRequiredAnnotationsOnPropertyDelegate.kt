annotation class Anno(konst s: String)

@delegate:Deprecated("delegate")
@delegate:Anno("delegate")
konst memberP<caret>roperty by lazy {
    "42"
}