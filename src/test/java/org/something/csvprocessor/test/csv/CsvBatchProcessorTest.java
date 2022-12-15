package org.something.csvprocessor.test.csv;

import akka.actor.ActorSystem;
import org.something.csvprocessor.csv.*;
import org.something.csvprocessor.csv.PrefixedCsvBatcher;
import org.something.csvprocessor.monad.Try;
import org.something.csvprocessor.processing.Materializer;
import org.something.csvprocessor.test.FileUtils;
import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CsvBatchProcessorTest {
  private final ActorSystem actorSystem = ActorSystem.create("system");

  @After
  public void after() {
    actorSystem.terminate();
  }

  @Test
  public void successfullyProcessLines() throws ExecutionException, InterruptedException {
    CsvBatchProcessor processor = new CsvBatchProcessor("|", actorSystem, 8);

    ArrayList<String> data = new ArrayList<>();
    data.add("A|a1");
    data.add("B|b1");
    data.add("C|c1");
    data.add("A|a2");
    data.add("B|b2");
    data.add("A|a3");

    ArrayList<String> result = processor.process(data, dummyBatcher(), dummyBatchReader(), dummyMaterializer()).get();
    assertEquals(result.get(0), "a1:b1:c1");
    assertEquals(result.get(1), "a2:b2");
    assertEquals(result.get(2), "a3");
  }

  @Test
  public void successfullyProcessFile() throws ExecutionException, InterruptedException, IOException {
    CsvBatchProcessor processor = new CsvBatchProcessor("|", actorSystem, 8);

    ArrayList<String> data = new ArrayList<>();
    data.add("A|a1");
    data.add("B|b1");
    data.add("C|c1");
    data.add("A|a2");
    data.add("B|b2");
    data.add("A|a3");

    File file = Files.createTempFile("tmp", "tmp").toFile();
    FileUtils.writeFile(file, data);

    ArrayList<String> result = processor.process(file, dummyBatcher(), dummyBatchReader(), dummyMaterializer()).get();
    assertEquals(result.get(0), "a1:b1:c1");
    assertEquals(result.get(1), "a2:b2");
    assertEquals(result.get(2), "a3");
  }

  @Test
  public void propagateErrors() {
    CsvBatchProcessor processor = new CsvBatchProcessor("|", actorSystem, 8);

    ArrayList<String> data = new ArrayList<>();
    data.add("D");

    Exception thrown = assertThrows(Exception.class, () -> processor.process(data, dummyBatcher(), dummyBatchReader(), dummyMaterializer()).get());

    assertEquals(thrown.getMessage(), "org.something.csvprocessor.exception.CsvProcessorException: Could not find part at index [1]");
  }

  private CsvBatcher dummyBatcher() {
    return new PrefixedCsvBatcher("A") {};
  }

  private CsvBatchReader<String> dummyBatchReader() {
    return new CsvBatchReader<String>() {
      @Override
      public Try<String> read(ArrayList<Try<CsvLine>> csvLines) {
        return drain("", csvLines, 0);
      }

      private Try<String> drain(String result, ArrayList<Try<CsvLine>> buffer, int index) {
        if (buffer.size() > index) {
          return buffer.get(index)
              .flatMap(csv -> csv.getPart(1)
                  .flatMap(part -> {
                    if (result.isEmpty()) {
                      return drain(part, buffer, index + 1);
                    } else {
                      return drain(result + ":" + part, buffer, index + 1);
                    }
                  }));
        } else {
          return Try.success(result);
        }
      }
    };
  }

  private Materializer<Try<String>, ArrayList<String>> dummyMaterializer() {
    return new Materializer<>() {
      private final ArrayList<String> result = new ArrayList<>();

      @Override
      public CompletableFuture<Void> materialize(Try<String> string) {
        try {
          result.add(string.get());
          return CompletableFuture.completedFuture(null);
        } catch (Throwable e) {
          return CompletableFuture.failedFuture(e);
        }
      }

      @Override
      public CompletableFuture<ArrayList<String>> result() {
        return CompletableFuture.completedFuture(result);
      }
    };
  }
}
