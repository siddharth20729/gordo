package com.xjeffrose.gordo;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import io.netty.util.internal.PlatformDependent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RendezvousHash<N> {
  private static final Logger log = LoggerFactory.getLogger(RendezvousHash.class.getName());

  private final HashFunction hasher;
  private final Funnel<N> nodeFunnel;
  private final int quorum;

  private ConcurrentSkipListSet<N> nodeList;

  public RendezvousHash(Funnel<N> nodeFunnel, Collection<N> init, int quorum) {
    this.hasher = Hashing.murmur3_128();
    this.nodeFunnel = nodeFunnel;
    this.nodeList = new ConcurrentSkipListSet<>(init);
    this.quorum = quorum;
  }

  public boolean remove(N node) {
    return nodeList.remove(node);
  }

  public boolean add(N node) {
    return nodeList.add(node);
  }

  public List<N> get(byte[] key) {
    Map<Long, N> hashMap = PlatformDependent.newConcurrentHashMap();
    List<N> _nodeList = new ArrayList<>();

    nodeList.stream()
        .filter(xs -> !_nodeList.contains(xs))
        .forEach(xs -> {
          hashMap.put(hasher.newHasher()
              .putBytes(key)
              .putObject(xs, nodeFunnel)
              .hash().asLong(), xs);

        });

    for (int i = 0; i < quorum; i++) {
      _nodeList.add(hashMap.remove(hashMap.keySet().stream().max(Long::compare).orElse(null)));
    }

    return _nodeList;
  }

}
