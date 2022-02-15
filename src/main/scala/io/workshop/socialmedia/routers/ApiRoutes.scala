package io.workshop.socialmedia.routers

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.concat
import akka.http.scaladsl.server.Route
import io.workshop.socialmedia.actors.SocialMediaManager
import io.workshop.socialmedia.actors.SocialMediaManager.Command
trait ApiRoutes {
  implicit val actorSystem: ActorSystem[Command] = ActorSystem[Command](SocialMediaManager.apply(), "Guardian")

  object ApiRoutes {
    private val shoutController = new ShoutController()
    def apiRoutes: Route        = concat(shoutController.route)
  }

}
