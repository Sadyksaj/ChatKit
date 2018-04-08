package com.stfalcon.chatkit.sample.features.main;



import android.util.Log;

import com.stfalcon.chatkit.sample.common.data.model.Dialog;
import com.stfalcon.chatkit.sample.features.demo.def.DefaultDialogsActivity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import impl.underdark.transport.aggregate.AggLink;
import impl.underdark.transport.bluetooth.BtLink;
import impl.underdark.transport.bluetooth.BtTransport;
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
    private DefaultDialogsActivity activity;
    private long nodeId;
    private Transport transport;

    private ArrayList<Link> links = new ArrayList<>();
    private Map<Long, UserInfo> storage = new HashMap<>();
    public Node(DefaultDialogsActivity activity)
    {
        this.activity = activity;

        do
        {
            nodeId = new Random().nextLong();
        } while (nodeId == 0);

        if(nodeId < 0)
            nodeId = -nodeId;
        this.transport = new BtTransport(
                234235,
                nodeId,
                this,
                null,
                activity.getApplicationContext()
        );
    }



    public void start()
    {
        Log.i("HI", "IM HERE");
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


    public void broadcastFrame(byte[] frameData)
    {
        if(links.isEmpty())
            return;

        for(Link link : links)
            link.sendFrame(frameData);
    }

    public UserInfo getUserInfo(){return null;}

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
        Log.i("There is node ",String.valueOf(link.getNodeId()));
        System.out.println(link.getNodeId());
        Dialog dialog = new Dialog(link.getNodeId(), ((BtLink) link).getDevice().getName());
        activity.onNewDialog(dialog);
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
        if (storage.get(link.getNodeId()) == null){
            UserInfo user = new UserInfo(new ArrayList<String>(),((BtLink) link).getDevice().getName() , link.getNodeId());
        }
        if (payload[payload.length - 1] == String.valueOf(nodeId)) {
            storage.get(link.getNodeId()).messages.add(msg.substring(0, msg.lastIndexOf("~")));
        }
        activity.refreshFrames();
        // TODO: refresh chat
    }
    //endregion
} // Node
