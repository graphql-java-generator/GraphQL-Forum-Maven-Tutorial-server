/**
 * 
 */
package org.forum.server.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import org.springframework.stereotype.Component;

import com.github.dozermapper.core.Mapper;

/**
 * A Spring bean that contains various utilities
 * 
 * @author etienne-sf
 */
@Component
public class Util {

	/**
	 * The <A HREF="http://dozer.sourceforge.net/">Dozer mapper</A> will allow the mapping between the JPA Entities
	 * (that are mapped to the database) and the GraphQL objects (that are mapped to the GraphQL schema)
	 */
	@Resource
	private Mapper mapper;

	/**
	 * Maps an {@link Iterable} of a given source class a list of target class.
	 * 
	 * @param <S>
	 *            The source class
	 * @param <T>
	 *            The target class
	 * @param sources
	 *            The {@link Iterable} of source instances.
	 * @param sourceClass
	 *            The source class
	 * @param targetClass
	 *            The target class
	 * @return The list of target classes, where each instance is mapped from the source class found in <I>sources</I>.
	 *         It returns null if <I>sources</I> is null.
	 */
	<S, T> List<T> mapList(Iterable<S> sources, Class<S> sourceClass, Class<T> targetClass) {
		if (sources == null)
			return null;

		List<T> ret = new ArrayList<>();
		for (S s : sources) {
			ret.add(mapper.map(s, targetClass));
		} // for

		return ret;
	}

}
