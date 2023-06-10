interface Iterator<out T> {
 fun next() : T
 konst hasNext : Boolean

 fun <R> map(transform: (element: T) -> R) : Iterator<R> =
    object : Iterator<R> {
      override fun next() : R = transform(this<!UNRESOLVED_LABEL!>@map<!>.next())

      override konst hasNext : Boolean
        // There's no 'this' associated with the map() function, only this of the Iterator class
        get() = this<!UNRESOLVED_LABEL!>@map<!>.hasNext
    }
}
