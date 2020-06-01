/**
 * 
 */
package org.forum.server.jpa.repositories;

import java.util.List;

import org.forum.server.jpa.TopicEntity;

/**
 * @author etienne-sf
 */
public interface FindTopicRepository {

	/**
	 * Search for {@link TopicEntity}, based on
	 * 
	 * @param boardName
	 * @param keyword
	 * @return
	 */
	List<TopicEntity> findByBoardNameAndKeywords(String boardName, List<String> keyword);

}
