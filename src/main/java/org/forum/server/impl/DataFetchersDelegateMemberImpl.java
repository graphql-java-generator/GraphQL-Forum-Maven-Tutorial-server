package org.forum.server.impl;

import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;

import org.dataloader.BatchLoaderEnvironment;
import org.forum.server.graphql.Member;
import org.forum.server.graphql.util.DataFetchersDelegateMember;
import org.forum.server.jpa.MemberEntity;
import org.forum.server.jpa.repositories.MemberRepository;
import org.springframework.stereotype.Component;

@Component
public class DataFetchersDelegateMemberImpl implements DataFetchersDelegateMember {

	/** An internal utility to map lists of a type into list of another type */
	@Resource
	private Util util;

	@Resource
	private MemberRepository memberRepository;

	@Override
	public List<Member> unorderedReturnBatchLoader(List<UUID> keys, BatchLoaderEnvironment env) {
		Iterable<MemberEntity> members = memberRepository.findAllById(keys);
		return util.mapList(members, MemberEntity.class, Member.class);
	}

}
