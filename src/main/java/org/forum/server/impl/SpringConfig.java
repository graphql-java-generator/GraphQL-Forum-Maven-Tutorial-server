/**
 * 
 */
package org.forum.server.impl;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;

/**
 * This Spring Config class allows to add the specific Spring configuration part for your code.
 * 
 * @author etienne-sf
 */
@Configuration
@EnableJpaRepositories(basePackages = { "org.forum.server.jpa" })
@EntityScan(basePackages = { "org.forum.server.jpa" })
public class SpringConfig {

	@Bean
	Mapper mapper() {
		// The mapper can be configured to manage differences between the JPA Entities and the GraphQL objects/
		// In our case, there is no difference between them. So there is no need to define any mapping
		return DozerBeanMapperBuilder.buildDefault();
	}

}
