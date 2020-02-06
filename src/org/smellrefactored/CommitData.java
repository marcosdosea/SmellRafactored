package org.smellrefactored;

import java.time.ZonedDateTime;

public class CommitData implements Comparable<CommitData> {

	private String id;
	private ZonedDateTime dateTimeUtc;
	private String authorName;
	private String authorEmail;
	private String shortMessage;
	private String fullMessage;
	private CommitData previous;
	private CommitData next;

	public String getId() {
		return id;
	}

	public void setId(String idCommit) {
		this.id = idCommit;
	}

	public ZonedDateTime getDateTimeUtc() {
		return dateTimeUtc;
	}

	public void setDateTimeUtc(ZonedDateTime dateTimeUtc) {
		this.dateTimeUtc = dateTimeUtc;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public void setFullMessage(String fullMessage) {
		this.fullMessage = fullMessage;
	}

	public CommitData getPrevious() {
		return previous;
	}

	public void setPrevious(CommitData previous) {
		this.previous = previous;
	}
	
	public CommitData getNext() {
		return next;
	}

	public void setNext(CommitData next) {
		this.next = next;
	}
	
	public int compareTo(CommitData o) {
		return getDateTimeUtc().compareTo(o.getDateTimeUtc());
	}	
}
