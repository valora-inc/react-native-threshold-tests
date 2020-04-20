package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.sun.jna.Native;
import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import android.util.Log;
import com.reactlibrary.Buffer;

public class MyFancyLibraryModule extends ReactContextBaseJavaModule {

  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
  public static String bytesToHex(byte[] bytes) {
      char[] hexChars = new char[bytes.length * 2];
      for (int j = 0; j < bytes.length; j++) {
          int v = bytes[j] & 0xFF;
          hexChars[j * 2] = HEX_ARRAY[v >>> 4];
          hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
      }
      return new String(hexChars);
  }


    private static final String TAG = "BlindThresholdBlsModule";

    private final ReactApplicationContext reactContext;

    public MyFancyLibraryModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        byte[] msg = new byte[10];
        this.blindMessage(msg, null);
    }

    static {
        Native.register(MyFancyLibraryModule.class, "blind_threshold_bls");
    }

    @Override
    public String getName() {
        return "MyFancyLibrary";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @ReactMethod
    public void blindMessage(byte[] message, Callback callback) {
        try {
            Log.d(TAG, "Blinding message" );
            byte[] seed = new byte[32];
            for (int i = 0; i < 31; i++) {
              seed[i] = (byte)i;
            }
            Log.d(TAG, "stuff -2");
            Buffer msg = new Buffer(message);
            Log.d(TAG, "stuff -1");
            Buffer seedBuf = new Buffer(seed);
            Buffer blindedMessage = new Buffer();
            PointerByReference token = new PointerByReference();
            blind(msg, seedBuf, blindedMessage, token);
            Log.d(TAG, "stuff 0");

            PointerByReference pKeys = new PointerByReference();
            keygen(seedBuf, pKeys);
            Log.d(TAG, "ptr -1: " + pKeys.getValue());

            Pointer pDev = private_key_ptr(pKeys.getValue());

            Log.d(TAG, "ptr: " + pDev);
            PointerByReference buffer = new PointerByReference();
            serialize_privkey(pDev, buffer);
            Log.d(TAG, "stuff 1: " + buffer.getValue());
            Log.d(TAG, "stuff 2: " + bytesToHex(buffer.getValue().getByteArray(0, 32)));

            Buffer sig = new Buffer();
            sign(pDev, blindedMessage, sig);
            sig.read();
            Log.d(TAG, "stuff 3: " + sig.len);
            Log.d(TAG, "stuff 3: " + bytesToHex(sig.message.getByteArray(0, sig.len)));

            destroy_keypair(pKeys.getValue());
            Log.d(TAG, "stuff 4");

        } catch (Exception e) {
            Log.e(TAG, "Exception while blinding the message: " + e.getMessage() );
        }

    }

    // Seed must be >= 32 characters long
    private static native void keygen(Buffer seed, PointerByReference ptr);
    private static native void destroy_keypair(Pointer ptr);
    private static native Pointer private_key_ptr(Pointer ptr);
    private static native void deserialize_privkey(byte[] privkey, PointerByReference ptr);
    private static native void serialize_privkey(Pointer ptr, PointerByReference pubkey_buf);
    private static native void sign(Pointer private_key, Buffer message, Buffer signature);
    private static native void blind(Buffer message,
           Buffer seed,
           Buffer blinded_message_out,
           PointerByReference blinding_factor_out);

}
