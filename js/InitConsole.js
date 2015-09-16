Mimodek = Java.type('mimodek2.Configurator');
IO = Java.type('mimodek2.serializer.LoaderSaver');
fps = Java.type('mimodek2.Mimodek').fps;
Mimodek.setSetting('AUTO_FOOD', true);

showDepth = function(){
  Java.type('mimodek2.Mimodek').showDepth = !Java.type('mimodek2.Mimodek').showDepth;
}

help = function(){
  var msg = "Mimodek Console - V 0.1\n";
     msg += "-*-*-*-*-*-*-*-*-*-*-*-\n";
     msg += "  help()                       Display this message.\n";
     msg += "  IO.saveToFile(fileName)      Save current Mimodek to file.\n";
     msg += "  IO.loadFromFile(fileName)    Load saved Mimodek from file.\n";
     msg += "  Mimodek.list()               List all available configuration settings.\n";
     msg += "  Mimodek.help()               List all available functions under the Mimodek namespace.\n";
     msg += "  fps()                        Get current frame rate.\n";
     msg += "  exit()                       Quit the application.\n";
  print(msg);
}
