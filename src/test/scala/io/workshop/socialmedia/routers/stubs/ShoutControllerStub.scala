package io.workshop.socialmedia.routers.stubs

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import io.workshop.socialmedia.actors.{ DataFetchError, ManipulationError }
import io.workshop.socialmedia.routers.error.ErrorHandler.exceptionHandler

class ShoutControllerStub extends PlayJsonSupport {
  val route: Route = concat(
    path("fetch") {
      get {
        handleExceptions(exceptionHandler) {
          throw DataFetchError("Data fetch error")
          complete("Hello there!")
        }
      }
    },
    path("manipulate") {
      get {
        handleExceptions(exceptionHandler) {
          throw ManipulationError("Manipulation error")
          complete("Hello there!")
        }
      }
    }
  )
}
