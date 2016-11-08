package edu.nju.tools.jgit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 文件中的一行（带有提交信息）
 * @author SuZiquan
 *
 */
@Getter
@Setter
@AllArgsConstructor
public class FileLineWithBlame {

	private String content;
	
	private GitBlameInfo blameInfo;

}
