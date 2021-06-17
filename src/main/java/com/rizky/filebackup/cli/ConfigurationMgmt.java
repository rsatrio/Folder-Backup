package com.rizky.filebackup.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "createConfig",mixinStandardHelpOptions = true,
description = "Configuration Management")
public class ConfigurationMgmt implements Callable<Integer> {

    @Option(names= {"-f","--fileName"},description = "Path of Configuration File",required = true)
    Path configFile;

    @Option(names = {"-s","--source"},description = "Source Path for Backup",required = true)
    Path sourcePath;

    @Option(names = {"-d","--destination"},description = "Destination Path for Backup",required = true)
    Path destPath;

    @Option(names = {"-t","--trash"},description = "Trash Path for Overwriten Backup File",required = true)
    Path trashPath;

    Logger log1=LoggerFactory.getLogger(ConfigurationMgmt.class);

    @Override
    public Integer call() throws Exception {
        // TODO Auto-generated method stub

        try {
            if(!configFile.toFile().exists())   {
                log1.info("Creating new configuration file {}",configFile);
                configFile.toFile().createNewFile();
            }

            BufferedWriter bw=new BufferedWriter(new FileWriter(configFile.toFile(),false));
            String toWrite=sourcePath.toString()+";"
                    +destPath.toString()+";"
                    +trashPath.toString()+";"
                    +UUID.randomUUID();
            bw.append(toWrite);bw.newLine();
            bw.flush();bw.close();
            log1.info("Config file {} created",configFile);
            return 0;
        }
        catch(Exception e)  {
            log1.error("Failed to create config file {}",configFile,e);
            return 100;
        }

    }


}
