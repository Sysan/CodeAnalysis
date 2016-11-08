package edu.nju.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

/**
 * 一个测试用例信息
 * 
 * @author SuZiquan
 *
 */
@Getter
@Setter
@Entity
public class Testcase {

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "project_id", referencedColumnName = "id")
	private Project project;

	private String className;

	private String name;

	@Column(columnDefinition = "enum('pass','fail','skip','error')")
	private String result;

}
