package edu.nju.executor;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.nju.config.DirectoryConfig;
import edu.nju.entity.Task;
import edu.nju.repository.TaskRepository;
import edu.nju.util.FileUtil;

/**
 * 添加并执行新的分析任务，分析任务被分为ScoreReportTask和AnalysisReportTask两个阶段
 * 
 * @author SuZiquan
 *
 */
@Component
public class TaskExecutor {

	private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisReportTask.class);

	@Autowired
	private ObjectFactory<ScoreReportTask> scoreCalcTaskFactory;

	@Autowired
	private ObjectFactory<AnalysisReportTask> analyseTaskFactory;

	private ExecutorService scoreCalcTaskThreadPool;

	private ExecutorService analyseTaskThreadPool;

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private DirectoryConfig directoryConfig;

	public TaskExecutor() {
		scoreCalcTaskThreadPool = Executors.newSingleThreadExecutor();
		analyseTaskThreadPool = Executors.newSingleThreadExecutor();
	}

	public void executeNewAnalyseTask(String gitUrl) {

		saveNewTask(gitUrl);

		ScoreReportTask scoreCalcTask = scoreCalcTaskFactory.getObject();
		scoreCalcTask.setGitUrl(gitUrl);

		AnalysisReportTask analyseTask = analyseTaskFactory.getObject();
		analyseTask.setGitUrl(gitUrl);

		CompletableFuture.runAsync(scoreCalcTask, scoreCalcTaskThreadPool).thenRunAsync(analyseTask,
				analyseTaskThreadPool);
	}

	@Transactional
	private void saveNewTask(String gitUrl) {

		Task task = new Task();
		String localUrl = null;

		synchronized (taskRepository) {

			Task findByGitUrl = taskRepository.findByGitUrl(gitUrl);
			if (findByGitUrl != null) {
				LOGGER.info("The git url has been analyzed.");
				return;
			}
			task.setGitUrl(gitUrl);

			String localBaseDir = FileUtil.uniformPathSeparator(directoryConfig.getGitDir());
			String projectName = gitUrl.substring(gitUrl.lastIndexOf("/"), gitUrl.lastIndexOf("."));

			// TODO git_url的命名规则？
			String uniqueDir = projectName + "-" + new Date().getTime();
			localUrl = FileUtil.jointPath(localBaseDir, uniqueDir, projectName);
			task.setLocalUrl(localUrl);

			taskRepository.save(task);

		}

	}
}
