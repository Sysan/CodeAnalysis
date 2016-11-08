package edu.nju;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.nju.tools.jgit.JGitHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CodeAnalyseApplicationTests {

	
	@Test
	public void contextLoads() {
		JGitHelper gitHelper = new JGitHelper("D:\\jgit\\MatrixExam-1478426191260\\MatrixExam");
		
		gitHelper.getCommitInfos().forEach((e)->{System.out.println(e.getCommitId());});;
	}


}
