package com.exxeta;

import com.exxeta.protos.example1.User;
import com.exxeta.protos.example4.TextMessage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

// protoc --java_out="./src/main/java" example1.proto
public class Main {
  public static void main(String[] args) throws IOException {
    createUserExample();
    binaryFileExample();
  }

  public static void createUserExample() throws IOException {
    // Creating a new user object
    User userOutput =
        User.newBuilder()
            .setId(12345)
            .setName("Username")
            .setEmail("username@some-mail.com")
            .build();

    // Writing user data to a file
    FileOutputStream output = new FileOutputStream("./user_data.bin");
    userOutput.writeTo(output);

    // Reading user data from a file
    FileInputStream input = new FileInputStream("./user_data.bin");
    User userInput = User.parseFrom(input);

    // Prints 'Username' to the console
    System.out.print(userInput.getName());
  }

  public static void binaryFileExample() throws IOException {
    TextMessage message = TextMessage.newBuilder().setText("abcde").build();

    // Writing user data to a file
    FileOutputStream output = new FileOutputStream("./data_example.bin");
    message.writeTo(output);
  }
}
