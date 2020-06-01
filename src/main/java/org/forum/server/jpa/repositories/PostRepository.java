/**
 * 
 */
package org.forum.server.jpa.repositories;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.forum.server.jpa.PostEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * @author etienne-sf
 */
public interface PostRepository extends CrudRepository<PostEntity, UUID> {

	@Query(value = "select p from Post p where p.topicId= ?1")
	List<PostEntity> findByTopicId(UUID topicId);

	@Query(value = "select p from Post p where p.topicId= ?1 and p.date >= ?2")
	List<PostEntity> findByTopicIdAndSince(UUID id, Date since);

	@Query(value = "select p from Post p where p.topicId= ?1 and p.authorId =?2 and p.date >= ?3")
	List<PostEntity> findByTopicIdAndMemberIdAndSince(UUID id, UUID memberId, Date since);

	@Query(value = "" //
			+ " select p "//
			+ " from Post p "//
			+ " join Member m on m.id=p.authorId" //
			+ " where p.topicId= ?1 "//
			+ " and m.name =?2 "//
			+ " and p.date >= ?3")
	List<PostEntity> findByTopicIdAndMemberNameAndSince(UUID id, String memberName, Date since);

	// It's actually a non sense request, as if you provide author_id, it's useless to provide his/her name. But, as
	// it's a technical possibility, the query must be defined
	@Query(value = "" //
			+ " select p "//
			+ " from Post p "//
			+ " join Member m on m.id=p.authorId" //
			+ " where p.topicId= ?1 "//
			+ " and p.authorId =?2 "//
			+ " and m.name = ?3 "//
			+ " and p.date >= ?4")
	List<PostEntity> findByTopicIdAndMemberIdAndMemberNameAndSince(UUID id, UUID memberId, String memberName, Date since);

	/** The query for the BatchLoader */
	@Query(value = "select p from Post p where id in ?1")
	List<PostEntity> findByIds(List<UUID> ids);
}
