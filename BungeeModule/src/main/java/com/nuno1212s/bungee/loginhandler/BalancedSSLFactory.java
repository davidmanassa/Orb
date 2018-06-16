package com.nuno1212s.bungee.loginhandler;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handle SSL connections thread safe
 */
public class BalancedSSLFactory extends SSLSocketFactory {

    private final SSLSocketFactory oldFactory;

    //in order to be thread-safe
    private final List<InetAddress> localAddresses;

    private final Object lock = new Object();

    private int id;

    public BalancedSSLFactory(SSLSocketFactory oldFactory, Set<InetAddress> localAddresses) {
        this.oldFactory = oldFactory;
        this.localAddresses = new ArrayList<>(localAddresses);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return oldFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return oldFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoclose) throws IOException {
        return oldFactory.createSocket(host, port, getNextLocalAddress(), 0);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return oldFactory.createSocket(host, port, getNextLocalAddress(), 0);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
            throws IOException, UnknownHostException {
        //default
        return oldFactory.createSocket(host, port, localAddress, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return oldFactory.createSocket(host, port, getNextLocalAddress(), 0);
    }

    @Override
    public Socket createSocket(InetAddress host, int port, InetAddress local, int localPort) throws IOException {
        //Default
        return oldFactory.createSocket(host, port, local, localPort);
    }

    private InetAddress getNextLocalAddress() {
        int next;
        synchronized (lock) {
            next = id;
            id++;
            if (next == Integer.MAX_VALUE) {
                id = 0;
            }
        }

        int index = next % localAddresses.size();
        return localAddresses.get(index);
    }

}
