package com.example;

import java.util.Properties;
import org.apache.log4j.PropertyConfigurator;

public class App {
    public static void main( String[] args ) throws Exception {
        InfiniteStreamRecognizeOptions options = InfiniteStreamRecognizeOptions.fromFlags(args);
        if (options == null) {
            // Could not parse.
            System.out.println("Failed to parse options.");
            System.exit(1);
        }
        
        Properties prop = new Properties();
        prop.setProperty("log4j.rootLogger", "WARN");
        PropertyConfigurator.configure(prop);
        
        TrackFrame trackFrame = new TrackFrame();
        MainFrame controlFrame = new MainFrame(trackFrame, options);
        
        trackFrame.initialize();
        controlFrame.initialize();
    }
}
