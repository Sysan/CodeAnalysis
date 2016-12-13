package edu.nju.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import edu.nju.config.ServerConfig;
import edu.nju.entity.Metric;
import edu.nju.entity.Project;
import edu.nju.entity.Task;
import edu.nju.entity.Testcase;
import edu.nju.mq.AnalysisReportSender;
import edu.nju.repository.ProjectRepository;
import edu.nju.repository.TaskRepository;
import edu.nju.tools.jdt.JDTAnalyzer;
import edu.nju.tools.jgit.GitCommitInfo;
import edu.nju.tools.jgit.JGitHelper;
import edu.nju.tools.maven.CompileErrorException;
import edu.nju.tools.maven.MavenInvoker;
import edu.nju.util.FileUtil;
import lombok.Setter;

/**
 * 对Git项目的每个版本进行分析，生成报告
 * 
 * @author SuZiquan
 *
 */
@Component
@Scope("prototype")
public class AnalysisReportTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisReportTask.class);

	@Setter
	private String gitUrl;

	@Autowired
	private JDTAnalyzer jdtAnalyzer;

	@Autowired
	private MavenInvoker mavenInvoker;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ServerConfig serverConfig;
	
	@Autowired
	private AnalysisReportSender analysisReportSender;
	
	@Override
	@Transactional
	public void run() {

		LOGGER.info("Start New Analysis Report Task...");

		Task task = taskRepository.findByGitUrl(gitUrl);
		if (task == null) {
			LOGGER.info("Nothing to analyse...");
			return;
		} else if (task.isAnalysisReportDone()) {
			LOGGER.info("The git url has been analysed as analysis report task.");
			return;
		} else if (!task.isScoreReportDone()) {
			LOGGER.error("Analysis report task must be after score report task.");
			return;
		}

		String localUrl = task.getLocalUrl();
		JGitHelper gitHelper = new JGitHelper(localUrl);

		List<GitCommitInfo> commitInfos = gitHelper.getCommitInfos();

		String srcDir = FileUtil.jointPath(localUrl, "src", "main", "java");

		List<Project> projects = new ArrayList<>();
		for (GitCommitInfo commitInfo : commitInfos) {

			Project existedProject = projectRepository.findByTaskIdAndGitCommitId(task.getId(),
					commitInfo.getCommitId());
			if (existedProject != null) {
				LOGGER.info("Project with git url " + task.getGitUrl() + " and commit id " + commitInfo.getCommitId());
				continue;
			}

			Project project = new Project();
			project.setTask(task);
			project.setGitCommitId(commitInfo.getCommitId());
			project.setGitCommitDate(commitInfo.getCommitDate());

			Metric jdtMetric = new Metric();

			gitHelper.checkout(commitInfo.getCommitId());

			Map<String, String> javaSources = FileUtil.getJavaSources(srcDir);
			for (Map.Entry<String, String> entry : javaSources.entrySet()) {
				jdtMetric.addMetric(jdtAnalyzer.parseJavaFile(entry.getKey(), entry.getValue()));
			}

			jdtMetric.setProject(project);
			project.setMetric(jdtMetric);

			List<Testcase> testcases = null;
			try {
				testcases = mavenInvoker.test(localUrl);
				for (Testcase testcase : testcases) {
					testcase.setProject(project);
				}
				project.setCompileSuccess(true);
			} catch (CompileErrorException e) {
				project.setCompileSuccess(false);
			}

			project.setTestcases(testcases);

			projects.add(project);
		}

		String lastestCommitId = task.getLastestGitCommitId();
		gitHelper.checkout(lastestCommitId);

		task.getProjects().addAll(projects);

		task.setAnalysisReportDone(true);

		taskRepository.save(task);
		
		//发送消息
		String analysisReportUrl = serverConfig.getAddress() + ":" + serverConfig.getPort() + "/analysisReport"
		+ "?gitUrl=" + gitUrl;
		analysisReportSender.send(gitUrl, analysisReportUrl);

		LOGGER.info("Analysis Report Task Finished.");
	}

}
