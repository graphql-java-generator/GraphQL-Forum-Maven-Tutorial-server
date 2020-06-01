/**
 * 
 */
package org.forum.server.jpa.repositories;

import java.util.List;
import java.util.UUID;

import org.forum.server.jpa.BoardEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 * 
 * @author etienne-sf
 */
public interface BoardRepository extends CrudRepository<BoardEntity, UUID> {

	/** The query for the BatchLoader */
	@Query(value = "select b from Board b where b.id in ?1")
	List<BoardEntity> findByIds(List<UUID> ids);

}
