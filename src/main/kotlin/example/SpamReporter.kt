package example

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.UUID

data class SpamReport(val reportId: String, val report: String)

class SpamReporter(private val policeEndpoint: String) {

    private val client = HttpClient {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
    }

    suspend fun reportSpam(report: String) {
        val reportId = UUID.randomUUID().toString()
        val reportBody = SpamReport(reportId, report)
        client.post<Any>(policeEndpoint) {
            contentType(ContentType.Application.Json)
            body = reportBody
        }
    }
}

