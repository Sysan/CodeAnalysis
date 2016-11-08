package edu.nju.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.ColumnDefault;

import lombok.Getter;
import lombok.Setter;

/**
 * 任务（对一个Git地址进行分析）信息
 * 
 * @author SuZiquan
 *
 */
@Getter
@Setter
@Entity
public class Task {

	@Id
	@GeneratedValue
	private long id;

	private String gitUrl;

	private String localUrl;

	private String lastestGitCommitId;

	@ColumnDefault("false")
	private boolean scoreReportDone;

	@ColumnDefault("false")
	private boolean analysisReportDone;

	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
	private List<Project> projects = new ArrayList<>();

	@OneToOne(mappedBy = "task", cascade = CascadeType.ALL)
	private Score score;
}
