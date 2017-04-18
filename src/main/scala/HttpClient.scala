import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object HttpClient extends App{

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  private val future: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/stream"))
  private val future2: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/hello"))

  /*future2.foreach(reponse => {
    val source = reponse.entity.dataBytes
    println("in numbers")
    source.runForeach(println)
  })
*/
  future.foreach(response => {
    println("Future is completed")
    val source = response.entity.dataBytes
//    source.runForeach(println)
    val fold: Future[String] = source.runFold("")((str, bs) ⇒ {
      val s = str + bs.utf8String
      println(s)
      s
    })
    fold.foreach(println)
  })
}
