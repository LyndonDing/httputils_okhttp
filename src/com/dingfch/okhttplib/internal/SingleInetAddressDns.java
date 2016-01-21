package com.dingfch.okhttplib.internal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import okhttp3.Dns;

/**
 * A network that resolves only one IP address per host. Use this when testing route selection
 * fallbacks to prevent the host machine's various IP addresses from interfering.
 */
public class SingleInetAddressDns implements Dns {
  @Override public List<InetAddress> lookup(String hostname) throws UnknownHostException {
    List<InetAddress> addresses = Dns.SYSTEM.lookup(hostname);
    return Collections.singletonList(addresses.get(0));
  }
}