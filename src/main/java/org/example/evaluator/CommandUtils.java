package org.example.evaluator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class CommandUtils {
  
  public static Optional<String> getCommandFilepath(String command) {
    if (command == null || command.trim().isEmpty()) {
      return Optional.empty();
    }
    
    String pathEnv = System.getenv("PATH");
    if (pathEnv == null) {
      return Optional.empty();
    }
    
    String[] pathDirs = pathEnv.split(File.pathSeparator);
    
    for (String pathDir : pathDirs) {
      Path commandPath = Paths.get(pathDir, command);
      File commandFile = commandPath.toFile();
      
      if (commandFile.isFile() && commandFile.canExecute()) {
        return Optional.of(commandPath.toString());
      }
    }
    
    return Optional.empty();
  }
}