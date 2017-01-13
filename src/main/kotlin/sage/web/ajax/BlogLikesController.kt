package sage.web.ajax

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import sage.entity.Blog
import sage.entity.BlogStat
import sage.entity.TweetStat
import sage.web.auth.Auth

@RestController
@RequestMapping("/blogs")
open class BlogLikesController {
  @RequestMapping("/{id}/like")
  open fun like(@PathVariable id: Long) {
    val uid = Auth.checkUid()
    BlogStat.like(id, uid)
    TweetStat.like(Blog.get(id).tweetId, uid)
  }

  @RequestMapping("/{id}/unlike")
  open fun unlike(@PathVariable id: Long) {
    val uid = Auth.checkUid()
    BlogStat.unlike(id, uid)
    TweetStat.unlike(Blog.get(id).tweetId, uid)
  }

  @RequestMapping("/{id}/likes")
  open fun likes(@PathVariable id: Long) = BlogStat.get(id).likes
}