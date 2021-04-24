# A tutorial for the GraphQL Maven plugin (server side)


This Tutorial describes how-to create a GraphQL server, with the [graphql-maven-plugin](https://github.com/graphql-java-generator/graphql-maven-plugin-project) and the [graphql Gradle plugin](https://github.com/graphql-java-generator/graphql-gradle-plugin-project).


The GraphQL plugin helps both on the server and on the client side. You'll find the tutorial here the [client Maven tutorial](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-client) and the [client Gradle tutorial](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-client).


# Schema first

This plugin allows a schema first approach.

This approach is the best approach for APIs: it allows to precisely control the Interface Contract. This contract is the heart of all connected systems.

This tutorial won't describe how to create a GraphQL schema. There are plenty of resources on the net for that, starting with the [official GraphQL site](https://graphql.org/).

# The Forum GraphQL schema

This sample is based on the Forum schema, [available here](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-server/blob/master/src/main/resources/forum.graphqls).

This schema contains:

* A custom scalar definition: Date.
    * This allows to define new type to define objet's field. We'll have to provide it's implementation to read and write Date fields. 
* A schema object. This declaration is optional. It allows to define query/mutation/subscription specific names. This schema declares:
    * QueryType as a query.
    * MutationType as a mutation
    * SubscriptionType as a subscription
    * These types are declared below, as any regular object. Their definition is that of standard Object, but their meaning is very different. These fields are respectively the queries, mutations and subscriptions that you can execute, as a client GraphQL schema that connects to a GraphQL server that implements this schema.
* Four regular GraphQL objects: Member, Board, Topic, Post
    * These are the objects defined in the Object model that can queried (with queries or subscriptions), or inserted/updated (with mutations)
* One enumeration: MemberType
    * Enumeration are a kind of scalar, that allows only a defined list of values. MemberType is one of ADMIN, MODERATOR or STANDARD.
* Three input types: TopicPostInput, TopicInput and PostInput
    * These are objects that are not in the Object model. They may not be returned by queries, mutations or subscriptions. As their name means, they can only be used as field parameters. And regular objects maynot be use as field parameters.

This schema is stored in the _/src/main/resources/_ project folder for convenience. 

It could be also be used in another folder, like _/src/main/graphql/_. In this case, the schema is not stored in the packaged jar (which is Ok), and you have to use the plugin _schemaFileFolder_ parameter, to indicate where to find this schema.

## The Maven pom.xml and Gradle build.gradle files

As a Maven or a Gradle plugin, you have to add the plugin in the build:
* For Maven, you add it in the build section of your pom (here is the [full pom](https://github.com/graphql-java-generator/GraphQL-Forum-Maven-Tutorial-server/blob/master/pom.xml)):
* For Gradle, you declare the plugin, then configure it (here is the full [build.gradle](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/build.gradle))

Let's first have a look at the Maven **pom.xml** file:  

```XML
	
	<build>
...
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.8.1</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
					<releasse>1.8</releasse>
				</configuration>
			</plugin>
			<plugin>
				<groupId>com.graphql-java-generator</groupId>
				<artifactId>graphql-maven-plugin</artifactId>
				<version>${graphql-maven-plugin.version}</version>
				<executions>
					<execution>
						<goals>
							<goal>generateServerCode</goal>
						</goals>
					</execution>
				</executions>
				<configuration>
					<mode>server</mode>
					<packageName>org.forum.server.graphql</packageName>
					<scanBasePackages>org.forum.server.impl, org.forum.server.jpa</scanBasePackages>
					<customScalars>
						<customScalar>
							<graphQLTypeName>Date</graphQLTypeName>
							<javaType>java.util.Date</javaType>
							<graphQLScalarTypeStaticField>com.graphql_java_generator.customscalars.GraphQLScalarTypeDate.Date</graphQLScalarTypeStaticField>
						</customScalar>
					</customScalars>
					<!-- The parameters below change the 1.x default behavior to respect 
						the future 2.x behavior -->
					<generateBatchLoaderEnvironment>true</generateBatchLoaderEnvironment>
					<separateUtilityClasses>true</separateUtilityClasses>
				</configuration>
			</plugin>
...
		</plugins>
	</build>

	<dependencies>
		<!-- Dependencies for GraphQL -->
		<dependency>
			<groupId>com.graphql-java-generator</groupId>
			<artifactId>graphql-java-server-dependencies</artifactId>
			<version>${graphql-maven-plugin.version}</version>
			<type>pom</type>
		</dependency>

		<!-- Add of the graphiql interface, to test your GraphQL server -->
		<!-- It's available at http://localhost:8180/graphiql -->
		<dependency>
			<!-- com.graphql-java:graphiql-spring-boot-starter is deprecated. This 
				project has been moved to com.graphql-java-kickstart -->
			<groupId>com.graphql-java-kickstart</groupId>
			<artifactId>graphiql-spring-boot-starter</artifactId>
			<version>6.0.1</version>
			<scope>runtime</scope>
		</dependency>

	</dependencies>
```

Then the Gradle **build.gradle** file:

```Groovy
plugins {
	id "com.graphql_java_generator.graphql-gradle-plugin" version "1.14.1"
	id 'java'
}

repositories {
	jcenter()
	mavenCentral()
}

dependencies {
	// The graphql-java-runtime module agregates all dependencies for the generated code, including the plugin runtime
	// CAUTION: this version should be exactly the same as the graphql-gradle-plugin's version
	implementation 'com.graphql-java-generator:graphql-java-server-dependencies:1.14.1'
	implementation 'com.github.dozermapper:dozer-core:6.5.0'
	implementation 'io.reactivex.rxjava2:rxjava:2.2.19'
	
	// The Spring Boot version should be the same as the Spring Boot version of the graphql-gradle-plugin
	implementation('org.springframework.boot:spring-boot-starter-data-jpa:2.4.4')
	
	runtimeOnly 'com.graphql-java-kickstart:graphiql-spring-boot-starter:6.0.1'
	runtimeOnly 'com.h2database:h2:1.4.200'
}

// The line below makes the GraphQL plugin be executed before Java compiles, so that all sources are generated on time
compileJava.dependsOn generateServerCode

// The line below adds the generated sources as a java source folder
sourceSets.main.java.srcDirs += '/build/generated/graphql-maven-plugin'

// Let's configure the GraphQL Gradle Plugin:
// All available parameters are described here: 
// https://graphql-maven-plugin-project.graphql-java-generator.com/graphql-maven-plugin/generateServerCode-mojo.html
generateServerCodeConf {
	packageName = 'org.forum.server'
	packageName = 'org.forum.server.graphql'	
	generateBatchLoaderEnvironment = true
	scanBasePackages = 'org.forum.server.impl, org.forum.server.jpa'
	customScalars = [ [
			graphQLTypeName: "Date",
			javaType: "java.util.Date",
			graphQLScalarTypeStaticField: "com.graphql_java_generator.customscalars.GraphQLScalarTypeDate.Date"
	] ]

	// The parameters below change the 1.x default behavior. They are set to respect the behavior of the future 2.x versions
	//generateBatchLoaderEnvironment = true
	separateUtilityClasses = true
}
```

The compiler must be set to version 1.8 (or higher).

In this plugin declaration:
* (for Maven only) The plugin execution is mapped to its generateServerCode goal
* Its mode is set to _server_
* The plugin generates the GraphQL code in the _packageName_ package (or in the _com.generated.graphql_ if this parameter is not defined)
* The _scanBasePackages_ allows you to define additional packages that Spring will scan, to discover Spring beans, Spring data repositories or JPA entities
* The _separateUtilityClasses_ set _true_ allows this separation:
    * All the classes generated directly from the GraphQL schema (object, enum, interfaces, input types...) are generated in _packageName_.
    * All the utility classes are generated in the sub-package _util_
    * This insures to have no collision between the GraphQL code and the GraphQL plugin's utility classes
    * If you set it to _false_, or don't define it, then all classes are generated in the _packageName_ package 
* And we declare the _Date_ scalar implementation. 
    * It is mandatory to give the implementation for each custom scalar defined in the GraphQL schema.
    * You'll find the relevant documentation on the [Plugin custom scalar doc page](https://graphql-maven-plugin-project.graphql-java-generator.com/customscalars.html)  

The generated source is added to the IDE sources, thanks to:
* (for Maven) The _build-helper-maven-plugin_, so that the generated source is automatically added to the build path of your IDE.
* (for Gradle) The _sourceSets.main.java.srcDirs += ..._ line

The _graphql-java-server-dependencies_ dependency provides all the necessary dependencies, for the generated code. Of course, __its version must be the same as the plugin's version__.

The [graphiql-spring-boot-starter](https://github.com/graphql-java-kickstart/graphql-spring-boot) adds a web GUI that allows to easily test your GraphQL server. It's very useful for testing. And it should of course be removed for production. This generate a web page that allow to execute GraphQL request. This page will be available [on the /graphiql path](http://localhost:8180/graphiql) of the server, once it is started.


# A look at the generated code

Don't forget to execute (or re-execute) a full build when you change the plugin configuration, to renegerate the proper code:
* (For Maven) Execute _mvn clean compile_
* (for Gradle) Execute _gradlew clean build_

This will generate the client code in the _packageName_ package (or in the _com.generated.graphql_ if this parameter is not defined).

The code is generated in the :
* (for Maven) _/target/generated-sources/graphql-maven-plugin_ folder. And thanks to the _build-helper-maven-plugin_, it should automatically be added as a source folder to your favorite IDE.
* (for Gradle) _/build/generated-sources/generateServerCode_ folder. And thanks to the  _sourceSets.main.java.srcDirs += ..._ line in the _build.gradle_ file, it should automatically be added as a source folder to your favorite IDE.

Let's take a look at the generated code:
* The __com.graphql_java_generator__ package and its sub-packages are the necessary runtime for the generated plugin.
* The __org.forum.server.graphql__ package contains all classes that maps to the GraphQL schema:
    * The classes starting by '__' (two underscores) are the GraphQL introspection classes. These are standard GraphQL types.
    * All other classes are directly the items defined in the forum GraphQL schema, with their fields, getter and setter. All fields are annotated with the GraphQL information necessary on runtime, and the JSON annotations to allow the deserialization of the server response.
* The __org.forum.server.graphql.util__ package contains:
    * _BatchLoaderDelegateXxxxImpl_ classes are utility classes that manages the data loader for you.
    * _DataFetchersDelegateXxx_ interfaces: these interfaces contain all method that indicates to the server how to retrieve the data. You'll have to implement these interfaces  
    * _GraphQLDataFetchers_ is a technical class that declares every Data Fetcher to the graphql-java framework
    * _GraphQLProvider_ declares all GraphQL components to the graphql-java framework
    * _GraphQLServerMain_ is the main entry point of the server. The application is a regular Spring Boot application or war, and you'll find lots of information on the net on how to configure such an app (and see below)
        * If the packaging is _jar_ (like in this tutorial), it contains the _main_ method that allow the produced jar to start as a java application
        * If the packaging is _war_, it inherits from _SpringBootServletInitializer_, allowing the module to be loaded as war webapp.
    * _WebSocketXxx_ are utility classes to manage Web Sockets. Web Sockets are the way to receive the notification back from a subscription.  


To sum up, you'll use:
* If your packaging type is _jar_ , then the _GraphQLServerMain_ as the java application to start. Of it's a _war_ then just load the _war_ in your app server.
* The _DataFetchersDelegateXxx_ interfaces, that you need to implement, in order to code the data access for your GraphQL server. See below for the details.
    * Note : these interfaces contain the method to use [data loader](https://www.graphql-java.com/documentation/v14/batching/) when possible. 

That's all!

# What is a Data Fetchers Delegate (that needs to be implemented)

The Data Fetchers are actually Spring beans, declared in the _GraphQLDataFetchers_ generated class, like defined in the [graphql-java doc](https://www.graphql-java.com/documentation/v14/data-fetching/).

These Data Fetchers are based on the Data Fetcher Delegates that you must provide:
* There is a Data Fetcher Delegate for each GraphQL object, including query, mutation and subscription
* A Data Fetcher Delegate is a Spring Bean, that is: a class __marked by the _@Component_ annotation__
* A Data Fetcher Delegate implements one of the generated DataFetcherDelegateXxx interfaces
* The methods of a Data Fetcher Delegate are all the Data Fetchers (or resolvers) for one GraphQL object, query, mutation or subscription.

In this sample based on the forum GraphQL schema, these Data Fetcher Delegates must be implemented:
* DataFetchersDelegateQueryType: manages all queries defined in the GraphQL schema
* DataFetchersDelegateMutationType: manages all mutations defined in the GraphQL schema
* DataFetchersDelegateSubscriptionType: manages all subscriptions defined in the GraphQL schema
* DataFetchersDelegateBoard, DataFetchersDelegateMember, DataFetchersDelegatePost and DataFetchersDelegateTopic manage the relations between objects, and their [data loader](https://www.graphql-java.com/documentation/v14/batching/), if relevant

# A note on the database for this sample

This sample embeds an H2 in-memory database, that is field with sample data when the server starts. The H2 console is available at this URL: [http://localhost:8180/h2-console](http://localhost:8180/h2-console). You'll have to enter this URL in the console to connect to the database: _jdbc:h2:mem:testdb_ (all other parameters are correct)

This sample reuses what has been done for the [graphql-maven-plugin-samples-Forum-server module](https://github.com/graphql-java-generator/graphql-maven-plugin-project/tree/master/graphql-maven-plugin-samples/graphql-maven-plugin-samples-Forum-server) of the graphql-maven-plugin. It contains these files, in the src/main/resources folder:
* _[data.xlsx](https://github.com/graphql-java-generator/graphql-maven-plugin-project/blob/master/graphql-maven-plugin-samples/graphql-maven-plugin-samples-Forum-server/src/main/resources/data.xlsx?raw=true)_ is the raw source data
* _[schema.sql](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/resources/schema.sql)_ is executed at startup, against the embedded H2 in-memory database. It creates the necessary tables
* _[data.sql](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/resources/data.sql)_ is then executed, also at startup. It loads the sample data in the database  

# The application.properties file

The _application.properties_ file is the standard Spring Boot configuration file. It allow to manage almost all the application parameters: server port, database parameters, tls... You'll lors of documentation about it on the net, starting with [the Spring doc](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html). 

The application.properties for this sample is [available here](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/resources/application.properties).

It's a very simple one, that contains these parameters:
* server.port = 8180
    * Obviously, the server will listen on port 8180.
    * As no ssl/tls has been configured, the GraphQL endpoint is available here: [http://localhost:8180/graphql](http://localhost:8180/graphql) once you starts it.
* spring.h2.console.enabled=true
    * This allows to access to the data through the H2 console. Of course, it's ok for this tutorial, but not for real production
* spring.jpa.generate-ddl=false
* spring.jpa.hibernate.ddl-auto=none
    * These two lines prevents the SQL schema to be automatically created. With this parameter set to false, it's up to the project to build the tables, indexes...


# How to access to the database (or any other kind of data source) ?

### Use JPA annotations

The plugin contains a _generateJPAAnnotation_ that adds JPA annotations into each data object created from the GraphQL schema. This allows to directly map you GraphQL entities into the database.

In this case, your code is minimal.

### Use DTO

Using DTOs allows you to separate the GraphQL schema from your database schema. This allows you to manage differences between an objet schema (GraphQL) and a relational schema or any other kind of datasource.

You can see these two posts, that give more insight into this: [How to use graphql with jpa if schema is different to database structure](https://stackoverflow.com/a/60480442/5056068) and [Graphql Tools: Map entity type to graphql type](https://stackoverflow.com/a/58809449/5056068).

It can be very interesting for complex schemas, for instance with Interfaces or Unions.

### JPA or DTO in this tutorial

For this GraphQL schema, which is quite simple, the use of the _generateJPAAnnotation_ plugin parameter is a perfect fit. You can see it in the [forum sample](https://github.com/graphql-java-generator/graphql-maven-plugin-project/tree/master/graphql-maven-plugin-samples/graphql-maven-plugin-samples-Forum-server) embedded in the [graphql maven plugin project](https://github.com/graphql-java-generator/graphql-maven-plugin-project).  

In this tutorial, we'll separate the GraphQL objects from the JPA entities, to demonstrate the use of DTOs with the graphql plugin, as it should be used in real life complex projects.

Of course, as the backend is a relational database, we'll use JPA entities objects. These JPA entities are not generated by the plugin as, if you don't use the  _generateJPAAnnotation_ plugin parameter, it means that your relational schema differs from the GraphQL schema. 

Another option is to start with  _generateJPAAnnotation_ plugin parameter set to _true_ , which makes simpler code. Then, when it doesn't fit any more, switch to  _generateJPAAnnotation_ set to _false_ and start using DTOs.

# Creating the Data Fetchers Delegate

When developping a GraphQL server, based on the code generated by the graphql plugin, your "only" task is to implement the Data Fetchers Delegate. That is: create a Spring bean for each DataFetchersDelegateXxx interface that has been generated by the plugin.

You can find these interfaces in the _<packageName>.util_ package, where:
* packageName is the name you've provided in your _pom.xml_ or _build.gradle_ file. If you didn't provide a package name, the default name is used: _com.generated.graphql_
* The _util_ exists as we've set the _separateUtilityClasses_ plugin parameter to _true_

For each of these DataFetchersDelegateXxx interfaces, you have to:
* Create a concrete class that implement the DataFetchersDelegateXxx interface
* Putting it in a package that will be scanned by Spring. It can be either:
    * The _<packageName>_ package, or one of its subpackages
    * The _scanBasePackages_ package or one of its subpackages, if the _scanBasePackages_ plugin parameter is defined.
* Add the @Component Spring annotation to allow Spring to discover it

Please note that, when you change something in the Maven pom.xml or Gradle build.gradle file, you need to execute a _mvn clean compile_ or a _gradlew clean compileJava_ to regenerate the code.

Once you created all DataFetchersDelegate implementation, the server should be able to start.

For instance, you can create each DataFetchersDelegate implementation, with all methods returning null. This allows to check that the global code structure is ready.

Just execute the _org.forum.server.graphql.util.GraphQLServerMain_ class to check that, where:
* _org.forum.server.graphql_ is the _packageName_ parameter, as defined in the Maven pom.xml or Gradle build.gradle file
* The _util_ subpackage is created because the _separateUtilityClasses_ plugin parameter is set to _true_

At this point, if it complains because it doesn't find a bean, check that:
* You've created a class of the specified type
* That you've added the @Component annotation
* That you've added its package (or one of its parent package) into the Spring scan path, by using the _scanBasePackages_ plugin parameter.

Once all this is Ok, your code is wired to GraphQL. And it's up to you to implement what's specific to your use case.

Let's dot it, for this forum sample. 


# Update of the project configuration

For the next steps, we'll add these updates to the Maven pom.xml or Gradle build.gradle file:

Let's first add the JPA dependencies, the H2 database runtime and the Dozer dependency.


Here is for the pom Maven file:

```XML
	<dependencies>
...
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
			<version>2.2.6.RELEASE</version>
		</dependency>
		<dependency>
			<!-- Only here for the tests, to load the data in an in-memory database -->
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
			<version>1.4.199</version>
		</dependency>
		<dependency>
			<groupId>com.github.dozermapper</groupId>
			<artifactId>dozer-core</artifactId>
			<version>6.5.0</version>
		</dependency>
...
	</dependencies>
```

Then for the build.gradle Gradle file:

```Groovy
dependencies {
...
	
	implementation 'com.github.dozermapper:dozer-core:6.5.0'
	// The Spring Boot version should be the same as the Spring Boot version of the graphql-gradle-plugin
	implementation('org.springframework.boot:spring-boot-starter-data-jpa:2.2.6.RELEASE') {
        exclude group: 'org.springframework.boot', module: 'spring-boot-starter-logging'
    }
	runtime 'com.h2database:h2:1.4.199'
}
```


Then, we need to create the dozer Spring Bean. So we created a SpringConfig class, in the _org.forum.server.impl_ package. This class:
* Declares the JPA package, where Entities and Spring Repositories are to be searched
* The dozer Mapper, that'll use to map Entities to and from GraphQL objects 

```Java
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
```

As we added the _org.forum.server.impl_ package in the _scanBasePackages_ plugin parameter, Spring will discover bean (marked with @Component) and configuration files (marked with @Configuration). These configuration classes, like the one above, declare Spring beans with _@Bean_ on methods. These methods returns Spring beans, which name are the method's name, and type is the method returning type.

You can then reuse these beans anywhere, by declaring them as a dependency in a class attribute like this:

```Java

import javax.annotation.Resource;
import com.github.dozermapper.core.Mapper;

@Component
public class MyUsefulClass {

	// mapper will be magically wired by Spring, when MyUsefulClass is instanciated
	@Resource
	private Mapper mapper;

...

}
```

The _mapper_ attribute is then set when the _MyBean_ is loaded, with the bean returned by the _mapper()_ method of the above _SpringConfig_ class.

You'll find lots of Spring documentation on the net, starting with the [Spring reference doc](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html)


# Creating the JPA Entities

We now need to create our JPA Entities. 

As we expect differences between the database and the GraphQL schemas, we create a separate set of classes for the JPA Entities. These classes have been created in the [org.forum.server.jpa](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/tree/master/src/main/java/org/forum/server/jpa) package.

Let's have a look at the Topic Entity:

```Java
@Entity(name = "Topic")
public class TopicEntity {

	@Id
	@GeneratedValue
	UUID id;

	Date date;

	Boolean publiclyAvailable;

	Integer nbPosts;

	String title;

	String content;

	UUID boardId;

	UUID authorId;

	@Transient
	List<PostEntity> posts;
	
... All getters and setters  (you can use Lombok if you don't want to write them manually

}
```

All its fields maps the database schema, of course.

The _@Entity_ annotation marks this class as a JPA Entity, and thus, it will be mapped to the database. We force its database name to be _Topic_ , as the classname is different than the database table. We could have chosen to name this entity _Topic_, but it would be confusing to have exactly the same name as the GraphQL object.

The Id is marked as ID, and its value is generated by the framework.

Note the _@Transient_ annotation, that makes sure that JPA won't load the objects that has a relation with this one: it's the GraphQL's job to optimize these accesses. So we don't want JPA framework to crawl through database relations.

__Spring Discovery__ : the JPA Entities are discovered by Spring the package of the starting configuration ( _packageName.util_ ) or one if its subpackage. In this tutorial, they are in another package. To manage this, there are two possibilities:
* The standard one is to add a _@EntityScan_ annotation to a Spring Configuration class:
    * It can be the main one, here the generated _GraphQLServerMain_ class. But as it's generated, your updates would be cleared at each generation.
    * You can add your own Configuration class, like done in this tutorial:
        * [SpringConfig](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/java/org/forum/server/impl/SpringConfig.java) is a Spring Configuration class, as it's annotated by _@Configuration_ __and__ it's in a package or subpackage marked in the _scanBasePackages_ plugin parameter of the _@SpringBootApplication_ of _GraphQLServerMain_'s annotation. This last part is done, thanks to the c
         * It contains the _@EntityScan_ annotation, that indicates to scan the _org.forum.server.jpa_ package
* This plugin will add the _@EntityScan_ annotation with all packages of the _@EntityScan_ annotation, __but only if__ the _generateJPAAnnotation_ plugin parameter is set to true

As we don't use the plugin _generateJPAAnnotation_ plugin parameter, we created [SpringConfig](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/java/org/forum/server/impl/SpringConfig.java) in the _org.forum.server.impl_ and this package is given to the plugin with the _scanBasePackages_ plugin parameter.

This _SpringConfig_ allows us to add whatever Spring configuration we want. Here, like explained above, we added: the scan for JPA repsitories (@EnableJpaRepositories), the scan for JPA entities (@EntityScan) and the mapper bean.

# Creating the Spring repositories

[Spring data Repositories](https://docs.spring.io/spring-data/data-commons/docs/current/reference/html/#repositories) are magical: just declare an interface, and it will guess the query to execute!

If you use JPA, like in this sample, you should also take a look at the [JPA Repositories](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.repositories).

In this tutorial, these repositories are stored in the [org.forum.server.jpa.repositories](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/tree/master/src/main/java/org/forum/server/jpa/repositories) package.

All repositories are standard Spring data repositories.

The only particular thing is the [FindTopicRepository](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/java/org/forum/server/jpa/repositories/FindTopicRepository.java) one. It adds a "complex" research capability, that is implemented in the [FindTopicRepositoryImpl](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/java/org/forum/server/jpa/repositories/FindTopicRepositoryImpl.java) class.

The [TopicRepository](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/java/org/forum/server/jpa/repositories/TopicRepository.java) repository just implement the _FindTopicRepository_ to benefit from this search capability.

Spring data and JPA repositories are very rich: you should really take a look at their document, if you haven't already, to see what capabilities they offer.


Once you've created the JPA Entities and the Spring Repositories, everything is ready for the actual Data Fetchers Delegate implementation.


# Implementing the Data Fetchers Delegate

### DTO versus JPA entities

As stated above, we'll separate the GraphQL objects from the JPA entities, to demonstrate the use of DTOs with the graphql plugin, as it should be used in real life complex projects.

As, in this current state, the database schema and the GraphQL schema same, we'll use [Dozer](https://dozermapper.github.io/) to map the JPA entity. Dozer allow customization, if the two schemas slightly differs. If they become too different, you'll have to code the mapping yourself. In this case, you'll be happy to have this mapping layer between the database and the GraphQL schemas, as it allows you to hide the complexity to the GraphQL consumers.

__Note about performance:__
* In relational database, the data is usually stored by rows. This means that, even if the GraphQL query asks only some fields of an object, you'll have to read all the content of the row from the storage, that is: read all the fields of the entity. So it won't change anything, if you map the full read object into the GraphQL object. Of course, only the query fields will be returned by the GraphQL server.
* In NoSQL databases and some relational database (for instance Oracle Exadata), it can happen to have column storage. In this case, the query should read only the requested fields from the data storage. It'll be up to you to implement this point. You'll have all the necessary information in the _dataFetchingEnvironment_ parameter provided to each data fetcher. Please read the graphql-java doc for more information. This will lead to performance optimization (only for big tables), and add complexity in your code. It's up to you, of course!

In this sample, we're using a standard relational database. So we let the Entity read the full object. And we fully map it into the GraphQL object. As said above, the graphql-java framework takes care of sending just the requested fields back to the consuming app.

### Mapping from and to the JPA entity

In the above class, we declare the Dozer Mapper. In this tutorial, the mapping is straightforward. But Dozer can manage complex mapping, with either mapping files or Java configuration. You'll find all the information on the [Dozer documentation](https://dozermapper.github.io/).

Dozer is very easy to use, and requires no configuration at all for standard mapping. But the drawback for this, is that it's not very fast. The overhead is small, but other frameworks offer better performances. You can check the [Baeldung test on mappers](https://www.baeldung.com/java-performance-mapping-frameworks). The two fastest are:
* MapStruct : seems very interesting. But you need to write one interface for each mapper. I guess, but didn't try, that you can group several mappers in one interface. But you still to write one method for each mapper.
* JMapper seems to be dead (no commit since 2017, and only 4 contributors)
* Orika seems also promising. It also requires no configuration for standard mapping. It throws an [annoying warning](https://github.com/orika-mapper/orika/issues/280), but it seems to be transparent, and it will be soon corrected.

As the Dozer map can't map a list of object, a utility method has been created in the [Util class](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/java/org/forum/server/impl/Util.java), that maps list of object. It is reused in the Data Fetcher Delegates

### Let's start with the simplest query: boards

To check that everything is properly wired, we'll just implement the _boards_ query.

It is very important to remember that the Data Fetchers __should return only the Objects of the Data Fetcher__ , not the linked objects. For instance, a _Board_ may contain several _Topics_ . You should not query that attached _Topics_ and if you return the _Board_ with its _Topics_ attached, these _Topics_ will be ignored.

If the _Topics_ for this _Board_ are also queried, then the _DataFetchersDelegateBoardImpl.topics()_ Data Fetcher will be called, to retrieve. We'll manage this in the later section. 

As it's a query, and the query type is name _QueryType_ in this GraphQL schema, this query is implemented in the _DataFetchersDelegateQueryTypeImpl_ class:

```Java
package org.forum.server.impl;

import java.util.List;

import javax.annotation.Resource;

import org.forum.server.graphql.Board;
import org.forum.server.graphql.Topic;
import org.forum.server.graphql.util.DataFetchersDelegateQueryType;
import org.forum.server.jpa.BoardEntity;
import org.forum.server.jpa.repositories.BoardRepository;
import org.springframework.stereotype.Component;

import com.github.dozermapper.core.Mapper;

import graphql.schema.DataFetchingEnvironment;

@Component
public class DataFetchersDelegateQueryTypeImpl implements DataFetchersDelegateQueryType {

	@Resource
	private BoardRepository boardRepository;

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Topic> topics(DataFetchingEnvironment dataFetchingEnvironment, String boardName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Topic> findTopics(DataFetchingEnvironment dataFetchingEnvironment, String boardName,
			List<String> keyword) {
		// TODO Auto-generated method stub
		return null;
	}

}
```

This class is a Spring Bean, as it is marked by the _@Component_ Spring annotation. It contains:
* Three Spring beans attributes, that will be set by the Spring IoC framework when this Bean is loaded:
    * The _boardRepository_ is a Spring data repository. It's a bean, as it implements a Spring Data repository interface. 
    * The _mapper_ bean is declared in the [SpringConfig](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/java/org/forum/server/impl/SpringConfig.java) configuration class.
    * The _util_ bean is declared in the [Util](https://github.com/graphql-java-generator/GraphQL-Forum-Gradle-Tutorial-server/blob/master/src/main/java/org/forum/server/impl/Util.java) class, and it's a Spring bean thanks to its _@Component_ annotation.
* The _boards_ method is the only implemented method, for a first test:
    * It executes the query against the database, by executing the relevant boardRepository method.
    * It then maps the result into a list of Board GraphQL objects

And you're done!

The graphql-java framework and the code generated by the plugin take care of handling the request, wiring it to this _boards()_ method, get its result, and return it back to the caller.


### Let's test this first implemented query

Starts the server, by executing the _org.forum.server.graphql.util.GraphQLServerMain_ class, available in the _/target/generated-sources/graphql-maven-plugin_ folder (or _/build/generated-sources/graphql-maven-plugin_ for Maven).

The output should finish by this message: _Started GraphQLServerMain in xxx seconds_

You can then access to graphiql on [http://localhost:8180/graphiql](http://localhost:8180/graphiql), and execute the _boards_ query by executing this request:


```
query {boards {id name}}
```

__If the server didn't start__, an error message is displayed. The most probable cause is that the Spring components are not properly configured. In this case, check that:
* The _scanBasePackages_ plugin parameter is defined in your pom or gradle.plugin file, and contains the package that contains the _SpringConfig_ class (or one of its parent package)
* All your Spring beans (startign by the Data Fetcher Delegate implementations) have the _@Component_ annotation (for classes) or _@Bean_ for method into a class annotated by _@Configuration_
* The _SpringConfig_ class is marked with the _org.springframework.context.annotation.Configuration_ annotation
 * The _SpringConfig_ class contains the _org.springframework.data.jpa.repository.config.EnableJpaRepositories_ annotation, which has this parameter: _(basePackages = { "xxxx" })_ (where _xxxx_  the package that contains your Spring repositories or one of its parent package)
* The _SpringConfig_ class contains the _org.springframework.boot.autoconfigure.domain.EntityScan_ which has this parameter: _(basePackages = { "xxxx" })_ (where _xxxx_  the package that contains your JPA entities or one of its parent package)
* You regenerated the code, by executing the _mvn clean package_ or a _gradlew clean compileJava_ command.

If all this fails, please raise an issue.


### Let's implement the relation between _Board_ and _Topic_

As stated above, the _DataFetchersDelegateQueryTypeImpl.boards()_ that we implement should return only _Board_ objects. It should not return any attached _Topics_ : it's up to the GraphQL framework, to check if subobjects (like _Topics_ here) are queried, and then call the appropriate Data Fetchers.

We now want to be able to query the _boards_ and their _Topics_ :

```
query {boards {id name topics {id date title nbPosts }}}
```

To do that, the GraphQL framework will execute:
* The _boards_ Query Data Fetcher that we have already implemented
* The _topics()_ Data Fetcher, of the _Board_ Data Fetcher Delegate for each found _Board_

We also implement the _batchLoader()_ method which will be explained in the next paragraph.

```Java
@Component
public class DataFetchersDelegateBoardImpl implements DataFetchersDelegateBoard {

	/** An internal utility to map lists of a type into list of another type */ 
	@Resource
	private Util util;

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
	public List<Board> batchLoader(List<UUID> keys, BatchLoaderEnvironment env) {
		Iterable<BoardEntity> boards = boardRepository.findAllById(keys);
		return util.mapList(boards, BoardEntity.class, Board.class);
	}
...

}
```

The _topics()_ method:
* First executes the query against the database, and retrieve a list of _TopicEntity_
    * The query to execute very, depending on the since parameter.
    * The since parameter is the since parameter, defined in the GraphQL schema, for the _topics_ field of the _Board_ object. 
        * Thanks to the plugin, all the field parameters are set as argument of the Data Fetcher method.
        * This also helps you to manage changes in the GraphQL schema: when the code is regenerated, if there is a new field parameter, then the Data Fetcher method will have this argument added. This will lead to a compilation error: it's then up to you to properly handle it.     
* The map this list into a list of GraphQL _Topics_ as expected


You can now start the GraphQL server, by executing the _GraphQLServerMain_ class. Go to the [http://localhost:8180/graphiql](http://localhost:8180/graphiql) URL, then type the GraphQL request:

 
```
query {boards {id name topics {id date title nbPosts }}}
```


# DataFetchersDelegateTopicImpl, DataFetchersDelegateMemberImpl and DataFetchersDelegatePostImpl (including data loaders)

We'll now implement the _DataFetchersDelegateTopicImp_, _DataFetchersDelegatePostImpl_ and _DataFetchersDelegateMemberImpl_ Data Fetcher Delegates.

With this step, __we'll have the full query system implemented__

The objective is to be able to also fetch the author of a _Topic_ like in this GraphQL query:

```
query {boards {id name topics {id date title nbPosts author{id name email}}}}
```

The _DataFetchersDelegateTopicImpl_ allows to navigate from a _Topic_ to its _author_ and its _posts_ as defined in the GraphQL schema.

You'll see the first implementation of a data loader:

```Java
	@Override
	public CompletableFuture<Member> author(DataFetchingEnvironment dataFetchingEnvironment,
			DataLoader<UUID, Member> dataLoader, Topic origin) {
		// TODO Store in cache the Topic (as it has already been read) to avoid the query below
		TopicEntity topic = topicRepository.findById(origin.getId()).get();

		return dataLoader.load(topic.getAuthorId());
	}
```

It allows to query only once the Member table, later, with all the member's ids. 



Here is the full implementation for the _DataFetchersDelegateTopicImpl_ Data Fetcher Delegate:

```Java
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
	public List<Topic> batchLoader(List<UUID> keys, BatchLoaderEnvironment env) {
		Iterable<TopicEntity> topics = topicRepository.findAllById(keys);
		return util.mapList(topics, TopicEntity.class, Topic.class);
	}

}
```

Then the implementation for the _DataFetchersDelegateMemberImpl_

```Java
@Component
public class DataFetchersDelegateMemberImpl implements DataFetchersDelegateMember {

	/** An internal utility to map lists of a type into list of another type */
	@Resource
	private Util util;

	@Resource
	private MemberRepository memberRepository;

	@Override
	public List<Member> batchLoader(List<UUID> keys, BatchLoaderEnvironment env) {
		Iterable<MemberEntity> members = memberRepository.findAllById(keys);
		return util.mapList(members, MemberEntity.class, Member.class);
	}
}
```

You have to note here, that the mapper does a conversion between the type in the database and the Entity (which is a String) and the GraphQL member's type, which is a _MemberType_ as defined in the GraphQL schema.

And the _DataFetchersDelegatePostImpl_ implementation:

```Java
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
```


With these implementation added, you can stop and restart the _GraphQLServerMain_ server, and execute these queries, that also fetches authors and posts:

```
query {boards {id name topics {id date title nbPosts author{id name email}}}}
```


```
query {boards {id name topics {id date title nbPosts author{id name email} posts(since:"2019-01-01"){id title content date}}}}
```


```
query {boards {id name topics {id date title nbPosts author{id name email} posts(since:"2019-01-01"){id title content date author{id name email}}}}}
```


# Implementing the mutations: DataFetchersDelegateSubscriptionTypeImpl

To implement the mutations defined in the GraphQL schema, we just have to implement the method of the _DataFetchersDelegateSubscriptionType_ interface in a Spring Component, like the other Data Fetcher Delegates.

Again, most of the magic is done:
* in the graphql-java framework that maps the mutation request into the relevant Data Fetcher (that is, the relevant method of your _DataFetchersDelegateSubscriptionType_)
* In the Spring repositories that execute the query (insert or update) into the database.

Here it goes:

```Java
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
		PostEntity newPost = new PostEntity();
		newPost.setTopicId(postParam.getTopicId());
		newPost.setAuthorId(postParam.getInput().getAuthorId());
		newPost.setPubliclyAvailable(postParam.getInput().getPubliclyAvailable());
		newPost.setDate(postParam.getInput().getDate());
		newPost.setTitle(postParam.getInput().getTitle());
		newPost.setContent(postParam.getInput().getContent());
		postRepository.save(newPost);
		return mapper.map(newPost, Post.class);
	}

	@Override
	public List<Post> createPosts(DataFetchingEnvironment dataFetchingEnvironment, List<PostInput> spam) {
		// Actually, this mutation is for sample only. We don't want to implement it !
		// :)
		throw new RuntimeException("Spamming is forbidden");
	}
}
```

And hope, you can stop and restart the _GraphQLServerMain_ server, and execute these queries, that create data into the database:


```
mutation {createBoard(name: "a new board's name",publiclyAvailable: true) {id, name, publiclyAvailable}}
```


```
mutation {
  createTopic(topic: {boardId: "00000000-0000-0000-0000-000000000001", input: {authorId: "00000000-0000-0000-0000-000000000001", date: "2020-05-31", publiclyAvailable: true, title: "a new title", content: "the new content"}}) {
    id
    title
    nbPosts
  }
}
```

```
mutation createPost {
  createPost(post: {topicId: "00000000-0000-0000-0000-000000000001", input: {authorId: "00000000-0000-0000-0000-000000000001", date: "2020-05-31", publiclyAvailable: true, title: " A new Post", content: " Some interesting news"}}) {
    id
    title
    content
  }
}
```


# Implementing the Subscriptions: DataFetchersDelegateSubscriptionTypeImpl

Like for queries and mutations, implementing the subscriptions defined in the GraphQL schema is "just" implementing all the Data Fetchers defined in the relevant Data Fetchers Delegate, that is implement all the methods of the _DataFetchersDelegateSubscriptionType_ interface in a Spring Component.

This being said, implementing a subscription is a little more complex, as you also have to provide the information flow that the customer apps will subscribed, with these subscriptions.

To do this, this tutorial uses ReactiveX, as the framework to manage this information flow. 

This GraphQL schema defines one subscription: _subscribeToNewPost_

To implement it, we'll have to: 
* Create a [ReactiveX Subject](http://reactivex.io/documentation/subject.html) that will receive all newly created posts
* Update the _createPost_ mutation, so that every newly created post is sent to this subject
* Implement the  _subscribeToNewPost_ Data Fetcher, so that it receive all posts that are sent to the ReactiveX Subject

Let's go !

We first add the ReactiveX dependency in the Maven pom.xml or Gradle build.gradle file:

The Maven **pom.xml** file:

```XML
	<dependencies>
...
      <dependency>
        <groupId>io.reactivex.rxjava2</groupId>
        <artifactId>rxjava</artifactId>
        <version>2.2.19</version>
      </dependency>
...
	</dependencies>
```

Or the Gradle **build.gradle** file:

```Groovy
dependencies {

...

	implementation 'io.reactivex.rxjava2:rxjava:2.2.19'
}
```

Then, we create the [ReactiveX Subject](http://reactivex.io/documentation/subject.html). To do that, we implement a _PostPublisher_ class in the _org.forum.server.impl_ package:

```Java
package org.forum.server.impl;

import org.forum.server.graphql.Post;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

@Component
public class PostPublisher {

	PublishSubject<Post> subject = PublishSubject.create();

	/**
	 * Let's emit this new {@link Post}
	 * 
	 * @param post
	 */
	void onNext(Post post) {
		subject.onNext(post);
	}

	/**
	 * Let's get a new publisher, for the GraphQL subscription that just occurred
	 * 
	 * @return
	 */
	Publisher<Post> getPublisher(String boardName) {
		return subject.toFlowable(BackpressureStrategy.BUFFER);
	}

}
```

Then, we update the mutation Data Fetcher _createPost()_ so that it sends every new post to this ReactiveX Subject. Below are the changes in the _DataFetchersDelegateMutationTypeImpl_ implementation:

```Java
import io.reactivex.subjects.Subject;

@Component
public class DataFetchersDelegateMutationTypeImpl implements DataFetchersDelegateMutationType {

...
	/**
	 * This {@link Subject} will be notified for each Post creation. This is the basis for the <I>subscribeToNewPost</I>
	 * subscription
	 */
	@Resource
	PostPublisher postPublisher;

...

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
...
}
```

Now, the ReactiveX Subject receives each newly created post. We wire the GraphQL Subscription to the ReactiveX Subject, and we're done:

```Java
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
```


# The end...

Your GraphQL server is now fully implemented.

It's now up to you to update the GraphQL schema and add queries, mutations, subscriptions, objects, interfaces, unions....