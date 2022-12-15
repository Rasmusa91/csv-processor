## CSV processor 

Provides a generic streaming interface for CSV processing. The processor accepts either an array of CSV lines, 
a file of CSV lines or an Akka source that can be of anything imaginable as long as it emits CSV lines.

The stream parses the CSV line as a convenient `CsvLine` object, batches them as described by the `CsvBatcher` interface, reads the batch described by the `CsvBatchReader` and finally materializes the result described by the `Materializer` interface.

Included in the project are two sample component implementations. These components expect a prefixed CSV line with a pipe (|) separator. 
These components support the following models:
* Person
  * Prefix: P  
  * Attributes: firstname (String), lastname (String), family (Family[]), address (Address[]), phone (Phone[])
  * Example: P|Carl Gustaf|Bernadotte
* Family
  * Prefix: F
  * Attributes: name (String), born (String), address (Address[]), phone (Phone[])
  * Example: F|Victoria|1977
* Address
  * Prefix: A
  * Attributes: city (String), street (String), zipcode (String)
  * Example: A|Drottningholms slott|Stockholm|10001
* Phone
  * Prefix: T
  * Attributes: mobile (String), landline (String)
  * Example: T|0768-101802|08-101802

They implement the `CsvBatcher` to buffer lines until a new `Person` CSV line is encountered, reads the batch by recursively creating and populating a `Person` model object.

1. `FileConverter`: reads a file or recursively scans a directory and passes the found files to the processor and batches the lines as described above. This component materializes each `Person` by writing them one by one to an XML file. To support large files, the matrializer initially creates the new file, writes the XML header and a wrapping people tag, and then it writes each person serialized to XML one by one to avoid keeping them in memory.
2. `InputConverter`: reads a provided array of CSV lines and passes them to the processor and batches them as described above. This component materalizes each person by serializing them to XML format and aggregates an XML document and finally outputs the result to the console.

The idea is that any source (file, HTTP, Kafka, MQ, w/e) of CSV line should be able to be processed to any destination (database, File, HTTP Kafka, MQ, etc.) and format (XML, JSON, YAML, etc.) by implementing the `CsvBatcher`, `CsvBatchReader` and `Materializer` in any way imaginable.

### Interfaces

#### The CsvLineReader interface
```java
public interface CsvLineReader<T> {
  Try<T> read(CsvLine csvLine);
}
```

Reads a `CsvLine` to anything, for example:
```java 
Try<T> read(CsvLine csvLine) {
  Try.success(new Person(csvLine.getPart(1).toOptional(), csvLine.getPart(2).toOptional()));
}
```

#### The CsvBatcher interface
```java
public interface CsvBatcher {
  Optional<ArrayList<Try<CsvLine>>> batch(Try<CsvLine> csvLine);
  ArrayList<Try<CsvLine>> batch();
}
```

The implementation of this interface should buffer the received `CsvLine` until a predicate or something is met.
While the batching is still in progress, an empty `Optional` should be returned and when a batch is complete, a non-empty `Optional` should be returned with the batch.
The `batch()` method without parameters is used to signal the batcher that no more lines will be provided, and it should return the current batch as is.

#### The Materializer interface
```java
public interface Materializer<T, U> {
  CompletableFuture<Void> materialize(T u);
  CompletableFuture<U> result();
}
```

Whenever a batch has been read as `T`, it will be passed to the materializer to do whatever with the result. An empty `CompletableFuture` is returned in case the operation is asynchronous. The result is however irrelevant to the processor, but any errors will fail the stream.
The result can be used to for example aggregate a result, or it can be omitted if irrelevant.

### Runnable .jar
The sample components can be run by executing the `target/csv-processor-samples-0.1.0-shaded.jar`. You will be presented with two options:

1. input converter: manually enter CSV described in above format line by line and finally enter `done` and the result will be printed to the console.
2. file converter: enter the path to a file or directory and all file(s) with suffix `.csv` will be read recursively and a sibling file with a `.xml` suffix will be generated with the result. 

A sample file can be found [here](src/test/resources/sample.csv). Either provide the file component its path (or parent directory) or the input component the raw lines.

Example 1:
```
java -jar target/csv-processor-samples-0.1.0-shaded.jar

CSV to XML converter, which component do you want to start?
1: input converter
2: file converter
exit: exit the application
> 1

Please enter CSV line or [done]
> P|Carl Gustaf|Bernadotte
> T|0768-101801|08-101801
> A|Drottningholms slott|Stockholm|10001
> F|Victoria|1977
> A|Haga Slott|Stockholm|10002
> F|Carl Philip|1979
> T|0768-101802|08-101802
> P|Barack|Obama
> A|1600 Pennsylvania Avenue|Washington, D.C
> done
```

Example 2:

```
java -jar target/csv-processor-samples-0.1.0-shaded.jar

CSV to XML converter, which component do you want to start?
1: input converter
2: file converter
exit: exit the application
> 2

Please enter source file or directory
> src/test/resources/sample.csv

cat src/test/resources/sample.xml
```