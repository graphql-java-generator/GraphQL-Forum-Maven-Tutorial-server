/**
 * 
 */
package org.forum.server.jpa.repositories;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.forum.server.jpa.TopicEntity;

/**
 * @author etienne-sf
 */
public class FindTopicRepositoryImpl implements FindTopicRepository {

	@PersistenceContext
	private EntityManager em;

	/**
	 * Search for {@link TopicEntity}, based on
	 * 
	 * @param boardName
	 * @param keyword
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<TopicEntity> findByBoardNameAndKeywords(String boardName, List<String> keyword) {
		String query = ""//
				+ " select t.* " //
				+ " from Topic t "//
				+ " join Board b on t.board_id = b.id "//
				+ " where b.name = '" + boardName.replace("'", "''") + "'";
		if (keyword != null) {
			for (String word : keyword) {
				String wordSQL = word.replace("'", "''"); // This should be secured and optimized for a real production
															// server
				query += " and ( t.title like '%" + wordSQL + "%' or t.content like '%" + wordSQL + "%')";
			}
		}
		return em.createNativeQuery(query, TopicEntity.class).getResultList();
	}

}
