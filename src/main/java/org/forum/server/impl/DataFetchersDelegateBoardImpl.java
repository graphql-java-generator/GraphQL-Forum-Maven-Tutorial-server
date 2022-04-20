package org.forum.server.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.dataloader.BatchLoaderEnvironment;
import org.forum.server.graphql.Board;
import org.forum.server.graphql.Topic;
import org.forum.server.graphql.util.DataFetchersDelegateBoard;
import org.forum.server.jpa.BoardEntity;
import org.forum.server.jpa.TopicEntity;
import org.forum.server.jpa.repositories.BoardRepository;
import org.forum.server.jpa.repositories.TopicRepository;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetchingEnvironment;

@Component
public class DataFetchersDelegateBoardImpl implements DataFetchersDelegateBoard {

	/** An internal utility to map lists of a type into list of another type */
	@Resource
	private Util util;

	@Resource
	BoardRepository boardRepository;
	@Resource
	TopicRepository topicRepository;

	@Override
	public List<Topic> topics(DataFetchingEnvironment dataFetchingEnvironment, Board origin, Date since) {
		List<TopicEntity> topics;

		// The query to execute depends on the since param: is it given?
		if (since == null) {
			topics = topicRepository.findByBoardId(origin.getId());
		} else {
			topics = topicRepository.findByBoardIdAndSince(origin.getId(), since);
		}

		return util.mapList(topics, TopicEntity.class, Topic.class);
	}

	@Override
	public List<Board> unorderedReturnBatchLoader(List<UUID> keys, BatchLoaderEnvironment env) {
		Iterable<BoardEntity> boards = boardRepository.findAllById(keys);
		return util.mapList(boards, BoardEntity.class, Board.class);
	}

}
