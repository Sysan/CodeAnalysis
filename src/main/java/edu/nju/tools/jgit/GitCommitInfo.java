package edu.nju.tools.jgit;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Git提交信息
 * 
 * @author SuZiquan
 *
 */
@Getter
@Setter
@AllArgsConstructor
public class GitCommitInfo {

	private String commitId;
	
	private Date commitDate;
	
	private String fullMessage;

}
