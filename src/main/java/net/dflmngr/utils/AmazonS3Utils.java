package net.dflmngr.utils;

import java.io.File;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class AmazonS3Utils {
	private static String bucketName = "dfl-mngr-reports";
	
	public static void uploadToS3(String keyName, String uploadFile) throws Exception {
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build();

		File file = new File(uploadFile);
		
		PutObjectRequest request = new PutObjectRequest(bucketName, keyName, file);
		request.setCannedAcl(CannedAccessControlList.PublicRead);
		
		s3Client.putObject(request);
	}
}
