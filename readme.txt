Usage:
  ./run.sh input_file output_file
  OR
  java -jar gedcomparser-1.0.jar input_file output_file

  e.g.
  ./run.sh input/input_1.txt output_1.txt

Maven Usage:
  - to compile
    mvn compile
  - to test
    mvn test
  - to make a jar file
    mvn package

Notes:
  - If source file contains lines with invalid format, they will be reported and parsing will stop there.
  - By default, 'mvn package' produces a jar file in the target folder
