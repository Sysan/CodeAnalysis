package edu.nju.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import lombok.Getter;
import lombok.Setter;

/**
 * 目录配置
 * 
 * @author SuZiquan
 *
 */
@Configuration
@ConfigurationProperties(prefix = "directory")
@PropertySource("classpath:directory.properties")
@Getter
@Setter
public class DirectoryConfig {

	/**
	 * Git下载的文件存放地址
	 */
	private String gitDir;

	/**
	 * Maven HOME
	 */
	private String mavenHome;

}