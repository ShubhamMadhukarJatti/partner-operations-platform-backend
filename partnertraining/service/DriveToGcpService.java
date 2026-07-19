package com.sharkdom.partnertraining.service;

import com.google.cloud.storage.*;
import com.sharkdom.constants.ErrorMessages;
import com.sharkdom.exception.ServiceException;
import com.sharkdom.partnertraining.dto.DriveUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.regex.*;

@Service
@Slf4j
public class DriveToGcpService {

    private static final Pattern DRIVE_FILE_PATTERN=Pattern.compile("https://drive\\.google\\.com/(file/d/|open\\?id=)([a-zA-Z0-9_-]+)");

    @Value("${gcp.bucket}") private String bucket;
    @Value("${gcp.folder}") private String folder;
    @Value("${gcp.max-file-mb}") private int maxFileMb;

    private final Storage storage;
    private final Tika tika=new Tika();

    public DriveToGcpService(Storage storage){ this.storage=storage; }

    // ========================= ENTRY =========================
    public DriveUploadResult validateAndUpload(String driveUrl){
        log.info("Start upload | driveUrl={}",driveUrl);
        String fileId=extractFileId(driveUrl);
        String downloadUrl=buildDownloadUrl(fileId);
        verifyPublicAccess(downloadUrl);
        FileMetadata meta=fetchFileMetadata(downloadUrl);
        validateFileSize(meta.size);
        File file=downloadFile(downloadUrl,meta.fileName);
        String gcpUrl=uploadToGcp(file,meta.fileName);
        boolean deleted=file.delete();
        log.info("Temp file deleted={}",deleted);
        return new DriveUploadResult(meta.fileName,meta.size,gcpUrl);
    }

    // ========================= EXTRACT FILE ID =========================
    private String extractFileId(String driveUrl){
        Matcher m=DRIVE_FILE_PATTERN.matcher(driveUrl);
        if(!m.find()) throw new ServiceException(ErrorMessages.SH166);
        return m.group(2);
    }

    // ========================= BUILD DOWNLOAD URL =========================
    private String buildDownloadUrl(String fileId){ return "https://drive.google.com/uc?export=download&id="+fileId; }

    // ========================= VERIFY PUBLIC ACCESS =========================
    private void verifyPublicAccess(String link){
        try{
            HttpURLConnection c=(HttpURLConnection)new URL(link).openConnection();
            c.setRequestMethod("GET"); c.setConnectTimeout(10000); c.setReadTimeout(10000);
            c.setRequestProperty("User-Agent","Mozilla/5.0"); c.setInstanceFollowRedirects(true);
            int code=c.getResponseCode(); String finalUrl=c.getURL().toString();
            if(finalUrl.contains("accounts.google.com")) throw new ServiceException(ErrorMessages.SH167);
            if(code>=400) throw new ServiceException(ErrorMessages.SH168);
        }catch(ServiceException e){ throw e; }
        catch(Exception e){ throw new ServiceException(ErrorMessages.SH168,e.getMessage()); }
    }

    // ========================= FETCH METADATA =========================
    private FileMetadata fetchFileMetadata(String url){
        try{
            HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
            c.setRequestMethod("HEAD");
            long size=c.getContentLengthLong();
            String name=extractFileName(c.getHeaderField("Content-Disposition"));
            return new FileMetadata(name,size);
        }catch(IOException e){ throw new ServiceException(ErrorMessages.SH168); }
    }

    // ========================= VALIDATE SIZE =========================
    private void validateFileSize(long size){
        long max=maxFileMb*1024L*1024L;
        if(size<=0||size>max) throw new ServiceException(ErrorMessages.SH173,"Max "+maxFileMb+" MB");
    }

    // ========================= DOWNLOAD FILE =========================
    private File downloadFile(String url,String name){
        try{
            File f=File.createTempFile("drive-","-"+name);
            try(InputStream in=new URL(url).openStream(); FileOutputStream out=new FileOutputStream(f)){ in.transferTo(out); }
            return f;
        }catch(IOException e){ throw new ServiceException(ErrorMessages.SH153,url); }
    }

    // ========================= UPLOAD TO GCP =========================
    private String uploadToGcp(File file,String name){
        try{
            String object=folder+"/"+UUID.randomUUID()+"-"+name;
            String type=tika.detect(file);
            BlobId id=BlobId.of(bucket,object);
            BlobInfo info=BlobInfo.newBuilder(id).setContentType(type).build();
            storage.create(info,new FileInputStream(file));
            return "https://storage.googleapis.com/"+bucket+"/"+object;
        }catch(Exception e){ throw new ServiceException(ErrorMessages.SH160,e.getMessage()); }
    }

    // ========================= EXTRACT FILE NAME =========================
    private String extractFileName(String cd){
        if(cd==null) return "drive-file";
        for(String p:cd.split(";")) if(p.trim().startsWith("filename=")) return p.split("=")[1].replace("\"","").trim();
        return "drive-file";
    }

    // ========================= INNER METADATA CLASS =========================
    private static class FileMetadata{ private final String fileName; private final long size; public FileMetadata(String f,long s){ this.fileName=f; this.size=s; } }

    // ========================= PARSE CONTENT RANGE =========================
    private long parseSizeFromContentRange(String range){
        try{ return Long.parseLong(range.substring(range.lastIndexOf("/")+1)); }
        catch(Exception e){ throw new ServiceException(ErrorMessages.SH168); }
    }
}