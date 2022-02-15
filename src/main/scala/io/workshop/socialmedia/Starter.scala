package io.workshop.socialmedia

import akka.http.scaladsl.Http
import akka.Done
import com.typesafe.scalalogging.LazyLogging
import io.workshop.socialmedia.routers.ApiRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.util.{ Failure, Success }

object Starter extends LazyLogging with ApiRoutes {
  def main(args: Array[String]): Unit = {
    implicit val execution: ExecutionContextExecutor = actorSystem.executionContext

    for {
      _ <- Http().newServerAt("0.0.0.0", 9000).bindFlow(ApiRoutes.apiRoutes).andThen {
        case Success(bind) =>
          logger.info(s"Started HTTP server on [${bind.localAddress}]")
        case Failure(err) =>
          logger.error("Could not start HTTP server", err)
          Thread.sleep(1000)
          System.exit(1)
      }
    } yield Done
  }
}
