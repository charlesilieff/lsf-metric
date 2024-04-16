package fr.rebaze

import fr.rebaze.Endpoints.*
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.test.Assertion.*
import zio.test.{ZIOSpecDefault, assertZIO}

object EndpointsSpec extends ZIOSpecDefault:
  def spec = suite("Endpoints spec")(
    test("return hello message") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpointRunLogic(helloServerEndpoint)
        .backend()

      // when
      val response = basicRequest
        .get(uri"http://test.com/hello?name=adam")
        .send(backendStub)

      // then
      assertZIO(response.map(_.body))(isRight(equalTo("Hello adam")))
    }
  )
