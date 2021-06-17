package com.rizky.filebackup.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MoveAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "runBackup",mixinStandardHelpOptions = true,
description = "Simple File Backup Between Folder")
public class FileBackup implements Callable<Integer> {

    @Option(names= {"-c","--configFileName"},
            description = "Path of Configuration File",required = true)
    Path configFile;

    String idBackup;
    String idListBackup;
    String idLastBackup;

    Path sourcePath;

    Path destPath;

    Path trashPath;

    LocalDateTime lastBackup;

    Logger log1=LoggerFactory.getLogger(FileBackup.class);

    int errorCount;
    int copyCount;
    int trashCount;

    LinkedList<String> listFiles=new LinkedList<String>();
    LinkedList<String> listFilesInBackup=new LinkedList<String>();

    public Integer call() throws Exception {
        // TODO Auto-generated method stub
        try {
            readConfiguration();
            checkLastBackup();
            addIndexToMemory();            

            log1.info("SourcePath:"+sourcePath);
            log1.info("DestinationPath:"+destPath);
            log1.info("TrashPath:"+trashPath);



            Files.newDirectoryStream(sourcePath).forEach(a->{printFile(a);});

            log1.info("There are {} files succesfully backup to {}",copyCount,destPath);
            log1.info("There are {} files moves to trash {}",trashCount,trashPath);
            log1.info("There are {} files failed to backup ",errorCount);



            createLastBackupTimestamp();

            writeSuccessBackup();

            return 0;
        }
        catch(Exception e)  {
            log1.error("Runtime Error:",e);            
            return 100;
        }
    }

    private void printFile(Path path)   {

        try {
            if(!path.toFile().isDirectory())  {

                BasicFileAttributeView basicView =
                        Files.getFileAttributeView(path,BasicFileAttributeView.class);

                //                log1.info(path.normalize()+","+
                //                        basicView.readAttributes().lastModifiedTime().toInstant()+","+path.toFile().length());
                Path path2=destPath;
                copyFileBackup(path, path2.resolve(sourcePath.relativize(path)));

            }
            else    {
                Path path2=destPath;
                //                log1.info(path2.resolve(sourcePath.relativize(path)).toString());
                this.buatDirektori(path2.resolve(sourcePath.relativize(path)));
                Files.newDirectoryStream(path.normalize()).forEach(a->{printFile(a);});
            }
        }
        catch(Exception e)  {
            log1.error("Failed to copy {}",path,e);
            errorCount++;
        }
    }

    private void buatDirektori(Path pathTujuan) throws Exception    {
        if(!Files.exists(pathTujuan)) {
            Files.createDirectory(pathTujuan);
            log1.info("Creating new directory:{}",pathTujuan);

        }

    }

    private void copyFileBackup(Path pathAsal,Path pathTujuan) throws Exception {

        if(pathAsal.toFile().getName().contains(".last-")||
                pathAsal.toFile().getName().contains(".list-") )   {
            return;
        }

        BasicFileAttributeView basicView =
                Files.getFileAttributeView(pathAsal,BasicFileAttributeView.class);
        LocalDateTime lastModif=LocalDateTime.ofInstant(basicView.readAttributes().lastModifiedTime().toInstant(), 
                ZoneOffset.UTC);

        if(lastModif.isAfter(lastBackup)
                ||!listFilesInBackup.contains(pathAsal.toString()))   {
            if(Files.exists(pathTujuan) && trashPath!=null)    {
                buangKeTrash(pathTujuan,destPath);
            }
            Files.copy(pathAsal,pathTujuan,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.COPY_ATTRIBUTES);
            log1.info("Create backup for file {}",pathAsal.toFile().toString());
            listFiles.add(pathAsal.toString());
            copyCount++;
        }


    }

    private void checkLastBackup() throws Exception  {
        if(Files.exists(sourcePath.resolve(idLastBackup)))    {
            List<String> isi=Files.readAllLines(sourcePath.resolve(idLastBackup));
            lastBackup=LocalDateTime.ofInstant(Instant.ofEpochMilli(new Long(isi.get(0))), ZoneOffset.UTC);

        }
        else    {
            lastBackup=LocalDateTime.of(1970, 12, 1, 0, 0);
        }
    }

    private void buangKeTrash(Path pathAsal,Path pathRelativeAsal)   throws Exception  {


        Path pathBak=trashPath;

        String[] namaFile=pathAsal.getFileName().toString().split("[.]");

        String namaFileBaru=pathAsal.getFileName().toString();
        DateTimeFormatter format1=DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime sekarang=LocalDateTime.now();
        if(namaFile.length>1)   {
            namaFileBaru=namaFileBaru+"."+sekarang.format(format1)+"."+namaFile[namaFile.length-1];
        }

        Path pathAsal3=pathAsal.resolveSibling(namaFileBaru);

        //        Path pathDest=pathBak.resolve(pathRelativeAsal.relativize(pathAsal3));
        Path pathDest=pathBak.resolve(namaFileBaru);
        Files.move(pathAsal, pathDest, StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);

        log1.info("Move outdated version of {} to trash",pathAsal.getFileName().toString());
        trashCount++;

    }

    private void createLastBackupTimestamp()  throws Exception {
        FileOutputStream stream1=new FileOutputStream(sourcePath.resolve(idLastBackup).toFile());
        stream1.write(new Long(Instant.now().toEpochMilli()).toString().getBytes());
        stream1.flush();
        stream1.close();
    }


    private void writeSuccessBackup() throws Exception  {

        File indexFile=sourcePath.resolve(idListBackup).toFile();
        if(!indexFile.exists()) {
            log1.info("Creating new index file {}",indexFile.getAbsolutePath());
            indexFile.createNewFile();
        }

        BufferedWriter bw=new BufferedWriter(new FileWriter(indexFile, 
                true));
        listFiles.forEach(f->{try {
            bw.append(f);
            bw.newLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }});
        bw.flush();
        bw.close();
    }

    private void addIndexToMemory() throws Exception{
        File indexFile=sourcePath.resolve(idListBackup).toFile();
        if(indexFile.exists())  {
            BufferedReader br=new BufferedReader(new FileReader(indexFile));
            String currLine;
            while((currLine=br.readLine())!=null)   {
                listFilesInBackup.add(currLine);
            }

            br.close();
        }
        log1.info("Index size:{}",listFilesInBackup.size());
    }

    private void readConfiguration()    {
        try {
            if(configFile.toFile().exists())    {


                BufferedReader br=new BufferedReader(new FileReader(configFile.toFile()));
                String[] isi=br.readLine().split("[;]");
                sourcePath=Paths.get(isi[0]);
                destPath=Paths.get(isi[1]);
                trashPath=Paths.get(isi[2]);
                idBackup=isi[3];
                idListBackup=".list-"+idBackup;
                idLastBackup=".last-"+idBackup;

                if(!sourcePath.toFile().exists())   {
                    log1.error("Backup Source Path {} did not exist",sourcePath);
                    System.exit(0);
                }
                else if(!sourcePath.toFile().isDirectory()) {
                    log1.error("Backup Source Path {} is not a Directory",sourcePath);
                    System.exit(0);
                }

                if(!destPath.toFile().exists())   {
                    log1.error("Backup Destination Path {} did not exist",destPath);
                    System.exit(0);
                }
                else if(!destPath.toFile().isDirectory())   {
                    log1.error("Backup Destination Path {} is not a Directory",destPath);
                    System.exit(0);
                }

                if(!trashPath.toFile().exists())    {
                    Files.createDirectories(trashPath);
                    log1.info("Creating new Folder {} for trash",trashPath);
                }
                else if(!trashPath.toFile().isDirectory())  {
                    log1.error("Trash Path {} is not a Directory",trashPath);
                    System.exit(0);
                }



            }
            else    {
                throw new Exception();
            }

        }
        catch(Exception e)  {
            log1.error("Failed to read configuration {}",configFile,e);
            System.exit(100);
        }
    }
}
