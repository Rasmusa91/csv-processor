package org.something.csvprocessor;

import akka.actor.ActorSystem;
import org.something.csvprocessor.component.Component;
import org.something.csvprocessor.component.FileConverterComponent;
import org.something.csvprocessor.component.InputConverterComponent;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {
  public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
    ActorSystem system = ActorSystem.create("system");
    int parallelism = 8;
    String separator = "|";

    run(separator, system, parallelism);

    system.terminate();
    system.getWhenTerminated().toCompletableFuture().get(20, TimeUnit.SECONDS);
  }

  private static void run(String separator, ActorSystem system, int parallelism) {
    try {
      Optional<Component> component = getComponent(separator, system, parallelism);

      if (component.isPresent()) {
        component.get().run().get();
        System.out.println("Component task finished successfully.");
        System.out.println();
        run(separator, system, parallelism);
      }
    } catch (Throwable e) {
      System.out.println("Component failed with [" + e.getMessage() + "].");
      System.out.println();
      run(separator, system, parallelism);
    }
  }

  private static Optional<Component> getComponent(String separator, ActorSystem system, int parallelism) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("CSV to XML converter, which component do you want to start?");
    System.out.println("1: input converter");
    System.out.println("2: file converter");
    System.out.println("exit: exit the application");
    System.out.print("> ");

    String action = scanner.nextLine();

    return switch (action) {
      case "1" -> Optional.of(getInputConverterComponent(separator, system, parallelism));
      case "2" -> Optional.of(getFileConverterComponent(separator, system, parallelism));
      case "exit" -> Optional.empty();
      default -> getComponent(separator, system, parallelism);
    };
  }

  public static Component getInputConverterComponent(String separator, ActorSystem system, int parallelism) {
    Scanner scanner = new Scanner(System.in);
    ArrayList<String> arrayList = new ArrayList<>();

    System.out.println("Please enter CSV line or [done]");

    while(true) {
      System.out.print("> ");
      String line = scanner.nextLine();
      if (!line.equalsIgnoreCase("done")) {
        arrayList.add(line);
      } else {
        break;
      }
    }

    return new InputConverterComponent(arrayList, separator, system, parallelism);
  }

  private static Component getFileConverterComponent(String separator, ActorSystem system, int parallelism) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("Please enter source file or directory");
    System.out.print("> ");

    String source = scanner.nextLine();

    return new FileConverterComponent(source, separator, system, parallelism);
  }
}