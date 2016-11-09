package edu.nju.executor;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.nju.entity.Metric;
import edu.nju.entity.Project;
import edu.nju.entity.Score;
import edu.nju.entity.Task;
import edu.nju.entity.Testcase;
import edu.nju.repository.TaskRepository;
import edu.nju.tools.jdt.JDTAnalyzer;
import edu.nju.tools.jgit.GitCommitInfo;
import edu.nju.tools.jgit.JGitHelper;
import edu.nju.tools.maven.CompileErrorException;
import edu.nju.tools.maven.MavenInvoker;
import edu.nju.util.FileUtil;
import lombok.Setter;

/**
 * 只对Git项目的最后一个版本进行分析，计算分数，获得报告
 * 
 * @author SuZiquan
 *
 */
@Component
@Scope("prototype")
public class ScoreReportTask implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScoreReportTask.class);

	@Setter
	private String gitUrl;

	@Autowired
	private MavenInvoker mavenInvoker;

	@Autowired
	private JDTAnalyzer jdtAnalyzer;

	@Autowired
	private TaskRepository taskRepository;

	@Override
	@Transactional
	public void run() {

		LOGGER.info("Start New Score Report Task......");

		Task task = taskRepository.findByGitUrl(gitUrl);
		if (task == null) {
			LOGGER.info("Nothing to analyse...");
			return;
		} else if (task.isScoreReportDone()) {
			LOGGER.info("The git url has been analysed as score report task.");
			return;
		}

		String localUrl = task.getLocalUrl();
		JGitHelper gitHelper = new JGitHelper(gitUrl, localUrl);

		LOGGER.info("Git project has been downloaded.");

		GitCommitInfo lastestCommitInfo = gitHelper.getLastestCommitInfo();

		Project project = new Project();
		project.setGitCommitId(lastestCommitInfo.getCommitId());
		project.setGitCommitDate(lastestCommitInfo.getCommitDate());

		project.setTask(task);
		task.setLastestGitCommitId(lastestCommitInfo.getCommitId());

		String srcDir = FileUtil.jointPath(localUrl, "src", "main", "java");

		Metric jdtMetric = new Metric();

		Map<String, String> javaSources = FileUtil.getJavaSources(srcDir);
		for (Map.Entry<String, String> entry : javaSources.entrySet()) {
			jdtMetric.addMetric(jdtAnalyzer.parseJavaFile(entry.getKey(), entry.getValue()));
		}

		jdtMetric.setProject(project);
		project.setMetric(jdtMetric);

		Score score = new Score();
		score.setTask(task);

		List<Testcase> testcases = null;
		try {
			testcases = mavenInvoker.test(localUrl);
			int passNums = 0;
			for (Testcase testcase : testcases) {
				testcase.setProject(project);
				if (testcase.getResult().equals("pass")) {
					passNums += 1;
				}
			}
			score.setScore(100 * passNums / testcases.size());
			project.setCompileSuccess(true);
		} catch (CompileErrorException e) {
			project.setCompileSuccess(false);
			score.setScore(0);
			e.printStackTrace();
		}

		project.getTestcases().addAll(testcases);

		LOGGER.info("Maven task finished...");

		task.setScore(score);

		task.getProjects().add(project);

		task.setScoreReportDone(true);

		taskRepository.save(task);
		
		LOGGER.info("Score Report Task Finished.");

	}

}
