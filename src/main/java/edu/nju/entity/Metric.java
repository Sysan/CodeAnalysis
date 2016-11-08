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
 * 度量值
 * 
 * @author SuZiquan
 *
 */
@Getter
@Setter
@Entity
public class Metric {

	@Id
	@GeneratedValue
	private long id;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "project_id")
	private Project project;

	private int totalLineCount;

	private int commentLineCount;

	private int fieldCount;

	private int methodCount;

	private int maxCoc;

	/**
	 * 对度量值进行叠加统计
	 * 
	 * @param another
	 *            另一个组件（Java文件、项目等）的度量值
	 */
	public void addMetric(Metric another) {
		this.totalLineCount += another.totalLineCount;
		this.commentLineCount += another.commentLineCount;
		this.fieldCount += another.fieldCount;
		this.methodCount += another.methodCount;
		if (another.maxCoc > this.maxCoc) {
			this.maxCoc = another.maxCoc;
		}

	}

}
