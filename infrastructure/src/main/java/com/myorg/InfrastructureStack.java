package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecr.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.*;
import software.amazon.awscdk.services.elasticloadbalancingv2.targets.*;
import software.amazon.awscdk.services.iam.*;
import software.constructs.Construct;

import java.util.List;

public class InfrastructureStack extends Stack {

    public InfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // 1. VPC - use default VPC
        IVpc vpc = Vpc.fromLookup(this, "DefaultVpc", VpcLookupOptions.builder()
                .isDefault(true)
                .build());

        // 2. Security Group
        SecurityGroup sg = SecurityGroup.Builder.create(this, "UserSubscriptionSG")
                .vpc(vpc)
                .securityGroupName("usersubscription-sg-cdk")
                .description("Security group for UserSubscription app")
                .allowAllOutbound(true)
                .build();

        // Allow HTTP
        sg.addIngressRule(Peer.anyIpv4(), Port.tcp(80), "Allow HTTP");
        // Allow app port
        sg.addIngressRule(Peer.anyIpv4(), Port.tcp(8080), "Allow app port");
        // Allow SSH
        sg.addIngressRule(Peer.anyIpv4(), Port.tcp(22), "Allow SSH");

        // 3. IAM Role for EC2
        Role ec2Role = Role.Builder.create(this, "EC2Role")
                .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
                .roleName("ec2-ecr-role-cdk")
                .managedPolicies(List.of(
                        ManagedPolicy.fromAwsManagedPolicyName("AmazonEC2ContainerRegistryReadOnly")
                ))
                .build();

        // 4. ECR Repository
        Repository ecrRepo = Repository.Builder.create(this, "UserSubscriptionRepo")
                .repositoryName("user-subscription-repo-cdk")
                .removalPolicy(RemovalPolicy.RETAIN)
                .build();

        // 5. User Data script - installs Docker on launch
        UserData userData = UserData.forLinux();
        userData.addCommands(
                "yum update -y",
                "yum install docker -y",
                "systemctl start docker",
                "systemctl enable docker",
                "usermod -aG docker ec2-user",
                "yum install awscli -y"
        );

        // 6. Primary EC2 Instance
        Instance primaryEc2 = Instance.Builder.create(this, "PrimaryEC2")
                .instanceType(InstanceType.of(InstanceClass.T3, InstanceSize.SMALL))
                .machineImage(MachineImage.latestAmazonLinux2023())
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .availabilityZones(List.of("us-east-1a"))
                        .build())
                .securityGroup(sg)
                .role(ec2Role)
                .userData(userData)
                .keyName("usersubscription")
                .build();

        // 7. Secondary EC2 Instance
        Instance secondaryEc2 = Instance.Builder.create(this, "SecondaryEC2")
                .instanceType(InstanceType.of(InstanceClass.T3, InstanceSize.SMALL))
                .machineImage(MachineImage.latestAmazonLinux2023())
                .vpc(vpc)
                .vpcSubnets(SubnetSelection.builder()
                        .availabilityZones(List.of("us-east-1b"))
                        .build())
                .securityGroup(sg)
                .role(ec2Role)
                .userData(userData)
                .keyName("usersubscription")
                .build();

        // 8. Application Load Balancer
        ApplicationLoadBalancer alb = ApplicationLoadBalancer.Builder.create(this, "UserSubscriptionALB")
                .vpc(vpc)
                .internetFacing(true)
                .loadBalancerName("usersubscription-alb-cdk")
                .securityGroup(sg)
                .build();

        // 9. Target Group
        ApplicationTargetGroup targetGroup = ApplicationTargetGroup.Builder.create(this, "UserSubscriptionTG")
                .vpc(vpc)
                .port(8080)
                .protocol(ApplicationProtocol.HTTP)
                .targetGroupName("usersubscription-tg-cdk")
                .targets(List.of(
                        new InstanceTarget(primaryEc2, 8080),
                        new InstanceTarget(secondaryEc2, 8080)
                ))
                .healthCheck(HealthCheck.builder()
                        .path("/health")
                        .port("8080")
                        .healthyHttpCodes("200")
                        .build())
                .build();

        // 10. ALB Listener
        alb.addListener("HTTPListener", BaseApplicationListenerProps.builder()
                .port(80)
                .defaultTargetGroups(List.of(targetGroup))
                .build());

        // 11. Outputs
        new CfnOutput(this, "ALBDnsName", CfnOutputProps.builder()
                .value(alb.getLoadBalancerDnsName())
                .description("ALB DNS Name")
                .build());

        new CfnOutput(this, "PrimaryEC2IP", CfnOutputProps.builder()
                .value(primaryEc2.getInstancePublicIp())
                .description("Primary EC2 Public IP")
                .build());

        new CfnOutput(this, "SecondaryEC2IP", CfnOutputProps.builder()
                .value(secondaryEc2.getInstancePublicIp())
                .description("Secondary EC2 Public IP")
                .build());
    }
}