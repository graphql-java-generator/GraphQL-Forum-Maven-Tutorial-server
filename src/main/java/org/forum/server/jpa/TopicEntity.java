/** Generated by the default template from graphql-java-generator */
package org.forum.server.jpa;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

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

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Boolean getPubliclyAvailable() {
		return publiclyAvailable;
	}

	public void setPubliclyAvailable(Boolean publiclyAvailable) {
		this.publiclyAvailable = publiclyAvailable;
	}

	public Integer getNbPosts() {
		return nbPosts;
	}

	public void setNbPosts(Integer nbPosts) {
		this.nbPosts = nbPosts;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<PostEntity> getPosts() {
		return posts;
	}

	public void setPosts(List<PostEntity> posts) {
		this.posts = posts;
	}

	public UUID getBoardId() {
		return boardId;
	}

	public void setBoardId(UUID boardId) {
		this.boardId = boardId;
	}

	public UUID getAuthorId() {
		return authorId;
	}

	public void setAuthorId(UUID authorId) {
		this.authorId = authorId;
	}

}
