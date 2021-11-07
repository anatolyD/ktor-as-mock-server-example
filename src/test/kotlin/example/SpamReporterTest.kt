package example

import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.random.Random
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SpamReporterTest {

    private val reports = mutableListOf<SpamReport>()
    private lateinit var embeddedServer: ApplicationEngine

    @BeforeAll
    fun setUp() {
        embeddedServer = embeddedServer(Netty, Random.nextInt(10000, 12000)) {
            install(ContentNegotiation) {
                jackson()
            }

            routing {
                post("/report") {
                    val report = call.receive<SpamReport>()
                    reports.add(report)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }.start(wait = false)
    }

    @AfterAll
    fun tearDownServer() {
        embeddedServer.stop(1000, 2000)
    }

    @Test
    internal fun `verify report was called`() {
        // arrange
        val port = embeddedServer.environment.connectors[0].port
        val policeEndpoint = "http://localhost:$port/report"
        val spamReporter = SpamReporter(policeEndpoint)

        // act
        runBlocking {
            spamReporter.reportSpam("I got a call from a bank")
        }

        // assert
        assertEquals(1, reports.size)
        assertEquals("I got a call from a bank", reports[0].report)
    }

}