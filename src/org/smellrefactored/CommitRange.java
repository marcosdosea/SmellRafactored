package org.smellrefactored;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommitRange {
	
	private String localFolder;
	private String initialCommitId;
	private String finalCommitId;
	
	private ArrayList<CommitData> commitsMergedIntoMaster = new ArrayList<CommitData>();
	
	static Logger logger = LoggerFactory.getLogger(CommitRange.class);
	
	public CommitRange(String localFolder, String initialCommitId, String finalCommitId) throws Exception {
		this.localFolder = localFolder;
		this.initialCommitId = initialCommitId;
		this.finalCommitId = finalCommitId;
		processRangeOfCommitsFromRepository();
	}

	private void processRangeOfCommitsFromRepository() throws Exception {
		ArrayList<CommitData> commitsMergedIntoMasterTemp = new ArrayList<CommitData>();

		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.openRepository(localFolder);
		Git git = new Git(repo);

		/*
		ArrayList<CommitData> commitsOfBranch = new ArrayList<CommitData>();
		String treeName = "refs/heads/master"; // tag or branch
		for (RevCommit revCommit : git.log().add(repo.resolve(treeName)).call()) {
			commitsOfBranch.add( newCommitData(revCommit) );
		    System.out.println(revCommit.getName());
		}
		*/
		
		gitService.checkout(repo, finalCommitId);

		RevWalk revWalk = new RevWalk(repo, 0);

		List<Ref> branches = git.branchList().call();
		for (Ref branch : branches) {
			if (!branch.getName().equals(Constants.R_HEADS + Constants.MASTER)) {
				continue;
			}
			Iterable<RevCommit> revCommits = git.log().all().call();
			for (RevCommit revCommit : revCommits) {
				boolean isMergedIntoMaster = false;
				ObjectId commitObjectId = repo.resolve(revCommit.getName());
				RevCommit parsedCommit = revWalk.parseCommit(commitObjectId);
				for (Map.Entry<String, Ref> entry : repo.getAllRefs().entrySet()) {
					if (entry.getKey().startsWith(Constants.R_HEADS)) {
						ObjectId headsObjectId = entry.getValue().getObjectId();
						RevCommit headsCommit = revWalk.parseCommit(headsObjectId);
						if (revWalk.isMergedInto(parsedCommit, headsCommit)) {
							if (entry.getValue().getName().equals(branch.getName())) {
								isMergedIntoMaster = true;
								break;
							}
						}
					}
				}
				if ( (!isMergedIntoMaster) && (revCommit.getId().getName().equals(initialCommitId)) ) {
					logger.error("Initial commit was not automatically added in commit range.");
					isMergedIntoMaster = true;
				}
				if ( (!isMergedIntoMaster) && (revCommit.getId().getName().equals(finalCommitId)) ) {
					logger.error("Final commit was not automatically added in commit range.");
					isMergedIntoMaster = true;
				}
				CommitData commitData = newCommitData(revCommit);
	            if (isMergedIntoMaster) {    
	            	commitsMergedIntoMasterTemp.add(commitData);
	            }
			}
		}
		revWalk.close();
		revWalk.dispose();
		git.close();

		Collections.sort(commitsMergedIntoMasterTemp);
		commitsMergedIntoMaster = getSegmentFromOrderedCommit(commitsMergedIntoMasterTemp, initialCommitId, finalCommitId);
		chainOrderedCommits(commitsMergedIntoMaster);

		// Checking inclusion of extreme commits in the list of commits.
		if (getCommitById(initialCommitId) == null) {
			throw new Exception("Initial commit "  + initialCommitId + " not found."); 
		}
		if (getCommitById(finalCommitId) == null) {
			throw new Exception("final commit "  + finalCommitId + " not found."); 
		}
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
	
	private ArrayList<CommitData> getSegmentFromOrderedCommit(ArrayList<CommitData> orderedCommits, String startCommitId, String endCommitId) {
		ArrayList<CommitData> result = new ArrayList<CommitData>();
		boolean inRange = false;
		for (CommitData commit: orderedCommits) {
			if ( (!inRange) && (commit.getId().equals(startCommitId)) ) {
				inRange = true; 
			}
			if (inRange) {
				result.add(commit);
			}
			if ( (inRange) && (commit.getId().equals(endCommitId)) ) {
				inRange = false;
				break;
			}
		}
		return (result);
	}

	private void chainOrderedCommits(ArrayList<CommitData> orderedCommits) {
		Iterator<CommitData> it = orderedCommits.iterator();
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
			if (commitId.equals(initialCommitId)) {
				throw new Exception("Initial commit "  + commitId + " not found."); 
			} else if (commitId.equals(finalCommitId)) {
				throw new Exception("Final commit "  + commitId + " not found."); 
			} else {
				throw new Exception("Commit "  + commitId + " not found."); 
			}
		}
		return result;
	}
	
	public CommitData getPreviousCommit(String commitId) throws Exception {
		CommitData result = null;
		CommitData commit = getCommitById(commitId);
		if (commit != null) {
			result = commit.getPrevious();
			if ( (result == null) && (!commit.getId().equals(initialCommitId)) ) {
				throw new Exception("Previous commit for commit "  + commitId + " not found."); 
			}
			if ( (result != null) && (result.getId().equals(commitId)) ) {
				throw new Exception("Previous commit for commit "  + commitId + " it is himself."); 
			}
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
		if ( (result == null) && (!commit.getId().equals(finalCommitId)) ) {
		 	throw new Exception("Next commit for commit "  + commitId + " not found."); 
		}
		if ( (result != null) && (result.getId().equals(commitId)) ) {
			throw new Exception("Next commit for commit "  + commitId + " it is himself."); 
		}
		return result;
	}

	public ArrayList<String> getIds() {
		ArrayList<String> result = new ArrayList<String>();
		for (CommitData commit : commitsMergedIntoMaster) {
			result.add(commit.getId());
		}
		return (result);
	}

	public int size() {
		return (commitsMergedIntoMaster.size());
	}
	
}
