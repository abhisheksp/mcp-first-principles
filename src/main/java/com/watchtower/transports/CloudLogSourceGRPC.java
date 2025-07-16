package com.watchtower.transports;

/**
 * GCP exposes their CloudLogSource via gRPC
 * 
 * Notice: They need protobuf definitions for functions
 */
public class CloudLogSourceGRPC {
    // service CloudLogSource {
    //   rpc ExecuteFunction(FunctionRequest) returns (FunctionResponse);
    // }
    //
    // message FunctionRequest {
    //   string function_name = 1;
    //   google.protobuf.Struct arguments = 2;
    // }
    
    // Different format than our FunctionDefinition!
}