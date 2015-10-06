Mimodek = Java.type('mimodek2.Configurator');
IO = Java.type('mimodek2.serializer.LoaderSaver');
fps = Java.type('mimodek2.Mimodek').fps;
useRealData = Java.type('mimodek2.Mimodek').useRealData;

Mimodek.setSetting('AUTO_FOOD', true);


bgColor = function(r,g,b){

  if( g == null && b == null)
    Java.type('mimodek2.Mimodek').setBgColor(r,r,r);
  else
    Java.type('mimodek2.Mimodek').setBgColor(r,g,b);
}

toggleBg = function(){
  Java.type('mimodek2.Mimodek').hideBg = !Java.type('mimodek2.Mimodek').hideBg;
}

help = function(){
  var msg = "Mimodek Console - V 0.1\n";
     msg += "-*-*-*-*-*-*-*-*-*-*-*-\n";
     msg += "  help()                       Display this message.\n";
     msg += "  IO.saveToFile(fileName)      Save current Mimodek to file.\n";
     msg += "  IO.loadFromFile(fileName)    Load saved Mimodek from file.\n";
     msg += "  Mimodek.list()               List all available configuration settings.\n";
     msg += "  Mimodek.help()               List all available functions under the Mimodek namespace.\n";
     msg += "  temperature(value)           Set the temperature.\n";
     msg += "  humidity(value)              Set the humidity.\n";
     msg += "  fps()                        Get current frame rate.\n";
     msg += "  showData()                   Display temperature and humidity.\n";
     msg += "  console.loadJSFile(fileName) Execute JavaScript code from a file.\n";
     msg += "  exit()                       Quit the application.\n";
  print(msg);
}

showData = function(){
  Mimodek.setSetting(  "SHOW_DATA_FLAG",  !Mimodek.getBooleanSetting( "SHOW_DATA_FLAG" ) );
}

temperature = function(temp){
  Mimodek.setSetting("DATA_TEMPERATURE", temp);
}


humidity = function(humid){
  Mimodek.setSetting("DATA_HUMIDITY", humid);
}
