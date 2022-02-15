package io.workshop.socialmedia.routers

import akka.actor.typed.{ ActorSystem, Scheduler }
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.http.scaladsl.server.{ RequestContext, Route, RouteResult }
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Source
import akka.util.Timeout
import akka.NotUsed
import akka.http.caching.scaladsl.{ Cache, CachingSettings }
import akka.http.caching.LfuCache
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.directives.CachingDirectives._
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport
import io.workshop.socialmedia.actors.SocialMediaManager.{
  Command,
  ProviderCreated,
  RequestFailed,
  TwitterProviderCreated
}
import io.workshop.socialmedia.actors.providers.{ ProviderResponse, Providers, UserMessagesRetrieved }
import io.workshop.socialmedia.actors.SocialMediaManager
import io.workshop.socialmedia.model.Model
import io.workshop.socialmedia.routers.cache.CacheSettings
import io.workshop.socialmedia.routers.error.ErrorHandler._

import scala.concurrent.{ ExecutionContextExecutor, Future }
import scala.concurrent.duration._

final class ShoutController()(implicit val system: ActorSystem[Command]) extends PlayJsonSupport with ApiRoutes {

  implicit lazy val timeout: Timeout                      = Timeout(5.seconds)
  implicit val scheduler: Scheduler                       = system.scheduler
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val keyerFunction: PartialFunction[RequestContext, Uri] = { case r: RequestContext =>
    r.request.uri
  }
  val cacheSettings: CachingSettings    = CachingSettings(system)
  val lfuSettings: CachingSettings      = CacheSettings.getConfig(cacheSettings)
  val lfuCache: Cache[Uri, RouteResult] = LfuCache(lfuSettings)

  def messageHandler(providerResponse: ProviderResponse): Seq[Model] =
    providerResponse match {
      case UserMessagesRetrieved(messages) => messages
      case RequestFailed(error)            => throw error
    }

  val route: Route = get {
    path("shout" / Segment) { twitterUserName =>
      parameter("limit".as[Int]) { limit =>
        validate(limit > 0 && limit <= 10, "The number of tweets requested must be between 1 and 10") {
          handleExceptions(exceptionHandler) {
            alwaysCache(lfuCache, keyerFunction) {
              val tweetsFuture: Future[Seq[Model]] = for {
                createdProvider <-
                  system
                    .ask[ProviderCreated](ref => SocialMediaManager.CreateProvider(Providers.Twitter, ref))
                    .mapTo[TwitterProviderCreated]
                maybeResponse <- system.ask[ProviderResponse](ref =>
                  SocialMediaManager.GetMessages(twitterUserName, limit, createdProvider.provider, ref)
                )
              } yield {
                val tweets = messageHandler(maybeResponse)
                tweets
              }

              val response: Source[String, NotUsed] =
                Source.future(tweetsFuture).mapConcat(identity).map(m => m.text)
              complete(response)
            }
          }
        }
      }
    }
  }
}
