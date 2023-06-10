// IGNORE_BACKEND: JS

fun box(): String {
    konst ref: (CharArray) -> CharArray = ::charArrayOf
    konst arr = ref(charArrayOf('O', 'K'))
    return "${arr[0]}${arr[1]}"
}
