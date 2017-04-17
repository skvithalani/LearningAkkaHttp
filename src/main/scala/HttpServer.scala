import java.io.OutputStream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Random

object WebServer extends App{

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val numbers: Source[Int, NotUsed] = Source.fromIterator(() => Iterator.continually(Random.nextInt()))


  val fileStream: Source[ByteString, Unit] = StreamConverters.asOutputStream().mapMaterializedValue(out => {
    println(s"I am materialized $out")
//    Future {
      out.write("Hello how you doing today".getBytes)
      out.close()
//    }
  })

  val route = get {
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, numbers.map(i => ByteString(i))))
      }
    } ~
      path("stream") {
        get {
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, fileStream))
        }
      }

  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}