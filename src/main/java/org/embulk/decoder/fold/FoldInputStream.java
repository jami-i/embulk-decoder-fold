package org.embulk.decoder.fold;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class FoldInputStream extends FilterInputStream
{
    private final int length;

    private long pos = 0;

    private int nlPos = 0;

    private byte[] insert;

    private boolean newLineFlg = false;

    public FoldInputStream(InputStream in, int length, byte[] insertion)
    {
        super(in);
        this.length = length;
        if (insertion.length  < 1) {
            throw new IllegalArgumentException("insert byte array length 0.");
        }
        this.insert = insertion;
    }

    public FoldInputStream(InputStream in, int length, String insertion)
    {
        this(in, length, insertion.getBytes());
    }

    public FoldInputStream(InputStream in, int length, String insertion, Charset charset)
    {
        this(in, length, insertion.getBytes(charset));
    }

    public FoldInputStream(InputStream underlying, int length)
    {
        this(underlying, length, "\n".getBytes());
    }

    public FoldInputStream(InputStream underlying, int length, Charset charset)
    {
        this(underlying, length, "\n".getBytes(charset));
    }

    @Override
    public synchronized int read() throws IOException
    {
        int v;
        if (pos != 0 && pos % length == 0 && !newLineFlg) {
            v = getCurrentInsertValue();
        }
        else {
            newLineFlg = false;
            pos++;
            v = in.read();
        }
        return v;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        skip(off);
        byte[] res = new byte[len];

        int i = 0;
        int buff;

        while (true) {
            buff =  read();
            if (buff < 0 || i == len) {
                break;
            }
            res[i] = (byte) buff;
            i++;
        }

        System.arraycopy(res, 0, b, 0, i);

        return i > 0 ? i : -1;
    }

    @Override
    public long skip(long n) throws IOException
    {
        long skipped = 0;
        for (long i = 0; i < n; i++) {
            read();
            skipped++;
        }
        return skipped;
    }

    @Override
    public int read(byte[] b) throws IOException
    {
        return read(b, 0, b.length);
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    private int getCurrentInsertValue()
    {
        byte v = insert.length > 1 ? insert[nlPos] : insert[0];

        if (nlPos + 1 < insert.length) {
            nlPos++;
        }
        else {
            nlPos = 0;
            newLineFlg = true;
        }
        return ByteBuffer.wrap(new byte[]{0, 0, 0, v}).getInt();
    }
}
