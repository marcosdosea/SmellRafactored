package org.smellrefactored;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
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
	
	private String repositoryUrl;
	private String repositoryPath;
	private String initialCommitId;
	private String finalCommitId;
	
	private LinkedHashMap<String, CommitData> commits = new LinkedHashMap<String, CommitData>();

	
	static Logger logger = LoggerFactory.getLogger(CommitRange.class);
	
	public CommitRange(String repositoryUrl, String repositoryPath, String initialCommitId, String finalCommitId) throws Exception {
		this.repositoryUrl = repositoryUrl;
		this.repositoryPath = repositoryPath;
		this.initialCommitId = initialCommitId;
		this.finalCommitId = finalCommitId;
		processRangeOfCommitsFromRepository();
	}

	private void processRangeOfCommitsFromRepository() throws Exception {
		GitService gitService = new GitServiceImpl();
		gitService.cloneIfNotExists(repositoryPath, repositoryUrl);

		Repository repo = gitService.openRepository(repositoryPath);
		
		// commits = getCommitsOfRangeV2(gitService, repo);
		// commits = getCommitsOfRangeV1Dosea(gitService, repo);
		commits = getCommitsOfRangeV3(gitService, repo);

		chainCommits(commits);
		
		// Checking inclusion of extreme commits in the list of commits.
		if (getCommitById(initialCommitId) == null) {
			throw new Exception("Initial commit "  + initialCommitId + " not found."); 
		}
		if (getCommitById(finalCommitId) == null) {
			throw new Exception("final commit "  + finalCommitId + " not found."); 
		}
	}

	private LinkedHashMap<String, CommitData> getCommitsOfRangeV3(GitService gitService, Repository repo) throws Exception {
		LinkedHashMap<String, CommitData> result = new LinkedHashMap<String, CommitData>();
		gitService.checkout(repo, finalCommitId);
	    Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repo, this.initialCommitId, this.finalCommitId);
	    Iterator<RevCommit> walkIterator = walk.iterator();
	    while (walkIterator.hasNext()) {
	        RevCommit commit = walkIterator.next();
	        try (RevWalk revWalk = new RevWalk(repo, 0)) {
	        	try {
	            	for (RevCommit parentCommit : commit.getParents()) {
	    			      if (parentCommit.getId().getName().equals(this.initialCommitId)) {
	    			        // logger.info("Adding initial commit: " + parentCommit.getId().getName());
	    			        CommitData parentCommitData = getNewCommitDataFromRevCommit(parentCommit);
	    		           	result.put(parentCommitData.getId(), parentCommitData);
	    			        // logger.info("OK: Adding initial commit: " + parentCommit.getId().getName());
	    			      }
	  	            }
				} finally {
	            	revWalk.close();
	            	revWalk.dispose();
				}
		    }
	        // logger.info(commit.getId().getName());
			CommitData commitData = getNewCommitDataFromRevCommit(commit);
           	result.put(commitData.getId(), commitData);
	    }
	    
		if ( (!result.containsKey(initialCommitId)) ) {
			logger.error("Initial commit was not automatically added in commit range.");
			CommitData commitData = getDetachedCommitByIdFromRepository(repo, initialCommitId);
           	result.put(commitData.getId(), commitData);
		}
		if ( (!result.containsKey(finalCommitId)) ) {
			logger.error("Final commit was not automatically added in commit range.");
			CommitData commitData = getDetachedCommitByIdFromRepository(repo, finalCommitId);
           	result.put(commitData.getId(), commitData);
		}
	    return (result);
	}
	
	private CommitData getDetachedCommitByIdFromRepository(Repository repo, String commitId) throws Exception {
		CommitData result = null;
		ObjectId commitObjectId = repo.resolve(commitId);
		try (RevWalk revWalk = new RevWalk(repo, 0)) {
			RevCommit commit = revWalk.parseCommit(commitObjectId);
			CommitData commitData = getNewCommitDataFromRevCommit(commit);
			result = commitData;
			}
		if (result == null) {
			throw new Exception("Commit " + commitId + " not found in repository.");
		}
		return (result);
	}
	
	public void addDetachedCommitById(String commitId) throws Exception {
		logger.info("Adding detached commit " + commitId + " to the commits range.");
		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.openRepository(repositoryPath);
		CommitData commitData = getDetachedCommitByIdFromRepository(repo, commitId);
		commits.put(commitId, commitData);
		chainCommits(commits);
	}
	


	private LinkedHashMap<String, CommitData> getCommitsOfRangeV2(GitService gitService, Repository repo)
			throws Exception, GitAPIException, NoHeadException, IOException, AmbiguousObjectException,
			IncorrectObjectTypeException, MissingObjectException {
		Git git = new Git(repo);

		LinkedHashMap<String, CommitData> result = new LinkedHashMap<String, CommitData>();
		
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
				CommitData commitData = getNewCommitDataFromRevCommit(revCommit);
	            if (isMergedIntoMaster) {    
	               	result.put(commitData.getId(), commitData);
	            }
			}
		}
		revWalk.close();
		revWalk.dispose();
		git.close();
		return result;
	}
	

	private LinkedHashMap<String, CommitData> getCommitsOfRangeV1Dosea(GitService gitService, Repository repo) throws Exception  {
		Git git = new Git(repo);
		LinkedHashMap<String, CommitData> result = new LinkedHashMap<String, CommitData>();

		gitService.checkout(repo, finalCommitId);

		Iterable<RevCommit> log = git.log().call();
		Iterator<RevCommit> logIterator = log.iterator();
		RevWalk revWalk = new RevWalk(repo, 3);
		RevCommit masterHead = revWalk.parseCommit(repo.resolve("refs/heads/master"));
		while (logIterator.hasNext()) {
			RevCommit currentCommit = logIterator.next();
			CommitData commitData = getNewCommitDataFromRevCommit(currentCommit);
			ObjectId id = repo.resolve(commitData.getId());
			RevCommit otherHead = revWalk.parseCommit(id);
			if (revWalk.isMergedInto(otherHead, masterHead)) {
				if ((otherHead.getParentCount() == 1)
						|| (otherHead.getShortMessage().toUpperCase().contains("MERGE")
								&& otherHead.getShortMessage().toUpperCase().contains("PULL"))) {
		           	result.put(commitData.getId(), commitData);
				}
			}
			revWalk.close();
			revWalk.dispose();
		}
		git.close();	
		return (result);
	}
		
		
	private CommitData getNewCommitDataFromRevCommit(RevCommit revCommit) {
		CommitData commitData = new CommitData();
		commitData.setId(revCommit.getId().getName());
		commitData.setDateTimeUtc(ZonedDateTime.ofInstant(Instant.ofEpochSecond(revCommit.getCommitTime()), ZoneOffset.UTC));
		try {
			commitData.setAuthorName(revCommit.getAuthorIdent().getName());
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			commitData.setAuthorEmail(revCommit.getAuthorIdent().getEmailAddress());
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			commitData.setFullMessage(revCommit.getFullMessage());
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			commitData.setShortMessage(revCommit.getShortMessage());
		} catch (Exception e) {
			// TODO: handle exception
		}
		return commitData;
	}
	
	private static void chainCommits(LinkedHashMap<String, CommitData> commitsToChain) {
		ArrayList<CommitData> commitList = new ArrayList<CommitData>(commitsToChain.values());
		Collections.sort(commitList);
		chainOrderedCommits(commitList);
	}

	private static void chainOrderedCommits(ArrayList<CommitData> orderedCommits) {
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
	
	public boolean exists(String commitId) {
		return (commits.containsKey(commitId));
	}

	public CommitData getCommitByIdAddingIfNotExists(String commitId) throws Exception {
		CommitData result = commits.get(commitId);
		if (result == null) {
			addDetachedCommitById(commitId);
			result = commits.get(commitId);
		}	
		return (result);
	}
	
	public CommitData getCommitById(String commitId) throws Exception {
		CommitData result = commits.get(commitId);
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

	public ArrayList<String> getIds() throws Exception {
		ArrayList<String> result = new ArrayList<String>();
		CommitData commit = getCommitById(this.initialCommitId);
		result.add(commit.getId());
		while (commit.getNext() != null) {
			commit = commit.getNext();
			result.add(commit.getId());
		}
		return (result);
	}

	public int size() {
		return (commits.size());
	}
	
	public String getRepositoryUrl() {
		return (this.repositoryUrl);
	}
	
	public String getRepositoryPath() {
		return (this.repositoryPath);
	}

	public String getInitialCommitId() {
		return (this.initialCommitId);
	}
	
	public String getFinalCommitId() {
		return (this.finalCommitId);
	}

	public String getCommitDateAsString(String commitId) {
		String result = null;
		CommitData commit = null;
		if (commitId != null) {
			try {
				commit = this.getCommitById(commitId);
			} catch (Exception e) {
				// do nothing
			}
		}
		if (commit != null) {
			result = String.valueOf(commit.getDateTimeUtc());
		}		
		return (result);
	}
	
}
