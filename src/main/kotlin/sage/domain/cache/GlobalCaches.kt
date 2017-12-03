package sage.domain.cache

import com.google.common.cache.CacheBuilder
import sage.entity.*
import sage.util.PaginationLogic
import java.util.concurrent.TimeUnit

object GlobalCaches {
  val blogsCache = ListCache(Blog)
  val tweetsCache = ListCache(Tweet)
  val tagsCache = ListCache(Tag)

  class ListCache<V : AutoModel>(val find: BaseFind<Long, V>) {
    private val cache = CacheBuilder.newBuilder().maximumSize(100).expireAfterWrite(5, TimeUnit.MINUTES)
        .build<String, Pair<List<Long>, Int>>()

    operator fun get(key: String, valueLoader: ()->List<V>): List<V> {
      val loadValue = {
        val list = valueLoader()
        cache.put(key, list.map { it.id } to 0)
        list
      }

      return cache.getIfPresent(key)?.let { entry ->
        val ids = entry.first
        val objects = find.where().`in`("id", ids).findMap("id", Long::class.java)
        val objectList = ids.mapNotNull { id -> objects[id] }
        if (objectList.size == ids.size) {
          objectList
        } else {
          loadValue()
        }
      } ?: loadValue()
    }

    operator fun get(name: String, pageIndex: Int, pageSize: Int): Pair<List<V>, Int> {
      val key = "$name?pageIndex=$pageIndex&pageSize=$pageSize"
      return cache.getIfPresent(key)?.let { value ->
        val (ids, pagesCount) = value
        val entities =
            if (ids.isEmpty()) emptyList<V>()
            else find.where().`in`("id", ids).findList().sortedByDescending { it.id }

        entities to pagesCount
      } ?: let { _ ->
        val entities = find.where().eq("deleted", false).orderBy("id desc")
            .findPagedList(pageIndex - 1, pageSize).list
        val pagesCount = PaginationLogic.pagesCount(pageSize, find.totalCount())
        cache.put(key, entities.map { it.id } to pagesCount)

        entities to pagesCount
      }
    }

    fun clear() {
      cache.invalidateAll()
    }
  }
}