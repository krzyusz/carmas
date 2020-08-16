package com.example.krzysiek.carmas;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class BTService extends Service implements BluetoothAdapter.LeScanCallback {

    public static final UUID SERIAL_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static class CommunicationStatus {
        public static final long SEND_TIME_OUT_MILLIS = TimeUnit.SECONDS.toMillis(2);
        public static final int COMMUNICATION_SUCCESS = 0;
        public static final int COMMUNICATION_TIMEOUT = -1;
    }

    private Context context;
    private WeakHashMap<Callback, Object> callbacks;
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;
    private boolean connectFirst;
    private boolean writeInProgress;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice("98:5D:AD:1F:73:46");


    private Queue<BluetoothGattCharacteristic> readQueue;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        this.connect(device);

        return START_STICKY;
    }

    public interface Callback {
        public void onConnected(Context context);
        public void onConnectFailed(Context context);
        public void onDisconnected(Context context);
        public void onReceive(Context context, BluetoothGattCharacteristic rx);
        public void onDeviceFound(BluetoothDevice device);
        public void onDeviceInfoAvailable();
        public void onCommunicationError(int status, String msg);
    }

    public BluetoothGatt getGatt() {
        return gatt;
    }

    public BTService() {
        super();
        this.callbacks = new WeakHashMap<Callback, Object>();
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.gatt = null;
        this.tx = null;
        this.rx = null;
        this.connectFirst = false;
        this.writeInProgress = false;
        this.readQueue = new ConcurrentLinkedQueue<BluetoothGattCharacteristic>();
    }

    public BTService setContext(Context context) {
        this.context = context;
        return this;
    }

    IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public BTService getBTServiceInstance() {
            return BTService.this;
        }
    }

    public boolean isConnected() {
        return (tx != null && rx != null);
    }

    public void send(byte[] data) {
        long beginMillis = System.currentTimeMillis();
        if (tx == null || data == null || data.length == 0) {
            return;
        }
        tx.setValue(data);
        writeInProgress = true;
        gatt.writeCharacteristic(tx);
        while (writeInProgress) {
            if (System.currentTimeMillis() - beginMillis > CommunicationStatus.SEND_TIME_OUT_MILLIS) {
                notifyOnCommunicationError(CommunicationStatus.COMMUNICATION_TIMEOUT, null);
                break;
            }
        }
    }
    public void send(String string) {
        int len = string.length(); int pos = 0;
        StringBuilder stringBuilder = new StringBuilder();

        while (len != 0) {
            stringBuilder.setLength(0);
            if (len >= 20) {
                stringBuilder.append(string.toCharArray(), pos, 20);
                len -= 20;
                pos += 20;
            } else {
                stringBuilder.append(string.toCharArray(), pos, len);
                len = 0;
            }
            send(stringBuilder.toString().getBytes());
        }
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (gatt != null) {
            gatt.readCharacteristic(characteristic);
        }
    }

    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (adapter != null || gatt != null) {
            if (gatt.setCharacteristicNotification(characteristic, enabled)) {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_UUID);

                if (descriptor != null) {
                    byte[] data = enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                    if (descriptor.setValue(data)) {
                        gatt.writeDescriptor(descriptor);
                    } else {
                        connectFailure();
                    }
                } else {
                    connectFailure();
                }
            } else {
                connectFailure();
            }
        }
        return true;
    }

    public void inf(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public boolean enableRXNotification() {
        if (gatt == null)   return false;

        BluetoothGattService SerialService = gatt.getService(SERIAL_SERVICE_UUID);
        if (SerialService == null)  return false;

        BluetoothGattCharacteristic RxChar = SerialService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            connectFailure();
            return false;
        }

        if (!setCharacteristicNotification(RxChar, true)) {
            connectFailure();
            return false;
        }

        return true;
    }

    public BTService registerCallback(Callback callback) {
        if ((!callbacks.containsKey(callback)) && (callback != null))
            callbacks.put(callback, null);

        return this;
    }

    public BTService unregisterCallback(Callback callback) {
        if (callbacks.containsKey(callback) && (callback != null))
            callbacks.remove(callback);

        return this;
    }

    public BTService disconnect() {
        if (gatt != null) {
            gatt.disconnect();
        }
        gatt = null;
        tx = null;
        rx = null;

        return this;
    }

    public BTService close() {
        if (gatt != null) {
            disconnect();
            gatt.close();
            gatt = null;
        }

        return this;
    }
    public BTService stopScan() {
        if (adapter != null) {
            adapter.stopLeScan(this);
        }

        return this;
    }
    public BTService startScan() {
        if (adapter != null) {
            adapter.startLeScan(this);
        }

        return this;
    }

    public BTService connectFirstAvailable() {
        disconnect();
        stopScan();
        connectFirst = true;
        startScan();

        return this;
    }

    public BluetoothGattCallback mGattCallback  = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (!gatt.discoverServices()) {
                        connectFailure();
                    }
                } else {
                    connectFailure();
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                rx = null;
                tx = null;
                notifyOnDisconnected(context);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_FAILURE) {
                connectFailure();
                return;
            }

            tx = gatt.getService(SERIAL_SERVICE_UUID).getCharacteristic(TX_CHAR_UUID);
            rx = gatt.getService(SERIAL_SERVICE_UUID).getCharacteristic(RX_CHAR_UUID);

            enableRXNotification();
            notifyOnConnected(context);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            notifyOnReceive(context, characteristic);
        }

        @Override
        public void onCharacteristicRead (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic nextRequest = readQueue.poll();
                if (nextRequest != null) {
                    gatt.readCharacteristic(nextRequest);
                } else {
                    notifyOnDeviceInfoAvailable();
                }
            } else {

            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                notifyOnCommunicationError(characteristic.getStringValue(0).length(), characteristic.getStringValue(0));
            }
            writeInProgress = false;
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    public BTService connect(BluetoothDevice device) {
        gatt = device.connectGatt(context, false, mGattCallback);
        return this;
    }


    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        List<UUID> uuids = parseUUIDs(scanRecord);
        if (uuids.contains(SERIAL_SERVICE_UUID)) {
            notifyOnDeviceFound(device);
            if (connectFirst) {
                // Stop scanning for devices.
                stopScan();
                connectFirst = false;
                gatt = device.connectGatt(context, true, mGattCallback);
            }
        }

    }

    private void notifyOnConnected(Context context) {
        for (Callback cb : callbacks.keySet()) {
            if (cb != null) {
                cb.onConnected(context);
            }
        }
    }

    private void notifyOnConnectFailed(Context context) {
        for (Callback cb : callbacks.keySet()) {
            if (cb != null) {
                cb.onConnectFailed(context);
            }
        }
    }

    private void notifyOnDisconnected(Context context) {
        for (Callback cb : callbacks.keySet()) {
            if (cb != null) {
                cb.onDisconnected(context);
            }
        }
    }

    private void notifyOnReceive(Context context, BluetoothGattCharacteristic rx) {
        for (Callback cb : callbacks.keySet()) {
            if (cb != null ) {
                cb.onReceive(context, rx);
                showMessage(rx.getStringValue(0));
            }
        }
    }

    public String getDataString(){
        if(isConnected()){
            return rx.getStringValue(0);
        }else{
            return "not connected";
        }

    }

    private void notifyOnDeviceFound(BluetoothDevice device) {
        for (Callback cb : callbacks.keySet()) {
            if (cb != null) {
                cb.onDeviceFound(device);
            }
        }
    }

    private void notifyOnDeviceInfoAvailable() {
        for (Callback cb : callbacks.keySet()) {
            if (cb != null) {
                cb.onDeviceInfoAvailable();
            }
        }
    }
    private void notifyOnCommunicationError(int status, String msg) {
        for (Callback cb : callbacks.keySet()) {
            if (cb != null) {
                cb.onCommunicationError(status, msg);
            }
        }
    }

    private void connectFailure() {
        rx = null;
        tx = null;
        notifyOnConnectFailed(context);
    }

    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            //Log.e(LOG_TAG, e.toString());
                            continue;
                        } finally {
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }

    private void showMessage(String msg){
        //Log.e(BTService.class.getSimpleName(),msg);
    }

    public int[] getSensorData(){
        String t = this.getDataString();
        if(t != null && !t.contains("not")){
            String[] s = this.getDataString().trim().split(" ");
            int[] data = new int[s.length];
            for(int i=0; i<s.length; i++){
                data[i] = Integer.parseInt(s[i]);
            }
            return data;
        }else{
            return new int[]{0,0,0,0,0,0};
        }

    }

    public int[] getHexData(){
        int[] d = new int[6];
        String t = this.getDataString();
        if(t != null && !t.contains("not")){
                String[] s = t.trim().split(" ");
                for(int i=0;i<6;i++){
                    d[i] = Integer.parseInt(s[i],16);
                }
                return d;
         }else{
                return new int[]{0,0,0,0,0,0};
         }

    }
}
