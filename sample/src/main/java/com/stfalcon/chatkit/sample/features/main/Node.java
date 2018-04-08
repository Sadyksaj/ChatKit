package com.stfalcon.chatkit.sample.features.main;



import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import io.underdark.Underdark;
import io.underdark.transport.Link;
import io.underdark.transport.Transport;
import io.underdark.transport.TransportKind;
import io.underdark.transport.TransportListener;
import io.underdark.util.nslogger.NSLogger;
import io.underdark.util.nslogger.NSLoggerAdapter;

public class Node implements TransportListener
{
    private boolean running;
    private MainActivity activity;
    private long nodeId;
    private Transport transport;

    private ArrayList<Link> links = new ArrayList<>();
    private Map<Long, ArrayList<String>> storage = new HashMap<>();
    private Map<Long, ArrayList<String>> newMessages = new HashMap<>();


    public Node(MainActivity activity)
    {
        this.activity = activity;

        do
        {
            nodeId = new Random().nextLong();
        } while (nodeId == 0);

        if(nodeId < 0)
            nodeId = -nodeId;



        EnumSet<TransportKind> kinds = EnumSet.of(TransportKind.BLUETOOTH, TransportKind.WIFI);
        //kinds = EnumSet.of(TransportKind.WIFI);
        //kinds = EnumSet.of(TransportKind.BLUETOOTH);

        this.transport = Underdark.configureTransport(
                234235,
                nodeId,
                this,
                null,
                activity.getApplicationContext(),
                kinds
        );
    }



    public void start()
    {
        if(running)
            return;

        running = true;
        transport.start();
    }

    public void stop()
    {
        if(!running)
            return;

        running = false;
        transport.stop();
    }

    public ArrayList<Link> getLinks()
    {
        return links;
    }

    public int getFramesCount()
    {
        return framesCount;
    }

    public void broadcastFrame(byte[] frameData)
    {
        if(links.isEmpty())
            return;

        ++framesCount;
        activity.refreshFrames();

        for(Link link : links)
            link.sendFrame(frameData);
    }

    //region TransportListener
    @Override
    public void transportNeedsActivity(Transport transport, ActivityCallback callback)
    {
        callback.accept(activity);
    }

    @Override
    public void transportLinkConnected(Transport transport, Link link)
    {
        links.add(link);
        activity.refreshPeers();
    }

    @Override
    public void transportLinkDisconnected(Transport transport, Link link)
    {
        links.remove(link);
        activity.refreshPeers();

        if(links.isEmpty())
        {
            activity.refreshFrames();
        }
    }

    @Override
    public void transportLinkDidReceiveFrame(Transport transport, Link link, final byte[] frameData)
    {
        String msg = new String(frameData);
        String[] payload = msg.split("~");
        if (payload[payload.length - 1] == String.valueOf(nodeId)) {
            newMessages.get(link.getNodeId()).add(msg.substring(0, msg.lastIndexOf("~")));
            storage.get(link.getNodeId()).add(msg.substring(0, msg.lastIndexOf("~")));
        }
        activity.refreshFrames();
        // TODO: refresh chat
    }
    //endregion
} // Node
