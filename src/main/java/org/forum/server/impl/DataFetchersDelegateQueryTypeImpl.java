package org.forum.server.impl;

import java.util.List;

import javax.annotation.Resource;

import org.forum.server.graphql.Board;
import org.forum.server.graphql.Topic;
import org.forum.server.graphql.util.DataFetchersDelegateQueryType;
import org.forum.server.jpa.BoardEntity;
import org.forum.server.jpa.TopicEntity;
import org.forum.server.jpa.repositories.BoardRepository;
import org.forum.server.jpa.repositories.TopicRepository;
import org.springframework.stereotype.Component;

import com.github.dozermapper.core.Mapper;

import graphql.schema.DataFetchingEnvironment;

@Component
public class DataFetchersDelegateQueryTypeImpl implements DataFetchersDelegateQueryType {

	@Resource
	private BoardRepository boardRepository;
	@Resource
	private TopicRepository topicRepository;

	/**
	 * The <A HREF="https://github.com/DozerMapper/dozer/">Dozer mapper</A> will allow the mapping between the JPA
	 * Entities (that are mapped to the database) and the GraphQL objects (that are mapped to the GraphQL schema)
	 */
	@Resource
	private Mapper mapper;

	@Resource
	private Util util;

	@Override
	public List<Board> boards(DataFetchingEnvironment dataFetchingEnvironment) {
		Iterable<BoardEntity> boards = boardRepository.findAll();
		return util.mapList(boards, BoardEntity.class, Board.class);
	}

	@Override
	public Integer nbBoards(DataFetchingEnvironment dataFetchingEnvironment) {
		return (int) boardRepository.count();
	}

	@Override
	public List<Topic> topics(DataFetchingEnvironment dataFetchingEnvironment, String boardName) {
		Iterable<TopicEntity> topics = topicRepository.findByBoardName(boardName);
		return util.mapList(topics, TopicEntity.class, Topic.class);
	}

	@Override
	public List<Topic> findTopics(DataFetchingEnvironment dataFetchingEnvironment, String boardName,
			List<String> keyword) {
		Iterable<TopicEntity> topics = topicRepository.findByBoardNameAndKeywords(boardName, keyword);
		return util.mapList(topics, TopicEntity.class, Topic.class);
	}

}
