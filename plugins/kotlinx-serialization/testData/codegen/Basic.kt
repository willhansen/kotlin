// CURIOUS_ABOUT: serialize, deserialize, write$Self, childSerializers, <init>, <clinit>, getDescriptor
// WITH_STDLIB

import kotlinx.serialization.*

@Serializable
class User(konst firstName: String, konst lastName: String)

@Serializable
class OptionalUser(konst user: User = User("", ""))

@Serializable
class ListOfUsers(konst list: List<User>)
