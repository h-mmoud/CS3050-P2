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

# Run tests
java -jar lib/junit-platform-console-standalone.jar \
    --class-path "bin:lib/*" \
    --scan-class-path 
    # --select-package interpreter