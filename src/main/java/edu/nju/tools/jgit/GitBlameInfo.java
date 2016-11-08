package edu.nju.tools.jgit;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件每一行的提交信息
 * 
 * @author SuZiquan
 *
 */
@Getter
@Setter
@AllArgsConstructor
public class GitBlameInfo {
	
	private String authorName;
	
	private String authorEmail;
	
	private Date commitDate;

}
