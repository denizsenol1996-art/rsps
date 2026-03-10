package io.xeros.util;

import io.netty.buffer.ByteBuf;

public class BufferUtilities {

    public static String readString(ByteBuf buffer) {
        int start = buffer.readerIndex();
        while (buffer.readByte() != 0) ;
        int len = buffer.readerIndex() - start;

        byte[] str = new byte[len];
        buffer.readerIndex(start);
        buffer.readBytes(str);

        return new String(str, 0, len - 1); /* Do not include null terminator */
    }

    public static String ezFormat(Object... args) {
        String format = new String(new char[args.length])
                .replace("\0", "[ %s ]");
        return String.format(format, args);
    }

    public static <T> T[] append(T[] arr, T lastElement) {
        final int N = arr.length;
        arr = java.util.Arrays.copyOf(arr, N+1);
        arr[N] = lastElement;
        return arr;
    }
    public static <T> T[] prepend(T[] arr, T firstElement) {
        final int N = arr.length;
        arr = java.util.Arrays.copyOf(arr, N+1);
        System.arraycopy(arr, 0, arr, 1, N);
        arr[0] = firstElement;
        return arr;
    }


    public static String readJagString(ByteBuf buffer) {
        if (buffer.readByte() != 0)
            return "";

        int start = buffer.readerIndex();
        while (buffer.readByte() != 0) ;
        int len = buffer.readerIndex() - start;

        byte[] str = new byte[len];
        buffer.readerIndex(start);
        buffer.readBytes(str);

        return new String(str, 0, len - 1); /* Do not include null terminator */
    }

}

