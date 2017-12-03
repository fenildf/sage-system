package sage.service

import org.springframework.stereotype.Service
import sage.domain.permission.FollowListEntityPermission
import sage.domain.permission.ResourceListEntityPermission
import sage.entity.FollowListEntity
import sage.entity.ResourceListEntity
import sage.entity.Tag
import sage.entity.User
import sage.transfer.*
import sage.util.StringUtil
import java.util.*

@Service
class ListService {

  fun getResourceList(id: Long): ResourceList {
    return ResourceList.fromEntity(ResourceListEntity.get(id))
  }

  fun addResourceList(rc: ResourceList, userId: Long): Long {
    val entity = escaped(rc).toEntity()
    entity.ownerId = userId
    entity.save()
    return entity.id
  }

  fun updateResourceList(rc: ResourceList, userId: Long) {
    val entity = ResourceListEntity.get(rc.id)
    ResourceListEntityPermission(userId, entity).canEdit()

    val neo = escaped(rc).toEntity()
    entity.name = neo.name
    entity.listJson = neo.listJson
    entity.update()
  }

  fun getFollowList(id: Long): FollowList {
    return FollowListLite.fromEntity(FollowListEntity.get(id)).toFull({ User.get(it).toUserLabel() }) { Tag.get(it).toTagLabel() }
  }

  fun addFollowList(fcLite: FollowListLite, userId: Long): Long {
    val entity = fcLite.toEntity()
    entity.ownerId = userId
    entity.save()
    return entity.id
  }

  fun updateFollowList(fcLite: FollowListLite, userId: Long): Long {
    val entity = FollowListEntity.get(fcLite.id)
    FollowListEntityPermission(userId, entity).canEdit()

    val neo = fcLite.toEntity()
    entity.name = neo.name
    entity.listJson = neo.listJson
    entity.update()
    return entity.id
  }

  private fun escaped(rc: ResourceList): ResourceList {
    val neo = ResourceList(rc.id, rc.ownerId, rc.name, ArrayList<ResourceInfo>())
    rc.list.forEach { info ->
      neo.list.add(ResourceInfo(StringUtil.escapeHtmlTag(info.link), StringUtil.escapeHtmlTag(info.desc)))
    }
    return neo
  }

  fun followListsOfOwner(ownerId: Long): List<FollowList> {
    return FollowListEntity.byOwner(ownerId).map { l ->
      FollowListLite.fromEntity(l).toFull({ User.get(it).toUserLabel() }, { Tag.get(it).toTagLabel() })
    }
  }

  fun resourceListsOfOwner(ownerId: Long): List<ResourceList> {
    return ResourceListEntity.byOwner(ownerId).map { l -> ResourceList.fromEntity(l) }
  }
}
