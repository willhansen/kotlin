// EMIT_JVM_TYPE_ANNOTATIONS
// RENDER_ANNOTATIONS
// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
package foo

@Target(AnnotationTarget.TYPE)
annotation class TypeAnn(konst name: String)

class Kotlin {

    fun foo(s: @TypeAnn("1") String, x: @TypeAnn("2") Int) {
    }


    fun fooArray(s: Array<@TypeAnn("3") String>, i: Array<@TypeAnn("3") Int>) {
    }

}
