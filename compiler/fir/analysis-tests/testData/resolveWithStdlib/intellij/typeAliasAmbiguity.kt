typealias ServiceId = String
fun ServiceId(serviceId: String): ServiceId = serviceId
// FE 1.0 resolves this to function
konst GaugeSpecTmsIntegrationServiceId = ServiceId("Gauge")
