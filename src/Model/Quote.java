package Model;

import java.util.Date;

public class Quote {
	String content;
	String user;
	String name; // unique id
	String author;
	int points;
	Date date;
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	@Override
	public String toString() {
		return "Quote [content=" + content + ", user=" + user + ", name="
				+ name + ", author=" + author + ", points=" + points
				+ ", date=" + date + "]";
	}
}
