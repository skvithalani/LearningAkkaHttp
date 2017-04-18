package demo.Server

import java.io.OutputStream

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString

import scala.io.StdIn

object WebServer extends App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  private val stream: Source[ByteString, OutputStream] = StreamConverters.asOutputStream()
  val streamWithoutClose: Source[ByteString, Unit] = stream.mapMaterializedValue(out => {
    out.write("Good Morning".getBytes)
    Thread.sleep(2000)
    out.write("\nLets learn streams today".getBytes)
    Thread.sleep(2000)
  })

  val streamWithClose: Source[ByteString, Unit] = stream.mapMaterializedValue(out => {
    out.write("Good Morning".getBytes)
    Thread.sleep(2000)
    out.write("\nLets learn streams today".getBytes)
    Thread.sleep(2000)
    out.close()
  })

  val streamWithFutureClose: Source[ByteString, Unit] = stream.mapMaterializedValue(out => {
    out.write("Good Morning".getBytes)
    Thread.sleep(2000)
    out.write("\nLets learn streams today".getBytes)
    Thread.sleep(2000)
    out.close()
  })

  val route = get {
      path("streamWithoutClose") {
        get {
          complete({
            val chunked: HttpEntity.Chunked = HttpEntity(ContentTypes.`text/plain(UTF-8)`, streamWithoutClose)
            println("in complete")
            chunked
          })
        }
      }

      path("streamWithClose") {
        get {
          complete({
            val chunked: HttpEntity.Chunked = HttpEntity(ContentTypes.`text/plain(UTF-8)`, streamWithClose)
            println("in complete")
            chunked
          })
        }
      }

      path("streamFutureClose") {
        get {
          complete({
            val chunked: HttpEntity.Chunked = HttpEntity(ContentTypes.`text/plain(UTF-8)`, streamWithFutureClose)
            println("in complete")
            chunked
          })
        }
      }

  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
