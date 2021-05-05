package com.zzx.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {

	@Resource
	private OSSClient ossClient;

	@Test
	public void testUpload() throws FileNotFoundException {
		InputStream inputStream = new FileInputStream("/Users/hds0m3zzx/Pictures/beautifulPic/earth.jpg");
		ossClient.putObject("gulimall-0505","earth.jpg",inputStream);
		ossClient.shutdown();
		System.out.println("上传完成...");
	}

}
