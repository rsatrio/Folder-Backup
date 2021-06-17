
# Simple Folder-To-Folder File Backup

A simple CLI software for backup a folder. Build with Java 8.

## Features
- Can be used for folder to folder backup (user that run the program should have read & write privileges to the folder)
- After the initial backup (first backup), subsequent backup will be incremental (only file added to the source folder or file modified after the last backup)
- Will not delete any file in destination path. Overwritten file in destination folder backup will be moved to trash

## Build
Use mvn package to build the module into jar file
```shell
mvn clean package
```


## Usage
- Create configuration file of things to backup. This example below will create configration to backup file from folder d:\source to d:\destination. The config filename is d:\backup.cfg and trash folder is in d:\trash:
```shell
java -jar file-backup.jar createConfig -s d:\source -d d:\destination -t d:\trash -f d:\backup.cfg
```
- Run the backup using config file created above: 
```shell
java -jar file-backup.jar runBackup -c d:\backup.cfg
```
- Voila, all the file and folder under d:\source is now backup to d:\destination

## Feedback
For feedback and feature request, please raise issues in the issue section of the repository. Enjoy!!.