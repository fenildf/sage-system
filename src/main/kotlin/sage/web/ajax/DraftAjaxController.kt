package sage.web.ajax

import org.springframework.web.bind.annotation.*
import sage.domain.permission.DraftPermission
import sage.entity.Draft
import sage.entity.User
import sage.web.auth.Auth

@RestController
@RequestMapping("/drafts")
class DraftAjaxController {
  @RequestMapping("/save", method = arrayOf(RequestMethod.POST))
  fun save(@RequestParam(required = false) draftId: Long?,
                @RequestParam(required = false) targetId: Long?,
                @RequestParam(defaultValue = "") title: String,
                @RequestParam(defaultValue = "") content: String): Long {
    val uid = Auth.checkUid()
    return draftId?.let { Draft.byId(it) }?.let {
      DraftPermission(uid, it).canEdit()
      it.title = title
      it.content = content
      it.update()
      it.id
    } ?: let {
      val draft = Draft(targetId ?: 0, title, content, owner = User.ref(uid))
      draft.save()
      draft.id
    }
  }
}