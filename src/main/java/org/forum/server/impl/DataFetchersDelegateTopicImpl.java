package org.forum.server.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.DataLoader;
import org.forum.server.graphql.Member;
import org.forum.server.graphql.Post;
import org.forum.server.graphql.Topic;
import org.forum.server.graphql.util.DataFetchersDelegateTopic;
import org.forum.server.jpa.MemberEntity;
import org.forum.server.jpa.PostEntity;
import org.forum.server.jpa.TopicEntity;
import org.forum.server.jpa.repositories.MemberRepository;
import org.forum.server.jpa.repositories.PostRepository;
import org.forum.server.jpa.repositories.TopicRepository;
import org.springframework.stereotype.Component;

import com.github.dozermapper.core.Mapper;

import graphql.schema.DataFetchingEnvironment;

@Component
public class DataFetchersDelegateTopicImpl implements DataFetchersDelegateTopic {

	@Resource
	private Mapper mapper;
	/** An internal utility to map lists of a type into list of another type */
	@Resource
	private Util util;

	@Resource
	private MemberRepository memberRepository;
	@Resource
	private PostRepository postRepository;
	@Resource
	private TopicRepository topicRepository;

	@Override
	public Member author(DataFetchingEnvironment dataFetchingEnvironment, Topic origin) {
		MemberEntity author = memberRepository.findAuthorOfTopic(origin.getId());
		return mapper.map(author, Member.class);
	}

	@Override
	public CompletableFuture<Member> author(DataFetchingEnvironment dataFetchingEnvironment,
			DataLoader<UUID, Member> dataLoader, Topic origin) {
		// TODO Store in cache the Topic (as it has already been read) to avoid the query below
		TopicEntity topic = topicRepository.findById(origin.getId()).get();

		return dataLoader.load(topic.getAuthorId());
	}

	@Override
	public List<Post> posts(DataFetchingEnvironment dataFetchingEnvironment, Topic origin, UUID memberId,
			String memberName, Date since) {
		List<PostEntity> posts;
		if (since == null) {
			// This should not happen, as since is mandatory
			throw new NullPointerException("since may not be null (its mandatory)");
		}

		// The memberId and memberName are Optional. The since param is mandatory.
		// So there are 4 combinations for the request.

		// since
		if (memberId == null && memberName == null) {
			posts = postRepository.findByTopicIdAndSince(origin.getId(), since);
		}
		// memberId, since
		else if (memberName == null) {
			posts = postRepository.findByTopicIdAndMemberIdAndSince(origin.getId(), memberId, since);
		}
		// memberName,since
		else if (memberId == null) {
			posts = postRepository.findByTopicIdAndMemberNameAndSince(origin.getId(), memberName, since);
		}
		// memberId, memberName, since
		else {
			posts = postRepository.findByTopicIdAndMemberIdAndMemberNameAndSince(origin.getId(), memberId, memberName,
					since);
		}

		return util.mapList(posts, PostEntity.class, Post.class);
	}

	@Override
	public List<Topic> unorderedReturnBatchLoader(List<UUID> keys, BatchLoaderEnvironment env) {
		Iterable<TopicEntity> topics = topicRepository.findAllById(keys);
		return util.mapList(topics, TopicEntity.class, Topic.class);
	}

}
