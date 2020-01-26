package org.smellrefactored;

import java.util.Date;

public class CommitData implements Comparable<CommitData> {
	private String id;
	private String shortMessage;
	private String fullMessage;
	private Date date;
	private CommitData previous;
	private CommitData next;

	public String getId() {
		return id;
	}

	public void setId(String idCommit) {
		this.id = idCommit;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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
		return getDate().compareTo(o.getDate());
	}	
}
