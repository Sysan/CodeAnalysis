package edu.nju.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

/**
 * 服务器配置
 * 
 * @author SuZiquan
 *
 */
@Configuration
@ConfigurationProperties(prefix = "server")
@Getter
@Setter
public class ServerConfig {

	/**
	 * IP地址
	 */
	private String address;

	/**
	 * 端口号
	 */
	private int port;

}