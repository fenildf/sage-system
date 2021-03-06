package sage.transfer

import sage.annotation.KotlinNoArg
import sage.entity.Comment
import sage.entity.User
import java.util.*

@KotlinNoArg
class CommentView {
  var id: Long = 0
  var content: String = ""
  var authorId: Long = 0
  var authorName: String = ""
  var avatar: String = ""
  var whenCreated: Date? = null
  var sourceType: Short = 0
  var sourceId: Long = 0
  var replyToUser: UserLabel? = null

  constructor(comment: Comment) {
    id = comment.id
    content = comment.content
    authorId = comment.author.id
    authorName = comment.author.name
    avatar = comment.author.avatar
    whenCreated = comment.whenCreated
    comment.replyUserId?.let {
      replyToUser = User.get(it).toUserLabel()
    }
  }
}
