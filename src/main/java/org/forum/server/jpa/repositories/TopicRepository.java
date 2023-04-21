/**
 * 
 */
package org.forum.server.jpa.repositories;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.forum.server.jpa.TopicEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * @author etienne-sf
 */
public interface TopicRepository extends CrudRepository<TopicEntity, UUID>, FindTopicRepository {

	@Query(value = "select t from Topic t where t.boardId= ?1")
	List<TopicEntity> findByBoardId(UUID boardId);

	@Query(value = "select t from Topic t where t.boardId= ?1 and t.date >= ?2")
	List<TopicEntity> findByBoardIdAndSince(UUID boardId, Date since);

	/**
	 * An example of a native query that could be used for some particular case
	 * 
	 * @param name
	 * @return
	 */
	@Query(value = "" //
			+ " select t.* " //
			+ " from Topic t "//
			+ " join Board b on t.board_id = b.id " //
			+ " where b.name = ?1" //
			, nativeQuery = true)
	List<TopicEntity> findByBoardName(String name);

	/** The query for the BatchLoader */
	@Query(value = "select t from Topic t where id in ?1")
	List<TopicEntity> findByIds(List<UUID> ids);
}
