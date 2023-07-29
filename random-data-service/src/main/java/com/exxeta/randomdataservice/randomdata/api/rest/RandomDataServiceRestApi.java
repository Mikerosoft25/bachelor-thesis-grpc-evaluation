package com.exxeta.randomdataservice.randomdata.api.rest;

import com.exxeta.randomdataservice.randomdata.api.rest.dto.RandomDataRestDto;
import com.exxeta.randomdataservice.randomdata.service.RandomDataService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(path = "/data", produces = MediaType.APPLICATION_JSON_VALUE)
public class RandomDataServiceRestApi {

  private final RandomDataService randomDataService;

  public RandomDataServiceRestApi(final RandomDataService randomDataService) {
    this.randomDataService = randomDataService;
  }

  @GetMapping
  public ResponseEntity<RandomDataRestDto> getRandomData(@RequestParam("byteCount") int byteCount) {
    if (byteCount > RandomDataService.MAX_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
    }

    String randomData = this.randomDataService.getRandomDataString(byteCount);
    return ResponseEntity.ok(new RandomDataRestDto(randomData));
  }
}
