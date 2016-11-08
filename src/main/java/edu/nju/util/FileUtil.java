package edu.nju.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件工具类
 * 
 * @author SuZiquan
 *
 */
public class FileUtil {

	/**
	 * 将文件路径中的分隔符进行统一
	 * @param path
	 * @return
	 */
	public static String uniformPathSeparator(String path) {
		String result = path;
		String[] separators = { "\\", "//", "/" };
		for (String separator : separators) {
			if (!File.separator.equals(separator)) {
				result = result.replace(separator, File.separator);
			}
		}
		return result;
	}

	/**
	 * 拼接文件路径
	 * 
	 * @param parts
	 * @return
	 */
	public static String jointPath(String... parts) {
		String path = "";
		for (String part : parts) {
			String toJoint = uniformPathSeparator(part);
			if (toJoint.startsWith(File.separator)) {
				toJoint = toJoint.substring(1, toJoint.length());
			}
			if (!path.endsWith(File.separator) && !path.trim().equals("")) {
				path += File.separator;
			}
			path = path + toJoint;
		}
		return path;
	}

	/**
	 * 读取某个文件的内容
	 * @param filePath
	 * @return
	 */
	public static String read(String filePath) {
		File file = new File(filePath);
		return read(file);
	}

	/**
	 * 读取某个文件的内容
	 * @param file
	 * @return
	 */
	public static String read(File file) {
		String content = "";
		BufferedReader bufferedReader = null;
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "utf-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				content += line;
				content += System.getProperty("line.separator");
			}
		} catch (Exception e) {
		} finally {
			if (bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return content;
	}

	/**
	 * 查找某个目录下所有的Java文件
	 * @param srcDir
	 * @return
	 */
	public static Map<String, String> getJavaSources(String srcDir) {
		Map<String, String> javaSources = new HashMap<>();
		travelJavaSourceDir(srcDir, javaSources);
		return javaSources;
	}

	/**
	 * 递归查找Java文件
	 * @param path
	 * @param javaSources
	 */
	private static void travelJavaSourceDir(String path, Map<String, String> javaSources) {
		File file = new File(path);
		if (file.exists()) {
			File[] files = file.listFiles();
			for (File file2 : files) {
				if (file2.isDirectory()) {
					travelJavaSourceDir(file2.getAbsolutePath(), javaSources);
				} else {
					String fileName = file2.getName();
					if (fileName.endsWith(".java")) {
						String fileContent = FileUtil.read(file2);
						javaSources.put(fileName, fileContent);
					}
				}
			}
		}
	}
}
