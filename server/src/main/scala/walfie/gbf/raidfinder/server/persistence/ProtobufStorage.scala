package walfie.gbf.raidfinder.server.persistence

import com.trueaccord.scalapb.{GeneratedMessage, GeneratedMessageCompanion, Message}
import java.net.URI
import redis.clients.jedis.{BinaryJedis, Jedis}

trait ProtobufStorage {
  type CacheItem[T] = GeneratedMessage with Message[T]

  def set[T <: CacheItem[T]](key: String, value: T): Unit
  def get[T <: CacheItem[T]](
    key: String
  )(implicit companion: GeneratedMessageCompanion[T]): Option[T]

  def close(): Unit
}

object ProtobufStorage {
  def redis(uri: URI): RedisProtobufStorage = {
    new RedisProtobufStorage(new BinaryJedis(uri))
  }
}

// TODO: Write integration test
class RedisProtobufStorage(redis: BinaryJedis) extends ProtobufStorage {
  def set[T <: CacheItem[T]](key: String, value: T): Unit = {
    redis.set(key.getBytes, value.toByteArray)
  }

  def get[T <: CacheItem[T]](
    key: String
  )(implicit companion: GeneratedMessageCompanion[T]): Option[T] = {
    Option(redis.get(key.getBytes)).flatMap { bytes =>
      companion.validate(bytes).toOption
    }
  }

  def close(): Unit = redis.close()
}

object NoOpProtobufStorage extends ProtobufStorage {
  def set[T <: CacheItem[T]](key: String, value: T): Unit = ()

  def get[T <: CacheItem[T]](
    key: String
  )(implicit companion: GeneratedMessageCompanion[T]): Option[T] = None

  def close(): Unit = ()
}

