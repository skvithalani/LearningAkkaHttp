package demo

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object App1 {

  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()

  val fileStream: Source[ByteString, Unit] = StreamConverters.asOutputStream().mapMaterializedValue(out => {
    println("I am materialized")
      out.write("Good Morning".getBytes)
      Thread.sleep(2000)
      out.write("\nLets learn streams today".getBytes)
      Thread.sleep(2000)
      out.close()
  })

  val foldedSink: Future[String] = fileStream.runFold("")((str, bs) ⇒ {
    str + bs.utf8String
  })

  foldedSink.foreach(println)


  def main(args: Array[String]) = {}
}
