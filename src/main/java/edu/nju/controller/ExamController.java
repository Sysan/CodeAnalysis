package edu.nju.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.nju.config.ServerConfig;
import edu.nju.entity.Score;
import edu.nju.entity.Task;
import edu.nju.executor.TaskExecutor;
import edu.nju.repository.TaskRepository;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import lombok.Getter;
import lombok.Setter;

/**
 * 对外接口，包括分析考试、获取考试分数、获取分析报告
 * 
 * @author SuZiquan
 *
 */
@RestController
public class ExamController {

	@Autowired
	private ServerConfig serverConfig;

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private TaskRepository taskRepository;

	@ApiOperation(value = "分析考试代码", notes = "分析某个git url的代码")
	@PostMapping("/analyse")
	public @ApiResponse(code = 200, message = "success") @ResponseBody AnalyseReponse analyse(
			@ApiParam(value = "git url 数组", required = true) @RequestBody String[] gitUrls) {
		AnalyseReponse analyseReponse = new AnalyseReponse();
		for (String gitUrl : gitUrls) {
			System.out.println("giturl:" + gitUrl);
			taskExecutor.executeNewAnalyseTask(gitUrl);
		}
		analyseReponse.setSuccess(true);
		return analyseReponse;
	}

	@ApiOperation(value = "获得考试分数", notes = "获得考试分数")
	@PostMapping("/getScores")
	public @ApiResponse(code = 200, message = "success") @ResponseBody List<GetScoreResponse> getScores(
			@ApiParam(value = "git url 数组", required = true) @RequestBody String[] gitUrls) {
		List<GetScoreResponse> result = new ArrayList<>();
		for (String gitUrl : gitUrls) {
			GetScoreResponse getScoreResponse = new GetScoreResponse();
			getScoreResponse.setGitUrl(gitUrl);
			Task task = taskRepository.findByGitUrl(gitUrl);
			if (task == null)
				continue;
			boolean scoreReportDone = task.isScoreReportDone();
			if (scoreReportDone) {
				Score score = task.getScore();
				getScoreResponse.setScore(score.getScore());
				getScoreResponse.setState("finish");
			} else {
				getScoreResponse.setState("computing");
			}
			result.add(getScoreResponse);
		}

		return result;
	}

	@ApiOperation(value = "获得考试分析报告", notes = "获得考试分析报告")
	@PostMapping("/getReports")
	public @ApiResponse(code = 200, message = "success") @ResponseBody List<GetReportResponse> getReports(
			@ApiParam(value = "git url 数组", required = true) @RequestBody String[] gitUrls) {
		List<GetReportResponse> result = new ArrayList<>();
		for (String gitUrl : gitUrls) {
			GetReportResponse getReportResponse = new GetReportResponse();
			getReportResponse.setGitUrl(gitUrl);
			Task task = taskRepository.findByGitUrl(gitUrl);
			if (task == null)
				continue;
			boolean scoreReportDone = task.isScoreReportDone();
			if (scoreReportDone) {
				String scoreReportUrl = serverConfig.getAddress() + ":" + serverConfig.getPort() + "/scoreReport"
						+ "?gitUrl=" + gitUrl;
				getReportResponse.setScoreReportUrl(scoreReportUrl);
				getReportResponse.setScoreReportState("finish");
			} else {
				getReportResponse.setScoreReportState("computing");
			}

			boolean analysisReportDone = task.isAnalysisReportDone();
			if (analysisReportDone) {
				String analysisReportUrl = serverConfig.getAddress() + ":" + serverConfig.getPort() + "/analysisReport"
						+ "?gitUrl=" + gitUrl;
				getReportResponse.setAnalysisReportUrl(analysisReportUrl);
				;
				getReportResponse.setAnalysisReportState("finish");
			} else {
				getReportResponse.setAnalysisReportState("computing");
			}
			result.add(getReportResponse);
		}

		return result;
	}

}

@Getter
@Setter
class AnalyseReponse {
	private boolean success;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}

@Getter
@Setter
class GetScoreResponse {
	private String gitUrl;
	private Integer score;
	private String state;

}

@Getter
@Setter
class GetReportResponse {
	private String gitUrl;
	private String scoreReportUrl;
	private String analysisReportUrl;
	private String scoreReportState;
	private String analysisReportState;

}
