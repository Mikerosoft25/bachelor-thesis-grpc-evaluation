package com.exxeta.performancetester.requests;

public interface Request {
  void executeGrpcRequest();

  void executeRestRequest();
}
