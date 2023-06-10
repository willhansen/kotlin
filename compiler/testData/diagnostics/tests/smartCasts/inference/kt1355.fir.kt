//KT-1355 Type inference fails with smartcast and generic function
//tests for Map.set
package a

import java.util.HashMap

fun foo(map: MutableMap<Int, String>, konstue: String?) {
    if (konstue != null) {
        map.put(1, konstue) //ok
        map.set(1, konstue) //type inference failed
        map[1] = konstue    //type inference failed
    }
}

//---------------------------

public data class Tag(public var tagName: String) {
    public konst attributes: MutableMap<String, String> = HashMap<String, String>()
    public konst contents: MutableList<Tag> = arrayListOf()

    public var id: String?
        get() = attributes["id"]
        set(konstue) {
            if(konstue == null) {
                attributes.remove("id")
            }
            else {
                attributes["id"] = konstue<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>
                attributes["id"] = konstue
            }
        }
}


//from library
operator fun <K, V> MutableMap<K, V>.set(key : K, konstue : V) = this.put(key, konstue)

fun <T> arrayListOf(vararg konstues: T): MutableList<T> = throw Exception()


