package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class InfrastructureApp {
    public static void main(final String[] args) {
        App app = new App();

        new InfrastructureStack(app, "InfrastructureStack", StackProps.builder()
                .env(Environment.builder()
                        .account("230989377092")  // your AWS account ID
                        .region("us-east-1")
                        .build())
                .build());

        app.synth();
    }
}

