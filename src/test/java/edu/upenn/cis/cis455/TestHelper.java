package edu.upenn.cis.cis455;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import edu.upenn.cis.cis455.crawler.ChannelEntity;
import edu.upenn.cis.cis455.crawler.ChannelNameEntity;
import edu.upenn.cis.cis455.crawler.CrawlEntity;
import edu.upenn.cis.cis455.crawler.HashEntity;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;

import static org.mockito.Mockito.*;

public class TestHelper {
    public static StorageInterface getMockStorageInterface(String directory){
        StorageInterface db = mock(StorageInterfaceImpl.class);
        
        Map<String, HashEntity> pIdxHash= new HashMap<>();
        Map<String, CrawlEntity> PIdxCrawl = new HashMap<>();
        Map<String, ChannelEntity> pIdxChannel = new HashMap<>();
        Map<String, ChannelNameEntity> pIdxChannelName = new HashMap<>();
        
        when(db.getChannelStore()).thenReturn(null);
//        when(db.getPIDxHash()).thenReturn(pIdxHash);
//        when(db.getPIDxCrawl()).thenReturn(PIdxCrawl);
//        when(db.getPIDxChannel()).thenReturn(pIdxChannel);
//        when(db.getPIDxChannelName()).thenReturn(pIdxChannelName);
        
        return db;
    }
    
    public static Socket getMockSocket(String socketContent, ByteArrayOutputStream output) throws IOException {
        Socket s = mock(Socket.class);
        byte[] arr = socketContent.getBytes();
        final ByteArrayInputStream bis = new ByteArrayInputStream(arr);

        when(s.getInputStream()).thenReturn(bis);
        when(s.getOutputStream()).thenReturn(output);
        when(s.getLocalAddress()).thenReturn(InetAddress.getLocalHost());
        when(s.getInetAddress()).thenReturn(InetAddress.getLocalHost());
        when(s.getRemoteSocketAddress()).thenReturn(InetSocketAddress.createUnresolved("host", 8080));
        return s;
    }
   
}