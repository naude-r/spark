package spark;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class Base64Test {

    //CS304 manually Issue link:https://github.com/perwendel/spark/issues/1061

    @Test
    public final void test_encode() {
        String in = "hello";
        String encode = Base64.encode(in);
        assertNotEquals(in, encode);
    }

    //CS304 manually Issue link:https://github.com/perwendel/spark/issues/1061

    @Test
    public final void test_decode() {
        String in = "hello";
        String encode = Base64.encode(in);
        String decode = Base64.decode(encode);

        assertEquals(in, decode);
    }

    @Test
    public final void testEncodeNull() {
        String in = null;
        //noinspection ConstantConditions
        String encode = Base64.encode(in);
        Assert.assertNull(encode);
    }

    @Test
    public final void testDecodeNull() {
        String in = null;
        //noinspection ConstantConditions
        String decode = Base64.decode(in);
        Assert.assertNull(decode);
    }

}
