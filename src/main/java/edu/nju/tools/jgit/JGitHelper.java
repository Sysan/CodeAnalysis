package edu.nju.tools.jgit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import lombok.Getter;

/**
 * Git工具类
 * 
 * @author SuZiquan
 *
 */
public class JGitHelper {

	private Git git;

	/**
	 * 按时间逆序
	 */
	@Getter
	private List<GitCommitInfo> commitInfos = new ArrayList<>();

	public JGitHelper(final String localUrl) {
		try {
			git = Git.open(new File(localUrl));
		} catch (Exception e) {
			e.printStackTrace();
		}
		init();
	}

	public JGitHelper(final String remoteUrl, final String localUrl) {
		File dstDir = new File(localUrl);
		if (dstDir.exists()) {
			try {
				git = Git.open(dstDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			dstDir.mkdirs();
			try {
				git = Git.cloneRepository().setURI(remoteUrl).setDirectory(dstDir).call();
			} catch (GitAPIException e) {
				e.printStackTrace();
			}
		}
		init();
	}

	private void init() {
		if (git == null)
			return;
		try {
			Iterator<RevCommit> iterator = git.log().call().iterator();
			while (iterator.hasNext()) {
				RevCommit commit = iterator.next();
				String commitId = commit.getId().getName();
				long seconds = commit.getCommitTime();
				long milliseconds = seconds * 1000;
				Date commitDate = new Date(milliseconds);
				String fullMessage = commit.getFullMessage();
				GitCommitInfo commitInfo = new GitCommitInfo(commitId, commitDate, fullMessage);
				commitInfos.add(commitInfo);
			}
		} catch (GitAPIException e) {
			e.printStackTrace();
		}

		commitInfos.sort((commit1, commit2) -> commit2.getCommitDate().compareTo(commit1.getCommitDate()));
	}

	public void checkout(String commitId) {
		String revStr = (commitId == null) ? getLastestCommitId() : commitId;
		try {
			git.checkout().setName(revStr).call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GitCommitInfo getLastestCommitInfo() {
		if (commitInfos.isEmpty())
			return null;
		return commitInfos.get(0);
	}

	public String getLastestCommitId() {
		GitCommitInfo lastestCommitInfo = getLastestCommitInfo();
		if (lastestCommitInfo == null)
			return null;
		return lastestCommitInfo.getCommitId();
	}

	public String getCurrentCommitId() {
		try {
			Iterator<RevCommit> iterator = git.log().call().iterator();
			if (iterator.hasNext()) {
				RevCommit commit = iterator.next();
				String commitId = commit.getId().getName();
				return commitId;
			}
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, String> getChildrenWithDir(final String commitId, final String dirPath) throws Exception {

		String revStr = (commitId == null) ? getLastestCommitId() : commitId;

		Map<String, String> children = new HashMap<>();

		Repository repository = git.getRepository();

		RevWalk revWalk = new RevWalk(repository);
		ObjectId objId = repository.resolve(revStr);
		RevCommit revCommit = revWalk.parseCommit(objId);
		RevTree revTree = revCommit.getTree();

		TreeWalk dirTreeWalk = null;

		if (dirPath == null) {
			dirTreeWalk = new TreeWalk(repository);
			dirTreeWalk.addTree(revTree);
		} else {
			TreeWalk treeWalk = TreeWalk.forPath(repository, dirPath, revTree);
			ObjectId dirTreeId = treeWalk.getObjectId(0);
			RevTree dirTree = revWalk.parseTree(dirTreeId);
			dirTreeWalk = new TreeWalk(repository);
			dirTreeWalk.addTree(dirTree);
		}

		while (dirTreeWalk.next()) {
			ObjectId entryId = dirTreeWalk.getObjectId(0);
			ObjectLoader entryLoader = repository.open(entryId);
			String entryName = dirTreeWalk.getNameString();
			int entryType = entryLoader.getType();

			String prefix = "";

			if (dirPath != null) {
				prefix += dirPath + (dirPath.endsWith("/") ? "" : "/");
			}

			switch (entryType) {
			case Constants.OBJ_TREE:
				children.put(prefix + entryName, "folder");
				break;
			case Constants.OBJ_BLOB:
				children.put(prefix + entryName, "file");
				break;
			}
		}

		revWalk.close();
		dirTreeWalk.close();

		return children;
	}

	public String getContentWithFile(final String commitId, final String filePath) throws Exception {

		String revStr = (commitId == null) ? getLastestCommitId() : commitId;

		Repository repository = git.getRepository();

		RevWalk revWalk = new RevWalk(repository);

		ObjectId objId = git.getRepository().resolve(revStr);
		RevCommit revCommit = revWalk.parseCommit(objId);
		RevTree revTree = revCommit.getTree();

		TreeWalk treeWalk = TreeWalk.forPath(repository, filePath, revTree);

		ObjectId blobId = treeWalk.getObjectId(0);
		ObjectLoader loader = repository.open(blobId);

		byte[] bytes = loader.getBytes();

		revWalk.close();
		treeWalk.close();

		if (bytes != null)
			return new String(bytes, Charset.forName("utf-8"));

		return null;
	}

	public FileWithBlame blameContentWithFile(final String filePath) throws Exception {
		FileWithBlame fileWithBlame = new FileWithBlame();
		fileWithBlame.setRelativePath(filePath);
		BlameResult blameResult = null;
		try {
			blameResult = git.blame().setTextComparator(RawTextComparator.WS_IGNORE_ALL).setFilePath(filePath).call();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < blameResult.getResultContents().size(); i++) {
			String authorName = blameResult.getSourceAuthor(i).getName();
			String authorEmail = blameResult.getSourceAuthor(i).getEmailAddress();
			Date commitDate = blameResult.getSourceCommitter(i).getWhen();
			String content = blameResult.getResultContents().getString(i);
			GitBlameInfo blameInfo = new GitBlameInfo(authorName, authorEmail, commitDate);
			FileLineWithBlame lineWithBlame = new FileLineWithBlame(content, blameInfo);
			fileWithBlame.addLine(lineWithBlame);
		}

		return fileWithBlame;
	}

}
