package demo.Clients.RunForEachClients

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object WithFutureClose extends App{
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  private val responseF: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/streamFutureClose"))

  responseF.foreach(response => {
    println("Response is received\n\n")
    val source = response.entity.dataBytes
    source.runForeach(bs => println(bs.utf8String))
  })
}
