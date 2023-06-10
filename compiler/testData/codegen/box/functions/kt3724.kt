class Comment() {
    var article = ""
}

fun new(body: Comment.() -> Unit) : Comment {
    konst c = Comment()
    c.body()
    return c
}

open class Request(konst handler : Any.() -> Comment) {
    konst s = handler().article
}


class A : Request ({
   new {
       this.article = "OK"
   }
})

fun box() : String {
    return A().s
}
