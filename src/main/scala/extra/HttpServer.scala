package extra

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


  private val stream: Source[ByteString, OutputStream] = StreamConverters.asOutputStream()
  val fileStream: Source[ByteString, Unit] = stream.mapMaterializedValue(out => {
    println(s"I am materialized $out")
      out.write("He".getBytes)
      Thread.sleep(4000)
      out.write("Ha".getBytes)
      println("Written to the file")
      Thread.sleep(2000)
    Future{
      println("in close")
      out.close()
      println("in close 2")
    }
  })

  val route = get {
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, numbers.map(i => ByteString(i))))
      }
    } ~
      path("stream") {
        get {
          complete({
            val chunked: HttpEntity.Chunked = HttpEntity(ContentTypes.`text/plain(UTF-8)`, fileStream)
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