package edu.nju.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.ColumnDefault;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目信息，Git项目的每个版本都当作一个项目
 * 
 * @author SuZiquan
 *
 */
@Getter
@Setter
@Entity
public class Project {

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "task_id", referencedColumnName = "id")
	private Task task;

	private String gitCommitId;
	private Date gitCommitDate;

	@ColumnDefault("true")
	private boolean compileSuccess;

	@OneToOne(mappedBy = "project", cascade = CascadeType.ALL)
	private Metric metric;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
	private List<Testcase> testcases = new ArrayList<>();

}
