package edu.nju.mq;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Component
public class ScoreReportSender {

	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScoreReportSender.class);
	
	public static final String QUEUE_NAME = "scoreReport";
	
	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void send(String gitUrl, String scoreReportUrl) {

		Map<String, Object> map = new HashMap<>();
		map.put("gitUrl", gitUrl);
		map.put("scoreReportUrl", scoreReportUrl);

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String message = gson.toJson(map);
		
		rabbitTemplate.convertAndSend(QUEUE_NAME, message);
	}

}
