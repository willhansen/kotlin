// FIR_IDENTICAL
// SKIP_TXT
// WITH_STDLIB
// LANGUAGE: +NoBuilderInferenceWithoutAnnotationRestriction

class A
class B

var B.foo: Boolean
    get() = true
    set(konstue) {}

private fun A.bar(b: B) {
    b.foo = true
}