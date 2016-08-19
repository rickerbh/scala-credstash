package au.com.simplemachines.scala.credstash

import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, GetItemResult, QueryResult }
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.{ Matchers, WordSpecLike }

import scala.collection.JavaConverters._

class SimpleCredStashClientTest extends SimpleCredStashClientTestHarness with WordSpecLike with Matchers {

  import org.mockito.Matchers.{ any => mockitoAny }
  import au.com.simplemachines.scala.credstash.reader.Readers._

  "SimpleCredStashClientTest and Dynamo" should {

    "return None when there exists no value in Dynamo" in {

      Mockito.when(dynamoClient.query(mockitoAny())).thenReturn(
        new QueryResult() { this.setCount(0) }
      )

      newClient.get[String]("password") shouldBe None
    }

    "return None when Dynamo fails" in {

      Mockito.when(dynamoClient.query(mockitoAny())).thenAnswer(new Answer[QueryResult]() {
        override def answer(invocation: InvocationOnMock) = throw new RuntimeException("oops")
      })

      newClient.get[String]("password") shouldBe None
    }

    "return None when a version does not exist" in {

      val version = "123"
      Mockito.when(
        dynamoClient.getItem(
          BaseClient.DefaultCredentialTableName,
          Map("name" -> new AttributeValue("password"), "version" -> new AttributeValue(version.toString)).asJava
        )
      ).thenAnswer(
          new Answer[GetItemResult]() {
            override def answer(invocation: InvocationOnMock) = throw new RuntimeException("oops")
          }
        )

      newClient.get[String]("password", version = version) shouldBe None
    }

  }

}
