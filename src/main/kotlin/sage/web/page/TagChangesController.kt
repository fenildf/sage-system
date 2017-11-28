package sage.web.page

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import sage.service.TagChangeService
import sage.service.TagService
import sage.web.auth.Auth

@Controller
@RequestMapping("/tag-changes")
class TagChangesController @Autowired constructor(
    private val tagService: TagService,
    private val tagChangeService: TagChangeService
) {

  @RequestMapping("/{id}")
  fun requests(@PathVariable id: Long, model: ModelMap): String {
    val cuid = Auth.checkUid()
    model.put("tag", tagService.getTagLabel(id))
    model.put("reqs", tagChangeService.getRequestsOfTag(id))
    model.put("userCanTransact", tagChangeService.userCanTransact(cuid))
    model.put("currentUserId", cuid)
    return "tag-requests"
  }

  @RequestMapping("/{id}/scope")
  fun scopeRequests(@PathVariable id: Long, model: ModelMap): String {
    val cuid = Auth.checkUid()
    model.put("tag", tagService.getTagLabel(id))
    model.put("reqs", tagChangeService.getRequestsOfTagScope(id))
    model.put("userCanTransact", tagChangeService.userCanTransact(cuid))
    model.put("currentUserId", cuid)
    return "tag-scope-requests"
  }

  @RequestMapping("{id}/do-change")
  fun doChange(@PathVariable id: Long, model: ModelMap): String {
    Auth.checkUid()
    model.put("tag", tagService.getTagCard(id))
    return "tag-do-change"
  }

  @RequestMapping
  fun allRequests() = "forward:/tag-changes/1/scope"
}