package org.smellrefactored;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;

public class CommitRange {
	
	private String localFolder;
	private String initialCommit;
	private String finalCommit;
	
	private ArrayList<CommitData> commitsMergedIntoMaster = new ArrayList<CommitData>();
	private ArrayList<CommitData> commitsNotMergedIntoMaster = new ArrayList<CommitData>();
	
	public CommitRange(String localFolder, String initialCommit, String finalCommit) throws Exception {
		this.localFolder = localFolder;
		this.initialCommit = initialCommit;
		this.finalCommit = finalCommit;
		processRangeOfCommitsFromRepository();
	}

	private void processRangeOfCommitsFromRepository() throws Exception {
		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.openRepository(localFolder);
		Git git = new Git(repo);
		gitService.checkout(repo, finalCommit);
		Iterable<RevCommit> log = git.log().call();
		Iterator<RevCommit> logIterator = log.iterator();
		RevWalk revWalk = new RevWalk(repo, 3);
		RevCommit masterHead = revWalk.parseCommit(repo.resolve("refs/heads/master"));
		boolean inRange = false;
		while (logIterator.hasNext()) {
			RevCommit revCommit = logIterator.next();
			/// if ( (!inRange) && (revCommit.getId().getName().equals(initialCommit)) ) {
				inRange = true;
			/// }
			if (inRange) {
				CommitData commitData = newCommitData(revCommit);
				ObjectId id = repo.resolve(commitData.getId());
				RevCommit otherHead = revWalk.parseCommit(id);
				if (revWalk.isMergedInto(otherHead, masterHead)) {
					if ((otherHead.getParentCount() == 1)
						|| (otherHead.getShortMessage().toUpperCase().contains("MERGE")
								&& otherHead.getShortMessage().toUpperCase().contains("PULL"))) {
						commitsMergedIntoMaster.add(commitData);
					} else {
						commitsNotMergedIntoMaster.add(commitData);
					}
				}
			}
			revWalk.close();
			revWalk.dispose();
		}
		git.close();

		Collections.sort(commitsMergedIntoMaster);
		Collections.sort(commitsNotMergedIntoMaster);

		
		chainOrderedCommitsMergedIntoMaster();
	}

	private CommitData newCommitData(RevCommit revCommit) {
		CommitData commitData = new CommitData();
		commitData.setId(revCommit.getId().getName());
		commitData.setDate(new Date(revCommit.getCommitTime() * 1000L));
		commitData.setAuthorName(revCommit.getAuthorIdent().getName());
		commitData.setAuthorEmail(revCommit.getAuthorIdent().getEmailAddress());
		commitData.setFullMessage(revCommit.getFullMessage());
		commitData.setShortMessage(revCommit.getShortMessage());
		return commitData;
	}
	
	private void chainOrderedCommitsMergedIntoMaster() {
		Iterator<CommitData> it = commitsMergedIntoMaster.iterator();
		CommitData previousCommit = null;
		while (it.hasNext()) {
			CommitData commit = it.next();
			commit.setPrevious(previousCommit);
			if (previousCommit!=null) {
				previousCommit.setNext(commit);
			}
			previousCommit = commit; 
		}
	}

	public ArrayList<CommitData> getCommitsMergedIntoMaster() {
		return (this.commitsMergedIntoMaster);
	}

	public ArrayList<CommitData> getCommitsNotMergedIntoMaster() {
		return (this.commitsNotMergedIntoMaster);
	}

	
	public ArrayList<CommitData> getCommitsMergedIntoMasterByIds(HashSet<String> commitIds) {
		ArrayList<CommitData> result = new ArrayList<CommitData>();
		Iterator<CommitData> it = commitsMergedIntoMaster.iterator();
		while (it.hasNext()) {
			CommitData commit = it.next();
			if (commitIds.contains(commit.getId())) {
				result.add(commit);
			}
		}
		return result;
	}
	
	public CommitData getCommitById(String commitId) throws Exception {
		CommitData result = null;
		for (CommitData commit : commitsMergedIntoMaster) {
			if (commit.getId().equals(commitId)) {
				result = commit;
			}
		}
		if (result == null) {
			throw new Exception("Commit "  + commitId + " not found."); 
		}
		return result;
	}
	
	public CommitData getPreviousCommit(String commitId) throws Exception {
		CommitData result = null;
		CommitData commit = getCommitById(commitId);
		if (commit != null) {
			result = commit.getPrevious();
		} else {
			throw new Exception("Commit "  + commitId + " not found."); 
		}
		if (result == null) {
			throw new Exception("Previous commit for commit "  + commitId + " not found."); 
		}
		if (result.getId() == commitId) {
			throw new Exception("Previous commit for commit "  + commitId + " it is himself."); 
		}
		return result;
	}

	
	public CommitData getNextCommit(String commitId) throws Exception {
		CommitData result = null;
		CommitData commit = getCommitById(commitId);
		if (commit != null) {
			result = commit.getNext();
		} else {
			throw new Exception("Commit "  + commitId + " not found."); 
		}
		// if (result == null) {
		// 	throw new Exception("Next commit for commit "  + commitId + " not found."); 
		// }
		if ( (result != null) && (result.getId() == commitId) ) {
			throw new Exception("Next commit for commit "  + commitId + " it is himself."); 
		}
		return result;
	}

	
	
}
