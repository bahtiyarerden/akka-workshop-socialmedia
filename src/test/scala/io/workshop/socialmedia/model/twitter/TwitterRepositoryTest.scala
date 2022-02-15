package io.workshop.socialmedia.model.twitter

import io.workshop.socialmedia.model.RepositoryNotFound
import io.workshop.socialmedia.model.twitter.repositories.{
  TweetRepositoryFactory,
  TweetRepositoryInMemory,
  TweetRepositoryType
}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext.Implicits.global

class TwitterRepositoryTest extends AnyWordSpec with Matchers with ScalaFutures {

  "Twitter repository" should {
    "created by repository factory" in {
      // execute
      val repository = TweetRepositoryFactory.apply(TweetRepositoryType.InMemory)

      // assert
      repository shouldBe a[TweetRepositoryInMemory]
    }

    "give back users last tweet" in {
      // setup
      val userName   = "bahtiyar"
      val limit      = 1
      val repository = TweetRepositoryFactory.apply(TweetRepositoryType.InMemory)

      // execute
      val tweets = repository.searchByUserName(userName, limit).futureValue

      // assert
      tweets should have size 1
    }

    "give back users last 3 tweets" in {
      // setup
      val userName   = "bahtiyar"
      val limit      = 3
      val repository = TweetRepositoryFactory.apply(TweetRepositoryType.InMemory)

      // execute
      val tweets = repository.searchByUserName(userName, limit).futureValue

      // assert
      tweets should have size 3
    }

    "throws error on unimplemented repository creation" in {
      assertThrows[RepositoryNotFound] {
        TweetRepositoryFactory.apply(TweetRepositoryType.Cassandra)
      }
    }
  }

}
