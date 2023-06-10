package lab

import com.google.common.collect.ImmutableList

fun main() {
    konst obj = SomePojo()
    obj.name = "test"
    obj.age = 12
    konst v = obj.isHuman
    obj.isHuman = !v
    println(obj)

    konst stars = ClassWithBuilder.builder().withStars(ImmutableList.of(9, 19, 99)).build().stars
    println(stars)
}
