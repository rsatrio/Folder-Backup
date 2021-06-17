package com.rizky.filebackup.cli;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "",mixinStandardHelpOptions = true,
description = "Simple File Backup Between Folder",
subcommands = {ConfigurationMgmt.class,FileBackup.class})
public class RootCommand implements Callable<Integer> {

    
    @Override
    public Integer call() throws Exception {
        // TODO Auto-generated method stub
        System.out.println("use subcommand: createConfig or runBackup");
        System.exit(0);
        return 0;
    }

    
}
