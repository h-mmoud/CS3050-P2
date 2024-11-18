# #!/bin/bash

# # Compile
# javac -cp "lib/*:src/main/java:src/test/java" \
#     src/main/java/interpreter/*.java \
#     src/test/java/interpreter/*.java

# # Run tests
# java -jar lib/junit-platform-console-standalone.jar \
#     --class-path "src/main/java:src/test/java" \
#     --scan-class-path

#!/bin/bash

# Create a bin directory for compiled classes
# mkdir -p bin

# find bin/ -name "*.class" -type -delete

# # Compile main classes
# javac -d bin -cp "lib/*" $(find src/main/java/interpreter -name "*java")

# javac -d bin -cp "lib/*" $(find src/main/java/parser -name "*java")

# # Compile test classes, including bin in the classpath to access main classes
# # javac -d bin -cp "lib/*:bin" $(find src/test/java -name "*.java")

# # Run tests using the compiled classes in bin
# java -jar lib/junit-platform-console-standalone.jar \
#     --class-path "bin" \
#     --scan-class-path

#!/bin/bash

# Create output directory
mkdir -p bin

# Clean previous builds
rm -rf bin/*

# Print current directory for debugging
echo "Working directory: $(pwd)"

# First compile parser.ast package
javac -d bin src/parser/ast/*.java

# echo "Compiling parser.ast package..."
javac -d bin -cp bin src/parser/*.java

# Then compile interpreter package
echo "Compiling interpreter package..."
javac -d bin -cp bin src/interpreter/*.java

# Finally compile tests
echo "Compiling tests..."
javac -d bin -cp "bin:lib/*" src/test/interpreter/*.java

# # List compiled files for verification
# echo "Compiled files in bin:"
# find bin -name "*.class"

# Run tests
java -jar lib/junit-platform-console-standalone.jar \
    --class-path "bin:lib/*" \
    --scan-class-path 
    # --select-package interpreter