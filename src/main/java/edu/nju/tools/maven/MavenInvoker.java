package edu.nju.tools.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationOutputHandler;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import edu.nju.config.DirectoryConfig;
import edu.nju.entity.Testcase;
import edu.nju.util.FileUtil;

/**
 * 执行Maven命令
 * 
 * @author SuZiquan
 *
 */
@Component
public class MavenInvoker {

	@Autowired
	private DirectoryConfig directoryConfig;

	/**
	 * 执行mvn clean命令
	 * @param projectDir
	 */
	public void clean(String projectDir) {
		InvocationRequest request = new DefaultInvocationRequest();
		String pomPath = FileUtil.jointPath(projectDir, "pom.xml");
		File pomFile = new File(pomPath);
		if (!pomFile.exists()) {
			return;
		}
		request.setPomFile(new File(pomPath));
		request.setGoals(Collections.singletonList("clean"));
		Invoker invoker = new DefaultInvoker();

		String mavenHome = FileUtil.uniformPathSeparator(directoryConfig.getMavenHome());

		invoker.setMavenHome(new File(mavenHome));
		invoker.setOutputHandler(new InvocationOutputHandler() {
			@Override
			public void consumeLine(String line) {
			}
		});

		try {
			invoker.execute(request);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 执行mvn clean test命令运行测试，解析完测试报告之后再次执行mvn clean命令清理项目
	 * @param projectDir
	 * @return
	 * @throws CompileErrorException
	 */
	@SuppressWarnings("unchecked")
	public List<Testcase> test(String projectDir) throws CompileErrorException {

		InvocationRequest request = new DefaultInvocationRequest();
		String pomPath = FileUtil.jointPath(projectDir, "pom.xml");
		File pomFile = new File(pomPath);
		if (!pomFile.exists()) {
			throw new CompileErrorException();
		}
		request.setPomFile(pomFile);
		String[] goals = { "clean", "test" };

		request.setGoals(Arrays.asList(goals));
		Invoker invoker = new DefaultInvoker();
		String mavenHome = FileUtil.uniformPathSeparator(directoryConfig.getMavenHome());

		invoker.setMavenHome(new File(mavenHome));

		CompileHanlder compileHanlder = new CompileHanlder();
		invoker.setOutputHandler(compileHanlder);

		try {
			invoker.execute(request);
		} catch (MavenInvocationException e) {
			e.printStackTrace();
		}

		if (compileHanlder.isCompileFail()) {
			throw new CompileErrorException();
		}

		/*解析surefire测试报告*/
		String surefireReportDirPath = FileUtil.jointPath(projectDir, "target", "surefire-reports");
		File surefireReportDir = new File(surefireReportDirPath);
		if (!surefireReportDir.exists()) {
			return null;
		}

		List<Testcase> results = new ArrayList<>();
		for (File reportFile : surefireReportDir.listFiles()) {
			if (reportFile.getName().endsWith(".xml")) {

				SAXReader reader = new SAXReader();
				Document document = null;
				try {
					document = reader.read(reportFile);
					Element testsuiteNode = document.getRootElement();
					List<Element> testcaseNodes = testsuiteNode.elements("testcase");
					for (Element testcaseNode : testcaseNodes) {
						Testcase testcase = new Testcase();
						String className = testcaseNode.attributeValue("classname");
						testcase.setClassName(className);
						String testCaseName = testcaseNode.attributeValue("name");
						testcase.setName(testCaseName);

						if (testcaseNode.element("failure") != null) {
							testcase.setResult("fail");
						} else if (testcaseNode.element("error") != null) {
							testcase.setResult("error");
						} else if (testcaseNode.element("skipped") != null) {
							testcase.setResult("skip");
						} else {
							testcase.setResult("pass");
						}

						results.add(testcase);
					}

				} catch (DocumentException e) {
					e.printStackTrace();
				}

			}
		}

		/*测试完成后，再次清理项目*/
		clean(projectDir);

		return results;

	}

	/**
	 * 根据Maven命令的输出结果判断是否有编译错误
	 * 
	 * @author SuZiquan
	 *
	 */
	private class CompileHanlder implements InvocationOutputHandler {

		private boolean compileFail;

		@Override
		public void consumeLine(String line) {
			if (line.startsWith("[ERROR] COMPILATION ERROR :")) {
				compileFail = true;
			}
		}

		public boolean isCompileFail() {
			return compileFail;
		}

	}

}
