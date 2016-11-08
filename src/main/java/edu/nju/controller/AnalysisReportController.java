package edu.nju.controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.nju.entity.Metric;
import edu.nju.entity.Project;
import edu.nju.entity.Task;
import edu.nju.entity.Testcase;
import edu.nju.repository.TaskRepository;
import edu.nju.tools.jgit.JGitHelper;
import edu.nju.util.DateUtil;
import edu.nju.util.FileUtil;
import sun.misc.BASE64Encoder;

/**
 * 对项目的每次提交进行分析，获得报告
 * 
 * @author SuZiquan
 *
 */
@SuppressWarnings("restriction")
@Controller
public class AnalysisReportController {

	@Autowired
	private TaskRepository taskRepository;

	/**
	 * 展示Analysis Report
	 * @param gitUrl
	 * @param modelMap
	 * @return
	 */
	@RequestMapping("/analysisReport")
	public String analysisReport(String gitUrl, ModelMap modelMap) {
		modelMap.put("gitUrl", gitUrl);

		Task task = taskRepository.findByGitUrl(gitUrl);
		List<Project> projects = task.getProjects();
		// 按照时间逆向排序
		projects.sort((a, b) -> {
			return a.getGitCommitDate().before(b.getGitCommitDate()) ? 1 : -1;
		});

		//针对考试的情况，这里假定所有版本的测试用例都是一样的
		List<Testcase> commonTestcases = projects.get(0).getTestcases();
		List<String> unitTestNames = new ArrayList<>();
		for (Testcase commonTestcase : commonTestcases) {
			unitTestNames.add(commonTestcase.getName());
		}

		List<Map<String, Object>> unitTestResults = new ArrayList<>();

		for (Project project : projects) {
			Map<String, Object> unitTestResult = new HashMap<>();
			unitTestResults.add(unitTestResult);

			unitTestResult.put("commitId", project.getGitCommitId());
			unitTestResult.put("commitDate", DateUtil.getData(project.getGitCommitDate()));
			unitTestResult.put("commitTime", DateUtil.getTime(project.getGitCommitDate()));
			unitTestResult.put("compileFailed", !project.isCompileSuccess());

			if (!project.isCompileSuccess())
				continue;

			List<Testcase> testcases = project.getTestcases();

			List<String> testResultList = new ArrayList<>();
			for (String unitTestName : unitTestNames) {
				boolean exist = false;
				for (Testcase testcase : testcases) {
					if (testcase.getName().equals(unitTestName)) {
						exist = true;
						testResultList.add(testcase.getResult());
					}
				}
				if (!exist) {
					testResultList.add("");
				}
			}
			unitTestResult.put("testResults", testResultList);
		}

		modelMap.put("unitTestNames", unitTestNames);
		modelMap.put("unitTestResults", unitTestResults);
		return "analyse_report";
	}

	/**
	 * 提供关于项目度量值变化的图表数据
	 * @param gitUrl
	 * @return
	 */
	@RequestMapping("getChartData")
	@ResponseBody
	public Map<String, Object> getChartData(String gitUrl) {

		Task task = taskRepository.findByGitUrl(gitUrl);
		List<Project> projects = task.getProjects();
		projects.sort((a, b) -> {
			return a.getGitCommitDate().before(b.getGitCommitDate()) ? -1 : 1;
		});

		List<String> commits = new ArrayList<>();
		List<Integer> totalLineCount = new ArrayList<>();
		List<Integer> commentLineCount = new ArrayList<>();
		List<Integer> fieldCount = new ArrayList<>();
		List<Integer> methodCount = new ArrayList<>();
		List<Integer> maxCoc = new ArrayList<>();

		for (Project project : projects) {
			commits.add(
					DateUtil.getData(project.getGitCommitDate()) + " " + DateUtil.getTime(project.getGitCommitDate()));
			Metric metric = project.getMetric();
			totalLineCount.add(metric.getTotalLineCount());
			commentLineCount.add(metric.getCommentLineCount());
			fieldCount.add(metric.getFieldCount());
			methodCount.add(metric.getMethodCount());
			maxCoc.add(metric.getMaxCoc());

		}
		Map<String, Object> chartData = new HashMap<>();

		chartData.put("commits", commits);
		chartData.put("totalLineCount", totalLineCount);
		chartData.put("commentLineCount", commentLineCount);
		chartData.put("fieldCount", fieldCount);
		chartData.put("methodCount", methodCount);
		chartData.put("maxCoc", maxCoc);

		return chartData;
	}

	/**
	 * 获得项目的提交信息
	 * @param gitUrl
	 * @return
	 */
	@RequestMapping("getCommitData")
	@ResponseBody
	public List<Map<String, Object>> getCommitData(String gitUrl) {
		Task task = taskRepository.findByGitUrl(gitUrl);
		List<Project> projects = task.getProjects();
		projects.sort((a, b) -> {
			return a.getGitCommitDate().before(b.getGitCommitDate()) ? -1 : 1;
		});
		List<Map<String, Object>> commitData = new ArrayList<>();
		for (Project project : projects) {
			Map<String, Object> commitInfo = new HashMap<>();
			commitInfo.put("commitId", project.getGitCommitId());
			commitData.add(commitInfo);
		}
		return commitData;
	}

	/**
	 * 读取在指定的Git版本中某个Java文件的内容
	 * @param gitUrl
	 * @param fileNodeId
	 * @param commitId
	 * @return
	 */
	@RequestMapping("getJavaContent")
	public @ResponseBody Map<String, Object> getFileContent(String gitUrl, String fileNodeId, String commitId) {
		Task task = taskRepository.findByGitUrl(gitUrl);
		String localUrl = task.getLocalUrl();
		JGitHelper gitHelper = new JGitHelper(localUrl);

		fileNodeId = fileNodeId.replace("\\", "/");
		if (fileNodeId.startsWith("/")) {
			fileNodeId = fileNodeId.substring(1);
		}

		String content = null;
		boolean exists = true;
		try {
			content = gitHelper.getContentWithFile(commitId, fileNodeId);
			//将Java代码中的Html特殊字符进行转义处理，防止在页面上显示出错
			content = StringEscapeUtils.escapeHtml(content);
		} catch (Exception e) {
			exists = false;
		}
		Map<String, Object> result = new HashMap<>();
		result.put("exists", exists);
		result.put("content", content);
		return result;
	}

	/**
	 * 获得某个文件/文件夹的内容
	 * @param gitUrl
	 * @param id
	 * @return
	 */
	@RequestMapping("/getFileNode")
	public @ResponseBody List<Map<String, Object>> getFileNode(String gitUrl, String id) {
		List<Map<String, Object>> res = new ArrayList<>();

		if (id == null || id.equals("#")) {
			id = "";
		}

		Task task = taskRepository.findByGitUrl(gitUrl);
		String localUrl = task.getLocalUrl();
		String path = FileUtil.jointPath(localUrl, id.replace("/", File.separator));
		File dir = new File(path);
		File[] files = dir.listFiles();

		String mainDirPath = FileUtil.jointPath("src", "main", "java");
		String testDirPath = FileUtil.jointPath("src", "test", "java");

		//过滤掉除主代码目录和测试代码目录的其他目录
		if (path.endsWith(mainDirPath) || path.endsWith(testDirPath)) {
			List<String> packagePathList = new ArrayList<>();
			resolvePackage(dir, packagePathList);

			for (String packagePath : packagePathList) {
				Map<String, Object> fileNode = new HashMap<>();
				String relativePath = packagePath.substring(packagePath.indexOf("java") + 5);
				String packageName = relativePath.replace(File.separator, ".");
				fileNode.put("text", packageName);
				fileNode.put("children", true);
				fileNode.put("id", id + "/" + relativePath);
				fileNode.put("icon", "folder");
				Map<String, Boolean> state = new HashMap<>();
				state.put("opened", true);
				fileNode.put("state", state);
				res.add(fileNode);
			}
		} else {

			boolean inPackage = path.contains(mainDirPath) || path.contains(testDirPath);
			for (File file : files) {
				String fileName = file.getName();
				Map<String, Object> fileNode = new HashMap<>();
				if (fileName.equals(".") || fileName.equals(".."))
					;
				if (fileName.startsWith(".") || fileName.equals("target")) {
					continue;
				}
				Map<String, Boolean> state = new HashMap<>();
				state.put("opened", true);
				fileNode.put("state", state);
				if (file.isDirectory()) {
					if (inPackage) {
						continue;
					}
					fileNode.put("text", file.getName());
					fileNode.put("children", true);
					fileNode.put("id", id + "/" + fileName);
					fileNode.put("icon", "folder");
				} else {
					fileNode.put("text", file.getName());
					fileNode.put("children", false);
					fileNode.put("id", id + "/" + fileName);
					fileNode.put("type", "file");

					if (fileName.lastIndexOf('.') != -1) {
						String ext = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
						fileNode.put("icon", "file file-" + ext);
					} else {
						fileNode.put("icon", "file");
					}

				}
				res.add(fileNode);
			}
		}

		if (id.equals("")) {
			List<Map<String, Object>> withRoot = new ArrayList<>();
			Map<String, Object> rootNode = new HashMap<>();
			File root = new File(localUrl);
			rootNode.put("text", root.getName());
			rootNode.put("children", res);
			rootNode.put("id", "");

			Map<String, Boolean> state = new HashMap<>();
			state.put("opened", true);
			rootNode.put("state", state);

			withRoot.add(rootNode);
			return withRoot;
		}

		return res;
	}

	/**
	 * 获取某个文件的内容
	 * @param gitUrl
	 * @param id
	 * @return
	 */
	@RequestMapping("/getFileContent")
	public @ResponseBody Map<String, Object> getFileContent(String gitUrl, String id) {
		Map<String, Object> fileContent = new HashMap<>();

		Task task = taskRepository.findByGitUrl(gitUrl);
		String localUrl = task.getLocalUrl();

		if (id == null || id.equals("#")) {
			id = "";
		}

		if (id.contains(":")) {
			fileContent.put("type", "multiple");
			fileContent.put("content", "");
			return fileContent;
		}

		String path = FileUtil.jointPath(localUrl, id.replace("/", File.separator));
		File file = new File(path);
		if (file.isDirectory()) {
			fileContent.put("type", "folder");
			fileContent.put("content", "");
			return fileContent;
		}

		String fileName = file.getName();
		String content = null;
		String ext = "";
		if (fileName.lastIndexOf('.') != -1) {
			ext = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length());
		}

		switch (ext) {
		case "jpg":
		case "jpeg":
		case "gif":
		case "png":
		case "bmp":
			String mime = null;
			try {
				mime = Files.probeContentType(Paths.get(path));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			String base64 = image2base64(file, ext);
			if (mime != null && base64 != null) {
				content = "data:" + mime + ";base64," + base64;
			}
			break;
		case "txt":
		case "text":
		case "md":
		case "js":
		case "json":
		case "css":
		case "html":
		case "htm":
		case "xml":
		case "c":
		case "cpp":
		case "h":
		case "sql":
		case "log":
		case "py":
		case "rb":
		case "htaccess":
		case "php":
		case "java":
		case "":
			StringBuilder builder = new StringBuilder();
			BufferedReader reader;
			String line;

			try {
				InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "utf-8");
				reader = new BufferedReader(inputStreamReader);
				while ((line = reader.readLine()) != null) {
					builder.append(line + "\r\n");
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			content = builder.toString();
			break;
		default:
			content = "File not recognized:" + fileName;
			break;
		}
		fileContent.put("type", ext);
		fileContent.put("content", content);

		return fileContent;
	}

	/**
	 * 将图片文件以Base64编码转字符串
	 * @param imageFile
	 * @param type
	 * @return
	 */
	public static String image2base64(File imageFile, String type) {
		String base64String = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			BufferedImage image = ImageIO.read(imageFile);
			ImageIO.write(image, type, bos);
			byte[] imageBytes = bos.toByteArray();
			BASE64Encoder encoder = new BASE64Encoder();
			base64String = encoder.encode(imageBytes);
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return base64String;
	}

	/**
	 * 从目录结构中解析出项目各个包名
	 * @param dir
	 * @param packageList
	 */
	private static void resolvePackage(File dir, List<String> packageList) {
		if (hasFile(dir) || dir.listFiles().length == 0) {
			packageList.add(dir.getPath());
		}
		for (File file : dir.listFiles())
			if (file.isDirectory())
				resolvePackage(file, packageList);
	}

	/**
	 * 某个目录下是否包含文件
	 * @param dir
	 * @return
	 */
	private static boolean hasFile(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isFile())
				return true;
		}
		return false;
	}

}
