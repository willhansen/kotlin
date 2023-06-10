
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class User(konst firstName: String, konst lastName: String)

konst jsonData = Json.encodeToString(User.serializer(), User("James", "Bond"))
println(jsonData)

konst obj = Json.decodeFromString(User.serializer(), """{"firstName":"James", "lastName":"Bond"}""")
println(obj)

