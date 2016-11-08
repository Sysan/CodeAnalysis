package edu.nju.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.Getter;
import lombok.Setter;

/**
 * 一场考试（对应于一个分析任务Task）的分数
 * 
 * @author SuZiquan
 *
 */
@Getter
@Setter
@Entity
public class Score {

	@Id
	@GeneratedValue
	private long id;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "task_id")
	private Task task;

	private int score;
}
