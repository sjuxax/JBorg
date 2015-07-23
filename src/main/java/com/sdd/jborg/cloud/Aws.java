package com.sdd.jborg.cloud;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.sdd.jborg.util.Logger;

public class Aws
{
	public static void create()
	{
		final AmazonEC2Client ec2Client = new AmazonEC2Client(new EnvironmentVariableCredentialsProvider());
		ec2Client.setEndpoint("ec2.us-east-1.amazonaws.com");

		RunInstancesRequest runInstancesRequest =
			new RunInstancesRequest();

		runInstancesRequest.withImageId("ami-9a562df2")
			.withInstanceType("m4.xlarge")
			.withMinCount(1)
			.withMaxCount(1)
			.withKeyName("wildworks_aws_9-11-2014")
			.withSecurityGroups("sg-e3ca2084");

		RunInstancesResult runInstancesResult =
			ec2Client.runInstances(runInstancesRequest);

		Logger.out("done creating new instance.");
	}
}
