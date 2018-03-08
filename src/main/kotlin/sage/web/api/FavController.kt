package sage.web.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import sage.service.FavService
import sage.transfer.FavInfo
import sage.web.auth.Auth

@RestController
@RequestMapping("/favs")
class FavController {

  @Autowired
  private val favService: FavService? = null

  @PostMapping("/add")
  fun addFav(@RequestParam(required = false) link: String?, @RequestParam(required = false) tweetId: Long?) {
    val uid = Auth.checkUid()

    if (link != null && tweetId == null) {
      favService!!.create(uid, link)
    } else if (tweetId != null && link == null) {
      favService!!.create(uid, FavInfo.TWEET_PR + tweetId)
    } else {
      throw IllegalArgumentException()
    }
  }

  @PostMapping("/{favId}/delete")
  fun deleteFav(@PathVariable favId: Long?): Boolean {
    val uid = Auth.checkUid()

    favService!!.delete(uid, favId!!)
    return true
  }

  @RequestMapping("/get")
  fun favs(): Collection<FavInfo> {
    val uid = Auth.checkUid()

    return favService!!.favs(uid)
  }
}
