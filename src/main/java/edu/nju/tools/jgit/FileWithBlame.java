package edu.nju.tools.jgit;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 文件内容（带有每行的提交信息）
 * 
 * @author SuZiquan
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileWithBlame {

	/**
	 * 文件在Git项目中的相对路径
	 */
	private String relativePath;

	/**
	 * 文件内容（带有每行的提交信息）
	 */
	private List<FileLineWithBlame> contents;

	/**
	 * 添加一行
	 * 
	 * @param line
	 *            文件中的一行（带有提交信息）
	 */
	public void addLine(FileLineWithBlame line) {
		if (contents == null) {
			contents = new ArrayList<>();
		}
		contents.add(line);
	}

}
