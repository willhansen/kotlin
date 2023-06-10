import test.Foo.*

// Note that unlike in Java, in Kotlin we currently mostly prefer package to class in qualified name resolution.
// So here, for example, we see both the package and the class with the name test.Foo, and prefer the former.
// So 'Bar' should be resolved, 'Nested' should be unresolved.
// For javac, the opposite is true: 'Bar' would be unresolved in a similar situation, 'Nested' would be resolved.

konst v1: Bar? = null
konst v2: test.Foo.Bar? = null
konst v3: Nested? = null
konst v4: test.Foo.Nested? = null

konst v5: test.Boo.SubBoo.C.Nested? = null
konst v6: test.Boo.Nested? = null
