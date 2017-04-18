package demo.Clients.RunFoldClients

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object WithClose extends App{
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  private val responseF: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/streamWithClose"))

  responseF.foreach(response => {
    println("Response is received\n\n")
    val source = response.entity.dataBytes

    val sinkF: Future[String] = source.runFold("")((str, bs) ⇒ {
      str + bs.utf8String
    })

    sinkF.foreach(println)
  })
}
