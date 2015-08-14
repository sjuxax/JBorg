package com.sdd.jborg.cloud;

//import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
//import com.amazonaws.services.ec2.AmazonEC2Client;
//import com.amazonaws.services.ec2.model.RunInstancesRequest;
//import com.amazonaws.services.ec2.model.RunInstancesResult;
//import com.sdd.jborg.Logger;
//
//public class Aws
//{
//	public static void create()
//	{
//		final AmazonEC2Client ec2Client = new AmazonEC2Client(new EnvironmentVariableCredentialsProvider());
//		ec2Client.setEndpoint("ec2.us-east-1.amazonaws.com");
//
//		RunInstancesRequest runInstancesRequest =
//			new RunInstancesRequest();
//
//		runInstancesRequest.withImageId("ami-9a562df2")
//			.withInstanceType("m4.large")
//			.withMinCount(1)
//			.withMaxCount(1)
//			.withKeyName("wildworks_aws_9-11-2014")
//			.withSubnetId("subnet-5a0d9771")
//			.withSecurityGroupIds("sg-e3ca2084");
//
//		RunInstancesResult runInstancesResult =
//			ec2Client.runInstances(runInstancesRequest);
//
//		Logger.info("done creating new instance.");
//	}
//}

import com.sdd.jborg.Logger;
import com.sdd.jborg.scripts.Standard;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.aws.ec2.*;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.options.TemplateOptions;

import static com.google.common.collect.Iterables.getOnlyElement;

import org.jclouds.domain.Location;
import org.jclouds.ec2.*;
import org.jclouds.ec2.domain.InstanceType;
import org.jclouds.location.reference.LocationConstants;

public class Aws
{
	public static void create()
	{

		Logger.info("key " + System.getenv("AWS_ACCESS_KEY") + " code " + System.getenv("AWS_SECRET_KEY"));
		final ComputeService compute = ContextBuilder.newBuilder("aws-ec2")
			.endpoint("ec2.us-east-1.amazonaws.com")
			.credentials(System.getenv("AWS_ACCESS_KEY"), System.getenv("AWS_SECRET_KEY"))
			.buildView(ComputeServiceContext.class)
			.getComputeService();

//		TemplateOptions ec2Options =

//		final Template template = compute.templateBuilder()
//			.osFamily(OsFamily.UBUNTU)
//			.hardwareId(InstanceType.C4_LARGE)
//			.imageId("us-east-1/ami-9a562df2")
//			.
//			.build();


//		Logger.info(template.getLocation().getId());

		Logger.info(compute.toString());
//		try{
//			compute.createNodesInGroup("",1,template);
//		}
//		catch(RunNodesException error) {
//			throw new IllegalStateException("Couldn't create instance", error);
//		}

	}
}