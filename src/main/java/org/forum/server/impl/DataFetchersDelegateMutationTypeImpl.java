package org.forum.server.impl;

import java.util.List;

import javax.annotation.Resource;

import org.forum.server.graphql.Board;
import org.forum.server.graphql.Post;
import org.forum.server.graphql.PostInput;
import org.forum.server.graphql.Topic;
import org.forum.server.graphql.TopicInput;
import org.forum.server.graphql.util.DataFetchersDelegateMutationType;
import org.forum.server.jpa.BoardEntity;
import org.forum.server.jpa.PostEntity;
import org.forum.server.jpa.TopicEntity;
import org.forum.server.jpa.repositories.BoardRepository;
import org.forum.server.jpa.repositories.PostRepository;
import org.forum.server.jpa.repositories.TopicRepository;
import org.springframework.stereotype.Component;

import com.github.dozermapper.core.Mapper;

import graphql.schema.DataFetchingEnvironment;
import io.reactivex.subjects.Subject;

@Component
public class DataFetchersDelegateMutationTypeImpl implements DataFetchersDelegateMutationType {

	@Resource
	private Mapper mapper;

	@Resource
	BoardRepository boardRepository;
	@Resource
	TopicRepository topicRepository;
	@Resource
	PostRepository postRepository;

	/**
	 * This {@link Subject} will be notified for each Post creation. This is the basis for the <I>subscribeToNewPost</I>
	 * subscription
	 */
	@Resource
	PostPublisher postPublisher;

	@Override
	public Board createBoard(DataFetchingEnvironment dataFetchingEnvironment, String name, Boolean publiclyAvailable) {
		BoardEntity board = new BoardEntity();
		board.setName(name);
		if (publiclyAvailable != null) {
			board.setPubliclyAvailable(publiclyAvailable);
		}
		boardRepository.save(board);
		return mapper.map(board, Board.class);
	}

	@Override
	public Topic createTopic(DataFetchingEnvironment dataFetchingEnvironment, TopicInput topicInput) {
		TopicEntity newTopic = new TopicEntity();
		newTopic.setBoardId(topicInput.getBoardId());
		newTopic.setAuthorId(topicInput.getInput().getAuthorId());
		newTopic.setPubliclyAvailable(topicInput.getInput().getPubliclyAvailable());
		newTopic.setDate(topicInput.getInput().getDate());
		newTopic.setTitle(topicInput.getInput().getTitle());
		newTopic.setContent(topicInput.getInput().getContent());
		newTopic.setNbPosts(0);
		topicRepository.save(newTopic);
		return mapper.map(newTopic, Topic.class);
	}

	@Override
	public Post createPost(DataFetchingEnvironment dataFetchingEnvironment, PostInput postParam) {
		PostEntity newPostEntity = new PostEntity();
		newPostEntity.setTopicId(postParam.getTopicId());
		newPostEntity.setAuthorId(postParam.getInput().getAuthorId());
		newPostEntity.setPubliclyAvailable(postParam.getInput().getPubliclyAvailable());
		newPostEntity.setDate(postParam.getInput().getDate());
		newPostEntity.setTitle(postParam.getInput().getTitle());
		newPostEntity.setContent(postParam.getInput().getContent());
		postRepository.save(newPostEntity);

		Post newPost = mapper.map(newPostEntity, Post.class);

		// Let's publish that new post, in case someone subscribed to the subscribeToNewPost GraphQL subscription
		postPublisher.onNext(newPost);

		return newPost;
	}

	@Override
	public List<Post> createPosts(DataFetchingEnvironment dataFetchingEnvironment, List<PostInput> spam) {
		// Actually, this mutation is for sample only. We don't want to implement it !
		// :)
		throw new RuntimeException("Spamming is forbidden");
	}

}
