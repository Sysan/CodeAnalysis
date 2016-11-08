package edu.nju.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.nju.entity.Metric;
import edu.nju.entity.Project;
import edu.nju.entity.Task;
import edu.nju.entity.Testcase;
import edu.nju.repository.ProjectRepository;
import edu.nju.repository.TaskRepository;

/**
 * 只对项目的最后一次提交进行分析，计算分数，获得报告
 * 
 * @author SuZiquan
 *
 */
@Controller
public class ScoreReportController {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ProjectRepository projectRepository;

	/**
	 * 展示Score Report
	 * @param gitUrl
	 * @param modelMap
	 * @return
	 */
	@RequestMapping("/scoreReport")
	public String scoreReport(String gitUrl, ModelMap modelMap) {
		modelMap.put("gitUrl", gitUrl);

		Task task = taskRepository.findByGitUrl(gitUrl);
		long taskId = task.getId();
		String lastestCommitId = task.getLastestGitCommitId();
		Project project = projectRepository.findByTaskIdAndGitCommitId(taskId, lastestCommitId);

		List<Testcase> testcases = project.getTestcases();
		Metric metric = project.getMetric();

		modelMap.put("metric", metric);
		modelMap.put("testcases", testcases);
		modelMap.put("score", task.getScore().getScore());

		return "score_report";
	}

}
