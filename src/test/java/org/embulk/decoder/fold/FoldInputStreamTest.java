package org.embulk.decoder.fold;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class FoldInputStreamTest
{
    private InputStream is;

    @Before
    public void setUp() throws Exception
    {
        is = new ByteArrayInputStream("123456".getBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fold_with_empty_byte_array() throws Exception
    {
        new FoldInputStream(is, 2, new byte[]{});
    }

    @Test
    public void fold_with_encoding_sjis() throws Exception
    {
        Charset ms932 = Charset.forName("MS932");
        is = new ByteArrayInputStream("あいう".getBytes(ms932));
        FoldInputStream stream = new FoldInputStream(is, 2, ms932);

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, ms932));

        assertThat(reader.readLine(), is("あ"));
        assertThat(reader.readLine(), is("い"));
        assertThat(reader.readLine(), is("う"));
        assertNull(reader.readLine());
    }

    @Test
    public void fold_with_encoding_utf8() throws Exception
    {
        Charset utf8 = Charset.forName("utf-8");
        is = new ByteArrayInputStream("あいう".getBytes(utf8));
        FoldInputStream stream = new FoldInputStream(is, 3, utf8);

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, utf8));

        assertThat(reader.readLine(), is("あ"));
        assertThat(reader.readLine(), is("い"));
        assertThat(reader.readLine(), is("う"));
        assertNull(reader.readLine());
    }

    @Test
    public void fold_with_string_encoding_sjis() throws Exception
    {
        Charset ms932 = Charset.forName("MS932");
        is = new ByteArrayInputStream("あいう".getBytes(ms932));
        FoldInputStream stream = new FoldInputStream(is, 2, "だ", ms932);

        System.out.println(Arrays.toString("あいう".getBytes(ms932)));
        System.out.println(Arrays.toString("だ".getBytes(ms932)));

        assertThat(stream.read(), is(130));
        assertThat(stream.read(), is(160));
        assertThat(stream.read(), is(130));
        assertThat(stream.read(), is(190));

        assertThat(stream.read(), is(130));
        assertThat(stream.read(), is(162));
        assertThat(stream.read(), is(130));
        assertThat(stream.read(), is(190));

        assertThat(stream.read(), is(130));
        assertThat(stream.read(), is(164));
        assertThat(stream.read(), is(130));
        assertThat(stream.read(), is(190));
    }

    @Test
    public void fold_with_string_encoding_sjis_buff() throws Exception
    {
        Charset ms932 = Charset.forName("MS932");
        is = new ByteArrayInputStream("あいう".getBytes(ms932));
        FoldInputStream stream = new FoldInputStream(is, 2, "だ", ms932);

        byte[] buff = new byte[12];

        int i = stream.read(buff);

        assertEquals(12, i);
        assertThat(i, is(12));
        assertThat(buff, is("あだいだうだ".getBytes(ms932)));
    }

    @Test
    public void fold_with_string_encoding_utf8() throws Exception
    {
        Charset utf8 = Charset.forName("utf-8");
        is = new ByteArrayInputStream("あいう".getBytes(utf8));
        FoldInputStream stream = new FoldInputStream(is, 3, "ほ", utf8);

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, utf8));

        assertThat(reader.readLine(), is("あほいほうほ"));
        assertNull(reader.readLine());
    }

    @Test
    public void fold_short() throws Exception
    {
        is = new ByteArrayInputStream("12345".getBytes());
        FoldInputStream stream = new FoldInputStream(is, 3);

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        assertThat(reader.readLine(), is("123"));
        assertThat(reader.readLine(), is("45"));
        assertNull(reader.readLine());
    }

    @Test
    public void fold() throws Exception
    {
        FoldInputStream stream = new FoldInputStream(is, 3);

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        assertThat(reader.readLine(), is("123"));
        assertThat(reader.readLine(), is("456"));
        assertNull(reader.readLine());
    }

    @Test
    public void fold2() throws Exception
    {
        FoldInputStream stream = new FoldInputStream(is, 3, "\r\n");

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        assertThat(reader.readLine(), is("123"));
        assertThat(reader.readLine(), is("456"));
        assertNull(reader.readLine());
    }

    @Test
    public void fold3() throws Exception
    {
        FoldInputStream stream = new FoldInputStream(is, 3, "789");

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        assertThat(reader.readLine(), is("123789456789"));
        assertNull(reader.readLine());
    }

    @Test
    public void test_read_empty_byte_array() throws Exception
    {
        is = new ByteArrayInputStream(new byte[]{});
        FoldInputStream stream = new FoldInputStream(is, 3);
        assertEquals(-1, stream.read());
    }

    @Test
    public void testSkip_negative() throws Exception
    {
        FoldInputStream stream = new FoldInputStream(is, 3, "789");
        assertEquals(stream.skip(-1), 0);
    }

    @Test
    public void testSkip_2line() throws Exception
    {
        FoldInputStream stream = new FoldInputStream(is, 3);
        assertEquals(stream.skip(4), 4);

        byte[] res = new byte[4];
        int read = stream.read(res);

        assertThat(read, is(4));
        assertThat(new String(res), is("456\n"));
    }

    @Test
    public void test_is_mark_suppoted() throws Exception
    {
        FoldInputStream stream = new FoldInputStream(is, 3);
        assertThat(stream.markSupported(), is(false));
    }
}
