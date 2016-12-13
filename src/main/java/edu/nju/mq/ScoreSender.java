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
public class ScoreSender {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScoreSender.class);

	public static final String QUEUE_NAME = "score";

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public void send(String gitUrl, int score) {
		Map<String, Object> map = new HashMap<>();
		map.put("gitUrl", gitUrl);
		map.put("score", score);

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		String message = gson.toJson(map);

		LOGGER.info(message);
		
		rabbitTemplate.convertAndSend(QUEUE_NAME, message);
	}

}
