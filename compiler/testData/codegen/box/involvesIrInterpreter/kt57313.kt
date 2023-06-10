// TARGET_BACKEND: JVM_IR

abstract class AsyncJob {
    abstract suspend fun execute(lifetime: AsyncLifetime, attempt: Int, due: DateTime, context: JobContext): JobContext
}

class OrgBootstrapRequest
class AsyncLifetime
class DateTime
class JobContext

class OrgBootstrapTriggerJob(konst orgId: Long, konst bootstrap: OrgBootstrapRequest, konst jetSalesSync: Boolean?) : AsyncJob() {
    override suspend fun execute(lifetime: AsyncLifetime, attempt: Int, due: DateTime, context: JobContext): JobContext {
        return JobContext()
    }
}

konst name = "${OrgBootstrapTriggerJob::class.simpleName}<!EVALUATED(".")!>.<!>${OrgBootstrapTriggerJob::execute.<!EVALUATED("execute")!>name<!>}"

fun box(): String {
    return "OK"
}