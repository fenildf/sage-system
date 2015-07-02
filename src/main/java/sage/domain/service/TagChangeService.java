package sage.domain.service;

import java.util.Collection;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sage.domain.commons.AuthorityException;
import sage.domain.commons.DomainRuntimeException;
import sage.domain.commons.IdCommons;
import sage.domain.concept.Authority;
import sage.domain.repository.TagChangeRequestRepository;
import sage.domain.repository.TagRepository;
import sage.domain.repository.UserRepository;
import sage.entity.Tag;
import sage.entity.TagChangeRequest;
import sage.entity.TagChangeRequest.Status;
import sage.entity.TagChangeRequest.Type;
import sage.entity.User;
import sage.transfer.TagChangeRequestCard;
import sage.util.Colls;

@Service
@Transactional
public class TagChangeService {
  private static final Logger log = LoggerFactory.getLogger(TagChangeService.class);
  @Autowired
  private TagRepository tagRepo;
  @Autowired
  private TagChangeRequestRepository reqRepo;
  @Autowired
  private UserRepository userRepo;

  public Long newTag(String name, long parentId, String intro) {
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException("name is empty!");
    }
    if (tagRepo.nullable(parentId) == null) {
      throw new IllegalArgumentException("parentId "+parentId+" is wrong!");
    }
    if (StringUtils.isBlank(intro)) {
      intro = "啊，" + name + "！";
    }
    Tag tag = new Tag(name, tagRepo.load(parentId), intro);
    if (tagRepo.byNameAndParent(name, parentId) == null) {
      tagRepo.save(tag);
      return tag.getId();
    } else {
      throw new DomainRuntimeException("Tag[name: %s, parentId: %s] already exists", name, parentId);
    }
  }

  public TagChangeRequest requestMove(Long userId, Long tagId, Long parentId) {
    if (tagRepo.nullable(parentId) == null) {
      throw new IllegalArgumentException("parentId "+parentId+" is wrong!");
    }
    return saveRequest(TagChangeRequest.forMove(tagRepo.load(tagId), userRepo.load(userId), parentId));
  }

  public TagChangeRequest requestRename(Long userId, Long tagId, String name) {
    if (StringUtils.isBlank(name)) {
      throw new IllegalArgumentException("name is empty!");
    }
    return saveRequest(TagChangeRequest.forRename(tagRepo.load(tagId), userRepo.load(userId), name));
  }

  public TagChangeRequest requestSetIntro(Long userId, Long tagId, String intro) {
    if (StringUtils.isBlank(intro)) {
      throw new IllegalArgumentException("intro is empty!");
    }
    return saveRequest(TagChangeRequest.forSetIntro(tagRepo.load(tagId), userRepo.load(userId), intro));
  }

  private TagChangeRequest saveRequest(TagChangeRequest req) {
    reqRepo.save(req);
    User submitter = req.getSubmitter();
    if (Authority.isTagAdminOrHigher(submitter.getAuthority())) {
      acceptRequest(submitter.getId(), req.getId());
      reqRepo.update(req);
    }
    return req;
  }

  public Collection<TagChangeRequestCard> getRequestsOfTag(long tagId) {
    return Colls.map(reqRepo.byTag(tagId), TagChangeRequestCard::new);
  }

  public int countPendingRequestsOfTag(long tagId) {
    return reqRepo.byTagAndStatus(tagId, Status.PENDING).size();
  }

  public Collection<TagChangeRequestCard> getRequestsOfTagScope(long tagId) {
    return Colls.map(reqRepo.byTagScope(tagRepo.get(tagId)), TagChangeRequestCard::new);
  }

  public int countPendingRequestsOfTagScope(long tagId) {
    return reqRepo.byTagScopeAndStatus(tagRepo.get(tagId), Status.PENDING).size();
  }

  public void cancelRequest(Long userId, Long reqId) {
    TagChangeRequest request = reqRepo.get(reqId);
    if (!IdCommons.equal(request.getSubmitter().getId(), userId)) {
      throw new DomainRuntimeException("User[%d] is not the owner of TagChangeRequest[%d]", userId, reqId);
    }
    request.setStatus(Status.CANCELED);
  }

  public void acceptRequest(Long userId, Long reqId) {
    transactRequest(userId, reqId, Status.ACCEPTED);
  }

  public void rejectRequest(Long userId, Long reqId) {
    transactRequest(userId, reqId, Status.REJECTED);
  }

  public boolean userCanTransact(long userId) {
    return Authority.isTagAdminOrHigher(userRepo.get(userId).getAuthority());
  }

  private void transactRequest(Long userId, Long reqId, Status status) {
    User user = userRepo.get(userId);
    if (!Authority.isTagAdminOrHigher(user.getAuthority())) {
      throw new AuthorityException("Require TagAdmin or higher.");
    }
    TagChangeRequest req = reqRepo.get(reqId);
    if (req.getStatus() != Status.PENDING) {
      throw new DomainRuntimeException("Don't repeat TagChangeService.transactRequest on obsolete request.");
    }
    req.setStatus(status);
    req.setTransactor(user);

    if (status == Status.ACCEPTED) {
      Long tagId = req.getTag().getId();
      if (req.getType() == Type.MOVE) {
        doTransact(tagId, tag -> tag.setParent(tagRepo.load(req.getParentId())));
      } else if (req.getType() == Type.RENAME) {
        doTransact(tagId, tag -> tag.setName(req.getName()));
      } else if (req.getType() == Type.SET_INTRO) {
        doTransact(tagId, tag -> tag.setIntro(req.getIntro()));
      }
      log.info("transactRequest done: " + ToStringBuilder.reflectionToString(req, ToStringStyle.SHORT_PREFIX_STYLE));
    }
  }

  private void doTransact(long tagId, Consumer<Tag> action) {
    Tag tag = tagRepo.get(tagId);
    action.accept(tag);
    tagRepo.update(tag);
  }

}
