package fr.rebaze

import fr.rebaze.Endpoints.Library.{Book, books}
import fr.rebaze.Endpoints.{booksListingServerEndpoint, helloServerEndpoint}
import sttp.client3.testing.SttpBackendStub
import sttp.client3.ziojson.asJson
import sttp.client3.{UriContext, basicRequest}
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.ztapir.RIOMonadError
import zio.test.Assertion._
import zio.test.{ZIOSpecDefault, assertZIO}

object EndpointsSpec extends ZIOSpecDefault {
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
    },
    test("list available books") {
      // given
      val backendStub = TapirStubInterpreter(SttpBackendStub(new RIOMonadError[Any]))
        .whenServerEndpointRunLogic(booksListingServerEndpoint)
        .backend()

      // when
      val response = basicRequest
        .get(uri"http://test.com/books/list/all")
        .response(asJson[List[Book]])
        .send(backendStub)

      // then
      assertZIO(response.map(_.body))(isRight(equalTo(books)))
    }
  )
}
