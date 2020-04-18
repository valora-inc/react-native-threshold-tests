package com.reactlibrary;

import com.sun.jna.Structure;
import com.sun.jna.Pointer;
import com.sun.jna.Memory;
import java.util.List;
import java.util.Arrays;
import android.util.Log;

public class Buffer extends Structure {
    public Buffer() { }
    public Buffer(byte[] in_message) { message = new Memory(in_message.length); message.write(0, in_message, 0, in_message.length); len = in_message.length; }

    protected List<String> getFieldOrder() {
        return Arrays.asList("message", "len");
    }

    public Pointer message;
    public int len;
}
