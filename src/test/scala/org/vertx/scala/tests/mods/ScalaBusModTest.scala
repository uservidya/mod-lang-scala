package org.vertx.scala.tests.mods

import org.vertx.scala.testtools.TestVerticle
import scala.concurrent.Promise
import scala.concurrent.Future
import org.vertx.scala.core.json._
import org.vertx.scala.core.FunctionConverters._
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import org.junit.Test
import org.vertx.scala.core.eventbus.Message
import org.vertx.testtools.VertxAssert._

class ScalaBusModTest extends TestVerticle {
  override def asyncBefore(): Future[Unit] = {
    val p = Promise[Unit]
    container.deployVerticle("scala:org.vertx.scala.tests.mods.TestBusMod", Json.obj(), 1, {
      case Success(deploymentId) => p.success()
      case Failure(ex) => p.failure(ex)
    }: Try[String] => Unit)
    p.future
  }

  val address = "test-bus-mod"

  @Test
  def syncHello(): Unit = {
    vertx.eventBus.send(address, Json.obj("action" -> "sync-hello"), { msg: Message[JsonObject] =>
      assertEquals("ok", msg.body.getString("status"))
      assertEquals("sync-hello-result", msg.body.getString("result"))
      testComplete()
    })
  }

  @Test
  def syncError(): Unit = {
    vertx.eventBus.send(address, Json.obj("action" -> "sync-error"), { msg: Message[JsonObject] =>
      assertEquals("Should receive an error status", "error", msg.body.getString("status"))
      assertEquals("Should receive a MODULE_EXCEPTION", "MODULE_EXCEPTION", msg.body.getString("error"))
      assertNotNull("Should receive an exception", msg.body.getString("exception"))
      testComplete()
    })
  }

  @Test
  def syncEcho(): Unit = {
    vertx.eventBus.send(address, Json.obj("action" -> "sync-echo", "echo" -> "bla"), { msg: Message[JsonObject] =>
      assertEquals("ok", msg.body.getString("status"))
      assertEquals("bla", msg.body.getString("echo"))
      testComplete()
    })
  }

  @Test
  def syncEchoError(): Unit = {
    vertx.eventBus.send(address, Json.obj("action" -> "sync-echo"), { msg: Message[JsonObject] =>
      assertEquals("error", msg.body.getString("status"))
      assertEquals("NO_ECHO_GIVEN", msg.body.getString("error"))
      testComplete()
    })
  }

  @Test
  def asyncHello(): Unit = {
    vertx.eventBus.send(address, Json.obj("action" -> "async-hello"), { msg: Message[JsonObject] =>
      assertEquals("ok", msg.body.getString("status"))
      assertEquals("async-hello-result", msg.body.getString("result"))
      testComplete()
    })
  }

  @Test
  def asyncErrorUncaught(): Unit = {
    vertx.eventBus.send(address, Json.obj("action" -> "async-error-1"), { msg: Message[JsonObject] =>
      assertEquals("error", msg.body.getString("status"))
      assertEquals("MODULE_EXCEPTION", msg.body.getString("error"))
      testComplete()
    })
  }

  @Test
  def asyncErrorUncaughtCascade(): Unit = {
    vertx.eventBus.send(address, Json.obj("action" -> "async-cascade-uncaught"), { msg: Message[JsonObject] =>
      assertEquals("error", msg.body.getString("status"))
      assertEquals("MODULE_EXCEPTION", msg.body.getString("error"))
      testComplete()
    })
  }

  @Test
  def asyncCascade(): Unit = {
    vertx.eventBus.send(address, Json.obj("action" -> "async-cascade"), { msg: Message[JsonObject] =>
      assertEquals("ok", msg.body.getString("status"))
      assertEquals("async-cascade-result", msg.body.getString("result"))
      testComplete()
    })
  }

  @Test
  def asyncErrorCaught(): Unit = {
    vertx.eventBus.send(address, Json.obj("action" -> "async-error-2"), { msg: Message[JsonObject] =>
      assertEquals("error", msg.body.getString("status"))
      assertEquals("CAUGHT_EXCEPTION", msg.body.getString("error"))
      testComplete()
    })
  }
}