package demo.Server
import java.io.OutputStream

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn

object WebServer extends App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val stream: Source[ByteString, OutputStream] = StreamConverters.asOutputStream()
  val streamWithoutClose: Source[ByteString, Unit] = stream.mapMaterializedValue(out => {
    println("I am materialized for Without Close")
    out.write("Good Morning".getBytes)
    Thread.sleep(2000)
    out.write("\nLets learn streams today".getBytes)
    Thread.sleep(2000)
  })

  val streamWithClose: Source[ByteString, Unit] = stream.mapMaterializedValue(out => {
    println("I am materialized for With Close")
    out.write("Good Morning".getBytes)
    Thread.sleep(2000)
    out.write("\nLets learn streams today".getBytes)
    Thread.sleep(2000)
    out.close()
  })

  val streamWithFutureClose: Source[ByteString, Unit] = stream.mapMaterializedValue(out => {
    println("I am materialized for Future Close")
    out.write("Good Morning".getBytes)
    Thread.sleep(2000)
    out.write("\nLets learn streams today".getBytes)
    Thread.sleep(2000)
    Future(out.close())
  })

  val route = get {
      path("streamWithoutClose") {
        get {
          complete({
            HttpEntity(ContentTypes.`text/plain(UTF-8)`, streamWithoutClose)
          })
        }
      } ~
        path("streamWithClose") {
        get {
          complete({
            HttpEntity(ContentTypes.`text/plain(UTF-8)`, streamWithClose)
          })
        }
      } ~
      path("streamFutureClose") {
        get {
          complete({
            HttpEntity(ContentTypes.`text/plain(UTF-8)`, streamWithFutureClose)
          })
        }
      }

  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
