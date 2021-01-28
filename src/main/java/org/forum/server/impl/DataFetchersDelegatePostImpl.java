package org.forum.server.impl;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Resource;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.DataLoader;
import org.forum.server.graphql.Member;
import org.forum.server.graphql.Post;
import org.forum.server.graphql.util.DataFetchersDelegatePost;
import org.forum.server.jpa.MemberEntity;
import org.forum.server.jpa.PostEntity;
import org.forum.server.jpa.repositories.MemberRepository;
import org.forum.server.jpa.repositories.PostRepository;
import org.springframework.stereotype.Component;

import com.github.dozermapper.core.Mapper;

import graphql.schema.DataFetchingEnvironment;

@Component
public class DataFetchersDelegatePostImpl implements DataFetchersDelegatePost {

	@Resource
	private Mapper mapper;
	/** An internal utility to map lists of a type into list of another type */
	@Resource
	private Util util;

	@Resource
	private MemberRepository memberRepository;
	@Resource
	private PostRepository postRepository;

	@Override
	public Member author(DataFetchingEnvironment dataFetchingEnvironment, Post origin) {
		MemberEntity author = memberRepository.findAuthorOfTopic(origin.getId());
		return mapper.map(author, Member.class);
	}

	@Override
	public CompletableFuture<Member> author(DataFetchingEnvironment dataFetchingEnvironment,
			DataLoader<UUID, Member> dataLoader, Post origin) {
		// TODO Store in cache the Post (as it has already been read) to avoid the query below
		PostEntity post = postRepository.findById(origin.getId()).get();

		return dataLoader.load(post.getAuthorId());
	}

	@Override
	public List<Post> batchLoader(List<UUID> keys, BatchLoaderEnvironment env) {
		Iterable<PostEntity> topics = postRepository.findAllById(keys);
		return util.mapList(topics, PostEntity.class, Post.class);
	}

}
