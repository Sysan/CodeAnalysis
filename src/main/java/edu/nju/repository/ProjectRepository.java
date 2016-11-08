package edu.nju.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.nju.entity.Project;

/**
 * 对项目（Project）的数据库操作
 * 
 * @author SuZiquan
 *
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

	public Project findByTaskIdAndGitCommitId(long taskId, String gitCommitId);

}
