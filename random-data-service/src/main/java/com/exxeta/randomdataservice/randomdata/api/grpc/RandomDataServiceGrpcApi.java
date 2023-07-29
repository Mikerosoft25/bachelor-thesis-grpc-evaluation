package com.exxeta.randomdataservice.randomdata.api.grpc;

import com.exxeta.randomdataservice.GetRandomDataRequest;
import com.exxeta.randomdataservice.RandomDataGrpcDto;
import com.exxeta.randomdataservice.RandomDataServiceGrpc.RandomDataServiceImplBase;
import com.exxeta.randomdataservice.randomdata.service.RandomDataService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class RandomDataServiceGrpcApi extends RandomDataServiceImplBase {

  private final RandomDataService randomDataService;

  public RandomDataServiceGrpcApi(RandomDataService randomDataService) {
    this.randomDataService = randomDataService;
  }

  @Override
  public void getRandomData(
      GetRandomDataRequest request, StreamObserver<RandomDataGrpcDto> responseObserver) {
    int byteCount = request.getByteCount();
    if (byteCount > RandomDataService.MAX_LENGTH) {
      responseObserver.onError(Status.INVALID_ARGUMENT.asRuntimeException());
      return;
    }

    String data = this.randomDataService.getRandomDataString(byteCount);

    RandomDataGrpcDto response = RandomDataGrpcDto.newBuilder().setData(data).build();

    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }
}
