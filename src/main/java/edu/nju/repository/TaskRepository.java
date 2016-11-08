package edu.nju.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.nju.entity.Task;

/**
 * 对分析任务（Task）的数据库操作
 * 
 * @author SuZiquan
 *
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

	public Task findByGitUrl(String gitUrl);

}
