package com.exxeta.randomdataservice.randomdata.service;

import java.util.Random;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class RandomDataService {
  // 10 Megabytes if each char is encoded in a single byte
  public static final int MAX_LENGTH = 10 * 1000 * 1000;

  private final String randomData10Mb;

  public RandomDataService() {
    this.randomData10Mb = this.generateRandomString();
  }

  /**
   * Returns a string with random generated characters.
   *
   * @param length the amount of characters that should be returned.
   * @return the random data string with the specified length.
   */
  public String getRandomDataString(int length) {
    return this.randomData10Mb.substring(0, length);
  }

  /**
   * Generates a string with a length of {@value #MAX_LENGTH} characters.
   *
   * @return the random generated string.
   */
  private String generateRandomString() {
    Random r = new Random();
    char[] chars = new char[MAX_LENGTH];
    for (int i = 0; i < chars.length; i++) {
      chars[i] = (char) (r.nextInt(26) + 'a');
    }

    return new String(chars);
  }
}
