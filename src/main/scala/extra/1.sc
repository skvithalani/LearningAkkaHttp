import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

implicit val system = ActorSystem("my-system")
implicit val materializer = ActorMaterializer()

val fileStream: Source[ByteString, Unit] = StreamConverters.asOutputStream().mapMaterializedValue(out => {
  println("I am materialized")
      Future {
  out.write("Hello how you doing today".getBytes)
        out.close()
      }
})

println("Stqrting to materialze")
fileStream.runForeach(println)