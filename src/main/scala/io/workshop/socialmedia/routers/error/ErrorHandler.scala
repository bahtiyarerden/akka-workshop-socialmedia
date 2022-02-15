package io.workshop.socialmedia.routers.error

import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives.{ complete, extractUri }
import akka.http.scaladsl.server.ExceptionHandler
import io.workshop.socialmedia.actors.{ DataFetchError, ManipulationError }
import io.workshop.socialmedia.model.RepositoryNotFound

object ErrorHandler {
  def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case err: DataFetchError =>
        extractUri { uri =>
          complete(HttpResponse(StatusCodes.InternalServerError, entity = err.getMessage))
        }
      case err: ManipulationError =>
        extractUri { uri =>
          complete(HttpResponse(StatusCodes.InternalServerError, entity = err.getMessage))
        }
      case err: RepositoryNotFound =>
        extractUri { uri =>
          complete(HttpResponse(StatusCodes.InternalServerError, entity = err.getMessage))
        }
      case _ =>
        extractUri { uri =>
          complete(
            HttpResponse(
              StatusCodes.InternalServerError,
              entity = "Something went wrong. We will solve it as soon as possible"
            )
          )
        }
    }
}
