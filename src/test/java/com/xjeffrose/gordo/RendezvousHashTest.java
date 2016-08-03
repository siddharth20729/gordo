package com.xjeffrose.gordo;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Funnels;
import java.nio.charset.Charset;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class RendezvousHashTest {

  @Test
  public void get() throws Exception {

    RendezvousHash rHash = new RendezvousHash(Funnels.stringFunnel(Charset.defaultCharset()), ImmutableList.of("one", "Two", "Three"), 3);

    List<String> l1 = rHash.get("Thing1".getBytes());
    List<String> l2 = rHash.get("Thing1".getBytes());
    List<String> l3 = rHash.get("POOOOP".getBytes());

    assertEquals(l1 , l2);
    assertNotEquals(l2, l3);
  }

}