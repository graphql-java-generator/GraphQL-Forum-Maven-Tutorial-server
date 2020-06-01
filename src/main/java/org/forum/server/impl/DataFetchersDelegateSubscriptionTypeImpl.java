package org.forum.server.impl;

import javax.annotation.Resource;

import org.forum.server.graphql.Post;
import org.forum.server.graphql.util.DataFetchersDelegateSubscriptionType;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetchingEnvironment;
import io.reactivex.subjects.Subject;

@Component
public class DataFetchersDelegateSubscriptionTypeImpl implements DataFetchersDelegateSubscriptionType {

	/** The logger for this instance */
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * This {@link Subject} will be notified for each Post creation. This is the basis for the <I>subscribeToNewPost</I>
	 * subscription
	 */
	@Resource
	PostPublisher postPublisher;

	@Override
	public Publisher<Post> subscribeToNewPost(DataFetchingEnvironment dataFetchingEnvironment, String boardName) {
		logger.debug("Received a Subscription for {}", boardName);
		return postPublisher.getPublisher(boardName);
	}

}
