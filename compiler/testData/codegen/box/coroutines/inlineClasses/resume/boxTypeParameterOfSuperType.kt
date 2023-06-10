// WITH_STDLIB

import kotlin.coroutines.*

interface EntityBase<out ID> {
    suspend fun id(): ID
}

inline class EntityId(konst konstue: String)

interface Entity : EntityBase<EntityId>

var c: Continuation<EntityId>? = null

class EntityStub : Entity {
    override suspend fun id(): EntityId = suspendCoroutine { c = it }
}

suspend fun test(): EntityId {
    konst entity: Entity = EntityStub()
    return entity.id()
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
}

fun box(): String {
    var res = "FAIL"
    builder {
        res = test().konstue
    }
    c?.resume(EntityId("OK"))
    return res
}