package io.workshop.socialmedia.routers.cache

import akka.http.caching.scaladsl.{ CachingSettings, LfuCacheSettings }
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object CacheSettings {
  def getConfig(settings: CachingSettings): CachingSettings = {
    val configFactory   = ConfigFactory.load()
    val initialCapacity = configFactory.getInt("cache.initialCapacity")
    val maxCapacity     = configFactory.getInt("cache.maxCapacity")
    val timeToLive      = configFactory.getInt("cache.timeToLive")
    val timeToIdle      = configFactory.getInt("cache.timeToIdle")

    val lfuCacheSettings: LfuCacheSettings =
      settings.lfuCacheSettings
        .withInitialCapacity(initialCapacity)
        .withMaxCapacity(maxCapacity)
        .withTimeToLive(timeToLive.seconds)
        .withTimeToIdle(timeToIdle.seconds)
    settings.withLfuCacheSettings(lfuCacheSettings)
  }
}
