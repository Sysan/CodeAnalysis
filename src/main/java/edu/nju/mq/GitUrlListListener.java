package edu.nju.mq;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import edu.nju.executor.TaskExecutor;

@Component
public class GitUrlListListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(GitUrlListListener.class);

	public static final String QUEUE_NAME = "gitUrlList";

	@Autowired
	private TaskExecutor taskExecutor;

	@RabbitListener(queues = QUEUE_NAME)
	public void process(String gitUrlList) {

		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		try {
			List<String> gitUrls = gson.fromJson(gitUrlList, new TypeToken<List<String>>() {
			}.getType());
			for (String gitUrl : gitUrls) {
				LOGGER.info("giturl:" + gitUrl);
				taskExecutor.executeNewAnalyseTask(gitUrl);
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
