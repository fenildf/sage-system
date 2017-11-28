package sage.web.ajax

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sage.entity.BlogStat
import sage.entity.Tweet
import sage.entity.TweetStat
import sage.web.auth.Auth

@RestController
@RequestMapping("/tweets")
class TweetLikesAjaxController {
  @RequestMapping("/{id}/like")
  fun like(@PathVariable id: Long) {
    val uid = Auth.checkUid()
    TweetStat.like(id, uid)
    Tweet.byId(id)?.let(Tweet::blogId)?.let { if (it > 0) {
      BlogStat.like(it, uid)
    }}
  }

  @RequestMapping("/{id}/unlike")
  fun unlike(@PathVariable id: Long) {
    val uid = Auth.checkUid()
    TweetStat.unlike(id, uid)
    Tweet.byId(id)?.let(Tweet::blogId)?.let { if (it > 0) {
      BlogStat.unlike(it, uid)
    }}
  }

  @RequestMapping("/{id}/likes")
  fun likes(@PathVariable id: Long) = TweetStat.get(id).likes
}