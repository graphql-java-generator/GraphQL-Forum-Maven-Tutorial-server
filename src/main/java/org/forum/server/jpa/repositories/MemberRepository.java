/**
 * 
 */
package org.forum.server.jpa.repositories;

import java.util.List;
import java.util.UUID;

import org.forum.server.jpa.MemberEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * @author etienne-sf
 */
public interface MemberRepository extends CrudRepository<MemberEntity, UUID> {

	/** The query for the BatchLoader */
	@Query(value = "select m from Member m where id in ?1")
	List<MemberEntity> findByIds(List<UUID> ids);

	/** Retrieves the Author of a Topic */
	@Query(value = ""//
			+ " select m " //
			+ " from Member m "//
			+ " join Topic t on m.id=t.authorId "//
			+ " where t.id=?1")
	MemberEntity findAuthorOfTopic(UUID topicId);
}